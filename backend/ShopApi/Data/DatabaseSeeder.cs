using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using ShopApi.Models;

namespace ShopApi.Data;

public static class DatabaseSeeder
{
    private const string DevelopmentAdminEmail = "admin@shop.local";
    private const string DevelopmentAdminUsername = "Admin";
    private const string DevelopmentAdminPassword = "Admin123456!";

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
}
