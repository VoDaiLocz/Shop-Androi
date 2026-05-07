using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using ShopApi.Models;

namespace ShopApi.Data;

public static class DatabaseSeeder
{
    public static async Task SeedAdminAsync(IServiceProvider services)
    {
        using var scope = services.CreateScope();
        var configuration = scope.ServiceProvider.GetRequiredService<IConfiguration>();
        var db = scope.ServiceProvider.GetRequiredService<ShopDbContext>();

        var email = configuration["AdminSeed:Email"] ?? "admin@shop.local";
        var username = configuration["AdminSeed:Username"] ?? "Admin";
        var password = configuration["AdminSeed:Password"];

        if (string.IsNullOrWhiteSpace(password))
        {
            throw new InvalidOperationException("Admin seed password is not configured. Set AdminSeed__Password before running --seed-admin.");
        }

        var adminExists = await db.Users.AnyAsync(user => user.Email == email);
        if (adminExists)
        {
            Console.WriteLine($"Admin user already exists: {email}");
            return;
        }

        var admin = new User
        {
            Username = username,
            Email = email,
            PasswordHash = string.Empty,
            Role = "ADMIN"
        };

        var passwordHasher = new PasswordHasher<User>();
        admin.PasswordHash = passwordHasher.HashPassword(admin, password);

        db.Users.Add(admin);
        await db.SaveChangesAsync();
        Console.WriteLine($"Admin user seeded: {email}");
    }
}
