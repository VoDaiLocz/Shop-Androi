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
| `Address` | Chưa tạo entity | Không làm API address book | Phase 2 |
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

- `Address API`: quản lý nhiều địa chỉ, địa chỉ mặc định.
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
Checkpoint 21: Rà Soát Và Khóa Contract Model Android - Backend
```

Hành động dự kiến:

```text
Đối chiếu toàn bộ model Android với backend entity/DTO/API, chốt cái nào cần backend, cái nào là model ghép, cái nào là code dư cần dọn.
```

File thay đổi:

```text
docs/plans/2026-05-07-rest-api-netcore-migration-plan.md
PROGRESS.md
```

Chỉ khi user duyệt checkpoint 21, Codex mới cập nhật docs review cuối và sang các phase code tiếp theo.

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

Android hiện có thêm một số model ngoài core:

```text
Address
Payment
Review
Notification
OrderWithItems
```

Không phải model Android nào cũng cần tạo bảng backend. Kết luận đúng là:

- `Address` cần backend thật vì màn địa chỉ đang dùng trong app và hiện chỉ lưu RAM.
- `OrderWithItems` không cần backend entity riêng vì chỉ là model ghép để UI hiển thị order kèm items.
- `Payment` chưa cần backend riêng nếu ứng dụng vẫn chốt thanh toán COD.
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
| `Address` | Chưa có | Thiếu thật vì đang có màn address | Thêm API address đơn giản |
| `Payment` | Chỉ có `PaymentMethod` trong `Order` | Đủ nếu COD only | Dọn placeholder hoặc để phase sau |
| `Review` | Chưa có | Chưa cần nếu chưa làm đánh giá thật | Dọn placeholder hoặc để phase sau |
| `Notification` | Chưa có | Chưa cần nếu chưa làm thông báo thật | Dọn placeholder hoặc để phase sau |

### 15.3 Vấn Đề Đang Tồn Tại

1. `AddressRepository` hiện lưu bằng `MutableStateFlow` trong RAM. Khi app bị kill hoặc login lại, địa chỉ mất.
2. `CartItem.productId` bên Android là `String`, trong khi backend và API dùng `int`. Code phải ép kiểu qua lại.
3. `OrderItemResponse` có `imageUrl` nhưng Android `OrderItem` chưa có field này, làm lịch sử đơn hàng khó hiển thị ảnh sản phẩm.
4. Các file `Payment`, `Review`, `Notification` hiện phần lớn là placeholder, dễ làm người đọc tưởng backend còn thiếu nhiều tính năng.
5. `data/remote/firebase` còn stub `FirebaseAuthService` và `FirestoreService`, không còn phù hợp với quyết định dùng ASP.NET Core REST API.
6. `ManageUserScreen` và `AdminUserViewModel` đang trống; nếu admin dashboard không dùng thì nên dọn hoặc ghi rõ chưa triển khai.

### 15.4 Nguyên Tắc Hoàn Thiện Từ Giai Đoạn Này

- Ưu tiên sửa phần đang ảnh hưởng luồng thật trước.
- Chỉ tạo backend cho model đang có UI/luồng nghiệp vụ thật.
- Giữ backend theo kiểu controller + DbContext khi cách này vẫn rõ và ít file.
- Mỗi phase chỉ xử lý một nhóm vấn đề để dễ review.
- Mỗi phase xong phải build, cập nhật `PROGRESS.md`, commit và push.

## 16. Phase Hoàn Thiện Sau Checkpoint 20

### Checkpoint 21: Rà Soát Và Khóa Contract Model Android - Backend

Vấn đề hiện tại:

- Android có nhiều model hơn backend nên dễ hiểu nhầm là backend thiếu toàn bộ.
- Chưa có bảng review chính thức sau checkpoint 20 để chốt model nào giữ, model nào thêm API, model nào dọn.

Mục tiêu thực hiện:

- Tạo một bản contract rõ ràng giữa Android và backend.
- Chốt thứ tự phase hoàn thiện tiếp theo.
- Không sửa code trong checkpoint này.

File thay đổi:

```text
docs/plans/2026-05-07-rest-api-netcore-migration-plan.md
PROGRESS.md
```

Hành động cụ thể:

- Ghi bảng đối chiếu Android model với backend entity/DTO/API.
- Ghi rõ `Address` là phần backend còn thiếu thật.
- Ghi rõ `Payment`, `Review`, `Notification` đang là placeholder nếu chưa làm feature.
- Ghi rõ `CartItem.productId` cần chuẩn hóa từ `String` sang `Int`.
- Ghi rõ `OrderItem.imageUrl` cần bổ sung nếu muốn lịch sử đơn hàng hiển thị ảnh.
- Ghi phase 22-26 để user review từng bước.

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

### Checkpoint 22: Thêm Address API Backend Đơn Giản

Vấn đề hiện tại:

- Android có màn danh sách địa chỉ và thêm địa chỉ.
- `AddressRepository` đang lưu địa chỉ trong RAM, không bền dữ liệu.
- Backend chưa có model/API address.

Mục tiêu thực hiện:

- Đưa address book về backend REST API.
- Giữ code backend đơn giản: một model, một DTO file, một controller.
- Address thuộc user đăng nhập, không nhận `userId` từ Android.

File dự kiến thay đổi:

```text
backend/ShopApi/Models/Address.cs
backend/ShopApi/Dtos/AddressDtos.cs
backend/ShopApi/Controllers/AddressesController.cs
backend/ShopApi/Data/ShopDbContext.cs
backend/ShopApi/Migrations/<timestamp>_AddAddresses.cs
backend/ShopApi/Migrations/ShopDbContextModelSnapshot.cs
PROGRESS.md
```

API dự kiến:

```text
GET    /api/addresses
POST   /api/addresses
DELETE /api/addresses/{id}
PUT    /api/addresses/{id}/default
```

Request tạo address:

```json
{
  "name": "Nhà riêng",
  "phoneNumber": "0909123456",
  "detail": "123 Nguyễn Trãi",
  "city": "TP.HCM",
  "isDefault": true
}
```

Response address:

```json
{
  "id": 1,
  "name": "Nhà riêng",
  "phoneNumber": "0909123456",
  "detail": "123 Nguyễn Trãi",
  "city": "TP.HCM",
  "isDefault": true
}
```

Hành động cụ thể:

- Tạo `Address` entity có `Id`, `UserId`, `Name`, `PhoneNumber`, `Detail`, `City`, `IsDefault`, `CreatedAt`.
- Thêm `DbSet<Address>` vào `ShopDbContext`.
- Cấu hình quan hệ `User` - `Address`.
- Tạo DTO request/response.
- Tạo `AddressesController` có `[Authorize]`.
- `GET /api/addresses`: trả địa chỉ của user từ JWT.
- `POST /api/addresses`: tạo địa chỉ cho user từ JWT.
- `DELETE /api/addresses/{id}`: chỉ xóa địa chỉ của chính user.
- `PUT /api/addresses/{id}/default`: set một địa chỉ mặc định, các địa chỉ khác của user thành không mặc định.
- Tạo migration và update database.

Validation tối thiểu:

- `name` không rỗng.
- `phoneNumber` không rỗng, độ dài hợp lý.
- `detail` không rỗng.
- `city` không rỗng.
- Không cho user thao tác địa chỉ của user khác.

Lệnh kiểm tra:

```powershell
dotnet build backend/ShopApi --nologo
dotnet ef migrations add AddAddresses --project backend/ShopApi
dotnet ef database update --project backend/ShopApi
```

Kiểm tra thủ công:

```text
1. Login user lấy JWT.
2. POST /api/addresses tạo địa chỉ.
3. GET /api/addresses thấy địa chỉ vừa tạo.
4. PUT /api/addresses/{id}/default set mặc định.
5. DELETE /api/addresses/{id} xóa được địa chỉ của chính user.
```

Hoàn thành khi:

- Backend build pass.
- Migration chạy được.
- Address API hoạt động bằng JWT.
- Commit được push sau khi user review.

### Checkpoint 23: Nối Android Address Sang Backend

Vấn đề hiện tại:

- Backend sau checkpoint 22 sẽ có Address API.
- Android vẫn đang dùng `AddressRepository` in-memory.
- UI address hiện quay lại ngay sau khi bấm lưu, chưa biết API thành công hay thất bại.

Mục tiêu thực hiện:

- Android address dùng Retrofit gọi backend.
- Địa chỉ không bị mất sau khi tắt app.
- UI báo lỗi đơn giản nếu lưu/xóa thất bại.

File dự kiến thay đổi:

```text
app/src/main/java/com/example/shop/data/remote/api/AddressApi.kt
app/src/main/java/com/example/shop/data/remote/dto/AddressDtos.kt
app/src/main/java/com/example/shop/data/repository/AddressRepository.kt
app/src/main/java/com/example/shop/di/AppModule.kt
app/src/main/java/com/example/shop/viewmodel/AddressViewModel.kt
app/src/main/java/com/example/shop/ui/address/AddressScreen.kt
app/src/main/java/com/example/shop/ui/address/AddAddressScreen.kt
PROGRESS.md
```

Hành động cụ thể:

- Thêm `AddressApi`.
- Thêm `AddressDtos` và mapper sang Android `Address`.
- Sửa `AppModule` để provide `AddressApi`.
- Sửa `AddressRepository`:
  - Bỏ `MutableStateFlow` in-memory.
  - `refreshAddresses()` gọi `GET /api/addresses`.
  - `addAddress()` gọi `POST /api/addresses`.
  - `deleteAddress()` gọi `DELETE /api/addresses/{id}`.
  - `setAsDefault()` gọi `PUT /api/addresses/{id}/default`.
- Sửa `AddressViewModel`:
  - Có loading/error tối giản.
  - Chỉ navigate back khi add thành công.
- Sửa `AddressScreen`:
  - Load danh sách từ backend.
  - Xóa địa chỉ rồi refresh.
- Sửa `AddAddressScreen`:
  - Không quay về nếu API fail.
  - Hiển thị lỗi text đơn giản.

Lệnh kiểm tra:

```powershell
./gradlew.bat :app:assembleDebug --no-daemon --console=plain --stacktrace
```

Kiểm tra thủ công:

```text
1. Chạy backend.
2. Login user trên emulator.
3. Mở Profile > Địa chỉ.
4. Thêm địa chỉ.
5. Tắt/mở lại app hoặc reload màn địa chỉ.
6. Địa chỉ vẫn còn vì lấy từ backend.
```

Hoàn thành khi:

- Android build pass.
- Address list/add/delete dùng backend.
- Commit được push sau khi user review.

### Checkpoint 24: Chuẩn Hóa Model Android Đang Lệch Contract

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

### Checkpoint 25: Dọn Code Placeholder Chưa Có Luồng Thật

Vấn đề hiện tại:

- `Payment`, `Review`, `Notification` có model/viewmodel/repository hoặc screen nhưng chưa nối backend và UI còn placeholder.
- `data/remote/firebase` còn stub dù dự án đã chốt REST API .NET.
- `ManageUserScreen` và `AdminUserViewModel` đang trống.

Mục tiêu thực hiện:

- Làm codebase dễ hiểu hơn.
- Người đọc không nhầm rằng các tính năng này đã hoàn thiện.
- Không xóa tính năng đang được route sử dụng thật nếu chưa kiểm tra.

File dự kiến rà soát/thay đổi:

```text
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
app/src/main/java/com/example/shop/navigation/MainNavGraph.kt
PROGRESS.md
```

Hành động cụ thể:

- Kiểm tra route nào thật sự dùng payment/review/notification/user admin.
- Nếu không có route dùng: xóa file placeholder để code gọn.
- Nếu có route nhưng chưa làm feature: đổi màn thành thông báo rõ ràng “Chức năng sẽ làm sau” hoặc gỡ khỏi navigation nếu không cần.
- Xóa Firebase stub nếu không còn import/use ở đâu.
- Build lại Android sau khi dọn.

Quy tắc review:

- Không xóa màn đang nằm trong flow chính như login, home, product, cart, checkout, order, address, admin product/category/order.
- Chỉ xóa placeholder không có logic thật.
- Nếu còn phân vân, giữ lại nhưng ghi rõ trong docs là phase sau.

Lệnh kiểm tra:

```powershell
rg "Payment|Review|Notification|Firebase|ManageUser" app/src/main/java/com/example/shop
./gradlew.bat :app:assembleDebug --no-daemon --console=plain --stacktrace
```

Hoàn thành khi:

- Android build pass.
- Không còn Firebase stub.
- Placeholder không gây hiểu nhầm backend thiếu tính năng đã cam kết.
- Commit được push sau khi user review.

### Checkpoint 26: Final E2E Sau Khi Đồng Bộ Model

Vấn đề hiện tại:

- Checkpoint 20 đã E2E core flow, nhưng chưa có Address API và chưa dọn model lệch.
- Sau checkpoint 22-25 cần chạy lại toàn bộ luồng.

Mục tiêu thực hiện:

- Xác nhận app hoàn thiện hơn sau khi model/backend được đồng bộ.
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
- Test API auth/product/category/cart/order/address.
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
7. Thêm địa chỉ nhận hàng.
8. Checkout COD.
9. Mở order history.
10. Login admin, đổi status order.
```

Hoàn thành khi:

- Backend build pass.
- Android build pass.
- Address không còn mất sau khi reload.
- Product/cart/order vẫn hoạt động sau khi chuẩn hóa model.
- Commit final được push sau khi user review.
