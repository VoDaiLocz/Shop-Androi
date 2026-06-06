# Admin API và bảo mật

## Admin role

Backend bảo vệ API admin bằng:

```csharp
[Authorize(Roles = "ADMIN")]
```

Các nhóm API admin:

- Product create/update/delete/upload image.
- Category create/update/delete.
- Order get all/update status.
- User get all/delete.

## Vì sao Android role check chưa đủ?

Android dùng role để điều hướng UI:

```text
ADMIN -> ADMIN_DASHBOARD
USER -> HOME
```

Nhưng bảo mật thật phải ở backend. Nếu người dùng sửa app hoặc gọi API thủ công, backend vẫn kiểm tra JWT role.

## UsersController

Admin có thể:

- Xem danh sách user.
- Xóa user.

Backend chặn:

- Không cho xóa chính mình.
- Không cho xóa admin account.

Khi xóa user, backend xử lý dữ liệu liên quan như cart/order để tránh foreign key lỗi.

## Product security

Xóa product có kiểm tra:

- Nếu product đã nằm trong order history -> không xóa.
- Nếu product đang nằm trong cart user -> không xóa.

Lý do:

> Order history cần giữ dữ liệu mua hàng. Nếu xóa product đang được tham chiếu có thể làm hỏng lịch sử hoặc vi phạm khóa ngoại.

## Category security

Xóa category có kiểm tra:

- Nếu category còn product -> không xóa.

Lý do:

> Product cần category hợp lệ. Delete behavior đang là Restrict.

## Image upload security

Backend kiểm tra:

- Có file.
- File không rỗng.
- File <= 5MB.
- Extension hợp lệ.

Không cho upload file tùy ý.
