# Auth Navigation Android theo hành động đơn

File này chỉ ghi các hành động auth/navigation. Mỗi mục là một hành động cụ thể từ UI hoặc một điểm rẽ navigation sau auth. Trace đầy đủ nằm ở `../shared/03-trace-chi-tiet-theo-chuc-nang.md`.

## Flow 01. Mở app

- Người dùng mở app.
- Code bắt đầu ở `MainActivity.kt`.
- Navigation được dựng trong `MainNavGraph.kt`.
- Start destination là login ở `MainNavGraph.kt:40-43`.
- Chưa gọi backend.
- Cần nhớ khi vấn đáp: app chưa có session restore đầy đủ nên màn đầu tiên là login.

## Flow 02. Gõ email đăng nhập

- Người dùng nhập email trong ô email.
- UI nằm ở `LoginScreen.kt:149-159`.
- `onValueChange` cập nhật biến `email`.
- Chưa gọi ViewModel.
- Chưa gọi backend.
- Cần nhớ khi vấn đáp: đây chỉ là cập nhật state Compose.

## Flow 03. Gõ password đăng nhập

- Người dùng nhập password trong ô password.
- UI nằm ở `LoginScreen.kt:171-182`.
- `onValueChange` cập nhật biến `password`.
- Chưa gọi ViewModel.
- Chưa gọi backend.
- Cần nhớ khi vấn đáp: password chỉ được gửi khi bấm Log in.

## Flow 04. Bấm Log in

- Người dùng bấm nút Log in ở `LoginScreen.kt:222-232`.
- `LoginScreen` gọi `AuthViewModel.login(email, password)`.
- ViewModel chạy `AuthViewModel.kt:24-35`.
- Repository gọi backend qua `AuthRepository.kt:32-36`.
- Retrofit endpoint là `AuthApi.kt:17-18`.
- Backend nhận `POST /api/auth/login`.
- Khi thành công, `AuthRepository.saveSession()` lưu user/token ở `AuthRepository.kt:70-74`.
- `MainNavGraph.kt:52-58` quyết định đi admin hay home theo role.

## Flow 05. Bấm Continue With Google

- Người dùng bấm Google button ở `LoginScreen.kt:110-136`.
- Android mở Google credential flow.
- Nếu lấy được idToken, UI gọi `AuthViewModel.loginWithGoogle(idToken)`.
- ViewModel chạy `AuthViewModel.kt:37-45`.
- Repository gọi `AuthRepository.kt:39-45`.
- Retrofit endpoint là `AuthApi.kt:20-21`.
- Backend nhận `POST /api/auth/google`.
- Backend mới validate token Google, Android không tự cấp JWT.
- Khi backend trả JWT nội bộ, app lưu session giống Flow 04.

## Flow 06. Bấm Sign Up

- Người dùng bấm Sign Up ở `LoginScreen.kt:244-249`.
- `navController.navigate(Routes.REGISTER)` được gọi.
- App chuyển sang `RegisterScreen`.
- Chưa gọi backend.
- Cần nhớ khi vấn đáp: đây chỉ là navigation.

## Flow 07. Bấm Đăng ký

- Người dùng nhập form register rồi bấm Đăng ký.
- UI gọi `AuthViewModel.register(...)`.
- ViewModel chạy `AuthViewModel.kt:48-58`.
- Repository gọi `AuthRepository.kt:48-58`.
- Retrofit endpoint là `AuthApi.kt:14-15`.
- Backend nhận `POST /api/auth/register`.
- Thành công thì quay về login để người dùng đăng nhập.

## Flow 08. Login thành công role USER

- Flow này xảy ra sau Flow 04 hoặc Flow 05.
- `MainNavGraph.kt:56-58` thấy role không phải ADMIN.
- App navigate về `Routes.HOME`.
- Back stack login bị pop để người dùng không quay lại màn login bằng nút Back.
- Cần nhớ khi vấn đáp: UI rẽ nhánh theo role chỉ để trải nghiệm, bảo mật thật vẫn ở backend.

## Flow 09. Login thành công role ADMIN

- Flow này xảy ra sau Flow 04 hoặc Flow 05.
- `MainNavGraph.kt:52-54` thấy role ADMIN.
- App navigate về `Routes.ADMIN_DASHBOARD`.
- Back stack login bị pop.
- Cần nhớ khi vấn đáp: admin route ở Android không thay thế `[Authorize(Roles = "ADMIN")]` của backend.

## Flow 45. Bấm xem đơn trong Profile

- Người dùng mở Profile rồi bấm xem đơn.
- Callback nằm ở `MainNavGraph.kt:237`.
- App navigate sang route order.
- Sau đó màn `OrderScreen` tự load đơn theo Flow 27.

## Flow 46. Bấm Logout

- Người dùng bấm Logout trong Profile.
- UI gọi `AuthViewModel.logout()`.
- ViewModel chạy `AuthViewModel.kt:61-65`.
- Repository chạy `AuthRepository.kt:61-64`.
- `currentUser` thành null.
- `currentToken` thành null.
- App quay về login.

## Flow 47. Repository cần token

- Repository cần gọi API yêu cầu đăng nhập.
- Repository gọi `AuthRepository.getAuthorizationHeader()`.
- Code nằm ở `AuthRepository.kt:66-68`.
- Header tạo ra có dạng `Authorization: Bearer <token>`.
- Nếu token null, repository không thể gọi API cần auth.
- Cần nhớ khi vấn đáp: Android chỉ gửi token, backend mới validate token.

## Flow 48. Backend kiểm tra role admin

- Android gọi endpoint admin bằng JWT.
- Backend đọc role claim từ JWT.
- Endpoint admin có `[Authorize(Roles = "ADMIN")]`.
- Nếu role không phải ADMIN, backend trả `403 Forbidden`.
- Cần nhớ khi vấn đáp: không tin role ở UI, chỉ tin role trong JWT đã được backend ký.
