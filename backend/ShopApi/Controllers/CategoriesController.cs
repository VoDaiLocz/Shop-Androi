using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using ShopApi.Data;
using ShopApi.Dtos;
using ShopApi.Models;

namespace ShopApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class CategoriesController : ControllerBase
{
    private readonly ShopDbContext _db;

    public CategoriesController(ShopDbContext db)
    {
        _db = db;
    }

    [HttpGet]
    public async Task<ActionResult<List<CategoryResponse>>> GetAll()
    {
        var categories = await _db.Categories
            .AsNoTracking()
            .OrderBy(category => category.Name)
            .Select(category => ToResponse(category))
            .ToListAsync();

        return Ok(categories);
    }

    [HttpGet("{id:int}")]
    public async Task<ActionResult<CategoryResponse>> GetById(int id)
    {
        var category = await _db.Categories
            .AsNoTracking()
            .SingleOrDefaultAsync(category => category.Id == id);

        if (category is null)
        {
            return NotFound(new { message = "Category not found." });
        }

        return Ok(ToResponse(category));
    }

    [Authorize(Roles = "ADMIN")]
    [HttpPost]
    public async Task<ActionResult<CategoryResponse>> Create(CreateCategoryRequest request)
    {
        var name = request.Name.Trim();
        if (string.IsNullOrWhiteSpace(name))
        {
            return BadRequest(new { message = "Category name is required." });
        }

        var category = new Category
        {
            Name = name,
            ImageUrl = NormalizeImageUrl(request.ImageUrl)
        };

        _db.Categories.Add(category);
        await _db.SaveChangesAsync();

        return CreatedAtAction(nameof(GetById), new { id = category.Id }, ToResponse(category));
    }

    [Authorize(Roles = "ADMIN")]
    [HttpPut("{id:int}")]
    public async Task<ActionResult<CategoryResponse>> Update(int id, UpdateCategoryRequest request)
    {
        var name = request.Name.Trim();
        if (string.IsNullOrWhiteSpace(name))
        {
            return BadRequest(new { message = "Category name is required." });
        }

        var category = await _db.Categories.FindAsync(id);
        if (category is null)
        {
            return NotFound(new { message = "Category not found." });
        }

        category.Name = name;
        category.ImageUrl = NormalizeImageUrl(request.ImageUrl);

        await _db.SaveChangesAsync();
        return Ok(ToResponse(category));
    }

    [Authorize(Roles = "ADMIN")]
    [HttpDelete("{id:int}")]
    public async Task<IActionResult> Delete(int id)
    {
        var category = await _db.Categories.FindAsync(id);
        if (category is null)
        {
            return NotFound(new { message = "Category not found." });
        }

        var hasProducts = await _db.Products.AnyAsync(product => product.CategoryId == id);
        if (hasProducts)
        {
            return Conflict(new { message = "Cannot delete category because it has products." });
        }

        _db.Categories.Remove(category);
        await _db.SaveChangesAsync();
        return NoContent();
    }

    private static string NormalizeImageUrl(string? imageUrl)
    {
        return string.IsNullOrWhiteSpace(imageUrl) ? string.Empty : imageUrl.Trim();
    }

    private static CategoryResponse ToResponse(Category category)
    {
        return new CategoryResponse(
            category.Id,
            category.Name,
            category.ImageUrl ?? string.Empty);
    }
}
