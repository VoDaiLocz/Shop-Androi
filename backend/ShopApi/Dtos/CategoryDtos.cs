using System.ComponentModel.DataAnnotations;

namespace ShopApi.Dtos;

public record CategoryResponse(
    int Id,
    string Name,
    string ImageUrl);

public record CreateCategoryRequest(
    [Required, StringLength(120, MinimumLength = 2)] string Name,
    [StringLength(500)] string? ImageUrl);

public record UpdateCategoryRequest(
    [Required, StringLength(120, MinimumLength = 2)] string Name,
    [StringLength(500)] string? ImageUrl);
