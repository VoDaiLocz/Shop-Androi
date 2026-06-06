# Tài liệu nội bộ ôn vấn đáp

Tài liệu này dùng để học và trả lời vấn đáp cho dự án Android bán nội thất. Đây không phải báo cáo nộp, nên nội dung viết thẳng vào flow, code path, endpoint, database và câu hỏi có thể bị hỏi.

## Cách học

1. Đọc `shared/00-ban-do-he-thong.md` để nắm toàn bộ hệ thống.
2. Đọc `shared/03-trace-chi-tiet-theo-chuc-nang.md` trước để học theo đúng kiểu vấn đáp: người dùng bấm gì, dòng nào nhận event, ViewModel/Repository/API/backend/database đi tiếp ra sao.
3. Đọc `shared/01-flow-end-to-end.md` để có bản nhìn tổng quan sau khi đã hiểu trace chi tiết.
4. Người phụ trách backend đọc toàn bộ thư mục `backend/`, đặc biệt `backend/08-endpoint-trace.md` để nhớ endpoint, quyền và bảng dữ liệu.
5. Người phụ trách Android đọc toàn bộ thư mục `android/`, đặc biệt `android/05-screen-viewmodel-repository-trace.md` để nhớ Screen -> ViewModel -> Repository -> API.
6. Người làm tích hợp đọc `integrations/01-tich-hop-google-oauth.md` và `integrations/02-tich-hop-sepay-vietqr.md` để nhớ các bước đưa dịch vụ ngoài vào dự án.
7. Cả hai cùng đọc `shared/02-cau-hoi-chung.md` để tránh bị hỏi chéo.

## Phân vai đúng khi vấn đáp

Người phụ trách backend:

- Backend API ASP.NET Core.
- Database MySQL và EF Core.
- JWT authentication.
- SePay/VietQR payment flow.
- API cho sản phẩm, danh mục, giỏ hàng, đơn hàng, admin.

Người phụ trách Android:

- Android app Kotlin + Jetpack Compose.
- MVVM, ViewModel, Repository Android.
- Retrofit gọi API.
- Navigation và UI flow.
- Google OAuth ở phía Android để lấy Google ID token rồi gửi backend xác thực.

## JWT có phải dịch vụ tích hợp không?

Không nên gọi JWT là "dịch vụ tích hợp" vì JWT không phải dịch vụ bên thứ ba. JWT là cơ chế/token chuẩn để backend xác thực và phân quyền request sau khi người dùng đăng nhập.

Ghi đúng:

- Dịch vụ tích hợp bên ngoài: Google Authentication/OAuth, SePay/VietQR.
- Cơ chế bảo mật nội bộ: JWT Authentication, role-based authorization.
