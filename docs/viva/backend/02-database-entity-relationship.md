# Database và entity relationship

## Các bảng chính

| Entity | Bảng | Vai trò |
|---|---|---|
| User | Users | Tài khoản user/admin |
| Category | Categories | Danh mục sản phẩm |
| Product | Products | Sản phẩm |
| CartItem | CartItems | Sản phẩm trong giỏ của từng user |
| Order | Orders | Đơn hàng |
| OrderItem | OrderItems | Chi tiết sản phẩm trong đơn hàng |

## User

Fields quan trọng:

- `Username`
- `Email`
- `PasswordHash`
- `Role`
- `GoogleSub`

Constraint:

- `Email` unique.
- `GoogleSub` unique.

Ý nghĩa:

- Email dùng cho login thường và liên kết Google.
- GoogleSub là id duy nhất của tài khoản Google.
- Role dùng phân quyền `USER`/`ADMIN`.

## Category và Product

Quan hệ:

```text
Category 1 - n Product
```

Delete behavior:

- Product -> Category dùng `Restrict`.
- Không cho xóa category nếu còn product.

Constraint product:

- `Price >= 0`
- `Quantity >= 0`

## CartItem

Quan hệ:

```text
User 1 - n CartItem
Product 1 - n CartItem
```

Constraint:

- `Quantity > 0`
- Unique `(UserId, ProductId)`

Ý nghĩa unique:

- Một user không có nhiều dòng giỏ hàng cho cùng một sản phẩm.
- Nếu thêm lại sản phẩm đã có trong giỏ, backend cộng quantity.

## Order và OrderItem

Quan hệ:

```text
User 1 - n Order
Order 1 - n OrderItem
Product 1 - n OrderItem
```

Order lưu:

- `TotalPrice`
- `Status`: Pending, Shipping, Delivered, Cancelled.
- `PaymentMethod`: COD hoặc SEPAY.
- `PaymentStatus`: Pending, Paid, Failed.
- `PaymentCode`
- `SepayTransactionId`
- `SepayReferenceCode`
- `PaidAt`

OrderItem lưu snapshot:

- `ProductName`
- `Price`
- `ImageUrl`
- `Quantity`

Vì sao OrderItem lưu snapshot?

> Vì sau khi đặt hàng, tên/giá/ảnh sản phẩm có thể thay đổi. Order history vẫn phải giữ thông tin tại thời điểm mua.

## Payment indexes

`PaymentCode` unique:

- Mỗi order có mã thanh toán riêng như `ODD000007`.

`SepayTransactionId` unique:

- Tránh xử lý cùng một giao dịch SePay nhiều lần.
