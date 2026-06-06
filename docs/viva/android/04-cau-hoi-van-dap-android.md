# Câu hỏi vấn đáp Android

## 1. App dùng kiến trúc gì?

Trả lời:

> App dùng MVVM. Compose Screen hiển thị UI, ViewModel xử lý event và giữ state cho UI, Repository gọi Retrofit API và map dữ liệu từ backend.

## 2. Retrofit dùng để làm gì?

Trả lời:

> Retrofit định nghĩa các API interface như `AuthApi`, `ProductApi`, `CartApi`, `OrderApi`. Repository gọi các interface này để gửi HTTP request đến backend.

## 3. Hilt dùng để làm gì?

Trả lời:

> Hilt dùng dependency injection. AppModule cung cấp Retrofit, OkHttpClient và API interface. Repository và ViewModel nhận dependency qua constructor injection, giảm việc tự khởi tạo thủ công.

## 4. StateFlow dùng để làm gì?

Trả lời:

> StateFlow giữ state hiện tại như current user, token, products, cart items, orders. Compose dùng `collectAsState()` để UI tự cập nhật khi state thay đổi.

## 5. Vì sao không gọi API trực tiếp trong Screen?

Trả lời:

> Screen chỉ nên lo UI. Nếu gọi API trong Screen thì UI bị lẫn logic dữ liệu. Dự án tách qua ViewModel và Repository để code dễ đọc, dễ test và đúng kiến trúc MVVM.

## 6. Token được gửi lên backend ở đâu?

Trả lời:

> Repository gọi `authRepository.getAuthorizationHeader()` để lấy chuỗi `Bearer <token>`, sau đó truyền vào Retrofit API qua `@Header("Authorization")`.

## 7. Khi login thành công app điều hướng thế nào?

Trả lời:

> LoginScreen gọi `onLoginSuccess(user)`. Nếu role là ADMIN thì navigate sang Admin Dashboard, ngược lại sang Home.

## 8. Payment QR tự biết đã thanh toán bằng cách nào?

Trả lời:

> `PaymentQrScreen` gọi API payment status mỗi 3 giây. Khi backend trả `paymentStatus = Paid`, app refresh cart và chuyển sang màn hình lịch sử đơn hàng.

## 9. Admin app bảo mật bằng UI có đủ không?

Trả lời:

> Không. UI chỉ điều hướng theo role để trải nghiệm đúng. Bảo mật thật nằm ở backend qua JWT và `[Authorize(Roles = "ADMIN")]`.

## 10. Nếu backend lỗi thì app xử lý ra sao?

Trả lời:

> Repository thường dùng `runCatching`, nếu lỗi thì không crash app và có thể trả null/false hoặc giữ danh sách rỗng. Một số flow như Google login đọc message lỗi từ backend để hiển thị rõ hơn.
