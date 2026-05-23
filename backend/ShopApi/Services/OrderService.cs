using Microsoft.EntityFrameworkCore;
using ShopApi.Data;
using ShopApi.Dtos;
using ShopApi.Models;

namespace ShopApi.Services;

public class OrderService : IOrderService
{
    private const string PaymentMethodCod = "COD";
    private const string PaymentMethodSepay = "SEPAY";
    private const string PaymentStatusPending = "Pending";

    private static readonly HashSet<string> AllowedStatuses = new(StringComparer.OrdinalIgnoreCase)
    {
        "Pending",
        "Shipping",
        "Delivered",
        "Cancelled"
    };

    private static readonly HashSet<string> AllowedPaymentMethods = new(StringComparer.OrdinalIgnoreCase)
    {
        PaymentMethodCod,
        PaymentMethodSepay
    };

    private readonly ShopDbContext _db;
    private readonly IConfiguration _configuration;

    public OrderService(ShopDbContext db, IConfiguration configuration)
    {
        _db = db;
        _configuration = configuration;
    }

    public async Task<ServiceResult<OrderResponse>> CreateAsync(int userId, CreateOrderRequest request)
    {
        var validationError = ValidateCreateRequest(request);
        if (validationError is not null)
        {
            return validationError;
        }

        var paymentMethod = NormalizePaymentMethod(request.PaymentMethod);
        var cartItems = await GetCartItemsAsync(userId);
        var cartError = ValidateCart(cartItems);
        if (cartError is not null)
        {
            return cartError;
        }

        await using var transaction = await _db.Database.BeginTransactionAsync();
        var order = BuildOrder(userId, request, paymentMethod, cartItems);
        _db.Orders.Add(order);
        await _db.SaveChangesAsync();

        ApplyPaymentFlow(order, cartItems);
        await _db.SaveChangesAsync();
        await transaction.CommitAsync();

        return ServiceResult<OrderResponse>.Success(await GetOrderResponseAsync(order.Id));
    }

    public Task<List<OrderResponse>> GetUserOrdersAsync(int userId)
    {
        return GetOrderResponsesAsync(userId);
    }

    public Task<List<OrderResponse>> GetAllAsync()
    {
        return GetOrderResponsesAsync();
    }

    public async Task<ServiceResult<OrderPaymentStatusResponse>> GetPaymentStatusAsync(
        int userId,
        bool isAdmin,
        int orderId)
    {
        var order = await _db.Orders.AsNoTracking().SingleOrDefaultAsync(order => order.Id == orderId);
        if (order is null)
        {
            return ServiceResult<OrderPaymentStatusResponse>.NotFound("Order not found.");
        }

        if (order.UserId != userId && !isAdmin)
        {
            return ServiceResult<OrderPaymentStatusResponse>.Forbidden();
        }

        return ServiceResult<OrderPaymentStatusResponse>.Success(ToPaymentStatusResponse(order));
    }

    public async Task<ServiceResult<OrderResponse>> UpdateStatusAsync(
        int orderId,
        UpdateOrderStatusRequest request)
    {
        var status = request.Status.Trim();
        if (!AllowedStatuses.Contains(status))
        {
            return ServiceResult<OrderResponse>.BadRequest("Status must be Pending, Shipping, Delivered, or Cancelled.");
        }

        var order = await _db.Orders.FindAsync(orderId);
        if (order is null)
        {
            return ServiceResult<OrderResponse>.NotFound("Order not found.");
        }

        order.Status = NormalizeStatus(status);
        order.UpdatedAt = DateTime.UtcNow;

        await _db.SaveChangesAsync();
        return ServiceResult<OrderResponse>.Success(await GetOrderResponseAsync(order.Id));
    }

    private ServiceResult<OrderResponse>? ValidateCreateRequest(CreateOrderRequest request)
    {
        var paymentMethod = NormalizePaymentMethod(request.PaymentMethod);
        if (!AllowedPaymentMethods.Contains(paymentMethod))
        {
            return ServiceResult<OrderResponse>.BadRequest("Payment method must be COD or SEPAY.");
        }

        if (paymentMethod == PaymentMethodSepay && !IsSepayConfigured())
        {
            return ServiceResult<OrderResponse>.BadRequest("SePay is not configured. Set Sepay:BankCode and Sepay:AccountNumber.");
        }

        if (string.IsNullOrWhiteSpace(request.Address))
        {
            return ServiceResult<OrderResponse>.BadRequest("Address is required.");
        }

        if (string.IsNullOrWhiteSpace(request.PhoneNumber))
        {
            return ServiceResult<OrderResponse>.BadRequest("Phone number is required.");
        }

        return null;
    }

    private static ServiceResult<OrderResponse>? ValidateCart(List<CartItem> cartItems)
    {
        if (cartItems.Count == 0)
        {
            return ServiceResult<OrderResponse>.BadRequest("Cart is empty.");
        }

        var item = cartItems.FirstOrDefault(item => item.Product.Quantity < item.Quantity);
        return item is null
            ? null
            : ServiceResult<OrderResponse>.Conflict($"Product '{item.Product.Name}' does not have enough stock.");
    }

