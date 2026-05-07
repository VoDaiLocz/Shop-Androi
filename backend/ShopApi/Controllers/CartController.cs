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
[Route("api/cart")]
public class CartController : ControllerBase
{
    private readonly ShopDbContext _db;

    public CartController(ShopDbContext db)
    {
        _db = db;
    }

    [HttpGet]
    public async Task<ActionResult<CartResponse>> GetCart()
    {
        var userId = GetUserId();
        if (userId is null)
        {
            return Unauthorized(new { message = "Invalid token." });
        }

        return Ok(await GetCartResponse(userId.Value));
    }

    [HttpPost("items")]
    public async Task<ActionResult<CartResponse>> AddItem(AddCartItemRequest request)
    {
        var userId = GetUserId();
        if (userId is null)
        {
            return Unauthorized(new { message = "Invalid token." });
        }

        if (request.Quantity <= 0)
        {
            return BadRequest(new { message = "Quantity must be greater than 0." });
        }

        var product = await _db.Products.FindAsync(request.ProductId);
        if (product is null)
        {
            return BadRequest(new { message = "Product does not exist." });
        }

        var cartItem = await _db.CartItems
            .SingleOrDefaultAsync(item => item.UserId == userId.Value && item.ProductId == request.ProductId);

        if (cartItem is null)
        {
            _db.CartItems.Add(new CartItem
            {
                UserId = userId.Value,
                ProductId = request.ProductId,
                Quantity = request.Quantity
            });
        }
        else
        {
            cartItem.Quantity += request.Quantity;
            cartItem.UpdatedAt = DateTime.UtcNow;
        }

        await _db.SaveChangesAsync();
        return Ok(await GetCartResponse(userId.Value));
    }

    [HttpPut("items/{id:int}")]
    public async Task<ActionResult<CartResponse>> UpdateItem(int id, UpdateCartItemRequest request)
    {
        var userId = GetUserId();
        if (userId is null)
        {
            return Unauthorized(new { message = "Invalid token." });
        }

        if (request.Quantity <= 0)
        {
            return BadRequest(new { message = "Quantity must be greater than 0." });
        }

        var cartItem = await _db.CartItems
            .SingleOrDefaultAsync(item => item.Id == id && item.UserId == userId.Value);

        if (cartItem is null)
        {
            return NotFound(new { message = "Cart item not found." });
        }

        cartItem.Quantity = request.Quantity;
        cartItem.UpdatedAt = DateTime.UtcNow;

        await _db.SaveChangesAsync();
        return Ok(await GetCartResponse(userId.Value));
    }

    [HttpDelete("items/{id:int}")]
    public async Task<ActionResult<CartResponse>> DeleteItem(int id)
    {
        var userId = GetUserId();
        if (userId is null)
        {
            return Unauthorized(new { message = "Invalid token." });
        }

        var cartItem = await _db.CartItems
            .SingleOrDefaultAsync(item => item.Id == id && item.UserId == userId.Value);

        if (cartItem is null)
        {
            return NotFound(new { message = "Cart item not found." });
        }

        _db.CartItems.Remove(cartItem);
        await _db.SaveChangesAsync();

        return Ok(await GetCartResponse(userId.Value));
    }

    [HttpDelete]
    public async Task<IActionResult> ClearCart()
    {
        var userId = GetUserId();
        if (userId is null)
        {
            return Unauthorized(new { message = "Invalid token." });
        }

        await _db.CartItems
            .Where(item => item.UserId == userId.Value)
            .ExecuteDeleteAsync();

        return NoContent();
    }

    private int? GetUserId()
    {
        var userIdValue = User.FindFirstValue(ClaimTypes.NameIdentifier);
        return int.TryParse(userIdValue, out var userId) ? userId : null;
    }

    private async Task<CartResponse> GetCartResponse(int userId)
    {
        var items = await _db.CartItems
            .AsNoTracking()
            .Include(item => item.Product)
            .Where(item => item.UserId == userId)
            .OrderBy(item => item.Id)
            .Select(item => new CartItemResponse(
                item.Id,
                item.ProductId,
                item.Product.Name,
                item.Product.Price,
                item.Product.ImageUrl ?? string.Empty,
                item.Quantity,
                item.Product.Price * item.Quantity))
            .ToListAsync();

        return new CartResponse(items, items.Sum(item => item.LineTotal));
    }
}
