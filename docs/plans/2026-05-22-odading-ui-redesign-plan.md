# Kế Hoạch Redesign UI Theo Mẫu Odading Furniture

Ngày tạo: 2026-05-22  
Branch triển khai: `feature/odading-ui-redesign`  
Mẫu tham chiếu: `https://dribbble.com/shots/23537838-Odading-Furniture-Mobile-App`

## 1. Mục Tiêu

Redesign giao diện Android hiện tại theo phong cách Odading Furniture Mobile App: tối giản, sáng ấm, nhiều khoảng trắng, ảnh sản phẩm lớn, card bo mềm và nút hành động rõ ràng.

Mục tiêu quan trọng nhất:

- Chỉ đổi UI Jetpack Compose.
- Không đổi REST API.
- Không đổi backend.
- Không đổi ViewModel nếu không bắt buộc.
- Không đổi navigation flow.
- Không thêm feature mới ngoài giao diện.
- Code phải gọn, dễ đọc, dễ review theo từng checkpoint.

## 2. Nguyên Tắc Thiết Kế

Phong cách chính:

- Nội thất tối giản, ấm, sạch.
- Nền trắng ngà/be nhạt.
- Màu nhấn nâu gỗ/đen than.
- Bo góc lớn cho ảnh, card, button.
- Ít màu, không gradient lòe loẹt.
- Ảnh sản phẩm là điểm nhấn chính.

Không làm:

- Không copy asset từ Dribbble.
- Không khôi phục onboarding đã xóa.
- Không thêm chọn màu sản phẩm vì backend chưa có field color.
- Không thêm animation phức tạp.
- Không đổi logic cart/checkout/order/auth/admin.
- Không sửa backend để phục vụ redesign.

## 3. Màn Hình Odading Được Áp Dụng Như Thế Nào

### 3.1 Hero/Onboarding Trong Mẫu

Mẫu Odading có màn hero full ảnh với chữ `Minimalist Furniture`.

Áp dụng vào dự án:

- Không tạo lại onboarding.
- Lấy tinh thần hero đó để làm phần đầu Home: banner lớn, ảnh/nền be, headline nội thất.

### 3.2 Home/List Trong Mẫu

Mẫu Odading có:

- Top bar gọn.
- Category chip ngang.
- Banner promo.
- Product grid 2 cột.
- Card sản phẩm nhẹ, ảnh nổi bật, giá rõ.

Áp dụng vào dự án:

- Redesign `HomeScreen.kt`.
- Redesign `ProductItem.kt`.
- Giữ nguyên `products` và `categories` lấy từ ViewModel hiện tại.
- Category vẫn gọi `onOpenCategory(category.id)`.
- Product vẫn gọi `onOpenProduct(product.id.toString())`.

### 3.3 Product Detail Trong Mẫu

Mẫu Odading có:

- Ảnh lớn ở đầu.
- Tên sản phẩm overlay trên ảnh.
- Khu vực thông tin bên dưới.
- Giá nổi bật.
- Button mua hàng bo tròn.

Áp dụng vào dự án:

- Redesign `ProductDetailScreen.kt`.
- Button vẫn thực hiện `cartViewModel.addToCart(...)`.
- Text button có thể giữ tiếng Việt: `Thêm vào giỏ hàng`.
- Không thêm chọn màu thật nếu backend không có dữ liệu.

## 4. Checkpoint Triển Khai

### Checkpoint UI-01: Tạo Design Tokens Gọn

#### Vấn đề hiện tại

Các màn đang dùng trực tiếp màu mặc định Material hoặc `Color.LightGray`, `Color.Red`, chưa có phong cách nội thất thống nhất.

#### Mục tiêu thực hiện

Tạo một nơi chứa màu/shape/spacing cơ bản để các màn dùng chung, tránh copy màu rải rác.

#### File dự kiến thay đổi

```text
app/src/main/java/com/example/shop/ui/theme/ShopDesign.kt
```

#### Hành động cụ thể

- Tạo object hoặc values đơn giản cho màu:
  - Nền chính.
  - Nền card.
  - Nâu gỗ.
  - Text chính.
  - Text phụ.
  - Border nhạt.
