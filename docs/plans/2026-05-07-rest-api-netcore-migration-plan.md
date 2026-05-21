# Kế Hoạch Thực Thi: Chuyển Android Shop Từ Room Sang ASP.NET Core REST API

Ngày tạo: 2026-05-07  
Trạng thái: Checkpoint 1-20 đã hoàn thành; đang rà soát phần model Android còn lệch/backend còn thiếu để hoàn thiện ứng dụng
Phạm vi: Backend ASP.NET Core + MySQL Code First + nối Android bằng Retrofit

## 1. Tóm Tắt Quyết Định Đã Chốt

| Nhóm quyết định | Kết luận |
|---|---|
| Phạm vi backend | Làm đầy đủ Auth + Product + Category + Cart + Order |
| Công nghệ backend | ASP.NET Core Web API |
| Database | MySQL localhost |
| Database approach | Entity Framework Core Code First |
| Vị trí backend | `backend/ShopApi` |
| Authentication | JWT cơ bản |
| Admin account | Seed sẵn admin mặc định |
| Product/category data | Không seed dữ liệu mẫu, database ban đầu trống |
| Admin API | Làm ngay API admin cho product/category |
| Ảnh sản phẩm | Upload ảnh lên backend |
| Upload ảnh | Mức đơn giản: kiểm tra định dạng, dung lượng, lưu vào `wwwroot/uploads/products` |
| Tính tiền order | Backend tự tính theo giá product trong database |
| Trạng thái đơn hàng | `Pending`, `Shipping`, `Delivered`, `Cancelled` |
| Địa chỉ giao hàng | Checkout gửi address text trực tiếp, chưa làm address book |
| Payment | COD only |
| Tồn kho | Có quản lý tồn kho đơn giản |
| Vượt tồn kho | Backend từ chối đặt hàng |
| API response | Trả object trực tiếp, dùng HTTP status code chuẩn |
| Nối Android | Nối từng phần |
| Room | Đã xóa hoàn toàn khỏi Android sau checkpoint cleanup |
| Emulator gọi backend | Dùng `http://10.0.2.2:<port>` |
| Dev CORS/HTTP | Cho phép dev origin rộng, dùng HTTP local |

## 2. Mục Tiêu

Mục tiêu là thay nguồn dữ liệu chính của Android app từ Room local sang backend REST API.

Luồng cũ trước khi migrate:

```text
Android UI
  -> ViewModel
  -> Repository
  -> Room DAO
  -> SQLite trong điện thoại
```

Luồng sau khi chuyển:

```text
Android UI
  -> ViewModel
  -> Repository
  -> Retrofit API
  -> ASP.NET Core Web API
  -> MySQL database
```

Backend sẽ chịu trách nhiệm:

- Đăng ký và đăng nhập.
- Phân quyền user/admin bằng JWT.
- Quản lý category.
- Quản lý product.
- Upload ảnh sản phẩm.
- Quản lý cart theo user đăng nhập.
- Tạo order từ cart.
- Tính tổng tiền ở server.
- Trừ tồn kho khi đặt hàng.
- Quản lý trạng thái đơn hàng.

Android sẽ chịu trách nhiệm:

- Gọi API bằng Retrofit.
- Lưu JWT token.
- Gửi token qua header `Authorization`.
- Hiển thị loading/error/success.
- Không tự xử lý database chính bằng Room.
- Hiển thị ảnh product từ `imageUrl` backend trả về.
- Upload ảnh product từ màn admin ở bước hoàn thiện sau.

## 3. Nguyên Tắc Triển Khai

Triển khai theo checkpoint nhỏ, có review trước khi làm.

Quy trình bắt buộc cho mỗi checkpoint:

```text
1. Codex nêu checkpoint sắp làm.
2. Codex liệt kê hành động cụ thể.
3. Codex liệt kê file/thư mục dự kiến thay đổi.
4. User review và duyệt.
5. Codex mới thực hiện.
6. Codex báo kết quả.
7. Codex nêu cách kiểm tra.
8. User review kết quả trước khi sang checkpoint tiếp theo.
```

Không được tự ý:

- Nhảy nhiều checkpoint cùng lúc.
- Tạo backend trước khi user duyệt checkpoint.
- Sửa Android trước khi backend API tương ứng chạy được.
- Xóa Room trước checkpoint nối Android.
- Đổi API contract đã chốt mà không báo.
- Chạy lệnh destructive.

## 4. Phạm Vi Không Làm Ở Giai Đoạn Đầu

Không làm các phần sau để giữ code gọn và dễ hiểu:

- Clean Architecture nhiều project.
- CQRS.
- MediatR.
- Repository pattern riêng bên backend.
- Unit of Work tự viết.
- Docker.
- Refresh token.
- Payment online thật.
- Address book.
- Offline cache.
- Upload ảnh nâng cao.
- Test automation phức tạp.

Lý do: mục tiêu hiện tại là backend chạy được, Android nối được, logic rõ ràng, dễ review.

## 5. Kiến Trúc Backend Dự Kiến

Backend đặt tại:

```text
backend/ShopApi
```

Cấu trúc thư mục:

```text
backend/ShopApi/
  Controllers/
    AuthController.cs
    CategoriesController.cs
    ProductsController.cs
    CartController.cs
    OrdersController.cs

  Data/
    ShopDbContext.cs

  Dtos/
    AuthDtos.cs
    CategoryDtos.cs
    ProductDtos.cs
    CartDtos.cs
    OrderDtos.cs

  Models/
    User.cs
    Category.cs
    Product.cs
    CartItem.cs
    Order.cs
    OrderItem.cs

  wwwroot/
    uploads/
      products/

  Program.cs
  appsettings.json
```

Thiết kế backend ở giai đoạn đầu:

```text
Controller
  -> ShopDbContext
  -> MySQL
```

Không tách service/repository riêng nếu chưa cần. Với quy mô bài này, controller gọi DbContext trực tiếp giúp ít file, dễ đọc, dễ debug.

## 6. Database Design

### 6.1 Bảng Users

Mục đích: lưu tài khoản user/admin.

Field dự kiến:

```text
Id
Username
Email
PasswordHash
Role
CreatedAt
```

Quy tắc:

- `Email` unique.
- Không lưu password plain text.
- Role ban đầu gồm `USER` và `ADMIN`.
- Admin mặc định được seed khi tạo database.

### 6.2 Bảng Categories

Mục đích: phân loại sản phẩm.

Field dự kiến:

```text
Id
Name
ImageUrl
CreatedAt
```

Quy tắc:

- Database ban đầu không seed category mẫu.
- Admin sẽ thêm category qua API.

### 6.3 Bảng Products

Mục đích: lưu sản phẩm.

Field dự kiến:

```text
Id
Name
Price
Description
ImageUrl
Quantity
CategoryId
CreatedAt
UpdatedAt
```

Quy tắc:

- `Price` phải lớn hơn hoặc bằng 0.
- `Quantity` phải lớn hơn hoặc bằng 0.
- `CategoryId` phải tồn tại.
- `ImageUrl` được cập nhật sau khi upload ảnh.

### 6.4 Bảng CartItems

Mục đích: lưu giỏ hàng hiện tại của user.

Field dự kiến:

```text
Id
UserId
ProductId
Quantity
CreatedAt
UpdatedAt
```

Quy tắc:

- Cart item thuộc user đăng nhập.
- Không tin `UserId` từ Android gửi lên.
- Nếu product đã có trong cart thì tăng quantity.
- Không lưu `ProductName`, `Price`, `ImageUrl` trong cart để tránh lệch dữ liệu.

### 6.5 Bảng Orders

Mục đích: lưu đơn hàng.

Field dự kiến:

```text
Id
UserId
OrderDate
TotalPrice
Status
Address
PhoneNumber
PaymentMethod
CreatedAt
UpdatedAt
```

Quy tắc:

- `PaymentMethod` mặc định là `COD`.
- `Status` chỉ nhận một trong các giá trị: `Pending`, `Shipping`, `Delivered`, `Cancelled`.
- `TotalPrice` do backend tự tính.
- Android không được quyết định tổng tiền.

### 6.6 Bảng OrderItems

Mục đích: lưu snapshot sản phẩm tại thời điểm đặt hàng.

Field dự kiến:

```text
Id
OrderId
ProductId
ProductName
Quantity
Price
ImageUrl
```

Quy tắc:

- Lưu `ProductName`, `Price`, `ImageUrl` để lịch sử đơn hàng không bị thay đổi khi admin sửa product sau này.
- Khi tạo order, backend trừ tồn kho product.
- Nếu tồn kho không đủ, backend từ chối tạo order.

## 7. API Contract

API trả object trực tiếp, không bọc envelope.

Ví dụ thành công:

```json
[
  {
    "id": 1,
    "name": "Sofa",
    "price": 2500000
  }
]
```

Ví dụ lỗi:

```http
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
```

Body lỗi có thể đơn giản:

```json
{
  "message": "Email already exists"
}
```

### 7.1 Auth API

```text
POST /api/auth/register
POST /api/auth/login
GET  /api/auth/me
```

Register request:

```json
{
  "username": "Nguyen Van A",
  "email": "user@gmail.com",
  "password": "123456"
}
```

Login request:

```json
{
  "email": "user@gmail.com",
  "password": "123456"
}
```

Login response:

```json
{
  "token": "jwt-token",
  "user": {
    "id": 1,
    "username": "Nguyen Van A",
    "email": "user@gmail.com",
    "role": "USER"
  }
}
```

### 7.2 Category API

Public:

```text
GET /api/categories
GET /api/categories/{id}
```

Admin:

```text
POST   /api/categories
PUT    /api/categories/{id}
DELETE /api/categories/{id}
```

Create category request:

```json
{
  "name": "Sofa",
  "imageUrl": ""
}
```

### 7.3 Product API

Public:

```text
GET /api/products
GET /api/products/{id}
GET /api/products?categoryId=1
```

Admin:

```text
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}
POST   /api/products/{id}/image
```

Create product request:

```json
{
  "name": "Sofa cao cấp",
  "price": 2500000,
  "description": "Sofa phòng khách",
  "quantity": 10,
  "categoryId": 1
}
```

Upload image:

