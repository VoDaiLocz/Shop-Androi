using System.ComponentModel.DataAnnotations;

namespace ShopApi.Dtos;

public record RegisterRequest(
    [Required, StringLength(100, MinimumLength = 2)] string Username,
    [Required, EmailAddress, StringLength(255)] string Email,
    [Required, StringLength(100, MinimumLength = 6)] string Password);

public record LoginRequest(
    [Required, EmailAddress, StringLength(255)] string Email,
    [Required] string Password);

public record UserResponse(
    int Id,
    string Username,
    string Email,
    string Role);

public record LoginResponse(
    string Token,
    UserResponse User);