- Tạo shape dùng chung:
  - Card radius.
  - Image radius.
  - Button radius.
- Không thay toàn bộ Material theme để tránh ảnh hưởng lớn.

#### Tiêu chí review

- File ngắn, dễ hiểu.
- Không tạo design system phức tạp.
- Không ảnh hưởng logic.

#### Verify

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon --console=plain --stacktrace "-Dkotlin.incremental=false"
```

### Checkpoint UI-02: Redesign Product Card

#### Vấn đề hiện tại

`ProductItem.kt` còn card cơ bản, ảnh nhỏ, spacing chưa giống app nội thất cao cấp.

#### Mục tiêu thực hiện

Đổi product card theo tinh thần Odading: ảnh lớn hơn, card nhẹ hơn, bo góc mềm, tên và giá rõ.

#### File dự kiến thay đổi

```text
app/src/main/java/com/example/shop/ui/components/ProductItem.kt
```

#### Hành động cụ thể

- Tăng chiều cao ảnh.
- Card dùng nền sáng và bo góc lớn.
- Tên sản phẩm tối đa 2 dòng.
- Giá dùng màu nâu/đen nổi bật.
- Giữ nguyên tham số hàm hiện tại để không ảnh hưởng caller.
- Vẫn dùng `Constants.toBackendImageUrl(imageUrl)`.

#### Tiêu chí review

- Home/product list đẹp hơn ngay cả khi chưa redesign Home.
- Không đổi API component.
- Không thêm logic format phức tạp.

#### Verify

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon --console=plain --stacktrace "-Dkotlin.incremental=false"
```

### Checkpoint UI-03: Redesign Home Screen

#### Vấn đề hiện tại

`HomeScreen.kt` đang là layout Material mặc định: top bar, category, grid. Đúng logic nhưng chưa có cảm giác shop nội thất.

#### Mục tiêu thực hiện

Biến Home thành màn chính gần Odading: header gọn, hero promo, category chip ngang, grid sản phẩm sạch.

#### File dự kiến thay đổi

```text
app/src/main/java/com/example/shop/ui/home/HomeScreen.kt
```

#### Hành động cụ thể

- Thay top area bằng header gọn: lời chào/title + cart icon.
- Thêm hero banner nội thất dạng card lớn.
- Category chip dùng style pill.
- Grid giữ 2 cột.
- Vẫn dùng `products.take(10)`.
- Không thêm search thật nếu chưa có backend/search logic.

#### Tiêu chí review

- Nhìn giống app nội thất hơn.
- Không phá category navigation.
- Không phá cart navigation.
- Không đổi ViewModel.

#### Verify

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon --console=plain --stacktrace "-Dkotlin.incremental=false"
```

### Checkpoint UI-04: Redesign Product Detail

#### Vấn đề hiện tại

`ProductDetailScreen.kt` đang hiển thị ảnh trong card xám và thông tin bên dưới theo style mặc định.

#### Mục tiêu thực hiện

Đổi detail theo mẫu Odading: ảnh lớn, title nổi bật, price section rõ, CTA bo tròn phía dưới.

#### File dự kiến thay đổi

```text
app/src/main/java/com/example/shop/ui/product/ProductDetailScreen.kt
```

#### Hành động cụ thể

- Ảnh sản phẩm lớn ở đầu.
- Nếu có ảnh thì crop đẹp; nếu không có ảnh thì placeholder nền be.
- Tên sản phẩm đặt rõ gần ảnh.
- Giá nổi bật.
- Mô tả giữ đơn giản.
- Button `Thêm vào giỏ hàng` full width, bo tròn.
- Nếu hết hàng, button disabled như hiện tại.

#### Tiêu chí review

- Logic add cart giữ nguyên.
- Không thêm color selector thật.
- Không dùng `println` để báo lỗi user nếu có thể thay bằng text đơn giản.

#### Verify

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon --console=plain --stacktrace "-Dkotlin.incremental=false"
```

### Checkpoint UI-05: Redesign Bottom Bar Nhẹ

