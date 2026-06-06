# Tích hợp SePay VietQR vào dự án

File này dùng để học cách tích hợp thanh toán SePay/VietQR từ đầu vào dự án. Đây không phải flow người dùng trong app, mà là các bước kỹ thuật đã làm để app tạo QR chuyển khoản, backend nhận webhook, xác nhận thanh toán.

## Mục tiêu tích hợp

- User chọn thanh toán SePay khi checkout.
- Backend tạo order pending.
- Backend tạo mã chuyển khoản riêng cho order.
- Backend tạo URL ảnh VietQR.
- Android hiển thị QR để người dùng quét bằng app ngân hàng.
- SePay gửi webhook về backend khi có tiền vào.
- Backend kiểm tra webhook, số tiền, mã thanh toán, transaction id, tồn kho.
- Backend mark order Paid, trừ kho, xóa cart.
- Android polling thấy Paid rồi chuyển sang lịch sử đơn.

Điểm phải nói khi vấn đáp:

> VietQR là ảnh QR để người dùng chuyển khoản. SePay là dịch vụ báo giao dịch tiền vào qua webhook. Backend chỉ xác nhận thanh toán khi webhook hợp lệ.

## Bước 1. Chọn thiết kế thanh toán

Dự án không tích hợp SDK thanh toán trong Android.

Thiết kế đã chọn:

```text
Android
-> POST /api/orders với paymentMethod = SEPAY
-> Backend tạo order pending + payment code + VietQR URL
-> Android hiển thị QR
-> SePay webhook gọi backend
-> Backend cập nhật Paid
-> Android polling payment status
```

Lý do chọn cách này:

- Android không cần giữ API key thanh toán.
- Backend là nơi duy nhất quyết định order đã thanh toán chưa.
- Webhook từ SePay là nguồn xác nhận tiền thật.
- Dễ test local bằng webhook giả trong development.

## Bước 2. Chuẩn hóa payment method

Trong hệ thống dùng hai payment method:

```text
COD
SEPAY
```

Trong `CheckoutScreen.kt`, state mặc định:

```kotlin
var selectedPayment by remember { mutableStateOf("SEPAY") }
```

Hai lựa chọn UI:

```kotlin
PaymentOption("Chuyển khoản SePay", "SEPAY", selectedPayment) { selectedPayment = it }
PaymentOption("Thanh toán khi nhận hàng", "COD", selectedPayment) { selectedPayment = it }
```

Khi bấm Place Order, Android gửi:

```kotlin
paymentMethod = selectedPayment
```

## Bước 3. Mở rộng model Order

Trong `backend/ShopApi/Models/Order.cs` đã thêm các field:

```csharp
public string PaymentMethod { get; set; } = "COD";
public string PaymentStatus { get; set; } = "Pending";
public string? PaymentCode { get; set; }
public string? SepayTransactionId { get; set; }
public string? SepayReferenceCode { get; set; }
public DateTime? PaidAt { get; set; }
```

Ý nghĩa:

- `PaymentMethod`: order dùng COD hay SePay.
- `PaymentStatus`: Pending, Paid, Failed.
- `PaymentCode`: nội dung chuyển khoản riêng của order.
- `SepayTransactionId`: id giao dịch SePay, dùng chống xử lý trùng.
- `SepayReferenceCode`: mã tham chiếu ngân hàng.
- `PaidAt`: thời điểm thanh toán thành công.

## Bước 4. Cấu hình database cho payment

Trong `ShopDbContext.cs` đã cấu hình:

```csharp
entity.Property(order => order.PaymentMethod).HasMaxLength(20).IsRequired();
entity.Property(order => order.PaymentStatus).HasMaxLength(20).IsRequired();
entity.Property(order => order.PaymentCode).HasMaxLength(40);
entity.Property(order => order.SepayTransactionId).HasMaxLength(64);
entity.Property(order => order.SepayReferenceCode).HasMaxLength(120);
entity.HasIndex(order => order.PaymentCode).IsUnique();
entity.HasIndex(order => order.SepayTransactionId).IsUnique();
```

Lý do:

- `PaymentCode` phải unique để webhook tìm đúng order.
- `SepayTransactionId` phải unique để chống webhook trùng.
- Field có max length để schema rõ ràng.

Migration đã thêm các cột payment vào bảng `Orders`.

## Bước 5. Thêm DTO order payment

Dự án cần DTO để Android gửi payment method khi đặt hàng.

Request tạo order gồm:

```text
address
phoneNumber
paymentMethod
```

Dự án cần DTO để Android đọc trạng thái thanh toán:

```text
orderId
totalPrice
status
paymentMethod
paymentStatus
paymentCode
paymentQrUrl
```

