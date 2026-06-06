# Android trace theo hành động đơn

File này là mục lục Android theo đúng flow đơn. Không dùng flow ghép. Chi tiết từng bước nằm ở `../shared/03-trace-chi-tiet-theo-chuc-nang.md`.

## Auth

- Flow 01: `MainNavGraph.kt:40-43` mở app vào login.
- Flow 02: `LoginScreen.kt:149-159` gõ email.
- Flow 03: `LoginScreen.kt:171-182` gõ password.
- Flow 04: `LoginScreen.kt:222-232` bấm Log in.
- Flow 05: `LoginScreen.kt:110-136` bấm Google.
- Flow 06: `LoginScreen.kt:244-249` bấm Sign Up.
- Flow 07: `RegisterScreen.kt:109-135` bấm Đăng ký.
- Flow 08: `MainNavGraph.kt:56-58` login USER.
- Flow 09: `MainNavGraph.kt:52-54` login ADMIN.

## Home

- Flow 10: `HomeScreen.kt:58` collect products.
- Flow 11: `HomeScreen.kt:59` collect categories.
- Flow 12: `HomeScreen.kt:134-147` bấm category.
- Flow 13: `HomeScreen.kt:85-93` hoặc `ProductScreen.kt:75-83` bấm product.

## Cart

- Flow 14: `ProductDetailScreen.kt:105-124` bấm Add to Cart.
- Flow 15: `HomeScreen.kt:118-120` bấm icon cart.
- Flow 16: `CartScreen.kt:169` bấm cộng.
- Flow 17: `CartScreen.kt:167` bấm trừ.
- Flow 18: `CartScreen.kt:173-184` bấm delete.

## Checkout

- Flow 19: `CartScreen.kt:91-94` bấm checkout.
- Flow 20: `CheckoutScreen.kt:118` chọn COD.
- Flow 21: `CheckoutScreen.kt:117` chọn SePay.
- Flow 22: `CheckoutScreen.kt:149-178` bấm Place Order COD.
- Flow 23: `CheckoutScreen.kt:149-178` bấm Place Order SePay.

## Payment

- Flow 24: `PaymentQrScreen.kt:64-80` mở QR.
- Flow 25: `PaymentQrScreen.kt:72-75` polling thấy Paid.

## Order

- Flow 27: `OrderScreen.kt:28-32` mở lịch sử đơn.

## Admin Product

- Flow 28: `DashboardScreen.kt:67-73` bấm card Sản phẩm.
- Flow 29: `ManageProductScreen.kt:60-66` bấm thêm sản phẩm.
- Flow 30: `AddProductScreen.kt:145-178` bấm Lưu sản phẩm.
- Flow 31: `ManageProductScreen.kt:192-198` bấm sửa sản phẩm.
- Flow 32: `UpdateProductScreen.kt:168-202` bấm Cập nhật sản phẩm.
- Flow 33: `ManageProductScreen.kt:200-206` bấm xóa sản phẩm.

## Admin Category

- Flow 34: `DashboardScreen.kt:75-81` bấm card Danh mục.
- Flow 35: `ManageCategoryScreen.kt:44-47` bấm thêm danh mục.
- Flow 36: `AddCategoryScreen.kt:65-69` bấm Lưu danh mục.
- Flow 37: `ManageCategoryScreen.kt:62-64` bấm sửa danh mục.
- Flow 38: `UpdateCategoryScreen.kt:71-80` bấm Cập nhật danh mục.
- Flow 39: `ManageCategoryScreen.kt:65-67` bấm xóa danh mục.

## Admin Order

- Flow 40: `DashboardScreen.kt:83-89` bấm card Đơn hàng.
- Flow 41: `ManageOrderScreen.kt:151-175` bấm đổi trạng thái.

## Admin User

- Flow 42: `DashboardScreen.kt:91-97` bấm card Người dùng.
- Flow 43: `ManageUserScreen.kt:60-62` bấm Tải lại.
- Flow 44: `ManageUserScreen.kt:146-148` bấm xóa user.

## Profile

- Flow 45: `MainNavGraph.kt:237` bấm xem đơn.
- Flow 46: `AuthViewModel.kt:61-65` bấm Logout.

## Security

- Flow 47: `AuthRepository.kt:66-68` lấy Bearer token.
- Flow 48: backend kiểm tra role admin.
