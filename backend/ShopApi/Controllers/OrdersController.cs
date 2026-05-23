using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShopApi.Dtos;
using ShopApi.Services;

namespace ShopApi.Controllers;

[Authorize]
[ApiController]
[Route("api/orders")]
public class OrdersController : ControllerBase
{
    private readonly IOrderService _orders;

    public OrdersController(IOrderService orders)
    {
        _orders = orders;
    }

    [HttpPost]
    public async Task<ActionResult<OrderResponse>> Create(CreateOrderRequest request)
    {
        var userId = GetUserId();
        if (userId is null)
        {
            return Unauthorized(new { message = "Invalid token." });
        }

        var result = await _orders.CreateAsync(userId.Value, request);
        return ToActionResult(result, StatusCodes.Status201Created);
    }

    [HttpGet("my")]
    public async Task<ActionResult<List<OrderResponse>>> GetMyOrders()
    {
        var userId = GetUserId();
        if (userId is null)
        {
            return Unauthorized(new { message = "Invalid token." });
        }

        return Ok(await _orders.GetUserOrdersAsync(userId.Value));
    }

    [HttpGet("{id:int}/payment-status")]
    public async Task<ActionResult<OrderPaymentStatusResponse>> GetPaymentStatus(int id)
    {
        var userId = GetUserId();
        if (userId is null)
        {
            return Unauthorized(new { message = "Invalid token." });
        }

        var result = await _orders.GetPaymentStatusAsync(userId.Value, User.IsInRole("ADMIN"), id);
        return ToActionResult(result);
    }

    [Authorize(Roles = "ADMIN")]
    [HttpGet]
    public async Task<ActionResult<List<OrderResponse>>> GetAll()
    {
        return Ok(await _orders.GetAllAsync());
    }

    [Authorize(Roles = "ADMIN")]
    [HttpPut("{id:int}/status")]
    public async Task<ActionResult<OrderResponse>> UpdateStatus(
        int id,
        UpdateOrderStatusRequest request)
    {
        var result = await _orders.UpdateStatusAsync(id, request);
        return ToActionResult(result);
    }

    private int? GetUserId()
    {
        var userIdValue = User.FindFirstValue(ClaimTypes.NameIdentifier);
        return int.TryParse(userIdValue, out var userId) ? userId : null;
    }

    private ActionResult<T> ToActionResult<T>(
        ServiceResult<T> result,
        int successStatusCode = StatusCodes.Status200OK)
    {
        if (result.Succeeded)
        {
            return StatusCode(successStatusCode, result.Value);
        }

        return ToErrorResult(result.Error!);
    }

    private ActionResult ToErrorResult(ServiceError error)
    {
        return error.Type switch
        {
            ServiceErrorType.BadRequest => BadRequest(new { message = error.Message }),
            ServiceErrorType.Conflict => Conflict(new { message = error.Message }),
            ServiceErrorType.NotFound => NotFound(new { message = error.Message }),
            ServiceErrorType.Forbidden => Forbid(),
            _ => BadRequest(new { message = error.Message })
        };
    }
}
