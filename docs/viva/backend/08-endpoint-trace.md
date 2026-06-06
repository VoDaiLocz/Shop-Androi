# Backend endpoint trace theo hành động đơn

File này dùng cho người phụ trách backend. Bản lifecycle đầy đủ từ người dùng bấm nút đến database nằm ở `../shared/03-trace-chi-tiet-theo-chuc-nang.md`.

## Pipeline backend phải nhớ

- `Program.cs:13`: đăng ký controllers.
- `Program.cs:14-15`: đăng ký `IOrderService`, `IPaymentService`.
- `Program.cs:20-21`: đăng ký `ShopDbContext` với MySQL.
- `Program.cs:37-51`: cấu hình JWT bearer validation.
- `Program.cs:79-81`: chạy authentication, authorization, map controllers.
- `ShopDbContext.cs:12-17`: các bảng chính: Users, Categories, Products, CartItems, Orders, OrderItems.

## Auth endpoints

### `POST /api/auth/register`

- Flow gọi vào: Flow 07. Bấm Đăng ký.
- Controller: `AuthController.Register` tại `AuthController.cs:30-60`.
- Quyền: public.
- Database:
  - Đọc `Users` để kiểm tra email trùng tại `AuthController.cs:40`.
  - Ghi `Users` tại `AuthController.cs:56-57`.
- Điểm vấn đáp:
  - Backend trim username/email.
  - Email lưu lowercase.
  - Password hash bằng `PasswordHasher`, không lưu password raw.

### `POST /api/auth/login`

- Flow gọi vào: Flow 04. Bấm Log in.
- Controller: `AuthController.Login` tại `AuthController.cs:62-80`.
- Quyền: public.
- Database:
  - Đọc `Users` theo email tại `AuthController.cs:66`.
- Điểm vấn đáp:
  - Verify password hash tại `AuthController.cs:72`.
  - Tạo JWT tại `AuthController.cs:78`.
  - JWT claims gồm id/name/email/role tại `AuthController.cs:176-182`.

### `POST /api/auth/google`

- Flow gọi vào: Flow 05. Bấm Continue With Google.
- Controller: `AuthController.Google` tại `AuthController.cs:82-143`.
- Quyền: public.
- Database:
  - Đọc `Users` theo `GoogleSub` và email.
  - Có thể ghi `Users` khi tạo user mới hoặc link GoogleSub.
- Điểm vấn đáp:
  - Android lấy Google idToken.
  - Backend validate idToken bằng Google client id tại `AuthController.cs:91-100`.
  - Sau khi Google hợp lệ, backend vẫn cấp JWT nội bộ tại `AuthController.cs:141-142`.

### `GET /api/auth/me`

- Flow gọi vào: app kiểm tra token/user hiện tại khi cần session.
- Controller: `AuthController.Me` tại `AuthController.cs:145-162`.
- Quyền: Bearer token.
- Database:
  - Đọc `Users` theo user id trong JWT tại `AuthController.cs:149-155`.

## Product endpoints

### `GET /api/products`

- Flow gọi vào:
  - Flow 10. Home load sản phẩm.
  - Flow 28. Bấm card Sản phẩm admin.
- Controller: `ProductsController.GetAll` tại `ProductsController.cs:32-51`.
- Quyền: public.
- Database:
  - Đọc `Products`.
  - Include `Categories`.
- Ghi chú:
  - Có query `categoryId` tại `ProductsController.cs:40-43`.
  - App hiện đang lọc category local ở `ProductScreen`.

### `GET /api/products/{id}`

- Flow gọi vào:
  - Flow 13. Bấm product card.
  - Flow 31. Bấm sửa sản phẩm admin.
- Controller: `ProductsController.GetById` tại `ProductsController.cs:53-67`.
- Quyền: public.
- Database:
  - Đọc `Products`, include `Category`.

### `POST /api/products`

- Flow gọi vào: Flow 30. Bấm Lưu sản phẩm admin.
- Controller: `ProductsController.Create` tại `ProductsController.cs:69-93`.
- Quyền: ADMIN.
- Database:
  - Đọc `Categories` để validate category tồn tại tại `ProductsController.cs:219`.
  - Ghi `Products` tại `ProductsController.cs:89-90`.

### `PUT /api/products/{id}`

- Flow gọi vào: Flow 32. Bấm Cập nhật sản phẩm admin.
- Controller: `ProductsController.Update` tại `ProductsController.cs:95-122`.
- Quyền: ADMIN.
- Database:
  - Đọc `Products`.
  - Đọc `Categories` để validate.
  - Ghi `Products`.

### `DELETE /api/products/{id}`

- Flow gọi vào: Flow 33. Bấm xóa sản phẩm admin.
- Controller: `ProductsController.Delete` tại `ProductsController.cs:124-150`.
- Quyền: ADMIN.
- Database:
  - Đọc `Products`.
  - Đọc `OrderItems` để chặn xóa nếu có lịch sử đơn tại `ProductsController.cs:134-138`.
  - Đọc `CartItems` để chặn xóa nếu đang nằm trong giỏ tại `ProductsController.cs:140-144`.
  - Xóa `Products` nếu an toàn.

