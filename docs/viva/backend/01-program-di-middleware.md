# Program, DI, middleware

## Program.cs làm gì?

`Program.cs` là nơi cấu hình ứng dụng backend.

Các nhóm cấu hình chính:

1. Đăng ký OpenAPI và Controllers.
2. Đăng ký service nghiệp vụ.
3. Đăng ký `ShopDbContext`.
4. Cấu hình JWT authentication.
5. Chạy migration và seed data development.
6. Cấu hình middleware pipeline.

## Dependency Injection

Đăng ký service:

```csharp
builder.Services.AddScoped<IOrderService, OrderService>();
builder.Services.AddScoped<IPaymentService, PaymentService>();
```

Vì sao dùng `Scoped`:

- Mỗi HTTP request có một scope.
- `ShopDbContext` cũng là scoped.
- Service phụ thuộc DbContext nên service cũng nên scoped.

Đăng ký DbContext:

```csharp
builder.Services.AddDbContext<ShopDbContext>(options =>
    options.UseMySql(connectionString, new MySqlServerVersion(new Version(8, 0, 0))));
```

## JWT configuration

Backend đọc:

- `Jwt:Key`
- `Jwt:Issuer`
- `Jwt:Audience`
- `Jwt:ExpiresMinutes`

Nếu development thiếu `Jwt:Key`, backend dùng key local:

```text
shop-local-development-jwt-key-1234567890
```

Production thì không nên dùng key mặc định.

## Middleware pipeline

Thứ tự:

```text
UseHttpsRedirection
UseStaticFiles
UseAuthentication
UseAuthorization
MapControllers
```

Điểm cần nhớ:

- `UseAuthentication()` phải đứng trước `UseAuthorization()`.
- `UseStaticFiles()` để backend phục vụ ảnh trong `wwwroot/uploads/...`.
- `MapControllers()` map các controller route.

## Migration và seed

Khi app start:

```csharp
await MigrateDatabaseAsync(app.Services);
```

Development:

- Seed admin mặc định.
- Seed catalog sản phẩm/hình ảnh.

Câu trả lời mẫu:

> Backend đăng ký DbContext, service, controller và JWT trong `Program.cs`. Khi chạy, app tự migrate database, development thì seed admin và dữ liệu sản phẩm. Middleware dùng authentication trước authorization để bảo vệ API theo JWT và role.
