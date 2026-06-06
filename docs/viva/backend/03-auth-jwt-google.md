# Auth, JWT và Google

## Register

Endpoint:

```text
POST /api/auth/register
```

Request:

- username
- email
- password

Backend xử lý:

1. Trim username.
2. Lowercase email.
3. Kiểm tra email tồn tại.
4. Hash password bằng `PasswordHasher<User>`.
5. Tạo user role `USER`.
6. Lưu database.

## Login email/password

Endpoint:

```text
POST /api/auth/login
```

Backend xử lý:

1. Tìm user theo email.
2. Verify password hash.
3. Tạo JWT.
4. Trả `LoginResponse(token, user)`.

## JWT gồm gì?

Claims:

- `NameIdentifier`: user id.
- `Name`: username.
- `Email`: email.
- `Role`: USER hoặc ADMIN.

JWT được ký bằng:

```text
HmacSha256 + Jwt:Key
```

Android gửi JWT:

```text
Authorization: Bearer <token>
```

Backend xác thực JWT trong middleware:

```text
UseAuthentication()
UseAuthorization()
```

## Google login

Endpoint:

```text
POST /api/auth/google
```

Request:

- `idToken`

Backend xử lý:

1. Đọc `Google:ClientId`.
2. Gọi `GoogleJsonWebSignature.ValidateAsync`.
3. Kiểm tra audience đúng client id.
4. Kiểm tra email verified.
5. Tìm user theo `GoogleSub`.
6. Nếu chưa có, tìm user theo email.
7. Nếu email chưa tồn tại, tạo user mới.
8. Nếu email tồn tại và chưa link Google, gán `GoogleSub`.
9. Nếu email đã link Google khác, trả conflict.
10. Cấp JWT của hệ thống.

## Phân biệt Google OAuth và JWT

Google OAuth:

- Dịch vụ bên ngoài.
- Xác minh người dùng Google.
- Android lấy `idToken`.
- Backend validate token với Google.

JWT:

- Token nội bộ của hệ thống.
- Backend tự cấp sau khi login thường hoặc Google login thành công.
- Dùng để gọi API nội bộ.

Trả lời ngắn:

> Google OAuth giúp xác minh danh tính Google. JWT là token của hệ thống dùng để bảo vệ API sau khi đã đăng nhập.

## API me

Endpoint:

```text
GET /api/auth/me
```

Backend:

- Lấy user id từ claim `NameIdentifier`.
- Tìm user trong database.
- Trả UserResponse.

Mục đích:

- Kiểm tra token còn hợp lệ.
- Lấy thông tin user hiện tại.