```text
POST /api/products/{id}/image
Content-Type: multipart/form-data
Field: file
```

Upload ảnh mức đơn giản:

- Chỉ nhận `.jpg`, `.jpeg`, `.png`, `.webp`.
- Giới hạn dung lượng.
- Lưu vào `wwwroot/uploads/products`.
- Cập nhật `Product.ImageUrl`.

### 7.4 Cart API

Tất cả API cart yêu cầu JWT.

```text
GET    /api/cart
POST   /api/cart/items
PUT    /api/cart/items/{id}
DELETE /api/cart/items/{id}
DELETE /api/cart
```

Add cart item request:

```json
{
  "productId": 1,
  "quantity": 2
}
```

Update quantity request:

```json
{
  "quantity": 3
}
```

Quy tắc:

- Backend lấy `UserId` từ token.
- Backend kiểm tra product tồn tại.
- Backend kiểm tra quantity hợp lệ.
- Nếu item đã có thì cộng quantity.

### 7.5 Order API

Tạo order yêu cầu JWT.

```text
POST /api/orders
GET  /api/orders/my
GET  /api/orders
PUT  /api/orders/{id}/status
```

Create order request:

```json
{
  "address": "123 Nguyen Trai, Quan 1",
  "phoneNumber": "0909123456"
}
```

Update status request:

```json
{
  "status": "Shipping"
}
```

Quy tắc:

- `POST /api/orders` tạo order từ cart hiện tại.
- Backend tự tính `TotalPrice`.
- Backend trừ tồn kho.
- Backend xóa cart sau khi tạo order thành công.
- Nếu cart rỗng, trả `400 Bad Request`.
- Nếu tồn kho không đủ, trả `409 Conflict`.
- `GET /api/orders` chỉ admin được gọi.
- `PUT /api/orders/{id}/status` chỉ admin được gọi.

## 8. Luồng Nghiệp Vụ Chính

### 8.1 Đăng ký

```text
Android nhập username/email/password
  -> POST /api/auth/register
  -> Backend kiểm tra email
  -> Backend hash password
  -> Backend lưu user role USER
  -> Backend trả user không có password
```

### 8.2 Đăng nhập

```text
Android gửi email/password
  -> POST /api/auth/login
  -> Backend kiểm tra thông tin
  -> Backend tạo JWT
  -> Android lưu token
```

Các request cần đăng nhập sẽ gửi:

```text
Authorization: Bearer <token>
```

### 8.3 Admin thêm sản phẩm

```text
Admin login
  -> POST /api/categories
  -> POST /api/products
  -> POST /api/products/{id}/image
  -> Product có imageUrl
```

### 8.4 User xem sản phẩm

```text
Android mở màn hình sản phẩm
  -> GET /api/products
  -> Backend trả danh sách product
  -> Android hiển thị
```

### 8.5 User thêm vào giỏ hàng

```text
User bấm Add To Cart
  -> POST /api/cart/items
  -> Backend lấy UserId từ token
  -> Backend kiểm tra product
  -> Backend thêm hoặc tăng quantity
  -> Android reload cart
```

### 8.6 User đặt hàng

```text
User nhập address/phone
  -> POST /api/orders
  -> Backend lấy cart
  -> Backend kiểm tra tồn kho
  -> Backend tính total
  -> Backend tạo order/order items
  -> Backend trừ tồn kho
  -> Backend xóa cart
  -> Android hiển thị đặt hàng thành công
```

## 9. Android Integration Plan

Android sẽ nối từng phần.

Thứ tự:

```text
1. Auth + Product
2. Category/Admin product nếu UI hiện tại cần
3. Cart
4. Order
5. Xóa Room khỏi luồng chính
```

Base URL khi chạy emulator:

```text
http://10.0.2.2:<port>/
```

Các thay đổi Android dự kiến:

```text
AndroidManifest.xml
  -> thêm INTERNET permission

app/build.gradle.kts
  -> thêm Retrofit/OkHttp
  -> xóa Room khi bắt đầu nối API

AppModule.kt
  -> provide Retrofit
  -> provide AuthApi/ProductApi/CartApi/OrderApi
  -> bỏ AppDatabase/DAO providers

data/remote/api/
  -> implement API interfaces thật

data/repository/
  -> chuyển từ DAO sang API

data/model/
  -> gỡ Room annotations nếu xóa Room
```

Rủi ro đã chốt:

- User chọn xóa Room khi bắt đầu nối API.
- Vì vậy checkpoint Android có thể sửa nhiều file cùng lúc.
- Trước khi làm checkpoint Android, phải liệt kê rõ toàn bộ file sẽ sửa.

## 9.1 Mapping Android Model Với Backend Entity/DTO

Android hiện có nhiều model hơn phạm vi backend giai đoạn đầu. Không ép backend entity phải giống 100% Android Room model. Cách làm đúng là:

```text
Backend entity
  -> phục vụ database và nghiệp vụ server

Backend DTO response
  -> phục vụ JSON trả cho Android

Android API model/DTO
  -> parse JSON từ backend
```

Mapping đã chốt:

| Android model | Backend entity | Backend DTO/API giai đoạn đầu | Trạng thái |
|---|---|---|---|
| `User` | `User` | `UserResponse`, `LoginResponse` | Làm ngay |
| `Category` | `Category` | `CategoryResponse` | Làm ngay |
| `Product` | `Product` | `ProductResponse` | Làm ngay |
| `CartItem` | `CartItem` + join `Product` | `CartItemResponse` có product info | Làm ngay |
| `Order` | `Order` | `OrderResponse` | Làm ngay |
| `OrderItem` | `OrderItem` | `OrderItemResponse` | Làm ngay |
| `OrderWithItems` | Không tạo entity riêng | `OrderResponse` có `items` | Làm ngay bằng DTO |
| `Address` | Không tạo entity | Checkout nhập address text trực tiếp vào order | Không làm address book |
| `Payment` | Không tạo entity riêng | `paymentMethod = "COD"` trong order | Giai đoạn đầu chỉ COD |
| `Review` | Chưa tạo entity | Chưa làm Review API | Phase 2 |
| `Notification` | Chưa tạo entity | Chưa làm Notification API | Phase 2 |

Những điểm lệch cần xử lý khi nối Android:

| Vấn đề | Android hiện tại | Backend hiện tại | Hướng xử lý |
|---|---|---|---|
| Password user | `password` trong `User` | `PasswordHash` trong entity | API không trả password/passwordHash; Android dùng request DTO riêng |
| Product price | `Double` | `decimal` | JSON number vẫn parse được; ưu tiên DTO rõ ràng |
| Category image | `imageUrl: String` | `string? ImageUrl` | Backend DTO trả `imageUrl = ""` nếu null, hoặc Android đổi nullable |
| Cart product id | `productId: String` | `ProductId: int` | Khi nối API, đổi Android API model sang `Int` |
| Cart product info | Android lưu `productName`, `price`, `imageUrl` | Backend DB không lưu trong cart | `CartItemResponse` join từ product và trả đủ info |
| Order id | `orderId` trong app model | `Id` | API trả `id`; Android DTO map sang `Order.orderId` |
| Order date | `Long` milliseconds trong app model | `DateTime` | API trả chuỗi ISO; Android DTO parse sang milliseconds |
| Order item id | `orderItemId` trong app model | `Id` | API trả `id`; Android DTO map sang `OrderItem.orderItemId` |
| Order item image | Android hiện chưa có | Backend có `ImageUrl` snapshot | Có thể thêm `imageUrl` vào Android khi làm order history |

DTO response thực tế để Android parse:

```json
{
  "id": 1,
  "userId": 2,
  "username": "user1",
  "orderDate": "2026-05-07T13:45:20.1234567Z",
  "totalPrice": 2500000,
  "status": "Pending",
  "address": "123 Nguyen Trai",
  "phoneNumber": "0909123456",
  "paymentMethod": "COD",
  "items": [
    {
      "id": 1,
      "productId": 10,
      "productName": "Sofa",
      "quantity": 1,
      "price": 2500000,
      "imageUrl": "/uploads/products/sofa.jpg",
      "lineTotal": 2500000
    }
  ]
}
```

Phạm vi phase 2:

- `Review API`: đánh giá sản phẩm.
- `Notification API`: thông báo đơn hàng.
- `Payment API`: nếu vượt ra ngoài COD.

## 10. Checkpoint Thực Thi

### Checkpoint 3.5: Chốt Mapping Android Model Với Backend DTO

Mục tiêu: đảm bảo Android model, backend entity và API DTO không bị hiểu nhầm là phải giống nhau 100%.

Hành động:

- Cập nhật tài liệu mapping Android model với backend entity/DTO.
- Ghi rõ model nào làm ngay và model nào để phase 2.
- Ghi rõ field lệch cần xử lý khi nối Retrofit.

File thay đổi:

```text
docs/plans/2026-05-07-rest-api-netcore-migration-plan.md
```

Hoàn thành khi:

- Tài liệu có bảng mapping model.
- Tài liệu có danh sách mismatch và hướng xử lý.
- Không sửa code backend/Android.

### Checkpoint 0: Kiểm Tra Môi Trường

Mục tiêu: xác nhận máy đủ điều kiện tạo backend.

Hành động:

- Kiểm tra `.NET SDK`.
- Kiểm tra MySQL localhost có thể dùng được hay chưa.
- Kiểm tra vị trí `backend/ShopApi` chưa tồn tại hoặc xử lý nếu đã tồn tại.

Lệnh dự kiến:

```powershell
dotnet --info
Get-ChildItem
```

Chưa chạy MySQL command nếu chưa được duyệt riêng.

File thay đổi:

```text
Không có
```

Hoàn thành khi:

- Biết phiên bản .NET SDK.
- Biết có thể bắt đầu tạo backend hay chưa.
- Không có file nào bị sửa.

### Checkpoint 1: Tạo Project Backend Rỗng

Mục tiêu: có ASP.NET Core Web API chạy được.

Hành động:

- Tạo project trong `backend/ShopApi`.
- Build project.
- Chạy thử backend.
- Kiểm tra Swagger.

Lệnh dự kiến:

```powershell
dotnet new webapi -n ShopApi -o backend/ShopApi
dotnet build backend/ShopApi
dotnet run --project backend/ShopApi
```

