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
        new("Sofa Oslo Cream", "Sofa", 12900000m, 12, "Sofa vải màu kem phong cách tối giản, phù hợp phòng khách nhỏ và căn hộ hiện đại.", "https://images.pexels.com/photos/1866149/pexels-photo-1866149.jpeg?auto=compress&cs=tinysrgb&w=900"),
        new("Sofa Nora Beige", "Sofa", 15900000m, 8, "Sofa nệm rộng với tone be ấm, tạo cảm giác mềm mại và sang cho không gian sống.", "https://images.pexels.com/photos/1571460/pexels-photo-1571460.jpeg?auto=compress&cs=tinysrgb&w=900"),
        new("Ghế Gỗ Theodore", "Ghế", 3490000m, 18, "Ghế gỗ tự nhiên kết hợp đệm ngồi, thiết kế thanh mảnh cho bàn ăn hoặc góc đọc sách.", "https://images.pexels.com/photos/1350789/pexels-photo-1350789.jpeg?auto=compress&cs=tinysrgb&w=900"),
        new("Ghế Lounge Aster", "Ghế", 4690000m, 10, "Ghế lounge thư giãn với dáng cong mềm, điểm nhấn nổi bật cho phòng khách.", "https://images.pexels.com/photos/276583/pexels-photo-276583.jpeg?auto=compress&cs=tinysrgb&w=900"),
        new("Bàn Cafe Mellow", "Bàn", 2890000m, 15, "Bàn cafe nhỏ gọn, mặt bàn tối giản dễ phối với sofa và thảm phòng khách.", "https://images.pexels.com/photos/1090638/pexels-photo-1090638.jpeg?auto=compress&cs=tinysrgb&w=900"),
        new("Bàn Ăn Haven", "Bàn", 8990000m, 7, "Bàn ăn gia đình tone gỗ ấm, bề mặt rộng và thiết kế sạch cho không gian bếp hiện đại.", "https://images.pexels.com/photos/1571453/pexels-photo-1571453.jpeg?auto=compress&cs=tinysrgb&w=900"),
        new("Giường Linen Calm", "Giường", 11900000m, 6, "Giường ngủ bọc vải sáng màu, thiết kế thấp và êm cho phòng ngủ tối giản.", "https://images.pexels.com/photos/271816/pexels-photo-271816.jpeg?auto=compress&cs=tinysrgb&w=900"),
        new("Tủ Gỗ Carlisle", "Tủ", 7590000m, 9, "Tủ gỗ thấp nhiều ngăn, dùng làm kệ TV hoặc tủ lưu trữ trong phòng khách.", "https://images.pexels.com/photos/2062431/pexels-photo-2062431.jpeg?auto=compress&cs=tinysrgb&w=900")
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

        if (await db.Products.AnyAsync())
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
