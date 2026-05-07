using System.ComponentModel.DataAnnotations;

namespace ShopApi.Dtos;

public record OrderResponse(
    int Id,
    int UserId,
    string Username,
    DateTime OrderDate,
    decimal TotalPrice,
    string Status,
    string Address,
    string PhoneNumber,
    string PaymentMethod,
    List<OrderItemResponse> Items);

public record OrderItemResponse(
    int Id,
    int ProductId,
    string ProductName,
    int Quantity,
    decimal Price,
    string ImageUrl,
    decimal LineTotal);

public record CreateOrderRequest(
    [Required, StringLength(500, MinimumLength = 1)] string Address,
    [Required, StringLength(30, MinimumLength = 6)] string PhoneNumber,
    [StringLength(20)] string? PaymentMethod);

public record UpdateOrderStatusRequest(
    [Required, StringLength(20)] string Status);