### `POST /api/products/{id}/image`

- Flow gọi vào:
  - Flow 30. Bấm Lưu sản phẩm admin khi có ảnh.
  - Flow 32. Bấm Cập nhật sản phẩm admin khi có ảnh mới.
- Controller: `ProductsController.UploadImage` tại `ProductsController.cs:152-195`.
- Quyền: ADMIN.
- Database/file:
  - Đọc/Ghi `Products.ImageUrl`.
  - Ghi file vào `wwwroot/uploads/products`.
- Guard:
  - File bắt buộc, max 5MB, chỉ `.jpg/.jpeg/.png/.webp`.

## Category endpoints

### `GET /api/categories`

- Flow gọi vào:
  - Flow 11. Home load danh mục.
  - Flow 34. Bấm card Danh mục admin.
  - Flow 29. Bấm thêm sản phẩm admin.
  - Flow 31. Bấm sửa sản phẩm admin.
- Controller: `CategoriesController.GetAll` tại `CategoriesController.cs:21-31`.
- Quyền: public.
- Database: đọc `Categories`.

### `GET /api/categories/{id}`

- Flow gọi vào: Flow 37. Bấm sửa danh mục admin.
- Controller: `CategoriesController.GetById` tại `CategoriesController.cs:33-46`.
- Quyền: public.
- Database: đọc `Categories`.

### `POST /api/categories`

- Flow gọi vào: Flow 36. Bấm Lưu danh mục admin.
- Controller: `CategoriesController.Create` tại `CategoriesController.cs:48-68`.
- Quyền: ADMIN.
- Database: ghi `Categories`.

### `PUT /api/categories/{id}`

- Flow gọi vào: Flow 38. Bấm Cập nhật danh mục admin.
- Controller: `CategoriesController.Update` tại `CategoriesController.cs:70-91`.
- Quyền: ADMIN.
- Database: đọc/ghi `Categories`.

### `DELETE /api/categories/{id}`

- Flow gọi vào: Flow 39. Bấm xóa danh mục admin.
- Controller: `CategoriesController.Delete` tại `CategoriesController.cs:93-112`.
- Quyền: ADMIN.
- Database:
  - Đọc `Categories`.
  - Đọc `Products` để chặn xóa category còn product tại `CategoriesController.cs:103-107`.
  - Xóa `Categories` nếu an toàn.

## Cart endpoints

`CartController` có `[Authorize]` tại `CartController.cs:11`, nên mọi endpoint cart cần JWT.

### `GET /api/cart`

- Flow gọi vào:
  - Flow 15. Bấm icon cart.
  - Flow 19. Bấm checkout khi Checkout cần cart hiện tại.
- Controller: `CartController.GetCart` tại `CartController.cs:23-33`.
- Database:
  - Đọc `CartItems`, include `Products`, filter theo user id trong JWT tại `CartController.cs:151-168`.

### `POST /api/cart/items`

- Flow gọi vào: Flow 14. Bấm Add to Cart.
- Controller: `CartController.AddItem` tại `CartController.cs:35-75`.
- Database:
  - Đọc `Products` kiểm tra product tồn tại.
  - Đọc `CartItems` theo user/product.
  - Insert hoặc update `CartItems`.

### `PUT /api/cart/items/{id}`

- Flow gọi vào:
  - Flow 16. Bấm nút cộng cart.
  - Flow 17. Bấm nút trừ cart khi quantity còn lớn hơn 0.
- Controller: `CartController.UpdateItem` tại `CartController.cs:77-104`.
- Database:
  - Đọc `CartItems` theo id và user id.
  - Update quantity.

### `DELETE /api/cart/items/{id}`

- Flow gọi vào:
  - Flow 18. Bấm icon delete cart.
  - Flow 17. Bấm nút trừ cart khi quantity bằng 1.
- Controller: `CartController.DeleteItem` tại `CartController.cs:106-127`.
- Database:
  - Đọc `CartItems` theo id và user id.
  - Xóa cart item.

### `DELETE /api/cart`

- Flow gọi vào: backend clear cart nội bộ nếu cần.
- Controller: `CartController.ClearCart` tại `CartController.cs:129-142`.
- Database:
  - Xóa toàn bộ `CartItems` của user bằng `ExecuteDeleteAsync`.

## Order endpoints

`OrdersController` có `[Authorize]` tại `OrdersController.cs:9`.

### `POST /api/orders`

- Flow gọi vào:
  - Flow 22. Bấm Place Order COD.
  - Flow 23. Bấm Place Order SePay.
- Controller: `OrdersController.Create` tại `OrdersController.cs:21-32`.
- Service: `OrderService.CreateAsync` tại `OrderService.cs:37-63`.
- Database:
  - Đọc `CartItems`, `Products`.
  - Ghi `Orders`, `OrderItems`.
  - COD: update `Products.Quantity`, xóa `CartItems`.
  - SEPAY: tạo `PaymentCode`, chưa trừ kho và chưa xóa cart.
