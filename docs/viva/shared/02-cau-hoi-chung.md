# Câu hỏi chung có thể bị hỏi

## 1. Dự án chia frontend/backend như thế nào?

Trả lời:

> Frontend là app Android viết bằng Kotlin và Jetpack Compose. Backend là ASP.NET Core Web API kết nối MySQL qua EF Core. Android gọi backend bằng Retrofit. Backend xử lý xác thực, dữ liệu, đặt hàng và thanh toán.

## 2. Vì sao Android có Repository?

Trả lời:

> Repository ở Android tách ViewModel khỏi Retrofit API. Nó lấy JWT token, gọi API, map DTO sang model và giữ state bằng StateFlow. Nếu không có repository thì ViewModel phải biết quá nhiều về API và mapping dữ liệu.

## 3. Vì sao backend không có Repository?

Trả lời:

> Backend dùng EF Core `DbContext`. `DbContext` và `DbSet` đã cung cấp data access cơ bản như query, add, remove, save, transaction. Nếu tạo repository chỉ để bọc lại DbContext thì bị thừa. Dự án tách Service cho nghiệp vụ phức tạp như order và payment.

## 4. JWT có phải dịch vụ tích hợp không?

Trả lời:

> Không. JWT là cơ chế xác thực nội bộ/chuẩn token để bảo vệ API. Dịch vụ tích hợp bên ngoài là Google OAuth và SePay/VietQR.

## 5. Google OAuth khác JWT như thế nào?

Trả lời:

> Google OAuth dùng để xác minh tài khoản Google và lấy Google ID token. Backend validate ID token đó với Google. Sau khi xác minh xong, backend cấp JWT của hệ thống. JWT được Android dùng để gọi các API nội bộ.

## 6. SePay khác VietQR như thế nào trong dự án?

Trả lời:

> VietQR là mã QR chuyển khoản hiển thị cho người dùng quét. SePay là dịch vụ gửi webhook về backend khi có giao dịch chuyển tiền. App dùng QR để người dùng thanh toán, backend dùng webhook SePay để xác nhận đơn đã thanh toán.

## 7. Vì sao SePay order không trừ kho ngay?

Trả lời:

> Vì người dùng mới tạo đơn và chưa chắc đã chuyển tiền. Nếu trừ kho ngay có thể giữ hàng sai. Dự án chỉ trừ kho và xóa cart khi webhook SePay xác nhận thanh toán thành công.

## 8. Vì sao COD trừ kho ngay?

Trả lời:

> COD không có webhook thanh toán online. Khi người dùng đặt COD, hệ thống xem như đơn đã được tạo để xử lý giao hàng nên trừ kho và xóa cart ngay.

## 9. Header Authorization dùng để làm gì?

Trả lời:

> Sau đăng nhập, Android lưu JWT token. Khi gọi API cần đăng nhập, Android gửi `Authorization: Bearer <token>`. Backend middleware kiểm tra token, lấy user id và role từ claims.

## 10. Role admin được kiểm tra ở đâu?

Trả lời:

> Backend kiểm tra bằng attribute `[Authorize(Roles = "ADMIN")]` trên các API quản trị. Android cũng điều hướng admin vào dashboard, nhưng bảo mật thật nằm ở backend.

## 11. Nếu Android tự sửa role thành ADMIN thì sao?

Trả lời:

> Không được, vì backend không tin role từ UI. Role nằm trong JWT do backend ký. Nếu token không hợp lệ hoặc không có role ADMIN, backend trả 401/403.

## 12. Nếu webhook SePay gửi trùng thì sao?

Trả lời:

> Backend lưu `SepayTransactionId` và kiểm tra transaction đã xử lý chưa. Nếu đã xử lý thì bỏ qua để tránh trừ kho hai lần.

## 13. Nếu số tiền chuyển không đúng thì sao?

Trả lời:

> `PaymentService` kiểm tra `TransferAmount == order.TotalPrice`. Nếu sai, order bị đánh dấu `PaymentStatus = Failed` để cần kiểm tra lại.

## 14. App load ảnh sản phẩm như thế nào?

Trả lời:

> Backend lưu `ImageUrl`, có thể là đường dẫn `/uploads/products/...`. Android dùng helper `Constants.toBackendImageUrl()` để biến đường dẫn tương đối thành URL backend đầy đủ rồi Coil/AsyncImage tải ảnh.

## 15. Khi vấn đáp bị hỏi phần không phải mình làm thì trả lời sao?

Trả lời:

> Em nắm flow tích hợp ở mức hệ thống. Phần em trực tiếp làm là backend/JWT/SePay, phần Android UI do bạn em phụ trách. Tuy nhiên flow end-to-end là Android gọi ViewModel, Repository, Retrofit đến API backend, backend xử lý và trả DTO cho Android cập nhật UI.
