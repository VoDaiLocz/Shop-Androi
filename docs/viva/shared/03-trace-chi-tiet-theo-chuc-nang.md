# Trace theo hành động đơn

File này là bản học chính khi vấn đáp. Mỗi flow bên dưới chỉ mô tả một hành động cụ thể. Không gom nhiều thao tác vào cùng một flow.

Chuỗi cần đọc theo một hướng:

```text
Hành động
-> dòng nhận event
-> callback/state
-> ViewModel
-> Repository
-> Retrofit API
-> Controller
-> Service nếu có
-> DbContext
-> bảng dữ liệu
-> response
-> UI cập nhật
```

## Flow 01. Mở app

1. Người dùng mở app.
2. `MainNavGraph` tạo `NavHost` tại `app/src/main/java/com/example/shop/navigation/MainNavGraph.kt:40`.
3. `startDestination = Routes.LOGIN` nằm ở `MainNavGraph.kt:42`.
4. Compose đi vào route login ở `MainNavGraph.kt:46`.
5. Hilt lấy `AuthViewModel` tại `MainNavGraph.kt:47`.
6. `LoginScreen` được render tại `MainNavGraph.kt:48-64`.
7. `LoginScreen` tạo state local tại `app/src/main/java/com/example/shop/ui/auth/LoginScreen.kt:63-70`.
8. Chưa gọi backend ở flow này.

Trả lời vấn đáp:

> App mở lên sẽ vào `MainNavGraph`. Start destination là `Routes.LOGIN`, nên `LoginScreen` render trước. Lúc này app chỉ tạo state UI, chưa gọi API.

## Flow 02. Gõ email đăng nhập

1. Người dùng gõ email ở màn login.
2. Ô email nằm tại `app/src/main/java/com/example/shop/ui/auth/LoginScreen.kt:149-159`.
3. `onValueChange = { email = it }` ở `LoginScreen.kt:151` nhận chuỗi mới.
4. State `email` đổi.
5. Compose recompose ô email.
6. Không gọi ViewModel.
7. Không gọi backend.

Trả lời vấn đáp:

> Gõ email chỉ cập nhật state local `email` trong `LoginScreen`. API login chỉ chạy khi bấm nút `Log in`.

## Flow 03. Gõ password đăng nhập

1. Người dùng gõ password ở màn login.
2. Ô password nằm tại `app/src/main/java/com/example/shop/ui/auth/LoginScreen.kt:171-182`.
3. `onValueChange = { password = it }` ở `LoginScreen.kt:173` nhận chuỗi mới.
4. State `password` đổi.
5. Compose recompose ô password.
6. Không gọi ViewModel.
7. Không gọi backend.

Trả lời vấn đáp:

> Gõ password chỉ đổi state local `password`. Backend chưa nhận password ở bước này.

## Flow 04. Bấm Log in

1. Người dùng bấm nút `Log in`.
2. Nút nằm tại `app/src/main/java/com/example/shop/ui/auth/LoginScreen.kt:222-240`.
3. `onClick` ở `LoginScreen.kt:223-232` chạy.
4. `LoginScreen.kt:224` gọi `viewModel.login(email, password)`.
5. Code vào `AuthViewModel.login` tại `app/src/main/java/com/example/shop/viewmodel/AuthViewModel.kt:24-35`.
6. `AuthViewModel.kt:25` mở coroutine bằng `viewModelScope.launch`.
7. `AuthViewModel.kt:27` gọi `authRepository.login(email, password)`.
8. Code vào `AuthRepository.login` tại `app/src/main/java/com/example/shop/data/repository/AuthRepository.kt:32-36`.
9. `AuthRepository.kt:34` tạo `LoginRequest(email.trim(), password)`.
10. `AuthRepository.kt:34` gọi `authApi.login(...)`.
11. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/AuthApi.kt:17-18`.
12. Request đi tới `POST /api/auth/login`.
13. Backend nhận request ở `backend/ShopApi/Controllers/AuthController.cs:62-80`.
14. `AuthController.cs:65` chuẩn hóa email.
15. `AuthController.cs:66` đọc bảng `Users` theo email.
16. Nếu không có user, `AuthController.cs:67-70` trả `401`.
17. `AuthController.cs:72` verify password hash.
18. Nếu password sai, `AuthController.cs:73-76` trả `401`.
19. Nếu đúng, `AuthController.cs:78` gọi `CreateToken(user)`.
20. `AuthController.cs:176-182` đưa id, username, email, role vào JWT claims.
21. `AuthController.cs:184-193` ký JWT.
22. `AuthController.cs:79` trả `LoginResponse(token, user)`.
23. Android nhận response trong `AuthRepository.login`.
24. `AuthRepository.kt:35` gọi `saveSession`.
25. `AuthRepository.kt:70-74` lưu `_currentToken`, `_currentUser`.
26. `AuthViewModel.kt:28-30` set `_isLoggedIn = true`, callback user.
27. `LoginScreen.kt:226-227` xóa lỗi, gọi `onLoginSuccess(user)`.
28. `MainNavGraph.kt:50-58` điều hướng theo role.
29. Nếu user role `ADMIN`, đi dashboard ở `MainNavGraph.kt:52-54`.
30. Nếu user role thường, đi home ở `MainNavGraph.kt:56-58`.
31. Nếu login fail, `LoginScreen.kt:228-230` set lỗi.

Bảng dữ liệu:

- Đọc `Users`.
- Không ghi bảng.

Trả lời vấn đáp:

> Bấm `Log in` gọi `AuthViewModel.login`, Repository gọi `POST /api/auth/login`. Backend đọc `Users`, verify password hash, tạo JWT có role, trả token. Android lưu token trong `AuthRepository`, sau đó `MainNavGraph` điều hướng theo role.

## Flow 05. Bấm Continue With Google

1. Người dùng bấm `Continue With Google`.
2. Nút nằm tại `app/src/main/java/com/example/shop/ui/auth/LoginScreen.kt:110-136`.
3. `LoginScreen.kt:115` nhận click.
4. `LoginScreen.kt:116` mở coroutine.
5. `LoginScreen.kt:117` set `isGoogleLoading = true`.
6. `LoginScreen.kt:118` xóa lỗi cũ.
7. `LoginScreen.kt:120` gọi `getGoogleIdToken(credentialManager, context)`.
8. `CredentialManager` được tạo ở `LoginScreen.kt:65`.
9. Nếu id token null, `LoginScreen.kt:121-124` báo lỗi, dừng flow.
10. Nếu có id token, `LoginScreen.kt:127` gọi `viewModel.loginWithGoogle(idToken)`.
11. Code vào `AuthViewModel.loginWithGoogle` tại `app/src/main/java/com/example/shop/viewmodel/AuthViewModel.kt:37-45`.
12. `AuthViewModel.kt:39` gọi `authRepository.loginWithGoogle(idToken)`.
13. Code vào `AuthRepository.loginWithGoogle` tại `app/src/main/java/com/example/shop/data/repository/AuthRepository.kt:39-45`.
14. `AuthRepository.kt:41` gọi `authApi.googleLogin(GoogleLoginRequest(idToken))`.
15. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/AuthApi.kt:20-21`.
16. Request đi tới `POST /api/auth/google`.
17. Backend nhận request ở `backend/ShopApi/Controllers/AuthController.cs:82-143`.
18. `AuthController.cs:85-89` đọc `Google:ClientId`.
19. `AuthController.cs:91-100` validate id token bằng Google.
20. Nếu token sai, `AuthController.cs:101-104` trả `401`.
21. `AuthController.cs:106-109` kiểm tra email Google đã verified.
22. `AuthController.cs:111` chuẩn hóa email.
23. `AuthController.cs:112` tìm `Users` theo `GoogleSub`.
24. Nếu chưa có, `AuthController.cs:115` tìm `Users` theo email.
25. Nếu email mới, `AuthController.cs:118-128` tạo user role `USER`.
26. Nếu email cũ chưa link Google, `AuthController.cs:129-132` gán `GoogleSub`.
27. Nếu email đã link Google khác, `AuthController.cs:133-136` trả `409`.
28. `AuthController.cs:138` lưu thay đổi nếu có.
29. `AuthController.cs:141` tạo JWT nội bộ.
30. `AuthController.cs:142` trả token kèm user.
31. `AuthRepository.kt:42` lưu session.
32. `AuthViewModel.kt:41-44` callback user hoặc lỗi.
33. `LoginScreen.kt:128-133` tắt loading, điều hướng hoặc hiển thị lỗi.

