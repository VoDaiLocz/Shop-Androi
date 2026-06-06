# Product Cart Checkout Android theo hành động đơn

File này chỉ ghi các hành động user ở home, product, cart, checkout. Mỗi mục là một action riêng. Trace đầy đủ nằm ở `../shared/03-trace-chi-tiet-theo-chuc-nang.md`.

## Flow 10. Home load sản phẩm

- Người dùng vào Home sau khi login USER.
- `HomeScreen.kt:58` collect `ProductViewModel.products`.
- ViewModel lấy dữ liệu từ `ProductRepository`.
- Repository gọi `ProductApi.getProducts()`.
- Retrofit gọi `GET /api/products`.
- UI render product grid khi StateFlow có dữ liệu.

## Flow 11. Home load danh mục

- Người dùng vào Home.
- `HomeScreen.kt:59` collect `UserCategoryViewModel.categories`.
- ViewModel lấy dữ liệu từ `CategoryRepository`.
- Repository gọi `CategoryApi.getCategories()`.
- Retrofit gọi `GET /api/categories`.
- UI render category chips khi StateFlow có dữ liệu.

## Flow 12. Bấm category chip

- Người dùng bấm một category chip trên Home.
- Code click nằm ở `HomeScreen.kt:142-147`.
- App navigate sang route product có `categoryId`.
- `ProductScreen.kt:28-37` nhận categoryId.
- Màn product lọc danh sách theo category.
- Backend không bắt buộc gọi lại vì app đã có danh sách product trong StateFlow.

## Flow 13. Bấm product card

- Người dùng bấm product card ở Home hoặc ProductScreen.
- Home click nằm ở `HomeScreen.kt:85-93`.
- ProductScreen click nằm ở `ProductScreen.kt:75-83`.
- App navigate sang `product_detail/{id}`.
- `ProductDetailScreen.kt:72-75` lấy product theo id.
- Nếu repository chưa có item, repository gọi `GET /api/products/{id}`.

## Flow 14. Bấm Add to Cart

- Người dùng bấm Add to Cart trong ProductDetail.
- Code nằm ở `ProductDetailScreen.kt:105-124`.
- UI gọi `CartViewModel.addToCart(...)`.
- ViewModel chạy `CartViewModel.kt:44-50`.
- Repository chạy `CartRepository.kt:41-53`.
- Repository lấy Bearer token từ `AuthRepository`.
- Retrofit gọi `POST /api/cart/items`.
- Backend trả cart mới.
- Repository cập nhật `_cartItems`.

## Flow 15. Bấm icon cart

- Người dùng bấm icon cart góc phải Home.
- Code nằm ở `HomeScreen.kt:118-120`.
- App navigate sang `Routes.CART`.
- `CartScreen.kt:64` collect cart items.
- Nếu repository refresh, backend endpoint là `GET /api/cart`.

## Flow 16. Bấm nút cộng cart

- Người dùng bấm nút cộng ở một dòng cart.
- Callback nằm ở `CartScreen.kt:169`.
- UI gọi `CartViewModel.increaseQuantity(item)`.
- ViewModel chạy `CartViewModel.kt:54-58`.
- Repository chạy `CartRepository.kt:55-67`.
- Retrofit gọi `PUT /api/cart/items/{id}`.
- Backend cập nhật quantity cho cart item thuộc user hiện tại.
- Repository cập nhật `_cartItems`.

## Flow 17. Bấm nút trừ cart

- Người dùng bấm nút trừ ở một dòng cart.
- Callback nằm ở `CartScreen.kt:167`.
- UI gọi `CartViewModel.decreaseQuantity(item)`.
- ViewModel chạy `CartViewModel.kt:61-68`.
- Nếu quantity lớn hơn 1, repository gọi `PUT /api/cart/items/{id}`.
- Nếu quantity bằng 1, ViewModel chuyển sang delete item theo Flow 18.

## Flow 18. Bấm icon delete cart

- Người dùng bấm icon thùng rác ở một dòng cart.
- Code nằm ở `CartScreen.kt:173-184`.
- UI gọi `CartViewModel.removeFromCart(item)`.
- ViewModel chạy `CartViewModel.kt:72-75`.
- Repository chạy `CartRepository.kt:70-77`.
- Retrofit gọi `DELETE /api/cart/items/{id}`.
- Backend chỉ xóa item thuộc user trong JWT.
- Repository cập nhật `_cartItems`.

## Flow 19. Bấm checkout

- Người dùng bấm checkout ở Cart.
- Code nằm ở `CartScreen.kt:91-94`.
- App navigate sang `Routes.CHECKOUT`.
- `CheckoutScreen.kt:60-67` đọc current user, cart items, loading state.
- Chưa gọi backend ở bước bấm này.

## Flow 20. Chọn COD

- Người dùng chọn payment method COD.
- Code nằm ở `CheckoutScreen.kt:118`.
- UI set `selectedPaymentMethod = "COD"`.
- Chưa gọi backend.
- Phương thức này chỉ được gửi khi bấm Place Order COD.

## Flow 21. Chọn SePay

- Người dùng chọn payment method SePay.
- Code nằm ở `CheckoutScreen.kt:117`.
- UI set `selectedPaymentMethod = "SEPAY"`.
- Chưa gọi backend.
- Phương thức này chỉ được gửi khi bấm Place Order SePay.

## Flow 22. Bấm Place Order COD

- Người dùng đang chọn COD rồi bấm Place Order.
- Code nằm ở `CheckoutScreen.kt:149-178`.
- UI gọi `OrderViewModel.placeOrder(...)`.
- ViewModel chạy `OrderViewModel.kt:51-80`.
- Repository chạy `OrderRepository.kt:33-49`.
- Retrofit gọi `POST /api/orders`.
- Backend tạo order COD.
- Backend trừ kho ngay.
- Backend xóa cart ngay.
- App navigate sang lịch sử đơn hàng.

## Flow 23. Bấm Place Order SePay

- Người dùng đang chọn SePay rồi bấm Place Order.
- Code nằm ở `CheckoutScreen.kt:149-178`.
- UI gọi `OrderViewModel.placeOrder(...)`.
- ViewModel chạy `OrderViewModel.kt:51-80`.
- Repository chạy `OrderRepository.kt:33-49`.
- Retrofit gọi `POST /api/orders`.
- Backend tạo order pending.
- Backend sinh `PaymentCode`.
- Backend sinh `PaymentQrUrl`.
- Backend chưa trừ kho.
- Backend chưa xóa cart.
- App navigate sang Payment QR.
