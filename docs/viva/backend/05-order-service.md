# OrderService

`OrderService` xử lý nghiệp vụ đơn hàng. Đây là phần backend quan trọng nhất khi vấn đáp vì một lần đặt hàng chạm cart, product, order item, payment method, transaction.

## Public methods

```csharp
CreateAsync(userId, request)
GetUserOrdersAsync(userId)
GetAllAsync()
GetPaymentStatusAsync(userId, isAdmin, orderId)
UpdateStatusAsync(orderId, request)
```

## Vì sao tách OrderService?

Tạo đơn hàng không phải CRUD đơn giản. Nó cần:

- Validate payment method.
- Validate SePay config.
- Lấy cart.
- Kiểm tra tồn kho.
- Tạo order.
- Tạo order item snapshot.
- Xử lý COD.
- Xử lý SePay.
- Dùng transaction.
- Trả DTO cho Android.

Nếu để hết trong controller thì controller dài, khó đọc, khó test.

## Flow 22. Bấm Place Order COD

1. Android gọi `POST /api/orders` với `paymentMethod = COD`.
2. `OrdersController.Create()` nhận request.
3. Controller lấy user id từ JWT.
4. Controller gọi `OrderService.CreateAsync(userId, request)`.
5. `ValidateCreateRequest()` kiểm tra address, phone, payment method.
6. `GetCartItemsAsync()` lấy cart của user.
7. `ValidateCart()` kiểm tra cart không rỗng, product còn đủ stock.
8. `BeginTransactionAsync()` mở transaction.
9. `BuildOrder()` tạo `Order`.
10. `_db.Orders.Add(order)` đưa order vào DbContext.
11. `SaveChangesAsync()` lấy order id.
12. `ApplyPaymentFlow()` xử lý nhánh COD.
13. COD trừ `Products.Quantity`.
14. COD xóa `CartItems`.
15. `SaveChangesAsync()` lưu thay đổi.
16. `CommitAsync()` commit transaction.
17. `GetOrderResponseAsync()` trả DTO cho Android.

Điểm vấn đáp: COD được xem như đơn đã tạo để giao hàng, nên backend trừ kho ngay.

## Flow 23. Bấm Place Order SePay

1. Android gọi `POST /api/orders` với `paymentMethod = SEPAY`.
2. `OrdersController.Create()` nhận request.
3. Controller lấy user id từ JWT.
4. Controller gọi `OrderService.CreateAsync(userId, request)`.
5. `ValidateCreateRequest()` kiểm tra address, phone, payment method.
6. `ValidateCreateRequest()` kiểm tra config `Sepay:BankCode`, `Sepay:AccountNumber`.
7. `GetCartItemsAsync()` lấy cart của user.
8. `ValidateCart()` kiểm tra cart không rỗng, product còn đủ stock.
9. `BeginTransactionAsync()` mở transaction.
10. `BuildOrder()` tạo `Order`.
11. `_db.Orders.Add(order)` đưa order vào DbContext.
12. `SaveChangesAsync()` lấy order id.
13. `ApplyPaymentFlow()` xử lý nhánh SePay.
14. `BuildPaymentCode()` tạo mã `ODD{orderId:D6}`.
15. `BuildSepayQrUrl()` tạo ảnh VietQR.
16. Backend giữ `PaymentStatus = Pending`.
17. Backend chưa trừ kho.
18. Backend chưa xóa cart.
19. `SaveChangesAsync()` lưu payment code.
20. `CommitAsync()` commit transaction.
21. `GetOrderResponseAsync()` trả DTO có `paymentQrUrl` cho Android.

Điểm vấn đáp: SePay chỉ tạo yêu cầu thanh toán, còn xác nhận tiền thật nằm ở webhook.

## Flow 24. Mở Payment QR

- Android gọi `GET /api/orders/{id}/payment-status`.
- `OrdersController.GetPaymentStatus()` nhận request.
- Controller lấy user id từ JWT.
- Controller xác định user có phải admin không.
- Controller gọi `OrderService.GetPaymentStatusAsync(...)`.
- Service tìm order theo id.
- Nếu order không tồn tại, trả `404 NotFound`.
- Nếu user không phải chủ order hoặc admin, trả `403 Forbidden`.
- Service trả payment status, amount, payment code, QR URL.

## Flow 25. Polling thấy Paid

- Android tiếp tục gọi `GET /api/orders/{id}/payment-status`.
- Service đọc order mới nhất trong database.
- Nếu webhook đã cập nhật `PaymentStatus = Paid`, response trả Paid.
- Android refresh cart rồi chuyển sang lịch sử đơn hàng.
- Điểm vấn đáp: polling không làm thay đổi database, nó chỉ đọc trạng thái.

## Flow 27. Mở lịch sử đơn hàng

- Android gọi `GET /api/orders/my`.
- `OrdersController.GetMyOrders()` nhận request.
- Controller lấy user id từ JWT.
- Controller gọi `OrderService.GetUserOrdersAsync(userId)`.
- Service đọc `Orders` theo user id.
- Service include `OrderItems`.
- Service trả danh sách order DTO.

## Flow 40. Bấm card Đơn hàng admin

- Android admin gọi `GET /api/orders`.
- `OrdersController.GetAll()` nhận request.
- Endpoint yêu cầu role ADMIN.
- Controller gọi `OrderService.GetAllAsync()`.
- Service đọc toàn bộ `Orders`.
- Service include `OrderItems`, `Users`.
- Service trả danh sách order DTO cho admin.

## Flow 41. Bấm đổi trạng thái đơn admin

- Android admin gọi `PUT /api/orders/{id}/status`.
- `OrdersController.UpdateStatus()` nhận request.
- Endpoint yêu cầu role ADMIN.
- Controller gọi `OrderService.UpdateStatusAsync(orderId, request)`.
- Service tìm order theo id.
- Service kiểm tra status nằm trong `Pending`, `Shipping`, `Delivered`, `Cancelled`.
- Service update `Order.Status`.
- Service lưu database.
- Service trả order DTO mới.

## Method cần nhớ

### `ValidateCreateRequest`

- Chỉ nhận `COD` hoặc `SEPAY`.
- Address không rỗng.
- Phone number không rỗng.
- SePay cần bank code, account number.

### `ValidateCart`

- Cart không rỗng.
- Mỗi product phải còn đủ quantity.
- Thiếu hàng thì trả `409 Conflict`.

### `BuildOrder`

- Tạo `Order`.
- Tạo `OrderItem` snapshot từ product hiện tại.
- Snapshot giúp lịch sử đơn không đổi khi product sau này đổi tên hoặc đổi giá.

### `ApplyPaymentFlow`

- COD: trừ kho, xóa cart.
- SePay: tạo payment code, tạo QR, chờ webhook.

## Câu trả lời mẫu

> Khi user bấm Place Order, Android gọi `POST /api/orders`. Backend vào `OrdersController`, controller lấy user id từ JWT rồi gọi `OrderService`. Service validate request, lấy cart, kiểm tra tồn kho, tạo order trong transaction. COD thì trừ kho và xóa cart ngay. SePay thì tạo payment code và QR, còn trừ kho sau khi webhook báo đã nhận tiền.
