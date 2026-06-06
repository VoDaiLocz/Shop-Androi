# Bản đồ hệ thống

## Mục tiêu dự án

Dự án là ứng dụng Android bán nội thất. Người dùng có thể đăng ký, đăng nhập, xem sản phẩm, thêm giỏ hàng, đặt hàng, thanh toán, xem lịch sử đơn hàng. Admin có thể quản lý sản phẩm, danh mục, đơn hàng, người dùng.

Hệ thống có hai phần:

- Android app: giao diện, thao tác của người dùng/admin.
- Backend API: xác thực, dữ liệu, nghiệp vụ, thanh toán, phân quyền.

## Kiến trúc tổng quát

```text
Android Screen
-> ViewModel
-> Repository
-> Retrofit API interface
-> ASP.NET Core Controller
-> Service hoặc DbContext
-> MySQL database
```

Với nghiệp vụ ngắn như product/category/cart, backend controller dùng trực tiếp `ShopDbContext`.

Với nghiệp vụ nhiều bước như order/payment, backend dùng:

```text
OrdersController -> OrderService -> ShopDbContext
PaymentsController -> PaymentService -> ShopDbContext
```

## Vì sao Android có Repository?

Android Repository nằm giữa ViewModel và Retrofit API. Nó có nhiệm vụ:

- Lấy JWT token từ `AuthRepository`.
- Gọi Retrofit API.
- Map DTO backend sang model Android.
- Giữ state bằng `MutableStateFlow`.
- Refresh dữ liệu sau khi action ghi dữ liệu.

Ví dụ giỏ hàng:

```text
CartScreen
-> CartViewModel
-> CartRepository
-> CartApi
-> /api/cart
```

## Vì sao backend không tách Repository?

Backend dùng EF Core. `ShopDbContext` kế thừa `DbContext`, còn từng bảng được khai báo bằng `DbSet<T>`.

Ví dụ:

```csharp
public DbSet<Product> Products => Set<Product>();
public DbSet<CartItem> CartItems => Set<CartItem>();
public DbSet<Order> Orders => Set<Order>();
```

EF Core đã cung cấp các thao tác data access cơ bản:

- `FindAsync`
- `SingleOrDefaultAsync`
- `Where`
- `Include`
- `Add`
- `Remove`
- `SaveChangesAsync`
- `BeginTransactionAsync`

Nếu tạo repository backend chỉ để bọc lại các method này thì sẽ dài hơn mà không sạch hơn. Vì vậy dự án tách service cho nghiệp vụ phức tạp, không tách repository giả.

## Các module chính

| Module | Android | Backend | Database |
|---|---|---|---|
| Auth | LoginScreen, RegisterScreen, AuthViewModel, AuthRepository | AuthController | Users |
| Product | HomeScreen, ProductScreen, ProductDetailScreen | ProductsController | Products, Categories |
| Category | Home tabs, admin category screens | CategoriesController | Categories |
| Cart | CartScreen, CartViewModel, CartRepository | CartController | CartItems, Products |
| Order | CheckoutScreen, OrderScreen, OrderViewModel | OrdersController, OrderService | Orders, OrderItems, CartItems, Products |
| Payment | PaymentQrScreen | PaymentsController, PaymentService | Orders, OrderItems, CartItems, Products |
| Admin | Dashboard, manage screens | Products/Categories/Orders/Users controllers | Users, Products, Categories, Orders |

## Phân quyền

Backend dùng JWT role claim.

- Role `USER`: xem sản phẩm, giỏ hàng, đặt hàng, xem đơn của mình.
- Role `ADMIN`: quản lý sản phẩm, danh mục, đơn hàng, người dùng.

Android sau đăng nhập kiểm tra `user.role`:

- `ADMIN` -> vào `ADMIN_DASHBOARD`.
- role khác -> vào `HOME`.