File/thư mục thay đổi:

```text
backend/ShopApi/
```

Hoàn thành khi:

- Backend build pass.
- Backend chạy được.
- Swagger hoạt động.

### Checkpoint 2: Thêm EF Core MySQL

Mục tiêu: backend kết nối được MySQL bằng EF Core.

Hành động:

- Thêm package EF Core MySQL.
- Tạo `ShopDbContext`.
- Cấu hình connection string.
- Đăng ký DbContext trong `Program.cs`.

File dự kiến:

```text
backend/ShopApi/ShopApi.csproj
backend/ShopApi/Data/ShopDbContext.cs
backend/ShopApi/Program.cs
backend/ShopApi/appsettings.json
```

Hoàn thành khi:

- Backend build pass.
- DbContext được đăng ký.
- Chưa cần migration.

### Checkpoint 3: Tạo Models Và Quan Hệ

Mục tiêu: định nghĩa database bằng Code First.

Hành động:

- Tạo models: `User`, `Category`, `Product`, `CartItem`, `Order`, `OrderItem`.
- Khai báo quan hệ trong DbContext.
- Khai báo ràng buộc cơ bản.

File dự kiến:

```text
backend/ShopApi/Models/User.cs
backend/ShopApi/Models/Category.cs
backend/ShopApi/Models/Product.cs
backend/ShopApi/Models/CartItem.cs
backend/ShopApi/Models/Order.cs
backend/ShopApi/Models/OrderItem.cs
backend/ShopApi/Data/ShopDbContext.cs
```

Hoàn thành khi:

- Backend build pass.
- Model đúng với database design.

### Checkpoint 4: Migration Và Seed Admin

Mục tiêu: tạo database MySQL bằng migration.

Hành động:

- Tạo migration đầu tiên.
- Update database.
- Seed admin mặc định.
- Không seed product/category mẫu.

Lệnh dự kiến:

```powershell
dotnet ef migrations add InitialCreate --project backend/ShopApi
dotnet ef database update --project backend/ShopApi
```

Hoàn thành khi:

- MySQL có database/bảng.
- Có admin mặc định.
- Không có product/category mẫu.

Thông tin admin mặc định:

```text
Email: admin@shop.local
Password: truyền tạm qua biến môi trường AdminSeed__Password khi chạy seed, không commit vào source
Role: ADMIN
```

### Checkpoint 5: Auth API

Mục tiêu: đăng ký, đăng nhập, JWT.

Hành động:

- Tạo DTO auth.
- Tạo `AuthController`.
- Implement register.
- Implement login.
- Implement endpoint `me`.
- Cấu hình JWT authentication.

File dự kiến:

```text
backend/ShopApi/Dtos/AuthDtos.cs
backend/ShopApi/Controllers/AuthController.cs
backend/ShopApi/Program.cs
backend/ShopApi/appsettings.json
```

Hoàn thành khi:

- Register user được.
- Login trả token.
- Token gọi được `/api/auth/me`.
- Response không trả password/password hash.

### Checkpoint 6: Category API

Mục tiêu: admin quản lý category, user xem category.

Hành động:

- Tạo DTO category.
- Tạo `CategoriesController`.
- Implement GET/POST/PUT/DELETE.
- Bảo vệ API admin bằng role.

Hoàn thành khi:

- User gọi GET được.
- Admin tạo/sửa/xóa được.
- User thường không gọi được API admin.

### Checkpoint 7: Product API Và Upload Ảnh

Mục tiêu: quản lý product và upload ảnh sản phẩm.

Hành động:

- Tạo DTO product.
- Tạo `ProductsController`.
- Implement GET/POST/PUT/DELETE.
- Implement `POST /api/products/{id}/image`.
- Cấu hình static files.
- Lưu ảnh vào `wwwroot/uploads/products`.

Hoàn thành khi:

- Admin tạo product được.
- Admin upload ảnh được.
- Product response có `imageUrl`.
- User xem danh sách product được.

### Checkpoint 8: Cart API

Mục tiêu: user quản lý cart qua backend.

Hành động:

- Tạo DTO cart.
- Tạo `CartController`.
- Implement GET cart.
- Implement add item.
- Implement update quantity.
- Implement delete item.
- Implement clear cart.

Hoàn thành khi:

- Cart gắn với user từ token.
- Không truyền `userId` từ Android.
- Product trùng thì tăng quantity.

### Checkpoint 9: Order API

Mục tiêu: user đặt hàng, admin quản lý trạng thái.

Hành động:

- Tạo DTO order.
- Tạo `OrdersController`.
- Implement create order from cart.
- Implement my orders.
- Implement admin all orders.
- Implement admin update status.
- Trừ tồn kho khi đặt hàng.

Hoàn thành khi:

- Cart rỗng không đặt được.
- Vượt tồn kho không đặt được.
- Đặt thành công tạo order/order items.
- Đặt thành công xóa cart.
- Admin cập nhật status được.

### Checkpoint 10: Backend Verification

Mục tiêu: backend API đủ ổn trước khi sửa Android.

Kiểm tra:

```text
Register
Login
Me
Create category
Create product
Upload product image
Get products
Add cart item
Update cart item
Create order
Get my orders
Admin get orders
Admin update order status
```

Hoàn thành khi:

- Backend build pass.
- Swagger test được các luồng chính.
- Không còn lỗi nghiêm trọng trước khi nối Android.

### Checkpoint 11: Nối Android Auth + Product Và Xóa Room

Mục tiêu: Android đăng nhập và xem product qua backend.

Hành động:

- Thêm Retrofit/OkHttp.
- Thêm Internet permission.
- Cấu hình base URL `10.0.2.2`.
- Implement `AuthApi`.
- Implement `ProductApi`.
- Sửa `AppModule`.
- Sửa `AuthRepository`.
- Sửa `ProductRepository`.
- Xóa Room khỏi luồng DI/repository liên quan.
- Gỡ Room annotations/dependency nếu không còn dùng.

Trước checkpoint này, Codex phải liệt kê chính xác file Android sẽ sửa.

Hoàn thành khi:

- Android build pass.
- Register/login qua backend.
- Product list lấy từ backend.

### Checkpoint 12: Nối Android Cart

Mục tiêu: Android cart dùng backend.

Hành động:

- Implement `CartApi`.
- Sửa `CartRepository`.
- Sửa `CartViewModel` nếu cần.
- Điều chỉnh UI nếu field response thay đổi.

Hoàn thành khi:

- Add to cart gọi backend.
- Cart screen hiển thị cart từ backend.
- Update/delete cart hoạt động.

### Checkpoint 13: Nối Android Order

Mục tiêu: Android checkout/order dùng backend.

Hành động:

- Implement `OrderApi`.
- Sửa `OrderRepository`.
- Sửa `OrderViewModel` nếu cần.
- Checkout gửi `address` và `phoneNumber`.

Hoàn thành khi:

- Checkout tạo order qua backend.
- Cart bị clear sau đặt hàng.
- Order history lấy từ backend.

### Checkpoint 14: Dọn Dẹp Và Kiểm Thử Cuối

Mục tiêu: hệ thống ổn định sau khi chuyển REST API.

Hành động:

- Tìm phần Room còn sót.
- Xóa code/dependency không còn dùng nếu user duyệt.
- Build backend.
- Build Android.
- Test luồng chính.

Luồng test cuối:

```text
Admin login
Admin tạo category
Admin tạo product
Admin upload ảnh product bằng backend HTTP endpoint
User register
User login
User xem product
User thêm cart
User đặt hàng COD
User xem order history
Admin cập nhật status order
```

Hoàn thành khi:

- Backend build pass.
- Android build pass.
- Luồng chính chạy end-to-end.
- Có danh sách vấn đề còn lại nếu có.

## 11. Quy Tắc Dừng

Phải dừng và hỏi user nếu gặp một trong các tình huống:

- Máy chưa có .NET SDK.
- Không kết nối được MySQL localhost.
- `backend/ShopApi` đã tồn tại và có nội dung.
- Cần sửa file Android đang có thay đổi chưa rõ nguồn gốc.
- Backend build fail do nguyên nhân ngoài checkpoint.
- Android build fail do lỗi không liên quan checkpoint.
- Cần xóa file lớn hoặc đổi kiến trúc.
- API contract cần đổi so với tài liệu này.
- Cần dùng package ngoài kế hoạch.

## 12. Tiêu Chí Hoàn Thành Toàn Bộ

Dự án được coi là hoàn thành khi:

```text
Backend ASP.NET Core chạy được.
MySQL có schema từ Code First migration.
JWT login hoạt động.
Admin quản lý category/product được.
Backend upload ảnh product hoạt động.
Android admin upload ảnh product từ app.
Android hiển thị ảnh product từ backend.
User xem product được.
User quản lý cart được.
User đặt hàng COD được.
Backend tự tính tổng tiền.
Backend trừ tồn kho.
Admin cập nhật trạng thái order được.
Android gọi backend bằng Retrofit.
Room đã được xóa hoàn toàn khỏi Android.
```

## 13. Checkpoint Tiếp Theo Chờ Duyệt

Checkpoint tiếp theo là:

```text
Checkpoint 25: Final E2E Sau Khi Đồng Bộ Model
```

Hành động dự kiến:

```text
Build lại backend/Android và kiểm thử luồng chính sau khi đã xóa address book, chuẩn hóa model và dọn placeholder.
```

File thay đổi:

```text
PROGRESS.md
```

Chỉ khi user duyệt checkpoint 25, Codex mới chạy final E2E và ghi lại kết quả.

## 14. Kế Hoạch Bổ Sung Sau Rà Soát 2026-05-10

Rà soát ngày 2026-05-10 cho thấy backend core đã đủ và code backend đang giữ mức đơn giản phù hợp. Phần cần làm tiếp chủ yếu nằm ở Android integration và trải nghiệm admin.

### Checkpoint 15: Đồng Bộ Docs Với Code Hiện Tại

Vấn đề hiện tại:

- Tài liệu cũ vẫn còn vài chỗ mô tả theo kế hoạch ban đầu, chưa khớp code đã triển khai.
- Mapping order trong docs chưa phản ánh đúng API thật.
- Chưa ghi rõ phần nào backend đã xong, phần nào Android còn thiếu.

