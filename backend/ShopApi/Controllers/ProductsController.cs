using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using ShopApi.Data;
using ShopApi.Dtos;
using ShopApi.Models;

namespace ShopApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ProductsController : ControllerBase
{
    private const long MaxImageBytes = 5 * 1024 * 1024;
    private static readonly HashSet<string> AllowedImageExtensions = new(StringComparer.OrdinalIgnoreCase)
    {
        ".jpg",
        ".jpeg",
        ".png",
        ".webp"
    };

    private readonly ShopDbContext _db;
    private readonly IWebHostEnvironment _environment;

    public ProductsController(ShopDbContext db, IWebHostEnvironment environment)
    {
        _db = db;
        _environment = environment;
    }

    [HttpGet]
    public async Task<ActionResult<List<ProductResponse>>> GetAll([FromQuery] int? categoryId)
    {
        var query = _db.Products
            .AsNoTracking()
            .Include(product => product.Category)
            .AsQueryable();

        if (categoryId.HasValue)
        {
            query = query.Where(product => product.CategoryId == categoryId.Value);
        }

        var products = await query
            .OrderBy(product => product.Name)
            .Select(product => ToResponse(product))
            .ToListAsync();

        return Ok(products);
    }

    [HttpGet("{id:int}")]
    public async Task<ActionResult<ProductResponse>> GetById(int id)
    {
        var product = await _db.Products
            .AsNoTracking()
            .Include(product => product.Category)
            .SingleOrDefaultAsync(product => product.Id == id);

        if (product is null)
        {
            return NotFound(new { message = "Product not found." });
        }

        return Ok(ToResponse(product));
    }

    [Authorize(Roles = "ADMIN")]
    [HttpPost]
    public async Task<ActionResult<ProductResponse>> Create(CreateProductRequest request)
    {
        var validationError = await ValidateProductInput(request.Name, request.Description, request.Price, request.Quantity, request.CategoryId);
        if (validationError is not null)
        {
            return validationError;
        }

        var product = new Product
        {
            Name = request.Name.Trim(),
            Price = request.Price,
            Description = request.Description.Trim(),
            Quantity = request.Quantity,
            CategoryId = request.CategoryId
        };

        _db.Products.Add(product);
        await _db.SaveChangesAsync();

        return CreatedAtAction(nameof(GetById), new { id = product.Id }, await GetProductResponse(product.Id));
    }

    [Authorize(Roles = "ADMIN")]
    [HttpPut("{id:int}")]
    public async Task<ActionResult<ProductResponse>> Update(int id, UpdateProductRequest request)
    {
        var validationError = await ValidateProductInput(request.Name, request.Description, request.Price, request.Quantity, request.CategoryId);
        if (validationError is not null)
        {
            return validationError;
        }

        var product = await _db.Products.FindAsync(id);

        if (product is null)
        {
            return NotFound(new { message = "Product not found." });
        }

        product.Name = request.Name.Trim();
        product.Price = request.Price;
        product.Description = request.Description.Trim();
        product.Quantity = request.Quantity;
        product.CategoryId = request.CategoryId;
        product.UpdatedAt = DateTime.UtcNow;

        await _db.SaveChangesAsync();
        return Ok(await GetProductResponse(product.Id));
    }

    [Authorize(Roles = "ADMIN")]
    [HttpDelete("{id:int}")]
    public async Task<IActionResult> Delete(int id)
    {
        var product = await _db.Products.FindAsync(id);
        if (product is null)
        {
            return NotFound(new { message = "Product not found." });
        }

        var usedInOrders = await _db.OrderItems.AnyAsync(orderItem => orderItem.ProductId == id);
        if (usedInOrders)
        {
            return Conflict(new { message = "Cannot delete product because it has order history." });
        }

        var usedInCarts = await _db.CartItems.AnyAsync(cartItem => cartItem.ProductId == id);
        if (usedInCarts)
        {
            return Conflict(new { message = "Cannot delete product because it is in user carts." });
        }

        _db.Products.Remove(product);
        await _db.SaveChangesAsync();

        return NoContent();
    }

    [Authorize(Roles = "ADMIN")]
    [HttpPost("{id:int}/image")]
    public async Task<ActionResult<ProductResponse>> UploadImage(int id, IFormFile? file)
    {
        var product = await _db.Products.FindAsync(id);

        if (product is null)
        {
            return NotFound(new { message = "Product not found." });
        }

        if (file is null || file.Length == 0)
        {
            return BadRequest(new { message = "Image file is required." });
        }

        if (file.Length > MaxImageBytes)
        {
            return BadRequest(new { message = "Image file is too large. Max size is 5MB." });
        }

        var extension = Path.GetExtension(file.FileName);
        if (!AllowedImageExtensions.Contains(extension))
        {
            return BadRequest(new { message = "Only .jpg, .jpeg, .png, and .webp images are allowed." });
        }

        var uploadDirectory = Path.Combine(_environment.WebRootPath ?? Path.Combine(_environment.ContentRootPath, "wwwroot"), "uploads", "products");
        Directory.CreateDirectory(uploadDirectory);

        var fileName = $"{Guid.NewGuid():N}{extension.ToLowerInvariant()}";
        var filePath = Path.Combine(uploadDirectory, fileName);

        await using (var stream = System.IO.File.Create(filePath))
        {
            await file.CopyToAsync(stream);
        }

        product.ImageUrl = $"/uploads/products/{fileName}";
        product.UpdatedAt = DateTime.UtcNow;
        await _db.SaveChangesAsync();

        return Ok(await GetProductResponse(product.Id));
    }

    private async Task<ActionResult?> ValidateProductInput(string name, string description, decimal price, int quantity, int categoryId)
    {
        if (string.IsNullOrWhiteSpace(name))
        {
            return BadRequest(new { message = "Product name is required." });
        }

        if (string.IsNullOrWhiteSpace(description))
        {
            return BadRequest(new { message = "Product description is required." });
        }

        if (price < 0)
        {
            return BadRequest(new { message = "Product price must be greater than or equal to 0." });
        }

        if (quantity < 0)
        {
            return BadRequest(new { message = "Product quantity must be greater than or equal to 0." });
        }

        var categoryExists = await _db.Categories.AnyAsync(category => category.Id == categoryId);
        if (!categoryExists)
        {
            return BadRequest(new { message = "Category does not exist." });
        }

        return null;
    }

    private static ProductResponse ToResponse(Product product)
    {
        return new ProductResponse(
            product.Id,
            product.Name,
            product.Price,
            product.Description,
            product.ImageUrl ?? string.Empty,
            product.Quantity,
            product.CategoryId,
            product.Category.Name);
    }

    private async Task<ProductResponse> GetProductResponse(int id)
    {
        var product = await _db.Products
            .AsNoTracking()
            .Include(product => product.Category)
            .SingleAsync(product => product.Id == id);

        return ToResponse(product);
    }
}
