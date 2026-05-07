using Microsoft.EntityFrameworkCore;

namespace ShopApi.Data;

public class ShopDbContext : DbContext
{
    public ShopDbContext(DbContextOptions<ShopDbContext> options) : base(options)
    {
    }
}
