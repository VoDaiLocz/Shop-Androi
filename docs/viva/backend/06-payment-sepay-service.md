# PaymentService SePay VietQR

`PaymentService` xử lý webhook SePay. Đây là phần xác nhận thanh toán thật. Android không được tự báo đã thanh toán.

## Endpoint

```text
POST /api/payments/sepay/webhook
```

Controller:

```text
PaymentsController.SepayWebhook()
-> IPaymentService.HandleSepayWebhookAsync()
```

## Webhook request

```csharp
SepayWebhookRequest(
    long Id,
    string? Code,
    string? Content,
    string? TransferType,
    decimal TransferAmount,
    string? ReferenceCode)
```

- `Id`: id giao dịch SePay.
- `Code`: mã thanh toán nếu SePay tách được.
- `Content`: nội dung chuyển khoản.
- `TransferType`: `in` là tiền vào.
- `TransferAmount`: số tiền nhận.
- `ReferenceCode`: mã tham chiếu ngân hàng.

## Flow 26. SePay gửi webhook

1. SePay server gọi `POST /api/payments/sepay/webhook`.
2. `PaymentsController.SepayWebhook()` nhận request.
3. Controller gọi `PaymentService.HandleSepayWebhookAsync(request, authorizationHeader)`.
4. `IsWebhookAuthorized()` kiểm tra webhook API key nếu có config.
5. Nếu không authorized, service trả unauthorized.
6. Service kiểm tra `TransferType`.
7. Nếu không phải tiền vào, service bỏ qua.
8. `ResolveIncomingPaymentCode()` lấy payment code từ `request.Code`.
9. Nếu `request.Code` rỗng, service tìm pattern `ODD\d{6}` trong `request.Content`.
10. `GetSepayOrderAsync(paymentCode)` tìm order theo payment code.
11. `ShouldIgnoreOrder()` bỏ qua order không tồn tại, không phải SePay, đã Paid.
12. `ApplyPaymentAsync()` mở transaction.
13. `IsTransactionProcessedAsync()` chống xử lý trùng transaction id.
14. `IsPaymentValid()` kiểm tra số tiền, tồn kho.
15. Nếu sai tiền hoặc thiếu stock, `MarkPaymentFailed()` cập nhật Failed.
16. Nếu hợp lệ, `CompletePaidOrder()` cập nhật Paid.
17. `CompletePaidOrder()` trừ stock.
18. `RemovePaidCartItemsAsync()` xóa cart item đã thanh toán.
19. `SaveChangesAsync()` lưu database.
20. Transaction commit.

Điểm vấn đáp: webhook là nguồn sự thật của thanh toán SePay, không phải app Android.

## Authorization webhook

Nếu có config:

```text
Sepay:WebhookApiKey
```

Backend yêu cầu header Authorization hợp lệ.

Nếu đang development mà không có key, backend cho phép webhook để test local.

## Resolve payment code

Backend lấy mã thanh toán theo thứ tự:

1. Dùng `request.Code` nếu có.
2. Tìm `ODD\d{6}` trong `request.Content`.

Ví dụ:

```text
ODD000007
```

## Khi nào service bỏ qua webhook?

Service trả success nhưng không xử lý nếu:

- Không phải tiền vào.
- Không tìm được payment code.
- Không tìm thấy order.
- Order không phải `SEPAY`.
- Order đã `Paid`.

Lý do vẫn trả success:

> Webhook cần response ổn định để SePay không retry vô hạn với giao dịch không liên quan.

## Khi nào service đánh dấu Failed?

Service đánh dấu `PaymentStatus = Failed` nếu:

- Transaction id chưa từng xử lý.
- Order tồn tại.
- Order là SePay.
- Order chưa Paid.
- Nhưng số tiền không bằng `order.TotalPrice` hoặc stock không đủ.

Khi failed, service vẫn lưu:

- `SepayTransactionId`.
- `SepayReferenceCode`.
- `UpdatedAt`.

## Khi nào service đánh dấu Paid?

Service đánh dấu `PaymentStatus = Paid` nếu:

- Transaction id chưa xử lý.
- `TransferAmount == order.TotalPrice`.
- Product còn đủ stock.

Sau đó service:

- Trừ tồn kho từng product.
- Lưu `SepayTransactionId`.
- Lưu `SepayReferenceCode`.
- Set `PaidAt`.
- Set `UpdatedAt`.
- Xóa cart item tương ứng của user.

## Vì sao cần transaction?

Webhook update nhiều bảng:

- `Orders`.
- `Products`.
- `CartItems`.

Nếu một bước lỗi mà không có transaction, database có thể bị lệch. Ví dụ order Paid nhưng stock chưa trừ.

## VietQR URL

VietQR URL được tạo trong `OrderService.BuildSepayQrUrl()`.

Format:

```text
https://img.vietqr.io/image/{bankCode}-{accountNumber}-{template}.png?amount={amount}&addInfo={paymentCode}
```

Config local:

```json
"Sepay": {
  "BankCode": "BIDV",
  "AccountNumber": "8894308684",
  "QrTemplate": "compact2"
}
```

## Câu trả lời mẫu

> Khi SePay báo tiền vào, backend nhận webhook ở `PaymentsController`, sau đó `PaymentService` kiểm tra key, tìm payment code, tìm order, chống xử lý trùng transaction, kiểm tra số tiền và tồn kho. Nếu hợp lệ thì service cập nhật Paid, trừ kho, xóa cart trong transaction. Android chỉ polling trạng thái order để biết khi nào đã Paid.
