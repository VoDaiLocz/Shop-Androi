using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using ShopApi.Data;
using ShopApi.Services;
using System.Text;

var builder = WebApplication.CreateBuilder(args);

// Render injects PORT env var – bind Kestrel to it
var port = Environment.GetEnvironmentVariable("PORT");
if (!string.IsNullOrEmpty(port))
{
    builder.WebHost.UseUrls($"http://+:{port}");
}

// Add services to the container.
// Learn more about configuring OpenAPI at https://aka.ms/aspnet/openapi
builder.Services.AddOpenApi();
builder.Services.AddControllers();
builder.Services.AddScoped<IOrderService, OrderService>();
builder.Services.AddScoped<IPaymentService, PaymentService>();

var connectionString = builder.Configuration.GetConnectionString("DefaultConnection")
    ?? throw new InvalidOperationException("Connection string 'DefaultConnection' is not configured.");

// Auto-detect DB provider: PostgreSQL (Render) uses "Host=" or "postgresql://", MySQL (local) uses "Server="
var usePostgres = connectionString.Contains("Host=", StringComparison.OrdinalIgnoreCase)
    || connectionString.StartsWith("postgres", StringComparison.OrdinalIgnoreCase);

if (usePostgres)
{
    // Render trả URI dạng postgresql://user:pass@host:port/db → convert sang key-value cho Npgsql
    if (connectionString.StartsWith("postgres", StringComparison.OrdinalIgnoreCase))
    {
        var uri = new Uri(connectionString);
        var userInfo = uri.UserInfo.Split(':');
        connectionString = $"Host={uri.Host};Port={(uri.Port > 0 ? uri.Port : 5432)};Database={uri.AbsolutePath.TrimStart('/')};Username={userInfo[0]};Password={userInfo[1]};SSL Mode=Require;Trust Server Certificate=true";
    }

    AppContext.SetSwitch("Npgsql.EnableLegacyTimestampBehavior", true);
    builder.Services.AddDbContext<ShopDbContext>(options => options.UseNpgsql(connectionString));
}
else
{
    builder.Services.AddDbContext<ShopDbContext>(options =>
        options.UseMySql(connectionString, new MySqlServerVersion(new Version(8, 0, 0))));
}

var jwtKey = builder.Configuration["Jwt:Key"];
if (string.IsNullOrWhiteSpace(jwtKey) && builder.Environment.IsDevelopment())
{
    jwtKey = "shop-local-development-jwt-key-1234567890";
}

if (string.IsNullOrWhiteSpace(jwtKey))
{
    throw new InvalidOperationException("JWT key is not configured. Set Jwt__Key before running the API.");
}

var issuer = builder.Configuration["Jwt:Issuer"] ?? "ShopApi";
var audience = builder.Configuration["Jwt:Audience"] ?? "ShopAndroid";

builder.Services
    .AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidateAudience = true,
            ValidateLifetime = true,
            ValidateIssuerSigningKey = true,
            ValidIssuer = issuer,
            ValidAudience = audience,
            IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtKey))
        };
    });

builder.Services.AddAuthorization();

var app = builder.Build();

await MigrateDatabaseAsync(app.Services, usePostgres);

if (args.Contains("--seed-admin", StringComparer.OrdinalIgnoreCase))
{
    await DatabaseSeeder.SeedAdminAsync(app.Services, app.Environment.IsDevelopment());
    return;
}

if (app.Environment.IsDevelopment())
{
    await DatabaseSeeder.SeedAdminAsync(app.Services, allowDevelopmentDefaults: true);
    await DatabaseSeeder.SeedCatalogAsync(app.Services);
}
else
{
    // Production: seed admin if AdminSeed__Password is set
    var adminPassword = app.Configuration["AdminSeed:Password"];
    if (!string.IsNullOrWhiteSpace(adminPassword))
    {
        await DatabaseSeeder.SeedAdminAsync(app.Services, allowDevelopmentDefaults: false);
    }
}

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
    app.UseHttpsRedirection();
}

app.UseStaticFiles();
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();

static async Task MigrateDatabaseAsync(IServiceProvider services, bool usePostgres)
{
    using var scope = services.CreateScope();
    var db = scope.ServiceProvider.GetRequiredService<ShopDbContext>();
    if (usePostgres)
        await db.Database.EnsureCreatedAsync();   // tạo schema từ model, không cần migration files
    else
        await db.Database.MigrateAsync();          // dùng MySQL migration files có sẵn
}
