using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using ShopApi.Models;

namespace ShopApi.Data;

public static class DatabaseSeeder
{
    private const string DevelopmentAdminEmail = "admin@shop.local";
    private const string DevelopmentAdminUsername = "Admin";
    private const string DevelopmentAdminPassword = "Admin123456!";
    private const string SeedImagePath = "/uploads/products/seed/";

    private static readonly SeedProduct[] SeedProducts =
    [
        new("Sofa Oslo", "Sofa", 890m, 7, "Sofa ba chỗ vải xám sáng, dáng thấp tối giản cho phòng khách hiện đại.", SeedImagePath + "sofa-oslo.jpg"),
        new("Kệ Tường Botanica", "Kệ", 245m, 14, "Kệ tường gỗ sồi bo góc, hai tầng mở để trưng bày đồ decor nhỏ.", SeedImagePath + "ke-tuong-botanica.jpg"),
        new("Kệ Sách Aurora", "Kệ sách", 420m, 9, "Kệ sách gỗ sồi cao, đường nét gọn và nhiều ngăn mở.", SeedImagePath + "ke-sach-aurora.jpg"),
        new("Ghế Mira", "Ghế", 360m, 8, "Ghế thư giãn bọc vải kem với tay gỗ tự nhiên.", SeedImagePath + "ghe-mira.jpg"),
        new("Băng Ghế Aria", "Ghế", 520m, 6, "Băng ghế đệm boucle màu ngà, chân gỗ ngắn và dáng mềm.", SeedImagePath + "bang-ghe-aria.jpg"),
        new("Tủ Noir", "Tủ", 760m, 5, "Tủ trưng bày kính vòm màu đen, khung mảnh và kệ mở bên trong.", SeedImagePath + "tu-noir.jpg"),
        new("Ghế Papasan Luna", "Ghế", 390m, 10, "Ghế papasan mây với đệm kem dày, dáng tròn thư giãn.", SeedImagePath + "ghe-papasan-luna.jpg"),
        new("Kệ Modular", "Kệ", 315m, 12, "Kệ ô thấp bằng gỗ sồi nhạt, sáu ngăn vuông cho lưu trữ gọn.", SeedImagePath + "ke-modular.jpg"),
        new("Bàn Console Sera", "Bàn", 340m, 9, "Bàn console gỗ sồi hẹp đi cùng gương tròn tối giản.", SeedImagePath + "ban-console-sera.jpg"),
        new("Kệ TV Gallery", "Kệ TV", 610m, 6, "Kệ TV trắng dài với ngăn kéo thấp và chân gỗ mảnh.", SeedImagePath + "ke-tv-gallery.jpg"),
        new("Ghế Windsor Oak", "Ghế", 280m, 11, "Ghế ăn gỗ sồi tự nhiên với lưng nan kiểu Windsor hiện đại.", SeedImagePath + "ghe-windsor-oak.jpg"),
        new("Bàn Side Lola", "Bàn", 299m, 10, "Bàn phụ tròn gỗ sồi với chân trụ, hợp sofa và góc đọc sách.", SeedImagePath + "ban-side-lola.jpg"),
        new("Đèn Bàn Facet", "Đèn", 284m, 13, "Đèn bàn gốm nhỏ với chụp vải be, ánh sáng ấm và dáng gọn.", SeedImagePath + "den-ban-facet.jpg"),
        new("Đôn Boucle Sofia", "Đôn", 374m, 8, "Đôn tròn boucle màu kem, chất liệu mềm và dáng thấp hiện đại.", SeedImagePath + "don-boucle-sofia.jpg"),
        new("Ghế Ăn Theodore", "Ghế", 322m, 10, "Ghế ăn bọc vải taupe, chân kim loại đen mảnh.", SeedImagePath + "ghe-an-theodore.jpg")
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

        if (await IsSeedCatalogCurrentAsync(db))
        {
            return;
        }

        await ClearCatalogAsync(db);

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

    private static async Task<bool> IsSeedCatalogCurrentAsync(ShopDbContext db)
    {
        var seedPrices = SeedProducts.ToDictionary(product => product.Name, product => product.Price);
        var products = await db.Products
            .AsNoTracking()
            .Select(product => new { product.Name, product.Price, product.ImageUrl })
            .ToListAsync();

        if (products.Count != SeedProducts.Length)
        {
            return false;
        }

        return products.All(product =>
            seedPrices.ContainsKey(product.Name) &&
            seedPrices[product.Name] == product.Price &&
            product.ImageUrl != null &&
            product.ImageUrl.StartsWith(SeedImagePath));
    }

    private static async Task ClearCatalogAsync(ShopDbContext db)
    {
        await db.CartItems.ExecuteDeleteAsync();
        await db.OrderItems.ExecuteDeleteAsync();
        await db.Orders.ExecuteDeleteAsync();
        await db.Products.ExecuteDeleteAsync();
        await db.Categories.ExecuteDeleteAsync();
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
