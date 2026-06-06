# Tích hợp Google OAuth vào dự án

File này dùng để học cách tích hợp Google OAuth từ đầu vào app. Đây không phải flow người dùng bấm nút, mà là các bước kỹ thuật đã làm để dịch vụ Google đăng nhập chạy được trong dự án.

## Mục tiêu tích hợp

- Android cho người dùng chọn tài khoản Google.
- Android lấy Google `idToken`.
- Android gửi `idToken` về backend.
- Backend validate `idToken` với Google.
- Backend tìm hoặc tạo user nội bộ.
- Backend cấp JWT của hệ thống.
- Android lưu JWT rồi gọi các API nội bộ như login thường.

Điểm phải nói khi vấn đáp:

> Google OAuth chỉ xác minh danh tính Google. Sau khi xác minh xong, backend vẫn cấp JWT riêng của hệ thống để bảo vệ API nội bộ.

## Bước 1. Tạo project Google Cloud

Đã tạo Google Cloud project:

```text
Project name: shop-android
Project ID: zippy-nexus-497009-c3
```

Tài liệu cấu hình console chi tiết nằm ở:

```text
docs/google-oauth-setup.md
```

Lý do cần project Google Cloud:

- Google cần biết app nào đang xin đăng nhập.
- Google cần biết package name Android.
- Google cần biết SHA-1 của app debug.
- Google cần cấp OAuth Client ID để Android xin `idToken`.

## Bước 2. Cấu hình OAuth consent screen

Trong Google Auth Platform đã cấu hình:

- User type: External.
- Publishing status: Testing.
- Test users: tài khoản nhóm dùng để test.
- Scopes: `openid`, email, profile.

Lý do cần consent screen:

- Google chỉ cho tài khoản test đăng nhập khi app còn ở trạng thái Testing.
- Scope email/profile cho phép backend đọc email đã verify, tên hiển thị.

## Bước 3. Tạo Web OAuth Client

Đã tạo Web application client:

```text
Client ID: 826757086511-es3htk7un7lq7lvlpmkqppmp3h7nnjd2.apps.googleusercontent.com
```

Client này dùng ở hai nơi:

- Android dùng làm `serverClientId` khi xin Google ID token.
- Backend dùng làm `Audience` khi validate ID token.

Điểm dễ sai:

> Android Credential Manager phải dùng Web Client ID làm `serverClientId`, không dùng Android Client ID ở chỗ này.

## Bước 4. Tạo Android OAuth Client

Đã tạo Android OAuth client với:

```text
Package name: com.example.shop
SHA-1: A4:E5:DC:3D:48:AD:F0:8A:5B:38:5E:58:6C:90:22:3B:A0:6E:CB:C6
```

Lý do cần Android client:

- Google kiểm tra app Android có đúng package name không.
- Google kiểm tra chữ ký debug SHA-1 có nằm trong client không.
- Nếu package name hoặc SHA-1 lệch, Google Sign-In sẽ lỗi dù Web Client ID đúng.

## Bước 5. Dùng debug keystore chung

Repo có file:

```text
app/debug.keystore
```

`app/build.gradle.kts` đã cấu hình debug signing:

```kotlin
signingConfigs {
    create("sharedDebug") {
        storeFile = file("debug.keystore")
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
    }
}

buildTypes {
    debug {
        signingConfig = signingConfigs.getByName("sharedDebug")
    }
}
```

Lý do làm vậy:

- Mọi thành viên clone repo có cùng SHA-1 debug.
- Google Cloud chỉ cần cấu hình một Android OAuth client.
- Tránh lỗi mỗi máy một SHA-1 khác nhau.

Kiểm tra SHA-1:

```bash
keytool -list -v \
  -keystore app/debug.keystore \
  -alias androiddebugkey \
  -storepass android \
  -keypass android
```

## Bước 6. Thêm dependency Android

Trong `app/build.gradle.kts` đã thêm:

```kotlin
implementation("androidx.credentials:credentials:1.3.0")
implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
```

Ý nghĩa:

- `credentials`: API Credential Manager của Android.
- `credentials-play-services-auth`: kết nối Credential Manager với Google Play services.
- `googleid`: parse Google ID token credential.

Yêu cầu runtime:

- Emulator phải là image có Google Play.
- Không dùng image chỉ có Google APIs nếu muốn test Google Sign-In ổn định.

## Bước 7. Lưu Web Client ID trong Android

Trong `app/src/main/java/com/example/shop/utils/Constants.kt`:

```kotlin
const val GOOGLE_WEB_CLIENT_ID =
    "826757086511-es3htk7un7lq7lvlpmkqppmp3h7nnjd2.apps.googleusercontent.com"
```

Lý do:

- `LoginScreen` cần client id này để xin đúng ID token.
- Backend cũng dùng cùng client id để validate audience.

Nếu Android dùng client id khác backend, backend sẽ trả:

```text
Invalid Google token.
```

## Bước 8. Lấy Google ID token trên Android

Trong `LoginScreen.kt`, hàm `getGoogleIdToken(...)` tạo Google option:

```kotlin
val googleIdOption = GetGoogleIdOption.Builder()
    .setFilterByAuthorizedAccounts(false)
    .setServerClientId(Constants.GOOGLE_WEB_CLIENT_ID)
    .build()
```

Sau đó tạo request:

```kotlin
val request = GetCredentialRequest.Builder()
    .addCredentialOption(googleIdOption)
    .build()
```

Rồi gọi Credential Manager:

```kotlin
val result = credentialManager.getCredential(context, request)
GoogleIdTokenCredential.createFrom(result.credential.data).idToken
```

Ý nghĩa:

- Android chỉ lấy `idToken`.
- Android không tự tạo user.
- Android không tự cấp JWT.

## Bước 9. Gắn nút Continue With Google

Trong `LoginScreen.kt`, nút Google gọi:

```kotlin
val idToken = getGoogleIdToken(credentialManager, context)
viewModel.loginWithGoogle(idToken) { user, backendError -> ... }
```

Nếu không lấy được token:

```text
Không thể đăng nhập Google.
```

Nếu backend không chấp nhận:

```text
Backend không chấp nhận tài khoản Google này.
```

Lý do cần phân biệt lỗi:

- Lỗi trước backend thường do emulator, Google Play services, OAuth client.
- Lỗi từ backend thường do `Google:ClientId`, email chưa verify, token audience sai.

## Bước 10. Thêm DTO Android

Trong `AuthDtos.kt` có request:

```kotlin
data class GoogleLoginRequest(
    val idToken: String
)
```

DTO này là body gửi về backend.

## Bước 11. Thêm Retrofit endpoint Android

Trong `AuthApi.kt`:

```kotlin
@POST("api/auth/google")
suspend fun googleLogin(@Body request: GoogleLoginRequest): LoginResponse
```

Endpoint này trả cùng kiểu `LoginResponse` với login thường:

- token JWT nội bộ.
- user.

Lý do dùng chung response:

- Sau Google login, app xử lý session giống login email/password.
- Không cần viết riêng cơ chế token cho Google.

## Bước 12. Thêm hàm trong AuthRepository

Trong `AuthRepository.kt`, `loginWithGoogle(idToken)`:

- Gọi `AuthApi.googleLogin(...)`.
- Nhận `LoginResponse`.
- Gọi `saveSession(response.token, response.user)`.
- Trả result cho ViewModel.

Nhiệm vụ của repository:

- Che Retrofit khỏi ViewModel.
- Lưu token.
- Lưu current user.
- Chuẩn hóa lỗi backend.

## Bước 13. Thêm hàm trong AuthViewModel

Trong `AuthViewModel.kt`, `loginWithGoogle(...)`:

- Chạy coroutine.
- Gọi repository.
- Trả user hoặc error message về UI.

Lý do:

- UI không gọi API trực tiếp.
- ViewModel giữ đúng vai trò xử lý event login.

## Bước 14. Cấu hình Google Client ID ở backend

Trong `backend/ShopApi/appsettings.json`:

```json
"Google": {
  "ClientId": "826757086511-es3htk7un7lq7lvlpmkqppmp3h7nnjd2.apps.googleusercontent.com"
}
```

Khi deploy có thể dùng biến môi trường:

```bash
Google__ClientId=826757086511-es3htk7un7lq7lvlpmkqppmp3h7nnjd2.apps.googleusercontent.com
```

Không cần commit Google client secret cho flow hiện tại.

Lý do:

- Backend chỉ validate `idToken` theo audience.
- Flow này không dùng authorization code exchange phía server.
- Client secret là credential thật, không nên lưu git.

## Bước 15. Thêm package Google Auth backend

Trong `backend/ShopApi/ShopApi.csproj` đã có:

```xml
<PackageReference Include="Google.Apis.Auth" Version="1.68.0" />
```

Package này cung cấp:

```csharp
GoogleJsonWebSignature.ValidateAsync(...)
```

Backend dùng hàm này để xác minh ID token do Google phát hành.

## Bước 16. Thêm cột GoogleSub cho User

Trong `User.cs` có:

```csharp
public string? GoogleSub { get; set; }
```

Trong `ShopDbContext.cs`:

```csharp
entity.Property(user => user.GoogleSub).HasMaxLength(64);
entity.HasIndex(user => user.GoogleSub).IsUnique();
```

Lý do cần `GoogleSub`:

- `payload.Subject` là id ổn định của tài khoản Google.
- Email có thể thay đổi hoặc bị link sai.
- Unique index chặn hai user trùng cùng một tài khoản Google.

## Bước 17. Thêm endpoint backend `POST /api/auth/google`

Trong `AuthController.cs`, action `Google(...)` làm các việc:

1. Đọc `Google:ClientId`.
2. Nếu thiếu config, throw lỗi cấu hình.
3. Gọi `GoogleJsonWebSignature.ValidateAsync(...)`.
4. Set `Audience = googleClientId`.
5. Nếu token sai, trả `401 Unauthorized`.
6. Kiểm tra `payload.EmailVerified`.
7. Lấy email lowercase.
8. Tìm user theo `GoogleSub`.
9. Nếu chưa thấy, tìm user theo email.
10. Nếu email chưa tồn tại, tạo user mới role `USER`.
11. Nếu email tồn tại nhưng chưa link Google, gán `GoogleSub`.
12. Nếu email đã link Google khác, trả `409 Conflict`.
13. Lưu database nếu có tạo/link user.
14. Gọi `CreateToken(user)`.
15. Trả `LoginResponse(token, user)`.

Điểm quan trọng:

> Backend không tin email do Android tự gửi. Backend chỉ tin email nằm trong ID token đã validate với Google.

## Bước 18. Dùng lại JWT nội bộ

Sau khi Google hợp lệ, backend gọi:

```csharp
var token = CreateToken(user);
return Ok(new LoginResponse(token, ToUserResponse(user)));
```

JWT có claims:

- user id.
- username.
- email.
- role.

Android nhận JWT rồi gọi API nội bộ bằng:

```text
Authorization: Bearer <token>
```

## Bước 19. Điều hướng theo role

Sau khi Google login trả user:

- Role `ADMIN` vào admin dashboard.
- Role `USER` vào home.

Điểm vấn đáp:

> Google OAuth chỉ là cách đăng nhập. Phân quyền admin/user vẫn dựa vào role trong user nội bộ và JWT do backend cấp.

## Bước 20. Test tích hợp

Checklist test:

1. Backend chạy ở `http://localhost:5053`.
2. Android dùng base URL `http://10.0.2.2:5053/`.
3. Emulator là Google Play image.
4. App debug dùng `app/debug.keystore`.
5. Tài khoản Google nằm trong test users nếu OAuth app còn Testing.
6. Bấm Continue With Google.
7. Chọn tài khoản Google.
8. Backend nhận `POST /api/auth/google`.
9. Database có user mới hoặc user cũ được link `GoogleSub`.
10. Android vào Home hoặc Admin theo role.

## Lỗi hay gặp

### Bấm Google nhưng không hiện tài khoản

Nguyên nhân thường gặp:

- Emulator không có Google Play.
- Chưa login Google trong emulator.
- Google Play services lỗi hoặc quá cũ.

### Backend báo `Invalid Google token`

Nguyên nhân thường gặp:

- Android dùng sai `GOOGLE_WEB_CLIENT_ID`.
- Backend `Google:ClientId` khác Android.
- Android OAuth client thiếu SHA-1 debug.
- Package name không đúng `com.example.shop`.

### Google báo app chưa được phép

Nguyên nhân thường gặp:

- OAuth app đang Testing.
- Tài khoản test chưa được thêm vào Google Auth Platform.

### Email đã link Google khác

Backend trả conflict khi:

- Email đã tồn tại.
- User đó đã có `GoogleSub`.
- GoogleSub hiện tại khác GoogleSub trong token mới.

Lý do chặn:

> Không cho một email nội bộ bị chiếm hoặc đổi sang tài khoản Google khác tùy tiện.

## Câu trả lời ngắn khi vấn đáp

> Để tích hợp Google OAuth, em tạo Google Cloud project, cấu hình consent screen, tạo Web Client ID, tạo Android Client theo package name và SHA-1 debug. Android thêm Credential Manager để lấy Google ID token bằng Web Client ID rồi gửi token về `POST /api/auth/google`. Backend dùng `Google.Apis.Auth` validate token theo `Google:ClientId`, kiểm tra email verified, tìm hoặc tạo user theo `GoogleSub`, sau đó cấp JWT nội bộ để app gọi các API còn lại.
