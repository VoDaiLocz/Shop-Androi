# Payment Order Admin Android theo hành động đơn

File này chỉ ghi các hành động payment, order, admin. Mỗi mục là một action riêng. Trace đầy đủ nằm ở `../shared/03-trace-chi-tiet-theo-chuc-nang.md`.

## Flow 24. Mở Payment QR

- App mở `PaymentQrScreen(orderId)` sau Flow 23.
- `PaymentQrScreen.kt:61-80` tạo state, lấy orderId, chạy polling.
- ViewModel gọi `OrderRepository.getPaymentStatus(orderId)`.
- Retrofit gọi `GET /api/orders/{id}/payment-status`.
- UI hiển thị QR từ `paymentQrUrl`.
- UI hiển thị amount.
- UI hiển thị payment code.

## Flow 25. Polling thấy Paid

- `PaymentQrScreen` đang polling mỗi 3 giây.
- Code kiểm tra nằm ở `PaymentQrScreen.kt:72-75`.
- ViewModel gọi lại payment status.
- Backend trả `paymentStatus = Paid`.
- App gọi `viewModel.refreshCart()`.
- App chạy callback `onPaid()`.
- App navigate sang OrderScreen.

## Flow 26. SePay gửi webhook

- Đây không phải action người dùng trong app.
- SePay server gọi backend.
- Endpoint là `POST /api/payments/sepay/webhook`.
- Android chỉ thấy kết quả gián tiếp qua Flow 25.
- Cần nhớ khi vấn đáp: Android không tự xác nhận đã thanh toán.

## Flow 27. Mở lịch sử đơn hàng

- Người dùng mở OrderScreen.
- `OrderScreen.kt:28-32` lấy current user.
- ViewModel gọi `OrderViewModel.getOrderHistory(user.id)`.
- Repository gọi `OrderRepository.getOrdersByUserId(...)`.
- Retrofit gọi `GET /api/orders/my`.
- UI hiển thị danh sách order.

## Flow 28. Bấm card Sản phẩm admin

- Admin bấm card Sản phẩm ở Dashboard.
- Code nằm ở `DashboardScreen.kt:67-73`.
- App navigate sang ManageProductScreen.
- Màn này collect danh sách product.
- Backend endpoint thường dùng là `GET /api/products`.

## Flow 29. Bấm thêm sản phẩm admin

- Admin bấm FAB thêm sản phẩm.
- Code nằm ở `ManageProductScreen.kt:60-66`.
- App navigate sang AddProductScreen.
- Chưa gọi backend ở bước bấm này.

## Flow 30. Bấm Lưu sản phẩm admin

- Admin nhập form rồi bấm Lưu.
- Code nằm ở `AddProductScreen.kt:145-178`.
- UI gọi `AdminProductViewModel.addProduct(...)`.
- ViewModel chạy `AdminProductViewModel.kt:38-69`.
- Repository gọi `ProductRepository.insertProduct(...)`.
- Retrofit gọi `POST /api/products`.
- Nếu có ảnh, repository gọi `POST /api/products/{id}/image`.
- Backend yêu cầu role ADMIN.

## Flow 31. Bấm sửa sản phẩm admin

- Admin bấm icon sửa trên product row.
- Code nằm ở `ManageProductScreen.kt:192-198`.
- App navigate sang UpdateProductScreen.
- `UpdateProductScreen.kt:47-60` load product cần sửa.

## Flow 32. Bấm Cập nhật sản phẩm admin

- Admin chỉnh form rồi bấm Cập nhật.
- Code nằm ở `UpdateProductScreen.kt:168-202`.
- UI gọi `AdminProductViewModel.updateProduct(...)`.
- ViewModel chạy `AdminProductViewModel.kt:71-99`.
- Repository gọi `ProductRepository.updateProduct(...)`.
- Retrofit gọi `PUT /api/products/{id}`.
- Nếu có ảnh mới, repository gọi upload image.
- Backend yêu cầu role ADMIN.

## Flow 33. Bấm xóa sản phẩm admin

- Admin bấm icon xóa product.
- Code nằm ở `ManageProductScreen.kt:200-206`.
- Callback delete nằm ở `ManageProductScreen.kt:140-146`.
- ViewModel chạy `AdminProductViewModel.kt:102-107`.
- Repository gọi `ProductRepository.deleteProduct(...)`.
- Retrofit gọi `DELETE /api/products/{id}`.
- Backend chặn xóa nếu product đang nằm trong order item hoặc cart item.