Mục tiêu thực hiện:

- Tài liệu phản ánh đúng trạng thái code hiện tại.
- Người review nhìn vào docs biết chính xác checkpoint nào đã xong và checkpoint nào còn phải làm.

Trạng thái: đã thực hiện và đã push commit `9a35762`.

File đã thay đổi:

```text
docs/plans/2026-05-07-rest-api-netcore-migration-plan.md
PROGRESS.md
```

Hành động chi tiết:

- Cập nhật trạng thái Room: đã xóa hoàn toàn khỏi Android.
- Cập nhật mapping order: API trả `id`, `orderDate` dạng chuỗi ISO; Android DTO tự map sang model hiện có.
- Ghi rõ backend upload ảnh đã có, Android upload ảnh từ app chưa hoàn thiện.
- Ghi rõ checkpoint tiếp theo là hiển thị ảnh product trên Android.
- Ghi lại các checkpoint bổ sung từ 16 đến 20.
- Cập nhật `PROGRESS.md` để có log Checkpoint 15.

Không thay đổi:

- Không sửa code Android.
- Không sửa code backend.
- Không chạy build vì checkpoint này chỉ là tài liệu.

Hoàn thành khi:

- Docs không còn mô tả sai contract hiện tại.
- `PROGRESS.md` có dòng Checkpoint 15.
- Commit docs được push lên GitHub.

### Checkpoint 16: Hiển Thị Ảnh Product Trên Android

Vấn đề hiện tại:

- Backend đã trả `imageUrl` trong product response.
- Android đã map `imageUrl` vào model `Product`.
- UI product list và product detail vẫn đang dùng ảnh placeholder, chưa dùng ảnh backend trả về.

Mục tiêu thực hiện:

- Product list hiển thị ảnh từ `imageUrl`.
- Product detail hiển thị ảnh lớn từ `imageUrl`.
- Nếu product chưa có ảnh hoặc ảnh lỗi, UI vẫn có fallback đơn giản.

File dự kiến thay đổi:

```text
app/build.gradle.kts
app/src/main/java/com/example/shop/utils/Constants.kt
app/src/main/java/com/example/shop/ui/components/ProductItem.kt
app/src/main/java/com/example/shop/ui/home/HomeScreen.kt
app/src/main/java/com/example/shop/ui/product/ProductScreen.kt
app/src/main/java/com/example/shop/ui/product/ProductDetailScreen.kt
PROGRESS.md
```

Hành động chi tiết:

- Thêm thư viện ảnh đơn giản cho Compose.
- Chuẩn hóa URL ảnh local: `/uploads/...` thành `http://10.0.2.2:5053/uploads/...`.
- Sửa `ProductItem`:
  - Nhận thêm tham số `imageUrl`.
  - Nếu `imageUrl` rỗng thì dùng ảnh placeholder hiện tại.
  - Nếu `imageUrl` có giá trị thì load ảnh bằng URL.
- Sửa `HomeScreen`:
  - Truyền `product.imageUrl` vào `ProductItem`.
- Sửa `ProductScreen`:
  - Truyền `product.imageUrl` vào `ProductItem`.
- Sửa `ProductDetailScreen`:
  - Hiển thị ảnh lớn từ `item.imageUrl`.
  - Nếu chưa có ảnh thì hiển thị text fallback đơn giản.
- Cập nhật `PROGRESS.md` sau khi build pass.

Không làm trong checkpoint này:

- Không làm chọn ảnh từ điện thoại.
- Không gọi API upload ảnh.
- Không sửa backend.
- Không đổi DTO/API contract.

Lệnh kiểm tra:

```powershell
./gradlew.bat :app:assembleDebug --no-daemon --console=plain
```

Hoàn thành khi:

- Android build pass.
- Product có `imageUrl` hiển thị ảnh trong danh sách và chi tiết.
- Commit được push sau khi user review.

### Checkpoint 17: Làm Admin Product Gọn Và Đúng Logic

Vấn đề hiện tại:

- `ProductRepository` đang dùng `runCatching` nhưng không trả lỗi rõ cho UI.
- Màn add/update product có thể quay lại ngay cả khi API fail.
- Khi delete product bị backend từ chối, ví dụ product đã nằm trong cart/order, UI chưa báo rõ cho admin.

Mục tiêu thực hiện:

- Admin biết thao tác tạo/sửa/xóa product thành công hay thất bại.
- Danh sách product được refresh sau thao tác thành công.
- Code vẫn giữ đơn giản, không thêm luồng state phức tạp.

File dự kiến thay đổi:

```text
app/src/main/java/com/example/shop/data/repository/ProductRepository.kt
app/src/main/java/com/example/shop/admin/viewmodel/AdminProductViewModel.kt
app/src/main/java/com/example/shop/admin/ui/product/AddProductScreen.kt
app/src/main/java/com/example/shop/admin/ui/product/UpdateProductScreen.kt
app/src/main/java/com/example/shop/admin/ui/product/ManageProductScreen.kt
PROGRESS.md
```

Hành động chi tiết:

- Cho `ProductRepository` trả kết quả thành công/thất bại.
- Refresh danh sách product sau create/update/delete thành công.
- Sửa `insertProduct`:
  - Trả `Boolean`.
  - `true` khi backend tạo thành công.
  - `false` khi thiếu token hoặc API lỗi.
- Sửa `updateProduct`:
  - Trả `Boolean`.
  - Refresh product list sau khi update thành công.
- Sửa `deleteProduct`:
  - Trả `Boolean`.
  - Refresh product list sau khi delete thành công.
- Sửa `AdminProductViewModel`:
  - Có state loading/error tối giản nếu cần.
  - UI nhận được kết quả thành công/thất bại.
- Sửa `AddProductScreen`:
  - Không tự `onNavigateBack()` ngay sau click.
  - Chỉ quay về khi repository báo thành công.
  - Nếu fail thì hiện lỗi đơn giản.
- Sửa `UpdateProductScreen`:
  - Chỉ quay về khi update thành công.
  - Nếu fail thì hiện lỗi đơn giản.
- Sửa `ManageProductScreen`:
  - Khi delete fail thì không xóa giả trên UI.
  - Hiển thị lỗi đơn giản nếu backend từ chối, ví dụ product đã có trong order/cart.

Không làm trong checkpoint này:

- Không đổi backend response.
- Không thêm framework state management mới.
- Không viết UI phức tạp.

Lệnh kiểm tra:

```powershell
./gradlew.bat :app:assembleDebug --no-daemon --console=plain
```

Hoàn thành khi:

- Android build pass.
- Admin biết thao tác product thành công hay thất bại.
- Commit được push sau khi user review.

### Checkpoint 18: Upload Ảnh Product Từ Android

Vấn đề hiện tại:

- Backend đã có endpoint upload ảnh product.
- Android admin hiện chỉ nhập link ảnh bằng text.
- Chưa có luồng chọn file ảnh từ điện thoại và upload lên backend.

Mục tiêu thực hiện:

- Admin có thể chọn ảnh trong app khi thêm/sửa product.
- App upload ảnh lên backend endpoint hiện có.
- Sau upload, product dùng `imageUrl` backend trả về để hiển thị ảnh.

File dự kiến thay đổi:

```text
app/src/main/AndroidManifest.xml
app/src/main/java/com/example/shop/data/remote/api/ProductApi.kt
app/src/main/java/com/example/shop/data/repository/ProductRepository.kt
app/src/main/java/com/example/shop/admin/viewmodel/AdminProductViewModel.kt
app/src/main/java/com/example/shop/admin/ui/product/AddProductScreen.kt
app/src/main/java/com/example/shop/admin/ui/product/UpdateProductScreen.kt
PROGRESS.md
```

Ghi chú file:

- `AndroidManifest.xml` chỉ sửa nếu cần permission theo cách chọn ảnh được dùng.
- Nếu dùng Photo Picker hiện đại và không cần permission, file này có thể không thay đổi.

Hành động chi tiết:

- Thêm multipart endpoint vào `ProductApi`.
- Thêm hàm upload ảnh trong `ProductRepository`.
- Thêm chọn ảnh trong `AddProductScreen`.
- Thêm chọn ảnh trong `UpdateProductScreen`.
- Flow add product:
  - User nhập thông tin product.
  - Nếu không chọn ảnh file, gửi `imageUrl` text như hiện tại.
  - Nếu có chọn ảnh file, tạo product trước.
  - Sau khi tạo product thành công, gọi upload ảnh cho product vừa tạo.
- Flow update product:
  - User sửa thông tin product.
  - Nếu không chọn ảnh file mới, update metadata như hiện tại.
  - Nếu có chọn ảnh file mới, update product trước.
  - Sau khi update thành công, gọi upload ảnh cho product đó.
- Sau upload thành công, refresh danh sách product.
- Không làm crop, preview nâng cao, cloud storage.

Không làm trong checkpoint này:

- Không thay backend upload endpoint nếu endpoint hiện tại đủ dùng.
- Không làm nhiều ảnh cho một product.
- Không lưu ảnh local trong app.

Lệnh kiểm tra:

```powershell
./gradlew.bat :app:assembleDebug --no-daemon --console=plain
```

Kiểm tra thủ công:

```text
1. Chạy backend local.
2. Login admin trên app.
3. Tạo product và chọn ảnh.
4. Kiểm tra ảnh xuất hiện trong backend/ShopApi/wwwroot/uploads/products.
5. Kiểm tra product list/detail hiển thị ảnh.
```

Hoàn thành khi:

- Android build pass.
- Ảnh được lưu trong `backend/ShopApi/wwwroot/uploads/products`.
- Product sau upload hiển thị ảnh từ backend.
- Commit được push sau khi user review.

### Checkpoint 19: Rà Soát Backend Tối Giản

Vấn đề hiện tại:

- Backend core đã chạy được nhưng cần rà soát lần cuối sau khi Android đã nối API.
- Cần chắc controller còn gọn, validation đủ dùng, phân quyền đúng.
- Cần xác nhận không có lỗi build hoặc package vulnerable.

Mục tiêu thực hiện:

- Backend đủ đúng cho các luồng Android đang dùng.
- Code backend dễ đọc, không có đoạn thừa rõ ràng.
- Build backend pass và package check không báo vulnerable.