    private async Task<List<CartItem>> GetCartItemsAsync(int userId)
    {
        return await _db.CartItems
            .Include(item => item.Product)
            .Where(item => item.UserId == userId)
            .OrderBy(item => item.Id)
            .ToListAsync();
    }

    private static Order BuildOrder(
        int userId,
        CreateOrderRequest request,
        string paymentMethod,
        List<CartItem> cartItems)
    {
        return new Order
        {
            UserId = userId,
            Address = request.Address.Trim(),
            PhoneNumber = request.PhoneNumber.Trim(),
            PaymentMethod = paymentMethod,
            PaymentStatus = PaymentStatusPending,
            TotalPrice = cartItems.Sum(item => item.Product.Price * item.Quantity),
            Items = cartItems.Select(ToOrderItem).ToList()
        };
    }

    private static OrderItem ToOrderItem(CartItem item)
    {
        return new OrderItem
        {
            ProductId = item.ProductId,
            ProductName = item.Product.Name,
            Quantity = item.Quantity,
            Price = item.Product.Price,
            ImageUrl = item.Product.ImageUrl
        };
    }

    private void ApplyPaymentFlow(Order order, List<CartItem> cartItems)
    {
        if (order.PaymentMethod == PaymentMethodSepay)
        {
            order.PaymentCode = BuildPaymentCode(order.Id);
            order.UpdatedAt = DateTime.UtcNow;
            return;
        }

        CompleteOrderInventory(cartItems);
        _db.CartItems.RemoveRange(cartItems);
    }

    private async Task<OrderResponse> GetOrderResponseAsync(int id)
    {
        var order = await _db.Orders
            .AsNoTracking()
            .Include(order => order.User)
            .Include(order => order.Items)
            .SingleAsync(order => order.Id == id);

        return ToResponse(order);
    }

    private async Task<List<OrderResponse>> GetOrderResponsesAsync(int? userId = null)
    {
        var query = _db.Orders
            .AsNoTracking()
            .Include(order => order.User)
            .Include(order => order.Items)
            .AsQueryable();

        if (userId.HasValue)
        {
            query = query.Where(order => order.UserId == userId.Value);
        }

        var orders = await query
            .OrderByDescending(order => order.OrderDate)
            .ToListAsync();

        return orders.Select(ToResponse).ToList();
    }

    private static string NormalizeStatus(string status)
    {
        return AllowedStatuses.Single(value => value.Equals(status, StringComparison.OrdinalIgnoreCase));
    }

    private static string NormalizePaymentMethod(string? paymentMethod)
    {
        return string.IsNullOrWhiteSpace(paymentMethod)
            ? PaymentMethodCod
            : paymentMethod.Trim().ToUpperInvariant();
    }

    private static string BuildPaymentCode(int orderId)
    {
        return $"ODD{orderId:D6}";
    }

    private static void CompleteOrderInventory(List<CartItem> cartItems)
    {
        foreach (var item in cartItems)
        {
            item.Product.Quantity -= item.Quantity;
        }
    }

    private bool IsSepayConfigured()
    {
        return !string.IsNullOrWhiteSpace(_configuration["Sepay:BankCode"]) &&
               !string.IsNullOrWhiteSpace(_configuration["Sepay:AccountNumber"]);
    }

    private string? BuildSepayQrUrl(Order order)
    {
        if (!CanBuildSepayQr(order))
        {
            return null;
        }

        var bankCode = _configuration["Sepay:BankCode"]!;
        var accountNumber = _configuration["Sepay:AccountNumber"]!;
        var template = _configuration["Sepay:QrTemplate"] ?? "compact2";
        var query = new QueryString()
            .Add("amount", decimal.ToInt64(order.TotalPrice).ToString())
            .Add("addInfo", order.PaymentCode!);

        return $"https://img.vietqr.io/image/{bankCode}-{accountNumber}-{template}.png" + query;
    }

    private bool CanBuildSepayQr(Order order)
    {
        return order.PaymentMethod.Equals(PaymentMethodSepay, StringComparison.OrdinalIgnoreCase) &&
               !string.IsNullOrWhiteSpace(order.PaymentCode) &&
               IsSepayConfigured();
    }

    private OrderPaymentStatusResponse ToPaymentStatusResponse(Order order)
    {
        return new OrderPaymentStatusResponse(
            order.Id,
            order.TotalPrice,
            order.Status,
            order.PaymentMethod,
            order.PaymentStatus,
            order.PaymentCode,
            BuildSepayQrUrl(order));
    }

    private OrderResponse ToResponse(Order order)
    {
        return new OrderResponse(
            order.Id,
            order.UserId,
            order.User.Username,
            order.OrderDate,
            order.TotalPrice,
            order.Status,
            order.Address,
            order.PhoneNumber,
            order.PaymentMethod,
            order.PaymentStatus,
            order.PaymentCode,
            BuildSepayQrUrl(order),
            order.Items.Select(ToOrderItemResponse).ToList());
    }

    private static OrderItemResponse ToOrderItemResponse(OrderItem item)
    {
        return new OrderItemResponse(
            item.Id,
            item.ProductId,
            item.ProductName,
            item.Quantity,
            item.Price,
            item.ImageUrl ?? string.Empty,
            item.Price * item.Quantity);
    }
}
