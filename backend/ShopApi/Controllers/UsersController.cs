using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using ShopApi.Data;
using ShopApi.Dtos;

namespace ShopApi.Controllers;

[Authorize(Roles = "ADMIN")]
[ApiController]
[Route("api/users")]
public class UsersController : ControllerBase
{
    private readonly ShopDbContext _db;

    public UsersController(ShopDbContext db)
    {
        _db = db;
    }

    [HttpGet]
    public async Task<ActionResult<List<UserResponse>>> GetAll()
    {
        var users = await _db.Users
            .AsNoTracking()
            .OrderBy(user => user.Id)
            .Select(user => new UserResponse(user.Id, user.Username, user.Email, user.Role))
            .ToListAsync();

        return Ok(users);
    }

    [HttpDelete("{id:int}")]
    public async Task<IActionResult> Delete(int id)
    {
        var currentUserId = GetCurrentUserId();
        if (currentUserId is null)
        {
            return Unauthorized(new { message = "Invalid token." });
        }

        if (id == currentUserId.Value)
        {
            return BadRequest(new { message = "Cannot delete your own account." });
        }

        var user = await _db.Users.FindAsync(id);
        if (user is null)
        {
            return NotFound(new { message = "User not found." });
        }

        if (user.Role.Equals("ADMIN", StringComparison.OrdinalIgnoreCase))
        {
            return BadRequest(new { message = "Cannot delete admin account." });
        }

        var orderIds = await _db.Orders
            .Where(order => order.UserId == id)
            .Select(order => order.Id)
            .ToListAsync();

        await using var transaction = await _db.Database.BeginTransactionAsync();

        if (orderIds.Count > 0)
        {
            await _db.OrderItems
                .Where(item => orderIds.Contains(item.OrderId))
                .ExecuteDeleteAsync();

            await _db.Orders
                .Where(order => order.UserId == id)
                .ExecuteDeleteAsync();
        }

        await _db.CartItems
            .Where(item => item.UserId == id)
            .ExecuteDeleteAsync();

        _db.Users.Remove(user);
        await _db.SaveChangesAsync();
        await transaction.CommitAsync();

        return NoContent();
    }

    private int? GetCurrentUserId()
    {
        var userIdValue = User.FindFirstValue(ClaimTypes.NameIdentifier);
        return int.TryParse(userIdValue, out var userId) ? userId : null;
    }
}