Lý do:

- `OrderResponse` dùng cho lịch sử đơn.
- `OrderPaymentStatusResponse` dùng riêng cho màn QR polling.

## Bước 6. Cấu hình SePay/VietQR trong backend

Trong `backend/ShopApi/appsettings.json`:

```json
"Sepay": {
  "BankCode": "",
  "AccountNumber": "",
  "QrTemplate": "compact"
}
```

Khi chạy local có thể cấu hình trong `appsettings.Development.json` hoặc biến môi trường:

```bash
Sepay__BankCode=BIDV
Sepay__AccountNumber=8894308684
Sepay__QrTemplate=compact2
Sepay__WebhookApiKey=local-secret
```

Ý nghĩa:

- `BankCode`: mã ngân hàng trong VietQR.
- `AccountNumber`: số tài khoản nhận tiền.
- `QrTemplate`: mẫu ảnh QR.
- `WebhookApiKey`: key để xác thực webhook SePay nếu cấu hình.

Lưu ý:

> Nếu dùng tài khoản thật, QR tạo ra có thể dẫn đến chuyển tiền thật. Khi test nên dùng số tiền nhỏ.

## Bước 7. Tách OrderService

Đặt hàng có nhiều nghiệp vụ nên đã đưa vào:

```text
OrdersController -> IOrderService -> OrderService -> ShopDbContext
```

Trong `Program.cs` đăng ký DI:

```csharp
builder.Services.AddScoped<IOrderService, OrderService>();
```

Lý do:

- Controller chỉ nhận request, trả response.
- Service xử lý validate, cart, stock, order, payment.
- Code dễ học, dễ vấn đáp hơn.

## Bước 8. Validate payment method khi tạo order

Trong `OrderService.ValidateCreateRequest(...)`:

```csharp
Payment method must be COD or SEPAY.
```

Nếu chọn SePay thì backend kiểm tra thêm:

```csharp
Sepay:BankCode
Sepay:AccountNumber
```

Nếu thiếu config, backend trả lỗi:

```text
SePay is not configured. Set Sepay:BankCode and Sepay:AccountNumber.
```

Lý do:

- Không tạo order SePay nếu backend không thể tạo QR.
- Tránh Android hiện màn QR rỗng hoặc sai ngân hàng.

## Bước 9. Tạo order pending cho SePay

Trong `OrderService.CreateAsync(...)`:

1. Validate request.
2. Normalize payment method.
3. Lấy cart items theo user.
4. Kiểm tra cart không rỗng.
5. Kiểm tra product còn đủ tồn kho.
6. Mở transaction.
7. Build order.
8. Save order để có order id.
9. Chạy `ApplyPaymentFlow(...)`.
10. Save database.
11. Commit transaction.
12. Trả order response.

Với SePay, order ban đầu:

```text
PaymentMethod = SEPAY
PaymentStatus = Pending
```

## Bước 10. Tạo payment code

Trong `OrderService.BuildPaymentCode(...)`:

```csharp
return $"ODD{orderId:D6}";
```

Ví dụ:

```text
Order id 7 -> ODD000007
```

Lý do:

- Mỗi order có mã chuyển khoản riêng.
- Webhook dựa vào mã này để tìm đúng order.
- Format cố định giúp regex tìm dễ trong nội dung chuyển khoản.

## Bước 11. Tạo VietQR URL

Trong `OrderService.BuildSepayQrUrl(...)`:

```csharp
https://img.vietqr.io/image/{bankCode}-{accountNumber}-{template}.png?amount={amount}&addInfo={paymentCode}
```

Backend set:

- `amount`: tổng tiền order.
- `addInfo`: payment code.

Lý do:

- Người dùng quét QR sẽ thấy sẵn số tiền.
- Nội dung chuyển khoản có payment code để SePay gửi lại.
- Backend không cần lưu ảnh QR, chỉ cần lưu URL.

## Bước 12. Không trừ kho ngay với SePay

Trong `ApplyPaymentFlow(...)`:

- Nếu COD: trừ kho, xóa cart.
- Nếu SEPAY: tạo payment code, chưa trừ kho, chưa xóa cart.

Lý do:

> User mới tạo order SePay nhưng chưa chắc đã chuyển tiền. Nếu trừ kho ngay có thể giữ hàng sai.

Stock chỉ trừ khi webhook hợp lệ.

## Bước 13. Thêm endpoint payment status

Android cần lấy QR và polling trạng thái nên backend có:

```text
GET /api/orders/{id}/payment-status
```

Trong `OrderService.GetPaymentStatusAsync(...)`:

- Tìm order.
- Chặn user xem order người khác.
- Admin được xem mọi order.
- Trả payment status, payment code, QR URL.