Bảng dữ liệu:

- Đọc `Users`.
- Có thể ghi `Users`.

Trả lời vấn đáp:

> Android chỉ lấy Google id token. Backend mới validate token đó với Google client id, rồi tìm hoặc tạo user nội bộ, cuối cùng cấp JWT của hệ thống.

## Flow 06. Bấm Sign Up

1. Người dùng bấm dòng chuyển sang đăng ký.
2. Nút nằm tại `app/src/main/java/com/example/shop/ui/auth/LoginScreen.kt:244-249`.
3. `LoginScreen.kt:245` gọi `onNavigateToRegister`.
4. Callback được truyền tại `app/src/main/java/com/example/shop/navigation/MainNavGraph.kt:61-63`.
5. `MainNavGraph.kt:62` navigate `Routes.REGISTER`.
6. Route register nằm tại `MainNavGraph.kt:67-73`.
7. `RegisterScreen` render.
8. Flow này chỉ đổi màn hình.
9. Không gọi backend.

Trả lời vấn đáp:

> Bấm Sign Up chỉ navigate từ login sang register. API đăng ký chỉ chạy khi bấm nút `Đăng ký`.

## Flow 07. Bấm Đăng ký

1. Người dùng nhập form đăng ký.
2. State form nằm tại `app/src/main/java/com/example/shop/ui/auth/RegisterScreen.kt:26-35`.
3. Nút `Đăng ký` nằm tại `RegisterScreen.kt:109-135`.
4. `RegisterScreen.kt:111-120` validate rỗng, confirm password, độ dài password.
5. Nếu lỗi, `errorMessage` đổi.
6. Nếu hợp lệ, `RegisterScreen.kt:122` gọi `viewModel.register(email, password, name)`.
7. Code vào `AuthViewModel.register` tại `app/src/main/java/com/example/shop/viewmodel/AuthViewModel.kt:48-58`.
8. `AuthViewModel.kt:50-55` tạo `User` role `USER`.
9. `AuthViewModel.kt:56` gọi `authRepository.register(newUser)`.
10. Code vào `AuthRepository.register` tại `app/src/main/java/com/example/shop/data/repository/AuthRepository.kt:48-58`.
11. `AuthRepository.kt:50-55` tạo `RegisterRequest`.
12. `AuthRepository.kt:50` gọi `authApi.register`.
13. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/AuthApi.kt:14-15`.
14. Request đi tới `POST /api/auth/register`.
15. Backend nhận request ở `backend/ShopApi/Controllers/AuthController.cs:30-60`.
16. `AuthController.cs:33-37` trim, validate username.
17. `AuthController.cs:39-44` chuẩn hóa email, kiểm tra trùng.
18. `AuthController.cs:46-52` tạo user role `USER`.
19. `AuthController.cs:54` hash password.
20. `AuthController.cs:56-57` add user, save database.
21. `AuthController.cs:59` trả `201`.
22. `RegisterScreen.kt:123-124` gọi `onRegisterSuccess` nếu thành công.
23. `MainNavGraph.kt:71` pop về login.

Bảng dữ liệu:

- Đọc `Users`.
- Ghi `Users`.

Trả lời vấn đáp:

> Bấm `Đăng ký` gọi `AuthViewModel.register`, Repository gửi `POST /api/auth/register`. Backend kiểm tra email trùng, hash password, lưu user role `USER`, trả 201, app quay lại login.

## Flow 08. Login thành công role USER

1. Login thường hoặc Google trả user.
2. `LoginScreen.kt:227` hoặc `LoginScreen.kt:130` gọi `onLoginSuccess(user)`.
3. Callback nằm tại `app/src/main/java/com/example/shop/navigation/MainNavGraph.kt:50-60`.
4. `MainNavGraph.kt:51` kiểm tra role.
5. Role không phải `ADMIN`.
6. `MainNavGraph.kt:56-58` navigate `Routes.HOME`.
7. Login bị pop khỏi back stack.
8. User thấy Home.

Trả lời vấn đáp:

> Role USER được điều hướng sang Home ở `MainNavGraph`, nhưng quyền API vẫn dựa vào JWT backend.

## Flow 09. Login thành công role ADMIN

1. Login thường hoặc Google trả user.
2. `LoginScreen` gọi `onLoginSuccess(user)`.
3. `MainNavGraph.kt:51` kiểm tra role.
4. Role là `ADMIN`.
5. `MainNavGraph.kt:52-54` navigate `Routes.ADMIN_DASHBOARD`.
6. Route dashboard render tại `MainNavGraph.kt:77-88`.
7. `DashboardScreen` hiển thị card quản trị tại `app/src/main/java/com/example/shop/admin/ui/dashboard/DashboardScreen.kt:67-98`.

Trả lời vấn đáp:

> Android dùng role để điều hướng admin. Backend vẫn kiểm tra role bằng `[Authorize(Roles = "ADMIN")]`.

## Flow 10. Home load sản phẩm

1. User vào Home.
2. Route Home render tại `app/src/main/java/com/example/shop/navigation/MainNavGraph.kt:159-164`.
3. `HomeScreen` inject `ProductViewModel` tại `app/src/main/java/com/example/shop/ui/home/HomeScreen.kt:55`.
4. `HomeScreen.kt:58` collect `productViewModel.products`.
5. `ProductViewModel` init tại `app/src/main/java/com/example/shop/viewmodel/ProductViewModel.kt:33-38`.
6. `ProductViewModel.kt:35` collect `repo.getAllProducts()`.
7. `ProductRepository.getAllProducts` nằm tại `app/src/main/java/com/example/shop/data/repository/ProductRepository.kt:34-37`.
8. `ProductRepository.kt:35` gọi `refreshProducts()`.
9. `ProductRepository.kt:98` gọi `productApi.getProducts()`.
10. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/ProductApi.kt:19-20`.
11. Request đi tới `GET /api/products`.
12. Backend nhận request ở `backend/ShopApi/Controllers/ProductsController.cs:32-51`.
13. `ProductsController.cs:35-38` đọc `Products`, include `Category`.
14. `ProductsController.cs:45-48` order, map response.
15. `ProductsController.cs:50` trả list.
16. `ProductRepository.kt:100` map DTO sang `Product`.
17. StateFlow đổi.
18. `HomeScreen.kt:85-94` render product cards.

Bảng dữ liệu:

- Đọc `Products`.
- Đọc `Categories` qua include.

Trả lời vấn đáp:

> Home collect products từ ViewModel. Repository gọi `GET /api/products`, backend đọc Products kèm Category, trả list, UI recompose grid.

