using Microsoft.EntityFrameworkCore;
using ShopApi.Models;

namespace ShopApi.Data;

public class ShopDbContext : DbContext
{
    public ShopDbContext(DbContextOptions<ShopDbContext> options) : base(options)
    {
    }

    public DbSet<User> Users => Set<User>();
    public DbSet<Category> Categories => Set<Category>();
    public DbSet<Product> Products => Set<Product>();
    public DbSet<CartItem> CartItems => Set<CartItem>();
    public DbSet<Order> Orders => Set<Order>();
    public DbSet<OrderItem> OrderItems => Set<OrderItem>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        ConfigureUsers(modelBuilder);
        ConfigureCategories(modelBuilder);
        ConfigureProducts(modelBuilder);
        ConfigureCartItems(modelBuilder);
        ConfigureOrders(modelBuilder);
        ConfigureOrderItems(modelBuilder);
    }

    private static void ConfigureUsers(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<User>(entity =>
        {
            entity.Property(user => user.Username).HasMaxLength(100).IsRequired();
            entity.Property(user => user.Email).HasMaxLength(255).IsRequired();
            entity.Property(user => user.PasswordHash).HasMaxLength(500).IsRequired();
            entity.Property(user => user.Role).HasMaxLength(20).IsRequired();
            entity.Property(user => user.GoogleSub).HasMaxLength(64);
            entity.HasIndex(user => user.Email).IsUnique();
            entity.HasIndex(user => user.GoogleSub).IsUnique();
        });
    }

    private static void ConfigureCategories(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Category>(entity =>
        {
            entity.Property(category => category.Name).HasMaxLength(120).IsRequired();
            entity.Property(category => category.ImageUrl).HasMaxLength(500);
        });
    }

    private static void ConfigureProducts(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Product>(entity =>
        {
            entity.ToTable(table =>
            {
                table.HasCheckConstraint("CK_Products_Price_NonNegative", "\"Price\" >= 0");
                table.HasCheckConstraint("CK_Products_Quantity_NonNegative", "\"Quantity\" >= 0");
            });

            entity.Property(product => product.Name).HasMaxLength(160).IsRequired();
            entity.Property(product => product.Description).HasMaxLength(2000).IsRequired();
            entity.Property(product => product.ImageUrl).HasMaxLength(500);
            entity.Property(product => product.Price).HasPrecision(18, 2);

            entity.HasOne(product => product.Category)
                .WithMany(category => category.Products)
                .HasForeignKey(product => product.CategoryId)
                .OnDelete(DeleteBehavior.Restrict);
        });
    }

    private static void ConfigureCartItems(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<CartItem>(entity =>
        {
            entity.ToTable(table =>
            {
                table.HasCheckConstraint("CK_CartItems_Quantity_Positive", "\"Quantity\" > 0");
            });

            entity.HasIndex(cartItem => new { cartItem.UserId, cartItem.ProductId }).IsUnique();

            entity.HasOne(cartItem => cartItem.User)
                .WithMany(user => user.CartItems)
                .HasForeignKey(cartItem => cartItem.UserId)
                .OnDelete(DeleteBehavior.Cascade);

            entity.HasOne(cartItem => cartItem.Product)
                .WithMany(product => product.CartItems)
                .HasForeignKey(cartItem => cartItem.ProductId)
                .OnDelete(DeleteBehavior.Restrict);
        });
    }

    private static void ConfigureOrders(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Order>(entity =>
        {
            entity.ToTable(table =>
            {
                table.HasCheckConstraint("CK_Orders_TotalPrice_NonNegative", "\"TotalPrice\" >= 0");
                table.HasCheckConstraint("CK_Orders_Status", "\"Status\" IN ('Pending', 'Shipping', 'Delivered', 'Cancelled')");
            });

            entity.Property(order => order.TotalPrice).HasPrecision(18, 2);
            entity.Property(order => order.Status).HasMaxLength(20).IsRequired();
            entity.Property(order => order.Address).HasMaxLength(500).IsRequired();
            entity.Property(order => order.PhoneNumber).HasMaxLength(30).IsRequired();
            entity.Property(order => order.PaymentMethod).HasMaxLength(20).IsRequired();
            entity.Property(order => order.PaymentStatus).HasMaxLength(20).IsRequired();
            entity.Property(order => order.PaymentCode).HasMaxLength(40);
            entity.Property(order => order.SepayTransactionId).HasMaxLength(64);
            entity.Property(order => order.SepayReferenceCode).HasMaxLength(120);
            entity.HasIndex(order => order.PaymentCode).IsUnique();
            entity.HasIndex(order => order.SepayTransactionId).IsUnique();

            entity.HasOne(order => order.User)
                .WithMany(user => user.Orders)
                .HasForeignKey(order => order.UserId)
                .OnDelete(DeleteBehavior.Restrict);
        });
    }

    private static void ConfigureOrderItems(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<OrderItem>(entity =>
        {
            entity.ToTable(table =>
            {
                table.HasCheckConstraint("CK_OrderItems_Quantity_Positive", "\"Quantity\" > 0");
                table.HasCheckConstraint("CK_OrderItems_Price_NonNegative", "\"Price\" >= 0");
            });

            entity.Property(orderItem => orderItem.ProductName).HasMaxLength(160).IsRequired();
            entity.Property(orderItem => orderItem.Price).HasPrecision(18, 2);
            entity.Property(orderItem => orderItem.ImageUrl).HasMaxLength(500);

            entity.HasOne(orderItem => orderItem.Order)
                .WithMany(order => order.Items)
                .HasForeignKey(orderItem => orderItem.OrderId)
                .OnDelete(DeleteBehavior.Cascade);

            entity.HasOne(orderItem => orderItem.Product)
                .WithMany(product => product.OrderItems)
                .HasForeignKey(orderItem => orderItem.ProductId)
                .OnDelete(DeleteBehavior.Restrict);
        });
    }
}
