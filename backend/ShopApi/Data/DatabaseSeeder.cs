using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using ShopApi.Models;

namespace ShopApi.Data;

public static class DatabaseSeeder
{
    private const string DevelopmentAdminEmail = "admin@shop.local";
    private const string DevelopmentAdminUsername = "Admin";
    private const string DevelopmentAdminPassword = "Admin123456!";

    private static readonly SeedProduct[] SeedProducts =
    [
        new("Carlisle Double", "Tủ", 583m, 9, "Tủ gỗ thấp nhiều ngăn, dùng làm kệ TV hoặc tủ lưu trữ trong phòng khách.", "https://images.pexels.com/photos/2082090/pexels-photo-2082090.jpeg?auto=compress&cs=tinysrgb&w=900"),
        new("Facet Table Lamp", "Đèn", 284m, 15, "Đèn bàn dáng thanh mảnh, ánh sáng ấm cho phòng ngủ hoặc góc đọc sách.", "https://images.pexels.com/photos/534151/pexels-photo-534151.jpeg?auto=compress&cs=tinysrgb&w=900"),
        new("Sofa Footstool", "Sofa", 495m, 12, "Ghế đôn bọc vải sáng màu, dễ phối cùng sofa và bàn trà tối giản.", "https://images.pexels.com/photos/116910/pexels-photo-116910.jpeg?auto=compress&cs=tinysrgb&w=900"),
        new("Theodore Chair", "Ghế", 322m, 18, "Ghế gỗ tự nhiên kết hợp đệm ngồi, thiết kế thanh mảnh cho bàn ăn hoặc góc đọc sách.", "https://images.pexels.com/photos/2762247/pexels-photo-2762247.jpeg?auto=compress&cs=tinysrgb&w=900"),
        new("Nora Lounge", "Ghế", 423m, 10, "Ghế lounge thư giãn với dáng cong mềm, điểm nhấn nổi bật cho phòng khách.", "https://images.pexels.com/photos/6580221/pexels-photo-6580221.jpeg?auto=compress&cs=tinysrgb&w=900"),
        new("Mellow Coffee Table", "Bàn", 369m, 15, "Bàn cafe nhỏ gọn, mặt bàn tối giản dễ phối với sofa và thảm phòng khách.", "https://images.pexels.com/photos/1090638/pexels-photo-1090638.jpeg?auto=compress&cs=tinysrgb&w=900"),
        new("Haven Dining Table", "Bàn", 512m, 7, "Bàn ăn gia đình tone gỗ ấm, bề mặt rộng và thiết kế sạch cho không gian bếp hiện đại.", "https://images.pexels.com/photos/1571468/pexels-photo-1571468.jpeg?auto=compress&cs=tinysrgb&w=900"),
        new("Linen Calm Bed", "Giường", 642m, 6, "Giường ngủ bọc vải sáng màu, thiết kế thấp và êm cho phòng ngủ tối giản.", "https://images.pexels.com/photos/271816/pexels-photo-271816.jpeg?auto=compress&cs=tinysrgb&w=900")
    ];

    public static async Task SeedAdminAsync(IServiceProvider services, bool allowDevelopmentDefaults = false)
    {
        using var scope = services.CreateScope();
        var configuration = scope.ServiceProvider.GetRequiredService<IConfiguration>();
        var db = scope.ServiceProvider.GetRequiredService<ShopDbContext>();

        var email = configuration["AdminSeed:Email"] ?? DevelopmentAdminEmail;
        var username = configuration["AdminSeed:Username"] ?? DevelopmentAdminUsername;
        var password = configuration["AdminSeed:Password"];

        if (string.IsNullOrWhiteSpace(password))
        {
            if (!allowDevelopmentDefaults)
            {
                throw new InvalidOperationException("Admin seed password is not configured. Set AdminSeed__Password before running --seed-admin.");
            }

            password = DevelopmentAdminPassword;
        }

        var admin = await db.Users.SingleOrDefaultAsync(user => user.Email == email);
        if (admin is null)
        {
            admin = new User
            {
                Username = username,
                Email = email,
                PasswordHash = string.Empty,
                Role = "ADMIN"
            };

            db.Users.Add(admin);
        }
        else
        {
            admin.Username = username;
            admin.Role = "ADMIN";
        }

        var passwordHasher = new PasswordHasher<User>();
        admin.PasswordHash = passwordHasher.HashPassword(admin, password);

        await db.SaveChangesAsync();
        Console.WriteLine($"Admin user ready: {email}");
    }

    public static async Task SeedCatalogAsync(IServiceProvider services)
    {
        using var scope = services.CreateScope();
        var db = scope.ServiceProvider.GetRequiredService<ShopDbContext>();

        var seedNames = SeedProducts.Select(product => product.Name).ToList();
        if (await db.Products.AnyAsync(product => seedNames.Contains(product.Name)))
        {
            return;
        }

        var categories = await EnsureCategoriesAsync(db);

        foreach (var seed in SeedProducts)
        {
            db.Products.Add(new Product
            {
                Name = seed.Name,
                Price = seed.Price,
                Description = seed.Description,
                Quantity = seed.Quantity,
                ImageUrl = seed.ImageUrl,
                CategoryId = categories[seed.CategoryName].Id
            });
        }

        await db.SaveChangesAsync();
        Console.WriteLine($"Seeded {SeedProducts.Length} furniture products.");
    }

    private static async Task<Dictionary<string, Category>> EnsureCategoriesAsync(ShopDbContext db)
    {
        var categoryNames = SeedProducts
            .Select(product => product.CategoryName)
            .Distinct(StringComparer.OrdinalIgnoreCase)
            .ToList();

        var categories = await db.Categories
            .Where(category => categoryNames.Contains(category.Name))
            .ToDictionaryAsync(category => category.Name, StringComparer.OrdinalIgnoreCase);

        foreach (var name in categoryNames)
        {
            if (categories.ContainsKey(name))
            {
                continue;
            }

            var category = new Category
            {
                Name = name,
                ImageUrl = string.Empty
            };

            db.Categories.Add(category);
            categories[name] = category;
        }

        await db.SaveChangesAsync();
        return categories;
    }

    private record SeedProduct(
        string Name,
        string CategoryName,
        decimal Price,
        int Quantity,
        string Description,
        string ImageUrl);
}