## Flow 11. Home load danh mục

1. User vào Home.
2. `HomeScreen` inject `UserCategoryViewModel` tại `app/src/main/java/com/example/shop/ui/home/HomeScreen.kt:56`.
3. `HomeScreen.kt:59` collect categories.
4. `UserCategoryViewModel.categories` nằm tại `app/src/main/java/com/example/shop/viewmodel/UserCategoryViewModel.kt:58-63`.
5. ViewModel lấy flow từ `CategoryRepository.getAllCategories()`.
6. `CategoryRepository.getAllCategories` nằm tại `app/src/main/java/com/example/shop/data/repository/CategoryRepository.kt:154-157`.
7. `CategoryRepository.kt:155` gọi `refreshCategories()`.
8. `CategoryRepository.kt:209` gọi `categoryApi.getCategories()`.
9. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/CategoryApi.kt:15-16`.
10. Request đi tới `GET /api/categories`.
11. Backend nhận request ở `backend/ShopApi/Controllers/CategoriesController.cs:21-31`.
12. `CategoriesController.cs:24-28` đọc `Categories`, order theo name, map response.
13. `CategoriesController.cs:30` trả list.
14. `CategoryRepository.kt:211` map DTO, set StateFlow.
15. `HomeScreen.kt:74-79` render tabs.

Bảng dữ liệu:

- Đọc `Categories`.

Trả lời vấn đáp:

> Home category tabs lấy dữ liệu từ `UserCategoryViewModel`, Repository gọi `GET /api/categories`, backend đọc bảng Categories rồi trả list.

## Flow 12. Bấm category chip

1. Người dùng bấm chip category ở Home.
2. Chip `ALL` nằm tại `app/src/main/java/com/example/shop/ui/home/HomeScreen.kt:134-139`.
3. Chip category thật nằm tại `HomeScreen.kt:142-147`.
4. `CategoryChip` nhận click tại `HomeScreen.kt:178-180`.
5. Callback `onOpenCategory(category.id)` chạy.
6. `MainNavGraph.kt:162` navigate `"${Routes.PRODUCT}/$id"`.
7. Route Product nằm tại `app/src/main/java/com/example/shop/navigation/MainNavGraph.kt:167-174`.
8. `MainNavGraph.kt:168` đọc `categoryId`.
9. `ProductScreen` render.
10. `ProductScreen.kt:28` collect products.
11. `ProductScreen.kt:31-37` lọc local theo categoryId.
12. UI render list lọc tại `ProductScreen.kt:66-85`.

Bảng dữ liệu:

- Thường không đọc mới.
- Dữ liệu lấy từ products đã load.

Trả lời vấn đáp:

> Bấm category chỉ navigate sang ProductScreen kèm categoryId. ProductScreen lọc local từ products đã có.

## Flow 13. Bấm product card

1. Người dùng bấm product card.
2. Ở Home, click nằm tại `app/src/main/java/com/example/shop/ui/home/HomeScreen.kt:85-93`.
3. Ở ProductScreen, click nằm tại `app/src/main/java/com/example/shop/ui/product/ProductScreen.kt:75-83`.
4. Callback navigate sang `product_detail/{id}`.
5. Route detail nằm tại `app/src/main/java/com/example/shop/navigation/MainNavGraph.kt:178-186`.
6. `ProductDetailScreen` nhận `productId`.
7. `ProductDetailScreen.kt:72` convert id.
8. `ProductDetailScreen.kt:73` gọi `viewModel.getProductById(id).collectAsState`.
9. `AdminProductViewModel.kt:32-35` gọi repository.
10. `ProductRepository.kt:42-46` gọi API.
11. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/ProductApi.kt:22-23`.
12. Request đi tới `GET /api/products/{id}`.
13. Backend nhận request ở `backend/ShopApi/Controllers/ProductsController.cs:53-67`.
14. `ProductsController.cs:56-59` đọc `Products`, include `Category`.
15. Nếu không có product, `ProductsController.cs:61-64` trả 404.
16. Nếu có product, `ProductsController.cs:66` trả response.
17. UI render detail tại `ProductDetailScreen.kt:142-199`.

Bảng dữ liệu:

- Đọc `Products`.
- Đọc `Categories` qua include.

Trả lời vấn đáp:

> Bấm product navigate sang detail, detail gọi `GET /api/products/{id}`, backend đọc product kèm category, UI render dữ liệu.

## Flow 14. Bấm Add to Cart

1. Người dùng bấm add cart ở detail.
2. Click nằm tại `app/src/main/java/com/example/shop/ui/product/ProductDetailScreen.kt:105-124`.
3. `ProductDetailScreen.kt:106` lấy current user.
4. Nếu user null, `ProductDetailScreen.kt:107-110` set lỗi.
5. Nếu có user, `ProductDetailScreen.kt:112-120` tạo `CartItem`.
6. `ProductDetailScreen.kt:112` gọi `cartViewModel.addToCart`.
7. `CartViewModel.addToCart` nằm tại `app/src/main/java/com/example/shop/viewmodel/CartViewModel.kt:44-50`.
8. `CartViewModel.kt:46` lấy current user từ AuthRepository.
9. `CartViewModel.kt:48` gọi `repository.addToCart`.
10. `CartRepository.addToCart` nằm tại `app/src/main/java/com/example/shop/data/repository/CartRepository.kt:41-53`.
11. `CartRepository.kt:42` lấy bearer token.
12. `CartRepository.kt:46-49` gọi `cartApi.addItem`.
13. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/CartApi.kt:18-22`.
14. Request đi tới `POST /api/cart/items`.
15. Backend nhận request ở `backend/ShopApi/Controllers/CartController.cs:35-75`.
16. `CartController.cs:38` lấy user id từ JWT.
17. `CartController.cs:44-47` validate quantity.
18. `CartController.cs:49-53` kiểm tra product.
19. `CartController.cs:55-56` tìm cart item theo user id, product id.
20. Nếu chưa có, `CartController.cs:58-65` thêm dòng cart.
21. Nếu đã có, `CartController.cs:67-70` cộng quantity.
22. `CartController.cs:73` save database.
23. `CartController.cs:74` trả cart mới.
24. `CartRepository.kt:50-51` cập nhật `_cartItems`.
25. `ProductDetailScreen.kt:123` gọi `onAddToCart`.
26. `MainNavGraph.kt:182-184` navigate cart.

Bảng dữ liệu:

- Đọc `Products`.
- Đọc `CartItems`.
- Ghi `CartItems`.

Trả lời vấn đáp:

> Bấm Add to Cart gọi CartViewModel, Repository gắn Bearer token, backend lấy user id từ JWT, thêm hoặc cộng quantity trong CartItems, trả cart mới.

## Flow 15. Bấm icon cart

1. Người dùng bấm icon cart ở Home.
2. Icon nằm tại `app/src/main/java/com/example/shop/ui/home/HomeScreen.kt:118-120`.
3. Callback `onOpenCart` chạy.
4. `MainNavGraph.kt:163` navigate `Routes.CART`.
5. Route cart nằm tại `app/src/main/java/com/example/shop/navigation/MainNavGraph.kt:190-194`.
6. `CartScreen` render.
7. `CartScreen.kt:64` collect items.
8. `CartViewModel.cartItems` nằm tại `app/src/main/java/com/example/shop/viewmodel/CartViewModel.kt:21-37`.
9. Khi có current user, `CartViewModel.kt:25-28` gọi `repository.refreshCart`.
10. `CartRepository.refreshCart` nằm tại `app/src/main/java/com/example/shop/data/repository/CartRepository.kt:25-38`.
11. `CartRepository.kt:33` gọi `cartApi.getCart`.
12. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/CartApi.kt:15-16`.
13. Request đi tới `GET /api/cart`.
14. Backend nhận request ở `backend/ShopApi/Controllers/CartController.cs:23-33`.
15. `CartController.cs:26` lấy user id từ JWT.
16. `CartController.cs:32` trả `GetCartResponse`.
17. `CartController.cs:151-168` đọc `CartItems`, include `Product`, tính total.
18. `CartRepository.kt:35` cập nhật `_cartItems`.
19. `CartScreen.kt:97-126` render empty hoặc list.

