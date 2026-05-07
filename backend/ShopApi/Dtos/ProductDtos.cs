using System.ComponentModel.DataAnnotations;

namespace ShopApi.Dtos;

public record ProductResponse(
    int Id,
    string Name,
    decimal Price,
    string Description,
    string ImageUrl,
    int Quantity,
    int CategoryId,
    string CategoryName);

public record CreateProductRequest(
    [Required, StringLength(160, MinimumLength = 2)] string Name,
    decimal Price,
    [Required, StringLength(2000, MinimumLength = 1)] string Description,
    int Quantity,
    int CategoryId,
    [StringLength(500)] string? ImageUrl = null);

public record UpdateProductRequest(
    [Required, StringLength(160, MinimumLength = 2)] string Name,
    decimal Price,
    [Required, StringLength(2000, MinimumLength = 1)] string Description,
    int Quantity,
    int CategoryId,
    [StringLength(500)] string? ImageUrl = null);