File dự kiến thay đổi:

```text
backend/ShopApi/Controllers/AuthController.cs
backend/ShopApi/Controllers/CategoriesController.cs
backend/ShopApi/Controllers/ProductsController.cs
backend/ShopApi/Controllers/CartController.cs
backend/ShopApi/Controllers/OrdersController.cs
backend/ShopApi/Dtos/*.cs
PROGRESS.md
```

Ghi chú file:

- Đây là danh sách có thể đụng tới khi rà soát.
- Nếu không cần sửa backend, chỉ cập nhật `PROGRESS.md`.

Hành động chi tiết:

- Rà soát controller Auth/Category/Product/Cart/Order.
- Chỉ dọn điểm nhỏ nếu giúp code dễ hiểu hơn.
- Kiểm tra validation tối thiểu:
  - Auth: username/email/password.
  - Category: name, imageUrl length.
  - Product: name, description, price, quantity, category tồn tại.
  - Cart: quantity, product tồn tại, user lấy từ JWT.
  - Order: address, phone, cart rỗng, tồn kho, status hợp lệ.
- Kiểm tra response không trả password/passwordHash.
- Kiểm tra admin-only endpoint có `[Authorize(Roles = "ADMIN")]`.
- Kiểm tra code không có secret hardcode ngoài connection string dev local.

Lệnh kiểm tra:

```powershell
dotnet build backend/ShopApi --nologo
dotnet list backend/ShopApi package --vulnerable --include-transitive
```

Hoàn thành khi:

- `dotnet build backend/ShopApi --nologo` pass.
- `dotnet list backend/ShopApi package --vulnerable --include-transitive` không báo vulnerable package.
- Commit được push sau khi user review nếu có thay đổi.

### Checkpoint 20: Final End-To-End

Vấn đề hiện tại:

- Các phần đã được làm theo checkpoint riêng lẻ.
- Cần chạy lại luồng tổng thể để chắc backend, Android, emulator và dữ liệu local phối hợp đúng.
- Cần ghi lại kết quả kiểm thử cuối vào progress.

Mục tiêu thực hiện:

- Xác nhận toàn bộ flow chính chạy được từ app Android tới backend.
- Ghi lại bằng chứng build/test cuối.
- Chỉ sửa lỗi nếu E2E phát hiện lỗi thật và đã báo user trước.

File dự kiến thay đổi:

```text
PROGRESS.md
```

Ghi chú file:

- Nếu E2E phát hiện lỗi, file thay đổi sẽ phụ thuộc lỗi thực tế và phải báo user trước khi sửa.

Hành động chi tiết:

- Register/login.
- Xem category/product.
- Admin thêm product có ảnh.
- User add cart.
- Checkout tạo order COD.
- Admin đổi status order.
- Ghi lại kết quả test cuối vào `PROGRESS.md`.

Lệnh kiểm tra:

```powershell
dotnet build backend/ShopApi --nologo
./gradlew.bat :app:assembleDebug --no-daemon --console=plain
```

Kiểm tra thủ công trên emulator:

```text
1. Chạy backend ở http://127.0.0.1:5053.
2. Mở app trên emulator.
3. Login admin.
4. Tạo category nếu chưa có.
5. Tạo product có ảnh.
6. Logout hoặc login user.
7. Mở product list/detail, kiểm tra ảnh hiển thị.
8. Add product vào cart.
9. Checkout COD.
10. Mở order history.
11. Login admin, đổi status order.
```

Không làm trong checkpoint này:

- Không thêm feature mới.
- Không đổi UI lớn.
- Không đổi database schema nếu không có lỗi bắt buộc.

Hoàn thành khi:

- Backend build pass.
- Android build pass.
- App chạy được trên emulator với backend local.
- Commit final được push sau khi user review.

## 15. Rà Soát Sau Checkpoint 20: Model Android Nhiều Hơn Backend

Ngày rà soát: 2026-05-10
Quyết định cập nhật: chọn cách đơn giản nhất, không làm address book riêng.

### 15.1 Kết Luận Review

Backend hiện đã có đủ nhóm core cho luồng bán hàng chính:

```text
User
Category
Product
CartItem
Order
OrderItem
```

Android từng có thêm một số model ngoài core:

```text
Address
Payment
Review
Notification
OrderWithItems
```

Kết luận sau khi chốt lại với user:

- `Address` không cần backend riêng. Checkout đã cho user nhập địa chỉ trực tiếp và backend lưu vào `Order.Address`, `Order.PhoneNumber`.
- `OrderWithItems` không cần backend entity riêng vì chỉ là model ghép bên Android để hiển thị order kèm items.
- `Payment` chưa cần backend riêng vì ứng dụng đang dùng COD qua `Order.PaymentMethod`.
- `Review` chưa cần backend nếu chưa triển khai màn đánh giá sản phẩm thật.
- `Notification` chưa cần backend nếu màn thông báo vẫn chỉ là placeholder.

### 15.2 Bảng Đối Chiếu Trạng Thái Model

| Android model | Backend hiện tại | Đánh giá | Hành động |
|---|---|---|---|
| `User` | Có `User`, `AuthDtos`, `AuthController` | Đủ cho login/register/me | Giữ |
| `Category` | Có `Category`, `CategoryDtos`, `CategoriesController` | Đủ cho admin/user | Giữ |
| `Product` | Có `Product`, `ProductDtos`, `ProductsController` | Đủ, đã có upload ảnh | Giữ |
| `CartItem` | Có `CartItem`, `CartDtos`, `CartController` | Chạy được nhưng Android `productId` đang là `String` | Chuẩn hóa Android `productId` thành `Int` |
| `Order` | Có `Order`, `OrderDtos`, `OrdersController` | Đủ cho checkout/history/admin status | Giữ |
| `OrderItem` | Có `OrderItem`, `OrderItemResponse` | Backend có `imageUrl`, Android model chưa giữ | Thêm `imageUrl` nếu UI order/cart cần hiển thị ảnh |
| `OrderWithItems` | Không có entity riêng | Đúng, đây là model ghép bên Android | Giữ |
| `Address` | Không tạo entity riêng | Checkout nhập trực tiếp address/phone vào order | Xóa/ẩn màn Profile > Địa chỉ |
| `Payment` | Chỉ có `PaymentMethod` trong `Order` | Đủ nếu COD only | Dọn placeholder hoặc để phase sau |
| `Review` | Chưa có | Chưa cần nếu chưa làm đánh giá thật | Dọn placeholder hoặc để phase sau |
| `Notification` | Chưa có | Chưa cần nếu chưa làm thông báo thật | Dọn placeholder hoặc để phase sau |

### 15.3 Vấn Đề Đang Tồn Tại

1. App đang có màn `Profile > Địa chỉ`, nhưng tính năng này không cần nếu checkout đã nhập địa chỉ trực tiếp.
2. `AddressRepository` chỉ lưu RAM, tắt app là mất. Vì đã chọn cách đơn giản, không nâng cấp nó thành API mà xóa khỏi flow.
3. `CartItem.productId` bên Android là `String`, trong khi backend và API dùng `int`. Code phải ép kiểu qua lại.
4. `OrderItemResponse` có `imageUrl` nhưng Android `OrderItem` chưa có field này, làm lịch sử đơn hàng khó hiển thị ảnh sản phẩm.
5. Các file `Payment`, `Review`, `Notification` hiện phần lớn là placeholder, dễ làm người đọc tưởng backend còn thiếu nhiều tính năng.
6. `data/remote/firebase` còn stub `FirebaseAuthService` và `FirestoreService`, không còn phù hợp với quyết định dùng ASP.NET Core REST API.
7. `ManageUserScreen` và `AdminUserViewModel` đang trống; nếu admin dashboard không dùng thì nên dọn hoặc ghi rõ chưa triển khai.

### 15.4 Nguyên Tắc Hoàn Thiện Từ Giai Đoạn Này

- Ưu tiên sửa phần đang ảnh hưởng luồng thật trước.
- Chỉ tạo backend cho model đang có UI/luồng nghiệp vụ thật.
- Nếu có hai cách, chọn cách ít màn hình, ít API, dễ giải thích hơn.
- Checkout là nơi nhập địa chỉ giao hàng; không làm address book riêng.
- Mỗi phase chỉ xử lý một nhóm vấn đề để dễ review.
- Mỗi phase xong phải build, cập nhật `PROGRESS.md`, commit và push.

## 16. Phase Hoàn Thiện Sau Checkpoint 20

### Checkpoint 21: Rà Soát Và Khóa Contract Model Android - Backend

Vấn đề hiện tại:

- Android có nhiều model hơn backend nên dễ hiểu nhầm là backend thiếu toàn bộ.
- Chưa có bảng review chính thức sau checkpoint 20 để chốt model nào giữ, model nào thêm API, model nào dọn.
- User đã chốt không làm address book, chỉ nhập địa chỉ ở checkout.

Mục tiêu thực hiện:

- Tạo một bản contract rõ ràng giữa Android và backend.
- Chốt không làm Address API.
- Chốt thứ tự phase hoàn thiện tiếp theo.

File thay đổi:

```text
docs/plans/2026-05-07-rest-api-netcore-migration-plan.md
PROGRESS.md
```

Hành động cụ thể:

- Ghi bảng đối chiếu Android model với backend entity/DTO/API.
- Ghi rõ `Address` không cần backend riêng vì checkout lưu trực tiếp vào order.
- Ghi rõ `Payment`, `Review`, `Notification` đang là placeholder nếu chưa làm feature.
- Ghi rõ `CartItem.productId` cần chuẩn hóa từ `String` sang `Int`.
- Ghi rõ `OrderItem.imageUrl` cần bổ sung nếu muốn lịch sử đơn hàng hiển thị ảnh.
- Ghi phase 22-25 để user review từng bước.

Tiêu chí review:

- Người đọc nhìn docs biết ngay backend thiếu gì thật.
- Không còn cảm giác “Android nhiều model nên backend phải tạo hết”.
- Phase tiếp theo đủ cụ thể để bắt đầu code từng bước.

Lệnh kiểm tra:

```powershell
git diff -- docs/plans/2026-05-07-rest-api-netcore-migration-plan.md PROGRESS.md
```