Bảng dữ liệu:

- Đọc `CartItems`.
- Đọc `Products`.

Trả lời vấn đáp:

> Bấm cart mở CartScreen. ViewModel refresh cart, Repository gọi `GET /api/cart`, backend lấy user id từ JWT, trả cart của user.

## Flow 16. Bấm nút cộng cart

1. Người dùng bấm `+` trong CartScreen.
2. Nút nằm tại `app/src/main/java/com/example/shop/ui/cart/CartScreen.kt:169`.
3. `CartScreen.kt:121` gọi `viewModel.increaseQuantity(item)`.
4. `CartViewModel.increaseQuantity` nằm tại `app/src/main/java/com/example/shop/viewmodel/CartViewModel.kt:54-58`.
5. `CartViewModel.kt:56` gọi `repository.updateQuantity(item.copy(quantity = item.quantity + 1))`.
6. `CartRepository.updateQuantity` nằm tại `app/src/main/java/com/example/shop/data/repository/CartRepository.kt:55-67`.
7. `CartRepository.kt:60-64` gọi `cartApi.updateItem`.
8. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/CartApi.kt:24-29`.
9. Request đi tới `PUT /api/cart/items/{id}`.
10. Backend nhận request ở `backend/ShopApi/Controllers/CartController.cs:77-104`.
11. Backend lấy user id từ JWT.
12. Backend tìm cart item theo id kèm user id.
13. Backend update quantity.
14. Backend trả cart mới.
15. Repository cập nhật StateFlow.
16. UI recompose total.

Bảng dữ liệu:

- Đọc `CartItems`.
- Ghi `CartItems`.

Trả lời vấn đáp:

> Bấm `+` gọi update quantity tăng 1. Backend chỉ sửa cart item thuộc user trong JWT.

## Flow 17. Bấm nút trừ cart

1. Người dùng bấm `-` trong CartScreen.
2. Nút nằm tại `app/src/main/java/com/example/shop/ui/cart/CartScreen.kt:167`.
3. `CartScreen.kt:122` gọi `viewModel.decreaseQuantity(item)`.
4. `CartViewModel.decreaseQuantity` nằm tại `app/src/main/java/com/example/shop/viewmodel/CartViewModel.kt:61-68`.
5. Nếu quantity > 1, `CartViewModel.kt:63-64` gọi update quantity giảm 1.
6. Nếu quantity == 1, `CartViewModel.kt:65-66` gọi `repository.deleteItem(item)`.
7. Với update, flow đi tiếp như Flow 16.
8. Với delete, flow đi tiếp như Flow 18.

Bảng dữ liệu:

- Có thể ghi `CartItems`.
- Có thể xóa `CartItems`.

Trả lời vấn đáp:

> Bấm `-` giảm quantity nếu còn hơn 1. Nếu đang là 1 thì xóa item, vì backend không nhận quantity bằng 0.

## Flow 18. Bấm icon delete cart

1. Người dùng bấm thùng rác trong CartScreen.
2. Icon nằm tại `app/src/main/java/com/example/shop/ui/cart/CartScreen.kt:173-184`.
3. `CartScreen.kt:123` gọi `viewModel.removeFromCart(item)`.
4. `CartViewModel.removeFromCart` nằm tại `app/src/main/java/com/example/shop/viewmodel/CartViewModel.kt:72-75`.
5. `CartViewModel.kt:74` gọi `repository.deleteItem(item)`.
6. `CartRepository.deleteItem` nằm tại `app/src/main/java/com/example/shop/data/repository/CartRepository.kt:70-77`.
7. `CartRepository.kt:74` gọi `cartApi.deleteItem`.
8. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/CartApi.kt:31-35`.
9. Request đi tới `DELETE /api/cart/items/{id}`.
10. Backend nhận request ở `backend/ShopApi/Controllers/CartController.cs:106-127`.
11. Backend lấy user id từ JWT.
12. Backend tìm cart item theo id kèm user id.
13. `CartController.cs:123` remove item.
14. `CartController.cs:124` save database.
15. `CartController.cs:126` trả cart mới.
16. Repository cập nhật StateFlow.
17. UI recompose list.

Bảng dữ liệu:

- Đọc `CartItems`.
- Xóa `CartItems`.

Trả lời vấn đáp:

> Bấm delete gọi `DELETE /api/cart/items/{id}`. Backend filter theo id kèm user id từ JWT, nên user không xóa được cart item của người khác.

## Flow 19. Bấm checkout

1. Người dùng bấm checkout trong cart.
2. Bottom checkout chỉ hiện khi cart không rỗng tại `app/src/main/java/com/example/shop/ui/cart/CartScreen.kt:91-94`.
3. Callback `onCheckout` chạy.
4. `MainNavGraph.kt:193` navigate `Routes.CHECKOUT`.
5. Route checkout nằm tại `app/src/main/java/com/example/shop/navigation/MainNavGraph.kt:197-209`.
6. `CheckoutScreen` render.
7. `CheckoutScreen.kt:60` collect current user.
8. `CheckoutScreen.kt:61` collect cart items.
9. `CheckoutScreen.kt:62` tính total.
10. `CheckoutScreen.kt:64-67` tạo address, phone, selectedPayment, error.
11. Chưa tạo order.

Trả lời vấn đáp:

> Bấm checkout chỉ mở CheckoutScreen, màn này refresh cart rồi hiển thị thông tin đặt hàng.

## Flow 20. Chọn COD

1. Người dùng chọn radio `Thanh toán khi nhận hàng`.
2. Option nằm tại `app/src/main/java/com/example/shop/ui/checkout/CheckoutScreen.kt:118`.
3. Callback `PaymentOption` set `selectedPayment = "COD"`.
4. State payment đổi.
5. Compose recompose radio.
6. Chưa gọi backend.

Trả lời vấn đáp:

> Chọn COD chỉ đổi state payment method trong CheckoutScreen. Order chỉ tạo khi bấm `Place Order`.

## Flow 21. Chọn SePay

1. Người dùng chọn radio `Chuyển khoản SePay`.
2. Option nằm tại `app/src/main/java/com/example/shop/ui/checkout/CheckoutScreen.kt:117`.
3. Callback set `selectedPayment = "SEPAY"`.
4. State payment đổi.
5. Compose recompose radio.
6. Chưa gọi backend.

Trả lời vấn đáp:

> Chọn SePay chỉ đổi payment method local. QR chỉ sinh sau khi bấm `Place Order`.

## Flow 22. Bấm Place Order COD

