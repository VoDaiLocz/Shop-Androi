# Backend overview

## Công nghệ

- ASP.NET Core Web API.
- EF Core.
- MySQL.
- JWT Bearer Authentication.
- Google token validation.
- SePay webhook + VietQR QR URL.

## Cấu trúc backend

```text
backend/ShopApi/
├── Controllers/
├── Services/
├── Data/
├── Dtos/
├── Models/
├── Migrations/
├── wwwroot/
└── Program.cs
```

## Trách nhiệm từng tầng

Controller:

- Nhận HTTP request.
- Kiểm tra authorization ở boundary.
- Gọi service hoặc DbContext.
- Trả HTTP response.

Service:

- Xử lý nghiệp vụ phức tạp.
- `OrderService`: tạo đơn, status, QR payment URL.
- `PaymentService`: webhook SePay, xác nhận tiền, trừ kho, xóa cart.

DbContext:

- Đại diện database session.
- Khai báo `DbSet`.
- Cấu hình entity relationship, index, constraint.

DTO:

- Request/response public contract của API.
- Không trả trực tiếp entity ra Android.

Model:

- Entity ánh xạ bảng database.

## Controller nào đang có service?

```text
OrdersController -> IOrderService -> OrderService -> ShopDbContext
PaymentsController -> IPaymentService -> PaymentService -> ShopDbContext
```

Các controller CRUD đơn giản vẫn dùng trực tiếp `ShopDbContext`:

- `AuthController`
- `ProductsController`
- `CategoriesController`
- `CartController`
- `UsersController`

Lý do: logic còn ngắn; thêm service/repository ở mọi nơi sẽ dài hơn mà chưa tăng giá trị.

## Câu trả lời mẫu khi bị hỏi về kiến trúc backend

> Backend dùng ASP.NET Core Web API. Các API đơn giản dùng controller và EF Core DbContext trực tiếp. Các nghiệp vụ phức tạp như đặt hàng và thanh toán được tách sang service để controller mỏng hơn và mỗi hàm làm một việc rõ hơn. Dữ liệu lưu ở MySQL qua EF Core migration.