Android dùng endpoint này ở `PaymentQrScreen`.

## Bước 14. Hiển thị QR ở Android

Sau khi `POST /api/orders` thành công với SePay:

```text
MainNavGraph -> PaymentQrScreen(orderId)
```

`PaymentQrScreen` gọi:

```kotlin
viewModel.getPaymentStatus(orderId)
```

UI hiển thị:

- QR image từ `paymentQrUrl`.
- Số tiền.
- Nội dung chuyển khoản.
- Trạng thái thanh toán.

QR image dùng Coil/AsyncImage để load từ URL.

## Bước 15. Polling trạng thái thanh toán

Trong `PaymentQrScreen`, màn QR lặp khi còn active:

```text
getPaymentStatus(orderId)
delay(3000)
```

Nếu status là `Paid`:

1. Refresh cart.
2. Gọi `onPaid()`.
3. Dừng polling.
4. Navigate sang lịch sử đơn hàng.

Lý do dùng polling:

- Webhook đi vào backend, không đi trực tiếp vào Android.
- Android cần hỏi backend để biết order đã Paid chưa.

## Bước 16. Tạo PaymentService xử lý webhook

Webhook phức tạp nên đã tách:

```text
PaymentsController -> IPaymentService -> PaymentService -> ShopDbContext
```

Trong `Program.cs` đăng ký DI:

```csharp
builder.Services.AddScoped<IPaymentService, PaymentService>();
```

Endpoint:

```text
POST /api/payments/sepay/webhook
```

Controller chỉ gọi:

```text
PaymentService.HandleSepayWebhookAsync(...)
```

## Bước 17. Xác thực webhook

Trong `PaymentService.IsWebhookAuthorized(...)`:

- Nếu có `Sepay:WebhookApiKey`, backend yêu cầu header Authorization đúng.
- Hỗ trợ format `Apikey <key>`.
- So sánh bằng `CryptographicOperations.FixedTimeEquals`.
- Nếu không có key ở development, backend cho phép để test local.

Lý do:

- Production không cho request lạ tự mark order Paid.
- Development vẫn test được khi chưa cấu hình key thật.

## Bước 18. Tìm payment code từ webhook

Trong `PaymentService.ResolveIncomingPaymentCode(...)`:

1. Chỉ nhận `TransferType = in`.
2. Nếu `request.Code` có giá trị, dùng code đó.
3. Nếu không, tìm pattern `ODD\d{6}` trong `request.Content`.

Lý do:

- Có lúc SePay tách được code riêng.
- Có lúc code chỉ nằm trong nội dung chuyển khoản.
- Backend cần fallback để robust hơn.

## Bước 19. Tìm order SePay

Backend tìm order bằng:

```text
PaymentCode
```

Sau đó bỏ qua nếu:

- Không tìm thấy order.
- Order không phải `SEPAY`.
- Order đã `Paid`.

Lý do:

- Webhook có thể chứa giao dịch không thuộc app.
- Webhook có thể bị gửi lại.
- Không xử lý order đã Paid lần nữa.

## Bước 20. Chống xử lý trùng transaction

Trong `PaymentService.IsTransactionProcessedAsync(...)`:

```csharp
Orders.AnyAsync(order => order.SepayTransactionId == value)
```

Nếu transaction id đã tồn tại, service dừng xử lý.

Lý do:

- SePay có thể retry webhook.
- Nếu không chống trùng, backend có thể trừ kho hai lần.

Database cũng có unique index:

```text
SepayTransactionId unique
```

## Bước 21. Kiểm tra số tiền

Trong `PaymentService.IsPaymentValid(...)`:

```csharp
request.TransferAmount == order.TotalPrice
```

Nếu sai tiền:

- Set `PaymentStatus = Failed`.
- Lưu `SepayTransactionId`.
- Lưu `SepayReferenceCode`.
- Không trừ kho.
- Không xóa cart.

Lý do:

> Chỉ đánh dấu Paid khi số tiền chuyển đúng bằng tổng tiền order.

## Bước 22. Kiểm tra tồn kho tại thời điểm webhook

Webhook có thể đến sau khi order được tạo một lúc.

Backend kiểm tra:

```csharp
order.Items.All(item => item.Product.Quantity >= item.Quantity)
```

Nếu thiếu stock:

- Set `PaymentStatus = Failed`.
- Không trừ kho âm.

Lý do:

- Tránh bán quá số lượng còn lại.
- Bảo vệ database ở bước xác nhận tiền thật.

## Bước 23. Mark Paid khi hợp lệ

Nếu transaction chưa xử lý, số tiền đúng, stock đủ:

```text
PaymentStatus = Paid
SepayTransactionId = request.Id
SepayReferenceCode = request.ReferenceCode
PaidAt = now
UpdatedAt = now
```

Sau đó backend:

- Trừ `Product.Quantity`.
- Xóa cart item tương ứng của user.
- SaveChanges.
- Commit transaction.

## Bước 24. Dùng transaction khi xử lý webhook

Trong `ApplyPaymentAsync(...)`:

```text
BeginTransactionAsync()
-> validate transaction
-> validate payment
-> mark failed hoặc paid
-> xóa cart nếu paid
-> SaveChangesAsync()
-> CommitAsync()
```

Lý do:

- Update order.
- Update product stock.
- Delete cart items.

Ba việc này phải thành công cùng nhau. Nếu lỗi giữa chừng, transaction rollback để database không lệch.

## Bước 25. Test tích hợp local

Checklist test:

1. Backend có `Sepay:BankCode`.
2. Backend có `Sepay:AccountNumber`.
3. Backend chạy ở `http://localhost:5053`.
4. Android base URL là `http://10.0.2.2:5053/`.
5. User thêm product vào cart.
6. User vào checkout.
7. User chọn SePay.
8. User bấm Place Order.
9. Backend trả order có `PaymentCode`.
10. Android mở Payment QR.
11. QR URL có `amount` và `addInfo`.
12. Gửi webhook test vào `POST /api/payments/sepay/webhook`.
13. Backend mark order Paid nếu số tiền đúng.
14. Android polling thấy Paid.
15. Cart được refresh.
16. Order hiện trong lịch sử.

## Webhook test mẫu

Khi development không cấu hình `Sepay:WebhookApiKey`, có thể test webhook local:

```bash
curl -X POST http://localhost:5053/api/payments/sepay/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "id": 10001,
    "code": "ODD000007",
    "content": "ODD000007",
    "transferType": "in",
    "transferAmount": 1000,
    "referenceCode": "TEST-001"
  }'
```

Khi có webhook key:

```bash
curl -X POST http://localhost:5053/api/payments/sepay/webhook \
  -H "Content-Type: application/json" \
  -H "Authorization: Apikey local-secret" \
  -d '{
    "id": 10002,
    "code": "ODD000008",
    "content": "ODD000008",
    "transferType": "in",
    "transferAmount": 1000,
    "referenceCode": "TEST-002"
  }'
```

Lưu ý:

- `code` phải khớp `PaymentCode` của order thật.
- `transferAmount` phải bằng `TotalPrice`.
- `id` phải mới, chưa từng xử lý.

## Lỗi hay gặp

### Đặt hàng báo thiếu cấu hình SePay

Nguyên nhân:

- `Sepay:BankCode` rỗng.
- `Sepay:AccountNumber` rỗng.

Cách sửa:

- Điền config trong `appsettings.Development.json` hoặc biến môi trường.
- Restart backend.

### QR ngân hàng báo tài khoản không hợp lệ

Nguyên nhân thường gặp:

- `BankCode` không đúng format VietQR.
- `AccountNumber` sai.
- Tài khoản nhận không hỗ trợ chức năng QR/chuyển khoản kiểu đó.
- App ngân hàng không nhận template QR hiện tại.

Cách debug:

- Mở trực tiếp URL `paymentQrUrl` trong browser.
- Kiểm tra đoạn URL `{bankCode}-{accountNumber}-{template}`.
- Thử `QrTemplate = compact2`.
- Thử amount nhỏ để loại trừ giới hạn giao dịch.

### Android cứ Pending

Nguyên nhân:

- Chưa có webhook gọi về backend.
- Webhook không tìm được payment code.
- Webhook sai `transferType`.
- Webhook sai amount nên order bị Failed.
- Android đang polling sai order id.

Cách debug:

- Xem `Orders.PaymentStatus`.
- Xem `Orders.PaymentCode`.
- Xem log backend khi gọi webhook.
- Gọi `GET /api/orders/{id}/payment-status` trực tiếp.

### Webhook bị gọi trùng

Kết quả đúng:

- Backend bỏ qua transaction đã xử lý.
- Không trừ kho lần hai.
- Không xóa cart lần hai.

## Câu trả lời ngắn khi vấn đáp

> Để tích hợp SePay, em thêm payment fields vào Order, thêm config bank code/account number, tạo logic trong `OrderService` để khi user chọn SePay thì tạo order Pending, sinh payment code `ODDxxxxxx`, sinh VietQR URL. Android hiển thị QR và polling status. Khi SePay gửi webhook, `PaymentService` xác thực webhook, tìm payment code, chống trùng transaction, kiểm tra số tiền và tồn kho. Nếu hợp lệ thì mark Paid, trừ kho, xóa cart trong transaction.