1. Người dùng chọn COD.
2. Người dùng bấm `Place Order`.
3. Nút nằm tại `app/src/main/java/com/example/shop/ui/checkout/CheckoutScreen.kt:149-178`.
4. `CheckoutScreen.kt:151-152` kiểm tra user kèm cart.
5. `CheckoutScreen.kt:153-159` gọi `orderViewModel.placeOrder`.
6. `OrderViewModel.placeOrder` nằm tại `app/src/main/java/com/example/shop/viewmodel/OrderViewModel.kt:51-80`.
7. `OrderViewModel.kt:62-70` tạo `Order` local.
8. `OrderViewModel.kt:71` gọi `orderRepository.placeOrder`.
9. `OrderRepository.placeOrder` nằm tại `app/src/main/java/com/example/shop/data/repository/OrderRepository.kt:33-49`.
10. `OrderRepository.kt:38-45` gọi `orderApi.createOrder`.
11. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/OrderApi.kt:15-19`.
12. Request đi tới `POST /api/orders`.
13. Backend nhận request ở `backend/ShopApi/Controllers/OrdersController.cs:21-32`.
14. `OrdersController.cs:24` lấy user id từ JWT.
15. `OrdersController.cs:30` gọi `OrderService.CreateAsync`.
16. `OrderService.CreateAsync` nằm tại `backend/ShopApi/Services/OrderService.cs:37-63`.
17. `OrderService.cs:39-51` validate request, đọc cart, kiểm tra stock.
18. `OrderService.cs:53` mở transaction.
19. `OrderService.cs:54-56` tạo order, save.
20. `OrderService.cs:58` gọi `ApplyPaymentFlow`.
21. Với COD, `OrderService.cs:204-205` trừ kho, xóa cart.
22. `OrderService.cs:59-60` save, commit.
23. Backend trả order.
24. `OrderViewModel.kt:72-74` refresh cart.
25. `CheckoutScreen.kt:160-162` gọi `onOrderCreated`.
26. `MainNavGraph.kt:204-206` navigate order history.

Bảng dữ liệu:

- Đọc `CartItems`, `Products`.
- Ghi `Orders`, `OrderItems`.
- Ghi `Products.Quantity`.
- Xóa `CartItems`.

Trả lời vấn đáp:

> COD tạo order xong thì backend trừ kho, xóa cart ngay trong transaction.

## Flow 23. Bấm Place Order SePay

1. Người dùng chọn SePay.
2. Người dùng bấm `Place Order`.
3. Từ UI đến `POST /api/orders` giống Flow 22.
4. Request có `paymentMethod = "SEPAY"`.
5. `OrderService.cs:125-128` kiểm tra cấu hình SePay.
6. `OrderService.cs:53-56` tạo order, save để có id.
7. `OrderService.cs:197-201` tạo `PaymentCode`.
8. `OrderService.cs:251-254` format code dạng `ODD000001`.
9. Nhánh SePay không chạy trừ kho.
10. Nhánh SePay không xóa cart.
11. `OrderService.cs:270-285` build VietQR URL.
12. Backend trả order có QR URL.
13. `MainNavGraph.kt:200-203` navigate `PaymentQrScreen`.

Bảng dữ liệu:

- Đọc `CartItems`, `Products`.
- Ghi `Orders`, `OrderItems`.
- Ghi `PaymentCode`.
- Chưa trừ kho.
- Chưa xóa cart.

Trả lời vấn đáp:

> SePay chỉ tạo order pending kèm payment code, QR URL. Backend đợi webhook tiền vào mới đánh dấu Paid.

## Flow 24. Mở Payment QR

1. App navigate `payment_qr/{orderId}`.
2. Route nằm tại `app/src/main/java/com/example/shop/navigation/MainNavGraph.kt:212-225`.
3. `MainNavGraph.kt:216` đọc orderId.
4. `PaymentQrScreen` render tại `MainNavGraph.kt:217-225`.
5. `PaymentQrScreen.kt:61-62` tạo state payment, error.
6. `PaymentQrScreen.kt:64` chạy `LaunchedEffect(orderId)`.
7. `PaymentQrScreen.kt:66` gọi `viewModel.getPaymentStatus(orderId)`.
8. `OrderViewModel.kt:88-90` gọi repository.
9. `OrderRepository.kt:51-56` gọi API.
10. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/OrderApi.kt:24-28`.
11. Request đi tới `GET /api/orders/{id}/payment-status`.
12. Backend nhận request ở `backend/ShopApi/Controllers/OrdersController.cs:46-57`.
13. `OrderService.cs:75-92` đọc order, kiểm tra quyền.
14. Response trả payment status, payment code, QR URL.
15. `PaymentQrScreen.kt:141` hiển thị QR.
16. `PaymentQrScreen.kt:145-147` hiển thị amount, code, status.

Bảng dữ liệu:

- Đọc `Orders`.

Trả lời vấn đáp:

> Payment QR screen gọi API trạng thái thanh toán để lấy QR URL, amount, payment code.

## Flow 25. Polling thấy Paid

1. `PaymentQrScreen` đang polling.
2. `PaymentQrScreen.kt:65-79` lặp khi màn còn active.
3. Backend trả `paymentStatus = Paid`.
4. `PaymentQrScreen.kt:72` kiểm tra status Paid.
5. `PaymentQrScreen.kt:73` gọi `viewModel.refreshCart()`.
6. `PaymentQrScreen.kt:74` gọi `onPaid()`.
7. `PaymentQrScreen.kt:75` break vòng polling.
8. `MainNavGraph.kt:220-224` navigate `Routes.ORDER`.

Trả lời vấn đáp:

> App không tự xác nhận tiền. App chỉ chuyển màn khi backend trả `Paid`.

## Flow 26. SePay gửi webhook

1. SePay gửi request tới backend sau khi có tiền vào.
2. Endpoint nằm tại `backend/ShopApi/Controllers/PaymentsController.cs:19-33`.
3. `PaymentsController.cs:23-25` gọi `PaymentService.HandleSepayWebhookAsync`.
4. `PaymentService.HandleSepayWebhookAsync` nằm tại `backend/ShopApi/Services/PaymentService.cs:33-56`.
5. `PaymentService.cs:37-40` kiểm tra webhook key.
6. `PaymentService.cs:85-93` chỉ nhận giao dịch tiền vào.
7. `PaymentService.cs:95-105` lấy payment code.
8. `PaymentService.cs:107-113` tìm order theo payment code.
9. `PaymentService.cs:115-120` bỏ qua order không hợp lệ.
10. `PaymentService.cs:122-124` mở transaction.
11. `PaymentService.cs:126-147` chống xử lý trùng transaction.
12. `PaymentService.cs:149-157` kiểm tra số tiền, stock.
13. Nếu sai, `PaymentService.cs:159-165` mark failed.
14. Nếu đúng, `PaymentService.cs:167-178` mark paid, trừ kho.
15. `PaymentService.cs:181-187` xóa cart items.
16. `PaymentService.cs:189-193` save, commit.
17. Controller trả `{ success = true }`.

Bảng dữ liệu:

- Đọc `Orders`, `OrderItems`, `Products`.
- Ghi `Orders`.
- Ghi `Products.Quantity`.
- Xóa `CartItems`.

Trả lời vấn đáp:

> Webhook là xác nhận thanh toán thật. Backend kiểm tra mã, số tiền, transaction id, stock rồi mới mark Paid, trừ kho, xóa cart.

## Flow 27. Mở lịch sử đơn hàng