Hoàn thành khi:

- Docs được cập nhật rõ ràng.
- `PROGRESS.md` có log checkpoint 21.
- Commit docs được push sau khi user review.

### Checkpoint 22: Xóa Luồng Address Book Riêng Khỏi Android

Vấn đề hiện tại:

- `ProfileScreen` có nút “Địa chỉ của tôi”.
- Navigation có route `ADDRESS_LIST` và `ADD_ADDRESS`.
- Android có `Address`, `AddressRepository`, `AddressViewModel`, `AddressScreen`, `AddAddressScreen` nhưng luồng này chỉ lưu RAM và không cần theo cách đơn giản đã chốt.

Mục tiêu thực hiện:

- Không còn màn quản lý địa chỉ riêng.
- Checkout vẫn là nơi nhập địa chỉ giao hàng.
- Không thêm backend API mới.
- Code Android gọn hơn, không giữ repository lưu RAM gây hiểu nhầm.

File dự kiến thay đổi:

```text
app/src/main/java/com/example/shop/navigation/Routes.kt
app/src/main/java/com/example/shop/navigation/MainNavGraph.kt
app/src/main/java/com/example/shop/ui/profile/ProfileScreen.kt
app/src/main/java/com/example/shop/data/model/Address.kt
app/src/main/java/com/example/shop/data/repository/AddressRepository.kt
app/src/main/java/com/example/shop/viewmodel/AddressViewModel.kt
app/src/main/java/com/example/shop/ui/address/AddressScreen.kt
app/src/main/java/com/example/shop/ui/address/AddAddressScreen.kt
PROGRESS.md
```

Hành động cụ thể:

- Xóa route `ADDRESS_LIST` và `ADD_ADDRESS`.
- Xóa import và composable route của `AddressScreen`, `AddAddressScreen` khỏi `MainNavGraph`.
- Xóa tham số `onNavigateToAddresses` khỏi `ProfileScreen`.
- Xóa nút “Địa chỉ của tôi” khỏi profile.
- Xóa model/repository/viewmodel/screen Address không còn dùng.
- Build Android để chắc không còn import lỗi.

Lệnh kiểm tra:

```powershell
rg "Address|ADDRESS|ADD_ADDRESS|ADDRESS_LIST|onNavigateToAddresses" app/src/main/java/com/example/shop
./gradlew.bat :app:assembleDebug --no-daemon --console=plain --stacktrace
```

Kiểm tra thủ công:

```text
1. Login user.
2. Mở Profile.
3. Không còn nút “Địa chỉ của tôi”.
4. Mở Checkout.
5. Vẫn nhập địa chỉ và số điện thoại trực tiếp để đặt hàng.
```

Hoàn thành khi:

- Android build pass.
- Không còn reference Address trong source chính.
- Checkout/order không bị ảnh hưởng.
- Commit được push sau khi user review.

### Checkpoint 23: Chuẩn Hóa Model Android Đang Lệch Contract

Vấn đề hiện tại:

- `CartItem.productId` bên Android là `String`, trong khi backend/API là `Int`.
- `OrderItem` Android chưa có `imageUrl`, trong khi backend trả `imageUrl`.
- Một số comment cũ còn ghi `Database` sau khi đã bỏ Room.

Mục tiêu thực hiện:

- Android model khớp REST API hơn.
- Giảm ép kiểu và comment gây hiểu nhầm.
- Không đổi backend nếu API hiện tại đã đúng.

File dự kiến thay đổi:

```text
app/src/main/java/com/example/shop/data/model/CartItem.kt
app/src/main/java/com/example/shop/data/model/OrderItem.kt
app/src/main/java/com/example/shop/data/remote/dto/CartDtos.kt
app/src/main/java/com/example/shop/data/remote/dto/OrderDtos.kt
app/src/main/java/com/example/shop/data/repository/CartRepository.kt
app/src/main/java/com/example/shop/viewmodel/CartViewModel.kt
app/src/main/java/com/example/shop/ui/cart/CartScreen.kt
app/src/main/java/com/example/shop/ui/order/OrderScreen.kt
PROGRESS.md
```

Hành động cụ thể:

- Đổi `CartItem.productId` từ `String` sang `Int`.
- Xóa các đoạn `toString()` và `toIntOrNull()` không cần thiết.
- Thêm `imageUrl: String = ""` vào `OrderItem`.
- Map `OrderItemResponse.imageUrl` sang `OrderItem.imageUrl`.
- Nếu màn cart/order có chỗ ảnh placeholder, cân nhắc dùng `imageUrl` đã có.
- Dọn comment cũ nói về `Database` nếu thực tế đang dùng API.

Lệnh kiểm tra:

```powershell
./gradlew.bat :app:assembleDebug --no-daemon --console=plain --stacktrace
```

Hoàn thành khi:

- Android build pass.
- Không còn ép kiểu `productId` String/Int trong cart.
- Order item có thể giữ ảnh snapshot từ backend.
- Commit được push sau khi user review.

### Checkpoint 24: Dọn Placeholder Và Làm Admin User Tối Giản

Vấn đề hiện tại:

- `Payment`, `Review`, `Notification` có model/viewmodel/repository hoặc screen nhưng chưa nối backend và UI còn placeholder.
- `data/remote/firebase` còn stub dù dự án đã chốt REST API .NET.
- `ManageUserScreen` và `AdminUserViewModel` đang trống, nhưng app admin cần tối thiểu có chức năng xem user và xóa user.

Mục tiêu thực hiện:

- Làm codebase dễ hiểu hơn.
- Người đọc không nhầm rằng các tính năng này đã hoàn thiện.
- Giữ phần admin user nhưng triển khai thật ở mức đơn giản nhất: xem danh sách user và xóa user thường.
- Không làm thêm sửa user, đổi role, khóa tài khoản, tìm kiếm, phân trang nếu chưa cần.

File dự kiến rà soát/thay đổi:

```text
backend/ShopApi/Controllers/UsersController.cs
app/src/main/java/com/example/shop/data/remote/api/UserApi.kt
app/src/main/java/com/example/shop/data/repository/UserRepository.kt
app/src/main/java/com/example/shop/data/model/Payment.kt
app/src/main/java/com/example/shop/data/model/Review.kt
app/src/main/java/com/example/shop/data/model/Notification.kt
app/src/main/java/com/example/shop/data/repository/PaymentRepository.kt
app/src/main/java/com/example/shop/data/repository/ReviewRepository.kt
app/src/main/java/com/example/shop/data/repository/NotificationRepository.kt
app/src/main/java/com/example/shop/viewmodel/PaymentViewModel.kt
app/src/main/java/com/example/shop/viewmodel/ReviewViewModel.kt
app/src/main/java/com/example/shop/viewmodel/NotificationViewModel.kt
app/src/main/java/com/example/shop/ui/payment/PaymentScreen.kt
app/src/main/java/com/example/shop/ui/payment/AddPaymentScreen.kt
app/src/main/java/com/example/shop/ui/review/ReviewScreen.kt
app/src/main/java/com/example/shop/ui/review/MyReviewScreen.kt
app/src/main/java/com/example/shop/ui/notification/NotificationScreen.kt
app/src/main/java/com/example/shop/data/remote/firebase/FirebaseAuthService.kt
app/src/main/java/com/example/shop/data/remote/firebase/FirestoreService.kt
app/src/main/java/com/example/shop/admin/ui/user/ManageUserScreen.kt
app/src/main/java/com/example/shop/admin/viewmodel/AdminUserViewModel.kt
app/src/main/java/com/example/shop/admin/ui/dashboard/DashboardScreen.kt
app/src/main/java/com/example/shop/navigation/MainNavGraph.kt
app/src/main/java/com/example/shop/navigation/Routes.kt
app/src/main/java/com/example/shop/di/AppModule.kt
PROGRESS.md
```

Hành động cụ thể:

- Kiểm tra route nào thật sự dùng payment/review/notification/user admin.
- Xóa `Payment`, `Review`, `Notification` nếu chỉ là placeholder và chưa có luồng thật.
- Xóa Firebase stub nếu không còn import/use ở đâu.
- Backend thêm `UsersController`.
- `GET /api/users`: chỉ admin gọi được, trả danh sách `UserResponse`.
- `DELETE /api/users/{id}`: chỉ admin gọi được, chỉ xóa user thường.
- Không cho admin xóa chính mình.
- Không cho xóa tài khoản `ADMIN`.
- Nếu user đã có đơn hàng, xóa luôn `OrderItems`, `Orders`, `CartItems`, rồi xóa user trong một transaction.
- Android thêm `UserApi`, `UserRepository`, `AdminUserViewModel`, `ManageUserScreen`.
- Dashboard admin thêm thẻ `Người dùng`.
- Navigation thêm route `ADMIN_MANAGE_USER`.
- Build lại backend và Android sau khi làm.

Quy tắc review:

- Không xóa màn đang nằm trong flow chính như login, home, product, cart, checkout, order, admin product/category/order.
- Chỉ xóa placeholder không có logic thật.
- Admin user giữ lại vì có yêu cầu thật: xem danh sách và xóa user.
- Code phải gọn: không thêm service layer backend, không thêm chức năng ngoài phạm vi.

Lệnh kiểm tra:

```powershell
dotnet build backend/ShopApi --nologo
rg "Payment|Review|Notification|Firebase|ManageUser|UserApi" app/src/main/java/com/example/shop backend/ShopApi
./gradlew.bat :app:assembleDebug --no-daemon --console=plain --stacktrace
```

Hoàn thành khi:

- Backend có endpoint admin user tối giản.
- Android admin mở được màn quản lý user.
- Admin xem được danh sách user.
- Admin xóa được user thường, kể cả user đã có đơn hàng.
- Admin không xóa được tài khoản admin hoặc chính mình.
- Android build pass.
- Backend build pass.
- Không còn Firebase stub.
- Placeholder không gây hiểu nhầm backend thiếu tính năng đã cam kết.
- Commit được push sau khi user review.

### Checkpoint 25: Final E2E Sau Khi Đồng Bộ Model

Vấn đề hiện tại:

- Checkpoint 20 đã E2E core flow, nhưng sau khi xóa address book và dọn model lệch cần chạy lại toàn bộ luồng.

