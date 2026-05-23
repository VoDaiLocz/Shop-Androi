# Thiết Kế Lại Clean Premium Catalog

Ngày 2026-05-23.

## Mục Tiêu

Thiết kế lại giao diện mua sắm nội thất theo hướng catalog cao cấp, sạch và gọn. Màn hình phải đẹp hơn bản hiện tại rõ ràng: bo góc mượt, banner phù hợp ảnh sản phẩm, thông tin dễ đọc, không làm thay đổi logic API, ViewModel, navigation, seed data hay OAuth.

## Tham Chiếu Thiết Kế

Hướng chính bám các mẫu Dribbble furniture mobile app:

- Odading Furniture Mobile App: https://dribbble.com/shots/23537838-Odading-Furniture-Mobile-App
- Furniture/ecommerce mobile shots trên Dribbble: https://dribbble.com/shots/

Áp dụng tinh thần chung của các mẫu này: nền off-white, typo đen rõ, ảnh sản phẩm là trọng tâm, category ngang nhẹ, product grid gọn, bottom navigation mỏng và ít gây chú ý.

## Phạm Vi

Sửa UI Compose ở các file hiện có:

- `ShopDesign.kt`: màu, radius, token dùng chung.
- `Type.kt`: typography nếu cần giảm cảm giác thô.
- `HomeScreen.kt`: header, banner, category row, section title, spacing grid.
- `ProductItem.kt`: card ảnh, tên, giá, crop ảnh.
- `BottomBar.kt`: bottom nav gọn và cao cấp hơn.
- `ProductDetailScreen.kt`: đồng bộ visual với home nếu cần, không đổi hành vi thêm giỏ hàng.

Ngoài phạm vi: backend, database seed, OAuth, route, repository, ViewModel, admin screens.

## Hướng Giao Diện Được Duyệt

Chọn approach 1: Editorial Catalog.

Home screen sẽ có cảm giác như catalog nội thất cao cấp:

- Header gọn: logo Odading ở giữa hoặc cân đối với icon, icon nhỏ và nhẹ.
- Headline ngắn, mạnh, không để quá nhiều dòng gây thô.
- Banner sáng hơn bản hiện tại, ưu tiên ảnh nội thất rõ sản phẩm. Không dùng overlay đen dày. Text trên banner ít, có thể dùng nhãn nhỏ hoặc caption ngắn để không che ảnh.
- Category là text tab ngang với underline nhẹ, không dùng pill nặng.
- Product card không có badge overlay. Ảnh lớn, bo góc 8-12dp, crop đẹp, tên và giá nằm dưới ảnh.
- Bottom nav mỏng, selected state nhẹ bằng màu đen/wood hoặc line nhỏ, không dùng capsule to.

## Tiêu Chí Chất Lượng

- Banner phải nhìn phù hợp với app nội thất, không bị tối, không bị rẻ tiền, không che mất sản phẩm.
- Các góc bo đồng nhất và mượt, ưu tiên 8-12dp cho card/ảnh.
- Layout trên emulator 341x701 không được chen chúc hoặc bị bottom nav che quá nhiều.
- Ảnh local 10 sản phẩm hiện có phải hiện đẹp nhất có thể với crop/spacing hợp lý.
- Code gọn: ưu tiên sửa component hiện có, không tách abstraction lớn nếu không cần.
- Build `:app:assembleDebug` phải pass.
- Sau khi cài APK, phải chụp screenshot emulator và tự kiểm tra Home screen.

## Rủi Ro Và Cách Xử Lý

- Ảnh local có tỷ lệ khác nhau nên crop có thể xấu. Xử lý bằng height/card ratio ổn định và content scale phù hợp, không đổi data.
- Nếu banner lấy ảnh sản phẩm đầu tiên không hợp, chọn ảnh hero từ product phù hợp hơn bằng logic nhỏ trong HomeScreen.
- Nếu thay đổi bottom nav gây lỗi route/import, giữ API `BottomBar(navController, cartItemCount)` như cũ.

## Kiểm Thử

- Build Android debug bằng JDK 21.
- Cài APK lên emulator đang chạy.
- Mở app, vào Home, chụp screenshot.
- Chạy `git diff --check`.
- Nếu Home còn xấu, sửa tiếp trong cùng phạm vi UI trước khi báo hoàn tất.