1. User mở OrderScreen.
2. Route nằm tại `app/src/main/java/com/example/shop/navigation/MainNavGraph.kt:228-231`.
3. `OrderScreen.kt:28` collect current user.
4. `OrderScreen.kt:31-32` gọi `orderViewModel.getOrderHistory`.
5. `OrderViewModel.kt:92-93` gọi repository.
6. `OrderRepository.kt:28-31` gọi `refreshMyOrders`.
7. `OrderRepository.kt:85-88` gọi `orderApi.getMyOrders`.
8. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/OrderApi.kt:21-22`.
9. Request đi tới `GET /api/orders/my`.
10. Backend nhận request ở `backend/ShopApi/Controllers/OrdersController.cs:34-44`.
11. Backend lấy user id từ JWT.
12. `OrderService.cs:65-68` gọi query user orders.
13. `OrderService.cs:221-236` đọc orders kèm items.
14. Android map DTO tại `app/src/main/java/com/example/shop/data/remote/dto/OrderDtos.kt:56-81`.
15. UI render list tại `OrderScreen.kt:44-50`.

Bảng dữ liệu:

- Đọc `Orders`.
- Đọc `OrderItems`.
- Đọc `Users`.

Trả lời vấn đáp:

> OrderScreen gọi `GET /api/orders/my`. Backend lấy user id từ JWT, trả đơn của user đó.

## Flow 28. Bấm card Sản phẩm admin

1. Admin bấm card `Sản phẩm`.
2. Card nằm tại `app/src/main/java/com/example/shop/admin/ui/dashboard/DashboardScreen.kt:67-73`.
3. `DashboardScreen.kt:72` gọi `onManageProducts`.
4. `MainNavGraph.kt:79` navigate `Routes.ADMIN_MANAGE_PRODUCT`.
5. Route nằm tại `MainNavGraph.kt:91-97`.
6. `ManageProductScreen.kt:35-36` collect products, categories.
7. Data load qua `GET /api/products`, `GET /api/categories`.

Trả lời vấn đáp:

> Bấm card Sản phẩm chỉ mở màn quản lý sản phẩm, sau đó màn này load products/categories.

## Flow 29. Bấm thêm sản phẩm admin

1. Admin bấm FAB thêm sản phẩm.
2. FAB nằm tại `app/src/main/java/com/example/shop/admin/ui/product/ManageProductScreen.kt:60-66`.
3. `ManageProductScreen.kt:62` gọi `onNavigateToAddProduct`.
4. `MainNavGraph.kt:94` navigate add product.
5. Route add nằm tại `MainNavGraph.kt:100-103`.
6. `AddProductScreen` render.
7. Chưa gọi backend.

Trả lời vấn đáp:

> Bấm FAB chỉ mở form thêm sản phẩm. Product chỉ được lưu khi bấm `Lưu sản phẩm`.

## Flow 30. Bấm Lưu sản phẩm admin

1. Admin bấm `Lưu sản phẩm`.
2. Button nằm tại `app/src/main/java/com/example/shop/admin/ui/product/AddProductScreen.kt:145-178`.
3. `AddProductScreen.kt:147-154` validate form.
4. `AddProductScreen.kt:156-164` gọi `viewModel.addProduct`.
5. `AdminProductViewModel.addProduct` nằm tại `app/src/main/java/com/example/shop/admin/viewmodel/AdminProductViewModel.kt:38-69`.
6. `AdminProductViewModel.kt:57` gọi `productRepository.insertProduct`.
7. `ProductRepository.insertProduct` nằm tại `app/src/main/java/com/example/shop/data/repository/ProductRepository.kt:48-64`.
8. `ProductRepository.kt:49` lấy bearer token.
9. `ProductRepository.kt:51-61` gọi `productApi.createProduct`.
10. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/ProductApi.kt:25-29`.
11. Request đi tới `POST /api/products`.
12. Backend nhận request ở `backend/ShopApi/Controllers/ProductsController.cs:69-93`.
13. Backend yêu cầu role admin.
14. `ProductsController.cs:197-226` validate input.
15. `ProductsController.cs:89-90` save product.
16. Nếu có image uri, `AdminProductViewModel.kt:58-60` gọi upload image.
17. Upload endpoint nằm tại `ProductApi.kt:44-50`.
18. Backend upload xử lý tại `ProductsController.cs:152-195`.
19. Repository refresh products.
20. UI quay lại nếu success.

Bảng dữ liệu:

- Đọc `Categories`.
- Ghi `Products`.
- Có thể ghi `Products.ImageUrl`.

Trả lời vấn đáp:

> Bấm lưu sản phẩm gọi admin ViewModel, Repository gọi `POST /api/products`, backend validate, lưu product. Nếu có ảnh thì gọi thêm endpoint upload.

## Flow 31. Bấm sửa sản phẩm admin

1. Admin bấm icon edit product.
2. Icon nằm tại `app/src/main/java/com/example/shop/admin/ui/product/ManageProductScreen.kt:192-198`.
3. Callback navigate update ở `ManageProductScreen.kt:148`.
4. `MainNavGraph.kt:95-96` navigate update product.
5. Route update nằm tại `MainNavGraph.kt:106-115`.
6. `UpdateProductScreen.kt:47` load product theo id.
7. `UpdateProductScreen.kt:50-60` đổ dữ liệu cũ vào form.
8. Chưa ghi backend cho đến khi bấm cập nhật.

Trả lời vấn đáp:

> Bấm edit chỉ mở form cập nhật, đồng thời load product hiện tại.

## Flow 32. Bấm Cập nhật sản phẩm admin

1. Admin bấm `Cập nhật thay đổi`.
2. Button nằm tại `app/src/main/java/com/example/shop/admin/ui/product/UpdateProductScreen.kt:168-202`.
3. `UpdateProductScreen.kt:170-177` validate.
4. `UpdateProductScreen.kt:179-188` gọi `viewModel.updateProduct`.
5. `AdminProductViewModel.updateProduct` nằm tại `app/src/main/java/com/example/shop/admin/viewmodel/AdminProductViewModel.kt:71-99`.
6. `AdminProductViewModel.kt:92` gọi `productRepository.updateProduct`.
7. `ProductRepository.updateProduct` nằm tại `app/src/main/java/com/example/shop/data/repository/ProductRepository.kt:67-84`.
8. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/ProductApi.kt:31-36`.
9. Request đi tới `PUT /api/products/{id}`.
10. Backend xử lý tại `backend/ShopApi/Controllers/ProductsController.cs:95-122`.
11. Backend validate input.
12. Backend update `Products`.
13. Nếu có ảnh mới, upload image chạy tiếp.
14. Repository refresh products.

Bảng dữ liệu:

- Đọc `Products`.
- Ghi `Products`.
- Đọc `Categories`.

Trả lời vấn đáp:

> Bấm cập nhật sản phẩm gọi `PUT /api/products/{id}`. Backend chỉ cho admin, validate dữ liệu rồi update product.

## Flow 33. Bấm xóa sản phẩm admin

1. Admin bấm icon delete product.
2. Icon nằm tại `app/src/main/java/com/example/shop/admin/ui/product/ManageProductScreen.kt:200-206`.
3. `ManageProductScreen.kt:140-146` gọi `viewModel.deleteProduct`.
4. `AdminProductViewModel.deleteProduct` nằm tại `app/src/main/java/com/example/shop/admin/viewmodel/AdminProductViewModel.kt:102-107`.
5. `ProductRepository.deleteProduct` nằm tại `app/src/main/java/com/example/shop/data/repository/ProductRepository.kt:87-93`.
6. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/ProductApi.kt:38-42`.
7. Request đi tới `DELETE /api/products/{id}`.
8. Backend xử lý tại `backend/ShopApi/Controllers/ProductsController.cs:124-150`.
9. Backend chặn nếu product có trong `OrderItems`.
10. Backend chặn nếu product có trong `CartItems`.
11. Nếu an toàn, backend xóa product.
12. Repository refresh products.