Mục tiêu thực hiện:

- Xác nhận app vẫn chạy đúng sau khi đơn giản hóa address.
- Đảm bảo code vẫn gọn, dễ chạy local.
- Ghi lại bằng chứng build/test cuối.

File dự kiến thay đổi:

```text
PROGRESS.md
```

Hành động cụ thể:

- Build backend.
- Build Android.
- Chạy backend local.
- Test API auth/product/category/cart/order.
- Test emulator các luồng chính.
- Ghi kết quả vào `PROGRESS.md`.

Lệnh kiểm tra:

```powershell
dotnet build backend/ShopApi --nologo
dotnet list backend/ShopApi package --vulnerable --include-transitive
./gradlew.bat :app:assembleDebug --no-daemon --console=plain --stacktrace
```

Luồng test thủ công:

```text
1. Login admin.
2. Tạo category.
3. Tạo product có ảnh.
4. Login user.
5. Mở product list/detail.
6. Add to cart.
7. Checkout nhập địa chỉ và số điện thoại trực tiếp.
8. Đặt hàng COD.
9. Mở order history.
10. Login admin, đổi status order.
```

Hoàn thành khi:

- Backend build pass.
- Android build pass.
- Product/cart/order vẫn hoạt động sau khi xóa address book.
- Commit final được push sau khi user review.


## 17. Phase Mở Rộng Sau Hoàn Thiện Core 2026-05-19: Đăng Nhập Bằng Google

Ngày bổ sung: 2026-05-19. Phase này chỉ bắt đầu sau khi Checkpoint 25 đã hoàn tất và user duyệt từng checkpoint con. Mục đích là cho phép người dùng đăng nhập bằng tài khoản Google bên cạnh luồng email/password hiện có, không thay đổi cơ chế JWT đã chốt từ Checkpoint 5.

### 17.1 Lý Do Mở Rộng

- App hiện chỉ cho đăng ký/đăng nhập bằng email + password thông qua `AuthController`.
- Người dùng quen với "Sign in with Google" trên các app shop khác và mong có lựa chọn này để bớt phải tạo tài khoản mới.
- Backend đã có `CreateToken` tái sử dụng được, nên việc thêm Google sign-in chỉ cần thêm 1 endpoint mới, không cần thay JWT hiện có.

### 17.2 Nguyên Tắc Đơn Giản Hóa

- Giữ nguyên `Register`, `Login`, `Me`, `CreateToken` trong `AuthController` để không phá luồng cũ.
- Thêm cột `GoogleSub` vào `User` ở dạng nullable, không bỏ `PasswordHash` required.
- Chỉ làm Google ở phase này; Facebook/Zalo/Apple để phase sau nếu cần.
- Không thay JwtBearer config trong `Program.cs`; không động đến claim format đang dùng.
- Mỗi checkpoint là một commit riêng, có thể rollback độc lập.
- Tuân thủ quy trình section 3: Codex nêu trước, user duyệt, mới thực hiện.

### 17.3 Quyết Định Đã Chốt Cho Phase Này

| Quyết định | Kết luận |
|---|---|
| Provider | Chỉ Google (chưa làm Facebook/Zalo/Apple) |
| Cơ chế | App lấy id_token từ Google rồi đổi sang JWT của ShopApi |
| JWT của ShopApi | Giữ nguyên cấu hình HMAC-SHA256, issuer/audience hiện có |
| Schema User | Thêm `GoogleSub` nullable + index unique. `PasswordHash` giữ required |
| User chỉ Google | `PasswordHash = string.Empty` để pass required check, không lưu plain password |
| Login | User cũ vẫn đăng nhập bằng email/password được; user mới có thể chọn Google |
| Liên kết tài khoản | Nếu user Google trùng `Email` với account đã có thì gán `GoogleSub` vào account đó |
| Android SDK | Dùng `androidx.credentials` + `googleid` (không dùng `GoogleSignIn` deprecated) |
| Secret | `Google:ClientId` đặt vào appsettings dev; production dùng env var `Google__ClientId` |
| Tốn phí | 0đ — Google Cloud OAuth Client ID free, verify id_token free quota |

### Checkpoint 26: Chuẩn Bị Google Cloud OAuth Client ID

Vấn đề hiện tại:

- Backend chưa biết `Client ID` nào là hợp lệ để verify id_token.
- App Android chưa có Web Client ID để xin id_token đúng audience.
- Google phải biết keystore nào của app được phép xin `id_token`, vì Android OAuth Client ID kiểm tra theo `package name` và SHA-1 của app signing key.

Mục tiêu thực hiện:

- Có một Google Cloud Project sẵn sàng cấp OAuth.
- Có Web Client ID để đưa vào backend và Android.
- Có Android Client ID đăng ký theo `package = com.example.shop` và SHA-1 của keystore debug dùng chung trong repo.
- Tài liệu hóa nơi đặt Client ID + nơi đặt secret để các checkpoint sau dùng lại.

File dự kiến thay đổi:

```text
app/build.gradle.kts
app/debug.keystore
PROGRESS.md
docs/google-oauth-setup.md
```

Ghi chú file:

- Không sửa code backend.
- Android chỉ cấu hình signing debug dùng chung, không sửa logic màn hình/API.
- Phần OAuth Client ID làm trên Google Cloud Console.

Hành động chi tiết:

- Tạo Google Cloud Project tên `shop-android` ở https://console.cloud.google.com.
- Cấu hình OAuth consent screen ở chế độ External với scope `email`, `profile`, `openid`.
- Tạo OAuth Client ID loại Web application, đặt tên `Shop Backend`. Lưu lại `Client ID` để dùng cho backend và Android.
- Tạo `app/debug.keystore` dùng chung cho nhóm.
- Cấu hình `app/build.gradle.kts` để debug build dùng `app/debug.keystore`.
- Lấy SHA-1 của `app/debug.keystore` bằng `keytool`.
- Tạo OAuth Client ID loại Android, đặt tên `Shop Android Shared Debug`, gắn package `com.example.shop` và SHA-1 vừa lấy.
- Ghi lại Web Client ID + Android Client ID vào nơi an toàn (vd: file `.env.local` không commit, hoặc note tạm).
- Cập nhật `PROGRESS.md` ghi nhận checkpoint chuẩn bị done.

Không làm trong checkpoint này:

- Không sửa code backend.
- Không sửa logic Android login.
- Không tạo Android Client ID release vì chưa có keystore production.
- Không bật API tính phí (Maps, Vision, ...).

Lệnh kiểm tra:

```powershell
keytool -list -v -keystore app/debug.keystore -alias androiddebugkey -storepass android -keypass android
./gradlew.bat :app:assembleDebug --no-daemon --console=plain --stacktrace
```

Hoàn thành khi:

- Có Web Client ID dạng `123456789-abc.apps.googleusercontent.com`.
- Có Android Client ID đã đăng ký package + SHA-1 debug dùng chung.
- `PROGRESS.md` có dòng Checkpoint 26.
- Commit có `app/debug.keystore`, signing config, docs và progress liên quan.

Kết quả thực tế ngày 2026-05-21:

- Google Cloud Project đã tạo: `shop-android` (`zippy-nexus-497009-c3`).
- OAuth consent đã cấu hình External, trạng thái Testing, test users `locv2659@gmail.com` và `khoaduy2608@gmail.com`.
- Scopes non-sensitive đã lưu: `openid`, `https://www.googleapis.com/auth/userinfo.email`, `https://www.googleapis.com/auth/userinfo.profile`.
- Web OAuth client `Shop Backend`: `826757086511-es3htk7un7lq7lvlpmkqppmp3h7nnjd2.apps.googleusercontent.com`.
- Android OAuth client dùng chung `Shop Android Shared Debug`: `826757086511-v1t2ise2a6e6c4133jlifgguploueojj.apps.googleusercontent.com`.
- Android OAuth client `Shop Android Debug`: `826757086511-gn16lrjbrlu8ml1mtodmulv81qbi7o8v.apps.googleusercontent.com`.
- Android package: `com.example.shop`.
- Shared debug SHA-1: `A4:E5:DC:3D:48:AD:F0:8A:5B:38:5E:58:6C:90:22:3B:A0:6E:CB:C6`.
- Shared debug SHA-256: `67:8C:B5:89:85:F0:8E:86:61:5E:21:81:B5:91:3C:98:ED:E5:E2:05:DE:C1:CC:EA:1E:2A:3C:21:E9:26:34:90`.
- Debug SHA-1: `33:DB:2C:01:F2:D8:E2:70:27:39:2F:EB:1B:5A:BF:8B:EA:A9:A2:86`.
- Debug SHA-256: `69:3F:6B:B7:90:DF:57:95:5B:3C:5F:84:D1:14:A9:9B:07:8C:91:C2:CF:41:79:C6:FC:DC:B9:C2:A7:18:89:48`.
- Chi tiết cấu hình và kết quả verify nằm ở `docs/google-oauth-setup.md`.
- Client secret Web application không ghi vào git. Luồng đã chốt chỉ cần Web Client ID để backend verify `id_token`.
- `app/debug.keystore` được commit có chủ đích để mọi thành viên nhóm dùng cùng SHA-1 debug khi clone repo.

### Checkpoint 27: Backend Thêm Endpoint /api/auth/google

Vấn đề hiện tại:

- `AuthController` hiện chỉ có `Register`, `Login`, `Me` cho luồng email/password.
- `User` chưa có chỗ lưu định danh từ Google nên không thể nhận biết user social ở lần đăng nhập sau.
- Backend chưa biết verify Google id_token.

Mục tiêu thực hiện:

- Backend nhận id_token từ Android, verify với Google, tự tạo hoặc liên kết user trong DB rồi trả JWT của ShopApi.
- Mọi luồng `[Authorize]` và `[Authorize(Roles = "ADMIN")]` cũ vẫn chạy nguyên.
- Migration sạch, có thể rollback.

File dự kiến thay đổi:

```text
backend/ShopApi/ShopApi.csproj
backend/ShopApi/Models/User.cs
backend/ShopApi/Data/ShopDbContext.cs
backend/ShopApi/Dtos/AuthDtos.cs
backend/ShopApi/Controllers/AuthController.cs
backend/ShopApi/appsettings.json
backend/ShopApi/Migrations/  (sinh tự động bởi `dotnet ef migrations add`)
PROGRESS.md
```