#### Vấn đề hiện tại

`BottomBar.kt` dùng NavigationBar mặc định, hoạt động đúng nhưng chưa ăn nhập phong cách Odading.

#### Mục tiêu thực hiện

Làm bottom bar gọn hơn, màu nền sáng, selected state rõ, badge giỏ hàng vẫn đúng số lượng.

#### File dự kiến thay đổi

```text
app/src/main/java/com/example/shop/ui/components/BottomBar.kt
```

#### Hành động cụ thể

- Đổi màu nền và indicator.
- Giữ 3 tab hiện tại: Home, Cart, Profile.
- Giữ badge cart thật từ `cartItemCount`.
- Không đổi route.

#### Tiêu chí review

- Không phá navigation.
- Badge vẫn đúng.
- Visual đồng bộ với Home/Product.

#### Verify

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon --console=plain --stacktrace "-Dkotlin.incremental=false"
```

### Checkpoint UI-06: Rà Soát Các Màn Liên Quan

#### Vấn đề hiện tại

Sau khi đổi Home/Product/BottomBar, các màn Cart/Profile/Login có thể nhìn lệch style.

#### Mục tiêu thực hiện

Chỉ chỉnh nhẹ các màn liên quan để không bị lệch, không redesign toàn bộ app nếu không cần.

#### File dự kiến thay đổi

```text
app/src/main/java/com/example/shop/ui/cart/CartScreen.kt
app/src/main/java/com/example/shop/ui/profile/ProfileScreen.kt
app/src/main/java/com/example/shop/ui/auth/LoginScreen.kt
```

#### Hành động cụ thể

- Chỉnh màu nền/card/button cho đồng bộ.
- Không đổi logic login Google/email.
- Không đổi checkout/order flow.
- Không đụng admin nếu chưa review riêng.

#### Tiêu chí review

- App nhìn nhất quán hơn.
- Code không phình to.
- Không thay đổi nghiệp vụ.

#### Verify

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon --console=plain --stacktrace "-Dkotlin.incremental=false"
```

### Checkpoint UI-07: Review, Emulator Smoke Test Và Push

#### Vấn đề hiện tại

Build pass chưa đủ để xác nhận UI ổn trên emulator.

#### Mục tiêu thực hiện

Chạy app và kiểm tra nhanh các luồng chính sau redesign.

#### Hành động cụ thể

- Build debug.
- Cài/chạy app trên emulator nếu emulator đang sẵn sàng.
- Kiểm tra:
  - Login.
  - Home hiển thị sản phẩm.
  - Category mở đúng.
  - Product detail mở đúng.
  - Add cart.
  - Bottom bar chuyển tab.
- Chụp hoặc mô tả vấn đề nếu UI bị vỡ.

#### Tiêu chí review

- Không crash.
- Luồng cũ vẫn chạy.
- UI gần mẫu Odading hơn rõ ràng.

#### Verify

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon --console=plain --stacktrace "-Dkotlin.incremental=false"
```

## 5. Cách Review Với User

Sau mỗi checkpoint:

- Nêu file đã đổi.
- Nêu logic có đổi hay không.
- Nêu kết quả build.
- Chờ user review trước checkpoint tiếp theo nếu user yêu cầu.
- Nếu checkpoint được duyệt thì commit/push checkpoint đó.

## 6. Rủi Ro Và Cách Kiểm Soát

| Rủi ro | Cách kiểm soát |
|---|---|
| UI đẹp nhưng code phức tạp | Giữ component nhỏ, không tạo abstraction quá sớm |
| Copy mẫu quá đà | Chỉ lấy layout/tone, không copy asset |
| Phá logic hiện có | Không đổi ViewModel/API/navigation |
| Màn khác bị lệch style | Có checkpoint rà soát nhẹ sau Home/Product |
| Ảnh sản phẩm xấu làm UI kém | Placeholder be sạch, card vẫn giữ layout ổn |

## 7. Trạng Thái Hiện Tại

Chưa triển khai code UI. Tài liệu này dùng để chốt phạm vi trước khi bắt đầu Checkpoint UI-01.