Bảng dữ liệu:

- Đọc `Products`.
- Đọc `OrderItems`.
- Đọc `CartItems`.
- Có thể xóa `Products`.

Trả lời vấn đáp:

> Xóa product bị backend guard để không phá order history hoặc cart.

## Flow 34. Bấm card Danh mục admin

1. Admin bấm card `Danh mục`.
2. Card nằm tại `app/src/main/java/com/example/shop/admin/ui/dashboard/DashboardScreen.kt:75-81`.
3. `DashboardScreen.kt:80` gọi `onManageCategories`.
4. `MainNavGraph.kt:80` navigate manage category.
5. Route nằm tại `MainNavGraph.kt:117-124`.
6. `ManageCategoryScreen.kt:31` collect categories.
7. Data load qua `GET /api/categories`.

Trả lời vấn đáp:

> Bấm card Danh mục mở màn quản lý danh mục, screen collect categories từ repository.

## Flow 35. Bấm thêm danh mục admin

1. Admin bấm FAB thêm danh mục.
2. FAB nằm tại `app/src/main/java/com/example/shop/admin/ui/category/ManageCategoryScreen.kt:44-47`.
3. `ManageCategoryScreen.kt:45` gọi `onNavigateToAddCategory`.
4. `MainNavGraph.kt:120` navigate add category.
5. Route nằm tại `MainNavGraph.kt:127-130`.
6. `AddCategoryScreen` render.
7. Chưa gọi backend.

Trả lời vấn đáp:

> Bấm FAB chỉ mở form thêm danh mục.

## Flow 36. Bấm Lưu danh mục admin

1. Admin bấm `Lưu danh mục`.
2. Button nằm tại `app/src/main/java/com/example/shop/admin/ui/category/AddCategoryScreen.kt:65-69`.
3. `AddCategoryScreen.kt:67` gọi `viewModel.addCategory`.
4. `AdminCategoryViewModel.addCategory` nằm tại `app/src/main/java/com/example/shop/admin/viewmodel/AdminCategoryViewModel.kt:136-141`.
5. `CategoryRepository.addCategory` nằm tại `app/src/main/java/com/example/shop/data/repository/CategoryRepository.kt:164-177`.
6. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/CategoryApi.kt:21-25`.
7. Request đi tới `POST /api/categories`.
8. Backend xử lý tại `backend/ShopApi/Controllers/CategoriesController.cs:48-68`.
9. Backend validate name.
10. Backend lưu category.
11. Repository refresh categories.

Bảng dữ liệu:

- Ghi `Categories`.

Trả lời vấn đáp:

> Bấm lưu danh mục gọi `POST /api/categories`, backend yêu cầu admin rồi lưu category.

## Flow 37. Bấm sửa danh mục admin

1. Admin bấm icon edit category.
2. Icon nằm tại `app/src/main/java/com/example/shop/admin/ui/category/ManageCategoryScreen.kt:62-64`.
3. App navigate update category tại `MainNavGraph.kt:121-122`.
4. Route update nằm tại `MainNavGraph.kt:133-141`.
5. `UpdateCategoryScreen.kt:27` load category.
6. `UpdateCategoryScreen.kt:30-35` đổ dữ liệu cũ vào form.
7. Chưa ghi backend.

Trả lời vấn đáp:

> Bấm edit category chỉ mở form cập nhật, dữ liệu cũ được load vào state.

## Flow 38. Bấm Cập nhật danh mục admin

1. Admin bấm `Cập nhật thay đổi`.
2. Button nằm tại `app/src/main/java/com/example/shop/admin/ui/category/UpdateCategoryScreen.kt:71-80`.
3. `UpdateCategoryScreen.kt:73-75` gọi `viewModel.updateCategory`.
4. `AdminCategoryViewModel.updateCategory` nằm tại `app/src/main/java/com/example/shop/admin/viewmodel/AdminCategoryViewModel.kt:144-150`.
5. `CategoryRepository.updateCategory` nằm tại `app/src/main/java/com/example/shop/data/repository/CategoryRepository.kt:190-204`.
6. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/CategoryApi.kt:27-32`.
7. Request đi tới `PUT /api/categories/{id}`.
8. Backend xử lý tại `backend/ShopApi/Controllers/CategoriesController.cs:70-91`.
9. Backend update category.
10. Repository refresh categories.

Bảng dữ liệu:

- Đọc `Categories`.
- Ghi `Categories`.

Trả lời vấn đáp:

> Bấm cập nhật danh mục gọi `PUT /api/categories/{id}`. Backend yêu cầu admin rồi update category.

## Flow 39. Bấm xóa danh mục admin

1. Admin bấm icon delete category.
2. Icon nằm tại `app/src/main/java/com/example/shop/admin/ui/category/ManageCategoryScreen.kt:65-67`.
3. `ManageCategoryScreen.kt:65` gọi `viewModel.deleteCategory`.
4. `AdminCategoryViewModel.deleteCategory` nằm tại `app/src/main/java/com/example/shop/admin/viewmodel/AdminCategoryViewModel.kt:153-157`.
5. `CategoryRepository.deleteCategory` nằm tại `app/src/main/java/com/example/shop/data/repository/CategoryRepository.kt:180-188`.
6. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/CategoryApi.kt:34-38`.
7. Request đi tới `DELETE /api/categories/{id}`.
8. Backend xử lý tại `backend/ShopApi/Controllers/CategoriesController.cs:93-112`.
9. Backend chặn nếu category còn product.
10. Nếu an toàn, backend xóa category.
11. Repository refresh categories.

Bảng dữ liệu:

- Đọc `Categories`.
- Đọc `Products`.
- Có thể xóa `Categories`.

Trả lời vấn đáp:

> Xóa category bị chặn nếu còn product đang tham chiếu category đó.

## Flow 40. Bấm card Đơn hàng admin

1. Admin bấm card `Đơn hàng`.
2. Card nằm tại `app/src/main/java/com/example/shop/admin/ui/dashboard/DashboardScreen.kt:83-89`.
3. `DashboardScreen.kt:88` gọi `onManageOrders`.
4. `MainNavGraph.kt:81` navigate manage order.
5. Route nằm tại `MainNavGraph.kt:145-148`.
6. `ManageOrderScreen.kt:31` collect all orders.
7. `AdminOrderViewModel.allOrders` nằm tại `app/src/main/java/com/example/shop/admin/viewmodel/AdminOrderViewModel.kt:19-24`.
8. `OrderRepository.kt:99-102` gọi `GET /api/orders`.
9. Backend xử lý tại `backend/ShopApi/Controllers/OrdersController.cs:59-64`.
10. Backend yêu cầu admin.
11. Backend trả toàn bộ orders.

Bảng dữ liệu:

- Đọc `Orders`.
- Đọc `OrderItems`.
- Đọc `Users`.

Trả lời vấn đáp:

> Bấm card Đơn hàng mở màn admin orders, màn này gọi `GET /api/orders` với token admin.

## Flow 41. Bấm đổi trạng thái đơn admin

1. Admin bấm nút `Trạng thái`.
2. Nút nằm tại `app/src/main/java/com/example/shop/admin/ui/order/ManageOrderScreen.kt:151-159`.
3. `ManageOrderScreen.kt:152` set `showMenu = true`.
4. Dropdown render tại `ManageOrderScreen.kt:161-175`.
5. Admin chọn một status.
6. `ManageOrderScreen.kt:169-172` gọi `onUpdateStatus(status)`.
7. `ManageOrderScreen.kt:65-67` gọi `viewModel.updateOrderStatus`.
8. `AdminOrderViewModel.updateOrderStatus` nằm tại `app/src/main/java/com/example/shop/admin/viewmodel/AdminOrderViewModel.kt:26-30`.
9. `OrderRepository.updateOrderStatus` nằm tại `app/src/main/java/com/example/shop/data/repository/OrderRepository.kt:64-75`.
10. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/OrderApi.kt:33-38`.
11. Request đi tới `PUT /api/orders/{id}/status`.
12. Backend xử lý tại `backend/ShopApi/Controllers/OrdersController.cs:66-74`.
13. `OrderService.cs:94-115` validate status, update order.
14. Repository refresh all orders.