- Điểm vấn đáp:
  - Service validate payment method/address/phone.
  - SePay yêu cầu cấu hình bank code/account number.
  - Tạo order chạy trong transaction.

### `GET /api/orders/my`

- Flow gọi vào: Flow 27. Mở lịch sử đơn hàng.
- Controller: `OrdersController.GetMyOrders` tại `OrdersController.cs:34-44`.
- Service: `OrderService.GetUserOrdersAsync` tại `OrderService.cs:65-68`.
- Database:
  - Đọc `Orders`, `OrderItems`, `Users`, filter theo user id trong JWT.

### `GET /api/orders/{id}/payment-status`

- Flow gọi vào: Flow 25. Polling thấy Paid.
- Controller: `OrdersController.GetPaymentStatus` tại `OrdersController.cs:46-57`.
- Service: `OrderService.GetPaymentStatusAsync` tại `OrderService.cs:75-92`.
- Database:
  - Đọc `Orders`.
- Guard:
  - User chỉ xem được order của mình.
  - Admin được xem mọi order.

### `GET /api/orders`

- Flow gọi vào: Flow 40. Bấm card Đơn hàng admin.
- Controller: `OrdersController.GetAll` tại `OrdersController.cs:59-64`.
- Service: `OrderService.GetAllAsync` tại `OrderService.cs:70-73`.
- Quyền: ADMIN.
- Database:
  - Đọc `Orders`, `OrderItems`, `Users`.

### `PUT /api/orders/{id}/status`

- Flow gọi vào: Flow 41. Bấm đổi trạng thái đơn admin.
- Controller: `OrdersController.UpdateStatus` tại `OrdersController.cs:66-74`.
- Service: `OrderService.UpdateStatusAsync` tại `OrderService.cs:94-115`.
- Quyền: ADMIN.
- Database:
  - Đọc/Ghi `Orders`.
- Guard:
  - Status chỉ được là `Pending`, `Shipping`, `Delivered`, `Cancelled`.

## Payment webhook

### `POST /api/payments/sepay/webhook`

- Flow gọi vào: Flow 26. SePay gửi webhook.
- Controller: `PaymentsController.SepayWebhook` tại `PaymentsController.cs:19-33`.
- Service: `PaymentService.HandleSepayWebhookAsync` tại `PaymentService.cs:33-56`.
- Quyền:
  - Anonymous route nhưng service kiểm tra webhook API key nếu cấu hình.
- Database:
  - Đọc `Orders`, `OrderItems`, `Products`.
  - Ghi `Orders.PaymentStatus`, transaction id/reference/paid time.
  - Update `Products.Quantity`.
  - Xóa `CartItems` đã thanh toán.
- Guard:
  - Chỉ xử lý tiền vào.
  - Tìm payment code `ODDxxxxxx`.
  - Bỏ qua order không tồn tại/không phải SePay/đã Paid.
  - Chống xử lý trùng transaction id.
  - Số tiền chuyển vào phải bằng order total.
  - Stock phải còn đủ.

## User admin endpoints

### `GET /api/users`

- Flow gọi vào:
  - Flow 42. Bấm card Người dùng admin.
  - Flow 43. Bấm Tải lại user admin.
- Controller: `UsersController.GetAll` tại `UsersController.cs:22-32`.
- Quyền: ADMIN.
- Database: đọc `Users`.

### `DELETE /api/users/{id}`

- Flow gọi vào: Flow 44. Bấm xóa user admin.
- Controller: `UsersController.Delete` tại `UsersController.cs:34-86`.
- Quyền: ADMIN.
- Database:
  - Đọc `Users`, `Orders`.
  - Xóa `OrderItems`, `Orders`, `CartItems`, `Users`.
- Guard:
  - Không xóa chính mình.
  - Không xóa admin account.
  - Xóa trong transaction.

## Status code hay bị hỏi

| Status | Ý nghĩa trong dự án |
|---|---|
| `200 OK` | Request thành công có body |
| `201 Created` | Tạo user/product/category/order thành công |
| `204 NoContent` | Xóa thành công không cần body |
| `400 BadRequest` | Input sai hoặc cấu hình thiếu |
| `401 Unauthorized` | Thiếu/sai JWT hoặc webhook key sai |
| `403 Forbidden` | Có token nhưng không có quyền |
| `404 NotFound` | Không tìm thấy resource |
| `409 Conflict` | Trùng email, thiếu stock, không thể xóa do đang được tham chiếu |

## Câu trả lời ngắn về backend architecture

> Backend là ASP.NET Core Web API. Request đi vào Controller. Với order/payment, Controller gọi Service để giữ controller mỏng và gom nghiệp vụ phức tạp. Service dùng `ShopDbContext` để đọc/ghi MySQL qua EF Core. Với auth/product/category/cart/users, controller còn dùng trực tiếp DbContext vì logic ngắn hơn. JWT middleware chạy trước controller để xác thực token và role.
