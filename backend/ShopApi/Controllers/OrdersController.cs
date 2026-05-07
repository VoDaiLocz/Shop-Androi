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
    private static readonly HashSet<string> AllowedStatuses = new(StringComparer.OrdinalIgnoreCase)
    {
        "Pending",
        "Shipping",
        "Delivered",
        "Cancelled"
    };

    private readonly ShopDbContext _db;

    public OrdersController(ShopDbContext db)
    {
        _db = db;
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
        var paymentMethod = string.IsNullOrWhiteSpace(request.PaymentMethod) ? "COD" : request.PaymentMethod.Trim();

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

        foreach (var item in cartItems)
        {
            item.Product.Quantity -= item.Quantity;
        }

        _db.Orders.Add(order);
        _db.CartItems.RemoveRange(cartItems);
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

    private static OrderResponse ToResponse(Order order)
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
