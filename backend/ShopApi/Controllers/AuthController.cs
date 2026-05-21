using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Google.Apis.Auth;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using ShopApi.Data;
using ShopApi.Dtos;
using ShopApi.Models;

namespace ShopApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
    private readonly ShopDbContext _db;
    private readonly IConfiguration _configuration;
    private readonly PasswordHasher<User> _passwordHasher = new();

    public AuthController(ShopDbContext db, IConfiguration configuration)
    {
        _db = db;
        _configuration = configuration;
    }

    [HttpPost("register")]
    public async Task<ActionResult<UserResponse>> Register(RegisterRequest request)
    {
        var username = request.Username.Trim();
        if (string.IsNullOrWhiteSpace(username))
        {
            return BadRequest(new { message = "Username is required." });
        }

        var email = request.Email.Trim().ToLowerInvariant();
        var emailExists = await _db.Users.AnyAsync(user => user.Email == email);
        if (emailExists)
        {
            return Conflict(new { message = "Email already exists." });
        }

        var user = new User
        {
            Username = username,
            Email = email,
            PasswordHash = string.Empty,
            Role = "USER"
        };

        user.PasswordHash = _passwordHasher.HashPassword(user, request.Password);

        _db.Users.Add(user);
        await _db.SaveChangesAsync();

        return StatusCode(StatusCodes.Status201Created, ToUserResponse(user));
    }

    [HttpPost("login")]
    public async Task<ActionResult<LoginResponse>> Login(LoginRequest request)
    {
        var email = request.Email.Trim().ToLowerInvariant();
        var user = await _db.Users.SingleOrDefaultAsync(user => user.Email == email);
        if (user is null)
        {
            return Unauthorized(new { message = "Invalid email or password." });
        }

        var verification = _passwordHasher.VerifyHashedPassword(user, user.PasswordHash, request.Password);
        if (verification == PasswordVerificationResult.Failed)
        {
            return Unauthorized(new { message = "Invalid email or password." });
        }

        var token = CreateToken(user);
        return Ok(new LoginResponse(token, ToUserResponse(user)));
    }

    [HttpPost("google")]
    public async Task<ActionResult<LoginResponse>> Google(GoogleLoginRequest request)
    {
        var googleClientId = _configuration["Google:ClientId"];
        if (string.IsNullOrWhiteSpace(googleClientId))
        {
            throw new InvalidOperationException("Google client ID is not configured. Set Google__ClientId before running the API.");
        }

        GoogleJsonWebSignature.Payload payload;
        try
        {
            payload = await GoogleJsonWebSignature.ValidateAsync(
                request.IdToken,
                new GoogleJsonWebSignature.ValidationSettings
                {
                    Audience = new[] { googleClientId }
                });
        }
        catch
        {
            return Unauthorized(new { message = "Invalid Google token." });
        }

        if (!payload.EmailVerified || string.IsNullOrWhiteSpace(payload.Email))
        {
            return Unauthorized(new { message = "Google account email is not verified." });
        }

        var email = payload.Email.Trim().ToLowerInvariant();
        var user = await _db.Users.SingleOrDefaultAsync(user => user.GoogleSub == payload.Subject);
        if (user is null)
        {
            user = await _db.Users.SingleOrDefaultAsync(user => user.Email == email);
            if (user is null)
            {
                user = new User
                {
                    Username = CreateGoogleUsername(payload, email),
                    Email = email,
                    PasswordHash = string.Empty,
                    Role = "USER",
                    GoogleSub = payload.Subject
                };

                _db.Users.Add(user);
            }
            else if (user.GoogleSub is null)
            {
                user.GoogleSub = payload.Subject;
            }
            else
            {
                return Conflict(new { message = "Email is already linked to another Google account." });
            }

            await _db.SaveChangesAsync();
        }

        var token = CreateToken(user);
        return Ok(new LoginResponse(token, ToUserResponse(user)));
    }

    [Authorize]
    [HttpGet("me")]
    public async Task<ActionResult<UserResponse>> Me()
    {
        var userIdValue = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (!int.TryParse(userIdValue, out var userId))
        {
            return Unauthorized(new { message = "Invalid token." });
        }

        var user = await _db.Users.FindAsync(userId);
        if (user is null)
        {
            return Unauthorized(new { message = "User no longer exists." });
        }

        return Ok(ToUserResponse(user));
    }

    private string CreateToken(User user)
    {
        var key = _configuration["Jwt:Key"];
        if (string.IsNullOrWhiteSpace(key))
        {
            throw new InvalidOperationException("JWT key is not configured. Set Jwt__Key before running the API.");
        }

        var issuer = _configuration["Jwt:Issuer"] ?? "ShopApi";
        var audience = _configuration["Jwt:Audience"] ?? "ShopAndroid";
        var expiresMinutes = int.TryParse(_configuration["Jwt:ExpiresMinutes"], out var minutes) ? minutes : 120;

        var claims = new[]
        {
            new Claim(ClaimTypes.NameIdentifier, user.Id.ToString()),
            new Claim(ClaimTypes.Name, user.Username),
            new Claim(ClaimTypes.Email, user.Email),
            new Claim(ClaimTypes.Role, user.Role)
        };

        var signingKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(key));
        var credentials = new SigningCredentials(signingKey, SecurityAlgorithms.HmacSha256);
        var token = new JwtSecurityToken(
            issuer: issuer,
            audience: audience,
            claims: claims,
            expires: DateTime.UtcNow.AddMinutes(expiresMinutes),
            signingCredentials: credentials);

        return new JwtSecurityTokenHandler().WriteToken(token);
    }

    private static UserResponse ToUserResponse(User user)
    {
        return new UserResponse(user.Id, user.Username, user.Email, user.Role);
    }

    private static string CreateGoogleUsername(GoogleJsonWebSignature.Payload payload, string email)
    {
        var username = string.IsNullOrWhiteSpace(payload.Name) ? email.Split('@')[0] : payload.Name.Trim();
        return username.Length <= 100 ? username : username[..100];
    }
}