Hành động chi tiết:

- Thêm `Google.Apis.Auth` 1.68.0 vào `ShopApi.csproj`.
- Thêm property `string? GoogleSub` vào `User` model (sau `Role`, trước `CreatedAt`).
- Trong `ConfigureUsers` của `ShopDbContext`, thêm `HasMaxLength(64)` cho `GoogleSub` và `HasIndex(...).IsUnique()`.
- Tạo migration `AddGoogleAuth` bằng `dotnet ef migrations add AddGoogleAuth --project backend/ShopApi`.
- Thêm record `GoogleLoginRequest(string IdToken)` vào `AuthDtos.cs`.
- Thêm endpoint `[HttpPost("google")] Google(GoogleLoginRequest)` trong `AuthController`:
  - Verify id_token bằng `GoogleJsonWebSignature.ValidateAsync` với audience là `Google:ClientId`.
  - Lookup user theo `GoogleSub` hoặc `Email`.
  - Nếu null thì tạo user `Role = "USER"`, `PasswordHash = string.Empty`, `GoogleSub = payload.Subject`.
  - Nếu user đã có nhưng `GoogleSub` null thì gán `GoogleSub` để liên kết tài khoản cũ.
  - Tái sử dụng `CreateToken` và `ToUserResponse` đã có (`AuthController.cs:96, 124`).
- Thêm section `Google: { ClientId: "..." }` vào `appsettings.json`.
- Không sửa `Register`, `Login`, `Me`, `CreateToken`.
- Cập nhật `PROGRESS.md` sau khi build pass và HTTP smoke test ok.

Không làm trong checkpoint này:

- Không sửa `Program.cs` JwtBearer config.
- Không thêm refresh token.
- Không xóa hoặc đổi password của user hiện có.
- Không làm UI Android.

Lệnh kiểm tra:

```powershell
dotnet build backend/ShopApi --nologo
dotnet ef migrations add AddGoogleAuth --project backend/ShopApi
dotnet ef database update --project backend/ShopApi
```

Smoke test thủ công (yêu cầu có id_token thật từ Android hoặc OAuth Playground):

```text
1. Lấy id_token Google hợp lệ với audience = Web Client ID.
2. Gọi POST http://localhost:5053/api/auth/google với body { "idToken": "..." }.
3. Nhận 200 + JWT token + UserResponse.
4. Gọi GET /api/auth/me với token đó, nhận về cùng user.
5. Đăng nhập lại bằng email/password tài khoản cũ vẫn chạy bình thường.
```

Hoàn thành khi:

- Backend build pass.
- Migration tạo cột `GoogleSub` trong bảng `Users` và index unique.
- Endpoint trả JWT khi id_token hợp lệ và 401 khi không hợp lệ.
- Account email/password cũ login bình thường.
- Commit được push sau khi user review.

### Checkpoint 28: Android Thêm Sign In With Google

Vấn đề hiện tại:

- Màn login hiện chỉ có form email/password gọi `/api/auth/login`.
- App chưa có cách lấy id_token Google.
- `AuthApi` và `AuthRepository` chưa có hàm gọi `/api/auth/google`.

Mục tiêu thực hiện:

- Người dùng bấm "Sign in with Google" → app lấy id_token bằng Credential Manager → gửi backend → nhận JWT → vào home.
- Form email/password vẫn nguyên, không thay UX cũ.
- Không thêm Firebase, không phụ thuộc dependency mới ngoài kế hoạch ngoài Credential Manager + googleid.

File dự kiến thay đổi:

```text
app/build.gradle.kts
app/src/main/java/com/example/shop/data/remote/dto/AuthDtos.kt
app/src/main/java/com/example/shop/data/remote/api/AuthApi.kt
app/src/main/java/com/example/shop/data/repository/AuthRepository.kt
app/src/main/java/com/example/shop/viewmodel/AuthViewModel.kt
app/src/main/java/com/example/shop/ui/auth/LoginScreen.kt
app/src/main/java/com/example/shop/utils/Constants.kt
PROGRESS.md
```

Hành động chi tiết:

- Thêm dependency vào `app/build.gradle.kts`:
  - `androidx.credentials:credentials:1.3.0`
  - `androidx.credentials:credentials-play-services-auth:1.3.0`
  - `com.google.android.libraries.identity.googleid:googleid:1.1.1`
- Đặt `GOOGLE_WEB_CLIENT_ID` (Web Client ID từ Checkpoint 26) vào `Constants.kt`.
- Thêm DTO `GoogleLoginRequest(idToken: String)` vào `AuthDtos.kt`.
- Thêm endpoint Retrofit `@POST("api/auth/google") suspend fun googleLogin(@Body request: GoogleLoginRequest): LoginResponse`.
- Thêm hàm `loginWithGoogle(idToken: String)` trong `AuthRepository`, lưu token và user giống `login` hiện có.
- Thêm hàm tương ứng trong `AuthViewModel` để xử lý loading/error.
- Trong `LoginScreen`:
  - Thêm 1 button "Sign in with Google" dưới form email/password.
  - Khi bấm, dùng `CredentialManager` xin `GoogleIdTokenCredential` với `setServerClientId(GOOGLE_WEB_CLIENT_ID)`.
  - Lấy `idToken` rồi gọi viewmodel `loginWithGoogle(idToken)`.
  - Khi thành công, navigate sang home giống flow login email/password.

Không làm trong checkpoint này:

- Không thay form email/password cũ.
- Không thêm Sign in with Facebook/Zalo/Apple.
- Không thêm logo/branding nâng cao; dùng button text đơn giản.
- Không lưu id_token Google ở local storage; chỉ dùng để đổi JWT rồi vứt.

Lệnh kiểm tra:

```powershell
./gradlew.bat :app:assembleDebug --no-daemon --console=plain --stacktrace
```

Hoàn thành khi:

- Android build pass.
- Login screen có nút Google bên cạnh form email/password.
- Bấm Google → vào tài khoản test → vào màn home.
- Login email/password tài khoản cũ vẫn chạy.
- Commit được push sau khi user review.

Kết quả thực hiện ngày 2026-05-21:

- Đã thêm dependency Credential Manager, Play Services Auth bridge và googleid vào Android.
- Đã thêm `GOOGLE_WEB_CLIENT_ID` dùng Web OAuth Client ID đã chốt ở Checkpoint 26.
- Đã thêm DTO/API/repository/viewmodel cho `POST /api/auth/google`.
- Đã thêm nút `Sign in with Google` trực tiếp trên `LoginScreen`, dùng Credential Manager lấy `idToken`, gửi backend, nhận JWT và điều hướng như login thường.
- Không thay đổi luồng email/password cũ.
- Verify build: `dotnet build backend/ShopApi --nologo` pass và `./gradlew.bat clean :app:assembleDebug --no-daemon --console=plain --stacktrace "-Dkotlin.incremental=false"` pass.
- E2E chọn tài khoản Google trên emulator sẽ thực hiện ở Checkpoint 29.

### Checkpoint 29: E2E Luồng Google Login

Vấn đề hiện tại:

- Backend và Android đã code xong từng phần ở Checkpoint 27, 28.
- Chưa có run E2E thực tế trên backend local + emulator.

Mục tiêu thực hiện:

- Chứng minh luồng Google sign-in hoạt động end-to-end với backend local và app emulator.
- Ghi lại bằng chứng vào `PROGRESS.md`.

File dự kiến thay đổi:

```text
PROGRESS.md
```

Hành động chi tiết:

- Chạy backend local `dotnet run --project backend/ShopApi`.
- Cài app debug lên emulator.
- Test các kịch bản:
  - User mới Google → tài khoản tạo trong DB với `GoogleSub`, `Email`, `PasswordHash = ""`.
  - User cũ email/password → login bằng Google cùng email → `GoogleSub` được liên kết.
  - User chỉ Google → login lần 2 bằng Google → trả JWT; không tạo trùng user.
  - Email/password cũ login → vẫn bình thường.
  - Token giả → backend trả 401, app báo lỗi rõ.
- Ghi `Checkpoint 29` vào `PROGRESS.md` với đầy đủ verify command + kết quả + bằng chứng.

Không làm trong checkpoint này:

- Không thêm feature mới.
- Không sửa schema thêm.
- Không thay đổi UI ngoài kết quả test.

Lệnh kiểm tra:

```powershell
dotnet build backend/ShopApi --nologo
./gradlew.bat :app:assembleDebug --no-daemon --console=plain --stacktrace
```

Luồng test thủ công:

```text
1. Backend local chạy tại http://localhost:5053.
2. Emulator cài app debug.
3. Trên màn login, bấm "Sign in with Google", chọn tài khoản test.
4. Vào màn home → confirm `GET /api/auth/me` trả đúng user.
5. Logout, login lại bằng Google → confirm vẫn cùng user (không tạo trùng).
6. Logout, login bằng email/password (tài khoản cũ) → vẫn vào được.
7. Test sai id_token → backend trả 401, app báo lỗi rõ.
```

Hoàn thành khi:

- Backend build pass.
- Android build pass.
- 6 kịch bản test thủ công pass.
- Commit final được push sau khi user review.

Kết quả thực tế ngày 2026-05-21:

- Backend local đã chạy tại `http://localhost:5053`.
- Emulator dùng image `Android 26 Google Play x86`, Google Play services đã cập nhật và tài khoản Google test đã đăng nhập được.
- Google Cloud Console đã có Android OAuth client dùng chung `Shop Android Shared Debug` cho package `com.example.shop` và SHA-1 của `app/debug.keystore`.
- Google Sign-In E2E đã pass: app mở Google consent, chọn tài khoản test, backend nhận `id_token`, liên kết `GoogleSub`, trả JWT ShopApi và Android vào được màn Home.
- Backend log xác nhận truy vấn theo `GoogleSub`, truy vấn theo `Email`, cập nhật `Users.GoogleSub`, rồi load products/categories/cart.
- Verify build: `dotnet build backend/ShopApi --nologo` pass và `./gradlew.bat :app:assembleDebug --no-daemon --console=plain --stacktrace "-Dkotlin.incremental=false"` pass.