Bảng dữ liệu:

- Đọc `Orders`.
- Ghi `Orders.Status`.

Trả lời vấn đáp:

> Đổi trạng thái đơn gọi `PUT /api/orders/{id}/status`. Backend chỉ cho admin, status chỉ được thuộc danh sách hợp lệ.

## Flow 42. Bấm card Người dùng admin

1. Admin bấm card `Người dùng`.
2. Card nằm tại `app/src/main/java/com/example/shop/admin/ui/dashboard/DashboardScreen.kt:91-97`.
3. `DashboardScreen.kt:96` gọi `onManageUsers`.
4. `MainNavGraph.kt:82` navigate manage user.
5. Route nằm tại `MainNavGraph.kt:152-155`.
6. `AdminUserViewModel` chạy init tại `app/src/main/java/com/example/shop/admin/viewmodel/AdminUserViewModel.kt:26-28`.
7. `AdminUserViewModel.kt:30-44` gọi `loadUsers`.
8. `UserRepository.kt:13-18` gọi API.
9. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/UserApi.kt:10-13`.
10. Request đi tới `GET /api/users`.
11. Backend xử lý tại `backend/ShopApi/Controllers/UsersController.cs:22-32`.
12. Backend yêu cầu admin.
13. UI collect users tại `app/src/main/java/com/example/shop/admin/ui/user/ManageUserScreen.kt:46-48`.

Bảng dữ liệu:

- Đọc `Users`.

Trả lời vấn đáp:

> Bấm card Người dùng mở màn users. ViewModel init tự gọi `GET /api/users`.

## Flow 43. Bấm Tải lại user admin

1. Admin bấm `Tải lại`.
2. Nút nằm tại `app/src/main/java/com/example/shop/admin/ui/user/ManageUserScreen.kt:60-62`.
3. `ManageUserScreen.kt:60` gọi `viewModel.loadUsers`.
4. `AdminUserViewModel.kt:30-44` set loading, gọi repository.
5. `UserRepository.kt:13-18` gọi `GET /api/users`.
6. UI cập nhật users hoặc error.

Bảng dữ liệu:

- Đọc `Users`.

Trả lời vấn đáp:

> Tải lại user gọi lại API `GET /api/users`, sau đó cập nhật StateFlow users.

## Flow 44. Bấm xóa user admin

1. Admin bấm nút xóa user.
2. Button nằm tại `app/src/main/java/com/example/shop/admin/ui/user/ManageUserScreen.kt:146-148`.
3. `ManageUserScreen.kt:94` gọi `viewModel.deleteUser(user.id)`.
4. `AdminUserViewModel.deleteUser` nằm tại `app/src/main/java/com/example/shop/admin/viewmodel/AdminUserViewModel.kt:46-56`.
5. `AdminUserViewModel.kt:48` gọi `userRepository.deleteUser`.
6. `UserRepository.deleteUser` nằm tại `app/src/main/java/com/example/shop/data/repository/UserRepository.kt:20-26`.
7. Retrofit endpoint nằm tại `app/src/main/java/com/example/shop/data/remote/api/UserApi.kt:15-18`.
8. Request đi tới `DELETE /api/users/{id}`.
9. Backend xử lý tại `backend/ShopApi/Controllers/UsersController.cs:34-86`.
10. Backend chặn xóa chính mình.
11. Backend chặn xóa admin account.
12. Backend xóa order items, orders, cart items trong transaction.
13. Backend xóa user.
14. UI remove user khỏi list tại `AdminUserViewModel.kt:49-51`.

Bảng dữ liệu:

- Đọc `Users`.
- Đọc `Orders`.
- Xóa `OrderItems`.
- Xóa `Orders`.
- Xóa `CartItems`.
- Xóa `Users`.

Trả lời vấn đáp:

> Xóa user là thao tác admin. Backend guard không cho xóa chính mình hoặc admin account, rồi xóa dữ liệu liên quan trong transaction.

## Flow 45. Bấm xem đơn trong Profile

1. User mở Profile.
2. Route profile nằm tại `app/src/main/java/com/example/shop/navigation/MainNavGraph.kt:235-244`.
3. User bấm mục đơn hàng.
4. Callback `onNavigateToOrders` nằm tại `MainNavGraph.kt:237`.
5. App navigate `Routes.ORDER`.
6. Flow 27 chạy.

Trả lời vấn đáp:

> Bấm xem đơn trong profile chỉ navigate sang OrderScreen, sau đó OrderScreen gọi API orders/my.

## Flow 46. Bấm Logout

1. User bấm logout.
2. Nếu gọi ViewModel, code vào `app/src/main/java/com/example/shop/viewmodel/AuthViewModel.kt:61-65`.
3. `AuthViewModel.kt:63` set `_isLoggedIn = false`.
4. `AuthViewModel.kt:64` gọi `authRepository.logout()`.
5. `AuthRepository.logout` nằm tại `app/src/main/java/com/example/shop/data/repository/AuthRepository.kt:61-64`.
6. `_currentUser` bị set null.
7. `_currentToken` bị set null.
8. `MainNavGraph.kt:239-242` navigate Login, clear stack.
9. Không gọi backend.

Trả lời vấn đáp:

> Logout hiện tại là logout local: xóa currentUser, currentToken, quay về login.

## Flow 47. Repository cần token

1. Repository chuẩn bị gọi API cần đăng nhập.
2. Repository gọi `authRepository.getAuthorizationHeader()`.
3. Hàm nằm tại `app/src/main/java/com/example/shop/data/repository/AuthRepository.kt:66-68`.
4. Nếu token tồn tại, hàm trả `Bearer <token>`.
5. Nếu token null, repository return null/false hoặc giữ state rỗng.
6. Retrofit gửi header qua `@Header("Authorization")`.
7. Backend middleware validate JWT tại `backend/ShopApi/Program.cs:37-51`.
8. `Program.cs:79-80` bật authentication, authorization.
9. Controller đọc user id từ JWT claim khi cần.

Trả lời vấn đáp:

> Token được lưu ở AuthRepository. Repository lấy header Bearer rồi gửi lên API. Backend middleware kiểm tra token trước khi controller xử lý.

## Flow 48. Backend kiểm tra role admin

1. Request gọi endpoint admin.
2. Android gửi Bearer token.
3. JWT middleware validate token.
4. Endpoint admin có `[Authorize(Roles = "ADMIN")]`.
5. Ví dụ `UsersController.cs:10`, `ProductsController.cs:69`, `OrdersController.cs:59`.
6. Role lấy từ claim được tạo ở `backend/ShopApi/Controllers/AuthController.cs:181`.
7. Nếu token không có role admin, backend trả 403.
8. Nếu hợp lệ, controller chạy.

Trả lời vấn đáp:

> UI admin chỉ là điều hướng. Quyền thật nằm ở backend bằng `[Authorize(Roles = "ADMIN")]`.
