using System.ComponentModel.DataAnnotations;

namespace ShopApi.Dtos;

public record CartResponse(
    List<CartItemResponse> Items,
    decimal TotalPrice);

public record CartItemResponse(
    int Id,
    int ProductId,
    string ProductName,
    decimal Price,
    string ImageUrl,
    int Quantity,
    decimal LineTotal);

public record AddCartItemRequest(
    int ProductId,
    [Range(1, int.MaxValue)] int Quantity);

public record UpdateCartItemRequest(
    [Range(1, int.MaxValue)] int Quantity);