## Flow 34. Bấm card Danh mục admin

- Admin bấm card Danh mục ở Dashboard.
- Code nằm ở `DashboardScreen.kt:75-81`.
- App navigate sang ManageCategoryScreen.
- Màn này collect danh sách category.
- Backend endpoint thường dùng là `GET /api/categories`.

## Flow 35. Bấm thêm danh mục admin

- Admin bấm nút thêm danh mục.
- Code nằm ở `ManageCategoryScreen.kt:44-47`.
- App navigate sang AddCategoryScreen.
- Chưa gọi backend ở bước bấm này.

## Flow 36. Bấm Lưu danh mục admin

- Admin nhập tên danh mục rồi bấm Lưu.
- Code nằm ở `AddCategoryScreen.kt:65-69`.
- ViewModel chạy `AdminCategoryViewModel.kt:136-141`.
- Repository chạy `CategoryRepository.kt:164-177`.
- Retrofit gọi `POST /api/categories`.
- Backend yêu cầu role ADMIN.

## Flow 37. Bấm sửa danh mục admin

- Admin bấm icon sửa category.
- Code nằm ở `ManageCategoryScreen.kt:62-64`.
- App navigate sang UpdateCategoryScreen.
- `UpdateCategoryScreen.kt:27-35` load category cần sửa.

## Flow 38. Bấm Cập nhật danh mục admin

- Admin chỉnh tên category rồi bấm Cập nhật.
- Code nằm ở `UpdateCategoryScreen.kt:71-80`.
- ViewModel chạy `AdminCategoryViewModel.kt:144-150`.
- Repository chạy `CategoryRepository.kt:190-204`.
- Retrofit gọi `PUT /api/categories/{id}`.
- Backend yêu cầu role ADMIN.

## Flow 39. Bấm xóa danh mục admin

- Admin bấm icon xóa category.
- Code nằm ở `ManageCategoryScreen.kt:65-67`.
- ViewModel chạy `AdminCategoryViewModel.kt:153-157`.
- Repository chạy `CategoryRepository.kt:180-188`.
- Retrofit gọi `DELETE /api/categories/{id}`.
- Backend chặn xóa nếu category còn product.

## Flow 40. Bấm card Đơn hàng admin

- Admin bấm card Đơn hàng ở Dashboard.
- Code nằm ở `DashboardScreen.kt:83-89`.
- App navigate sang ManageOrderScreen.
- ViewModel gọi `AdminOrderViewModel.loadOrders()`.
- Repository gọi `OrderRepository.getALLOrders()`.
- Retrofit gọi `GET /api/orders`.
- Backend yêu cầu role ADMIN.

## Flow 41. Bấm đổi trạng thái đơn admin

- Admin bấm trạng thái trong ManageOrderScreen.
- UI nằm ở `ManageOrderScreen.kt:151-175`.
- ViewModel chạy `AdminOrderViewModel.kt:26-30`.
- Repository chạy `OrderRepository.kt:71-75`.
- Retrofit gọi `PUT /api/orders/{id}/status`.
- Backend kiểm tra status hợp lệ.
- Backend yêu cầu role ADMIN.

## Flow 42. Bấm card Người dùng admin

- Admin bấm card Người dùng ở Dashboard.
- Code nằm ở `DashboardScreen.kt:91-97`.
- App navigate sang ManageUserScreen.
- `ManageUserScreen.kt:46-48` collect users.
- ViewModel load user.
- Retrofit gọi `GET /api/users`.
- Backend yêu cầu role ADMIN.

## Flow 43. Bấm Tải lại user admin

- Admin bấm nút Tải lại.
- Code nằm ở `ManageUserScreen.kt:60-62`.
- ViewModel chạy `AdminUserViewModel.kt:30-44`.
- Repository chạy `UserRepository.kt:13-18`.
- Retrofit gọi `GET /api/users`.
- UI cập nhật StateFlow users.

## Flow 44. Bấm xóa user admin

- Admin bấm icon xóa user.
- Code nằm ở `ManageUserScreen.kt:146-148`.
- ViewModel chạy `AdminUserViewModel.kt:46-56`.
- Repository chạy `UserRepository.kt:20-26`.
- Retrofit gọi `DELETE /api/users/{id}`.
- Backend chặn xóa chính mình.
- Backend chặn xóa admin account.
