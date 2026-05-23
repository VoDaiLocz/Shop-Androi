using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using ShopApi.Data;
using ShopApi.Dtos;
using ShopApi.Models;

namespace ShopApi.Controllers;

[Authorize]
[ApiController]
[Route("api/orders")]
public class OrdersController : ControllerBase
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

    public OrdersController(ShopDbContext db, IConfiguration configuration)
    {
        _db = db;
        _configuration = configuration;
    }

    [HttpPost]
    public async Task<ActionResult<OrderResponse>> Create(CreateOrderRequest request)
    {
        var userId = GetUserId();
        if (userId is null)
        {
            return Unauthorized(new { message = "Invalid token." });
        }

        var address = request.Address.Trim();
        var phoneNumber = request.PhoneNumber.Trim();
        var paymentMethod = NormalizePaymentMethod(request.PaymentMethod);
        if (!AllowedPaymentMethods.Contains(paymentMethod))
        {
            return BadRequest(new { message = "Payment method must be COD or SEPAY." });
        }

        if (paymentMethod == PaymentMethodSepay && !IsSepayConfigured())
        {
            return BadRequest(new { message = "SePay is not configured. Set Sepay:BankCode and Sepay:AccountNumber." });
        }

        if (string.IsNullOrWhiteSpace(address))
        {
            return BadRequest(new { message = "Address is required." });
        }

        if (string.IsNullOrWhiteSpace(phoneNumber))
        {
            return BadRequest(new { message = "Phone number is required." });
        }

        var cartItems = await _db.CartItems
            .Include(item => item.Product)
            .Where(item => item.UserId == userId.Value)
            .OrderBy(item => item.Id)
            .ToListAsync();

        if (cartItems.Count == 0)
        {
            return BadRequest(new { message = "Cart is empty." });
        }

        foreach (var item in cartItems)
        {
            if (item.Product.Quantity < item.Quantity)
            {
                return Conflict(new { message = $"Product '{item.Product.Name}' does not have enough stock." });
            }
        }

        await using var transaction = await _db.Database.BeginTransactionAsync();

        var order = new Order
        {
            UserId = userId.Value,
            Address = address,
            PhoneNumber = phoneNumber,
            PaymentMethod = paymentMethod,
            PaymentStatus = PaymentStatusPending,
            TotalPrice = cartItems.Sum(item => item.Product.Price * item.Quantity),
            Items = cartItems.Select(item => new OrderItem
            {
                ProductId = item.ProductId,
                ProductName = item.Product.Name,
                Quantity = item.Quantity,
                Price = item.Product.Price,
                ImageUrl = item.Product.ImageUrl
            }).ToList()
        };

        _db.Orders.Add(order);
        await _db.SaveChangesAsync();

        if (paymentMethod == PaymentMethodSepay)
        {
            order.PaymentCode = BuildPaymentCode(order.Id);
            order.UpdatedAt = DateTime.UtcNow;
        }
        else
        {
            CompleteOrderInventory(cartItems);
            _db.CartItems.RemoveRange(cartItems);
        }

        await _db.SaveChangesAsync();
        await transaction.CommitAsync();

        return StatusCode(StatusCodes.Status201Created, await GetOrderResponse(order.Id));
    }

    [HttpGet("my")]
    public async Task<ActionResult<List<OrderResponse>>> GetMyOrders()
    {
        var userId = GetUserId();
        if (userId is null)
        {
            return Unauthorized(new { message = "Invalid token." });
        }

        return Ok(await GetOrderResponses(userId.Value));
    }

    [HttpGet("{id:int}/payment-status")]
    public async Task<ActionResult<OrderPaymentStatusResponse>> GetPaymentStatus(int id)
    {
        var userId = GetUserId();
        if (userId is null)
        {
            return Unauthorized(new { message = "Invalid token." });
        }

        var order = await _db.Orders.AsNoTracking().SingleOrDefaultAsync(order => order.Id == id);
        if (order is null)
        {
            return NotFound(new { message = "Order not found." });
        }

        if (order.UserId != userId.Value && !User.IsInRole("ADMIN"))
        {
            return Forbid();
        }

        return Ok(ToPaymentStatusResponse(order));
    }

    [Authorize(Roles = "ADMIN")]
    [HttpGet]
    public async Task<ActionResult<List<OrderResponse>>> GetAll()
    {
        return Ok(await GetOrderResponses());
    }

    [Authorize(Roles = "ADMIN")]
    [HttpPut("{id:int}/status")]
    public async Task<ActionResult<OrderResponse>> UpdateStatus(int id, UpdateOrderStatusRequest request)
    {
        var status = request.Status.Trim();
        if (!AllowedStatuses.Contains(status))
        {
            return BadRequest(new { message = "Status must be Pending, Shipping, Delivered, or Cancelled." });
        }

        var order = await _db.Orders.FindAsync(id);
        if (order is null)
        {
            return NotFound(new { message = "Order not found." });
        }

        order.Status = NormalizeStatus(status);
        order.UpdatedAt = DateTime.UtcNow;

        await _db.SaveChangesAsync();
        return Ok(await GetOrderResponse(order.Id));
    }

    private int? GetUserId()
    {
        var userIdValue = User.FindFirstValue(ClaimTypes.NameIdentifier);
        return int.TryParse(userIdValue, out var userId) ? userId : null;
    }

    private async Task<OrderResponse> GetOrderResponse(int id)
    {
        var order = await _db.Orders
            .AsNoTracking()
            .Include(order => order.User)
            .Include(order => order.Items)
            .SingleAsync(order => order.Id == id);

        return ToResponse(order);
    }

    private async Task<List<OrderResponse>> GetOrderResponses(int? userId = null)
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
        if (!order.PaymentMethod.Equals(PaymentMethodSepay, StringComparison.OrdinalIgnoreCase) ||
            string.IsNullOrWhiteSpace(order.PaymentCode))
        {
            return null;
        }

        var bankCode = _configuration["Sepay:BankCode"];
        var accountNumber = _configuration["Sepay:AccountNumber"];
        if (string.IsNullOrWhiteSpace(bankCode) || string.IsNullOrWhiteSpace(accountNumber))
        {
            return null;
        }

        var template = _configuration["Sepay:QrTemplate"] ?? "compact2";
        var query = new QueryString()
            .Add("amount", decimal.ToInt64(order.TotalPrice).ToString())
            .Add("addInfo", order.PaymentCode);

        return $"https://img.vietqr.io/image/{bankCode}-{accountNumber}-{template}.png" + query;
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
            order.Items.Select(item => new OrderItemResponse(
                    item.Id,
                    item.ProductId,
                    item.ProductName,
                    item.Quantity,
                    item.Price,
                    item.ImageUrl ?? string.Empty,
                    item.Price * item.Quantity))
                .ToList());
    }
}
