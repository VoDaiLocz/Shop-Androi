# Cấu Hình Google OAuth

Ngày cập nhật: 2026-05-22
Trạng thái: Google Sign-In đã chạy được E2E trên emulator Google Play. Repo đã có debug keystore dùng chung để thành viên nhóm clone về dùng cùng một SHA-1.

## Google Cloud Project

| Trường | Giá trị |
|---|---|
| Tên project | `shop-android` |
| Project ID | `zippy-nexus-497009-c3` |
| Console | `https://console.cloud.google.com/auth/clients?project=zippy-nexus-497009-c3` |
| Tài khoản cấu hình | `locv2659@gmail.com` |

## Quyền Xem Google Cloud Console

Các quyền IAM dưới đây chỉ dùng để thành viên nhóm xem cấu hình Google Cloud/OAuth khi cần kiểm tra lỗi đăng nhập Google. Không dùng các quyền này để đăng nhập app.

| Tài khoản | IAM role | Mục đích |
|---|---|---|
| `khoaduy2608@gmail.com` | `Browser` | Cho phép nhìn thấy project và duyệt tài nguyên cơ bản trong Google Cloud Console |
| `khoaduy2608@gmail.com` | `OAuth Config Viewer (Beta)` | Cho phép xem cấu hình OAuth client, consent screen, test users, scopes |

Giới hạn quyền:

- Không có quyền `Owner`.
- Không có quyền `Editor`.
- Không có quyền sửa IAM.
- Không có quyền sửa OAuth config.
- Không có quyền billing/deployment/resource admin.

Nếu cần thay đổi cấu hình OAuth, dùng tài khoản owner `locv2659@gmail.com` để thực hiện rồi cập nhật lại tài liệu này.

## OAuth Consent

| Mục | Giá trị |
|---|---|
| Nền tảng | Google Auth Platform |
| User type | External |
| Publishing status | Testing |
| Test users | `locv2659@gmail.com`, `khoaduy2608@gmail.com` |
| OAuth user cap | `2 users (2 test, 0 other) / 100 user cap` |

Các scope non-sensitive đã lưu:

| Scope | Mô tả trên Google Console |
|---|---|
| `openid` | Associate you with your personal info on Google |
| `https://www.googleapis.com/auth/userinfo.email` | See your primary Google Account email address |
| `https://www.googleapis.com/auth/userinfo.profile` | See your personal info, including any personal info you've made publicly available |

## OAuth Clients

### Web Application

| Trường | Giá trị |
|---|---|
| Name | `Shop Backend` |
| Type | Web application |
| Client ID | `826757086511-es3htk7un7lq7lvlpmkqppmp3h7nnjd2.apps.googleusercontent.com` |
| Mục đích | Backend verify Google `id_token`; Android Credential Manager dùng làm server client ID |

### Android Shared Debug

| Trường | Giá trị |
|---|---|
| Name | `Shop Android Shared Debug` |
| Type | Android |
| Client ID | `826757086511-v1t2ise2a6e6c4133jlifgguploueojj.apps.googleusercontent.com` |
| Package name | `com.example.shop` |
| SHA-1 | `A4:E5:DC:3D:48:AD:F0:8A:5B:38:5E:58:6C:90:22:3B:A0:6E:CB:C6` |
| SHA-256 | `67:8C:B5:89:85:F0:8E:86:61:5E:21:81:B5:91:3C:98:ED:E5:E2:05:DE:C1:CC:EA:1E:2A:3C:21:E9:26:34:90` |
| Mục đích | Client chính cho debug build của repository. Thành viên nhóm clone repo sẽ dùng fingerprint này. |

### Android Debug Cũ

Client cũ vẫn để lại trên Google Console để không làm hỏng máy đã test trước đó:

| Trường | Giá trị |
|---|---|
| Name | `Shop Android Debug` |
| Type | Android |
| Client ID | `826757086511-gn16lrjbrlu8ml1mtodmulv81qbi7o8v.apps.googleusercontent.com` |
| Package name | `com.example.shop` |
| SHA-1 | `33:DB:2C:01:F2:D8:E2:70:27:39:2F:EB:1B:5A:BF:8B:EA:A9:A2:86` |

## Debug Keystore Dùng Chung Trong Repo

Repository có file:

```text
app/debug.keystore
```

`app/build.gradle.kts` đã cấu hình debug build dùng đúng file này:

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

Ý nghĩa:

- Mọi máy clone repo sẽ ký bản debug bằng cùng một keystore.
- SHA-1 debug giống nhau trên mọi máy.
- Google Cloud Console chỉ cần có một Android OAuth Client ID cho SHA-1 dùng chung.
- Không cần thêm SHA-1 debug cá nhân của từng thành viên nhóm.

Lưu ý: file này chỉ dùng cho debug/dev. Không dùng `app/debug.keystore` để ký bản release thật.

Lệnh kiểm tra:

```powershell
keytool -list -v \
  -keystore app/debug.keystore \
  -alias androiddebugkey \
  -storepass android \
  -keypass android
```

Kết quả fingerprint đã verify ngày 2026-05-21:

```text
Alias name: androiddebugkey
Owner: CN=Android Debug, O=Android, C=US
SHA1: A4:E5:DC:3D:48:AD:F0:8A:5B:38:5E:58:6C:90:22:3B:A0:6E:CB:C6
SHA256: 67:8C:B5:89:85:F0:8E:86:61:5E:21:81:B5:91:3C:98:ED:E5:E2:05:DE:C1:CC:EA:1E:2A:3C:21:E9:26:34:90
```

## Nơi Dùng Các Giá Trị Này

Backend Checkpoint 27:

```json
{
  "Google": {
    "ClientId": "826757086511-es3htk7un7lq7lvlpmkqppmp3h7nnjd2.apps.googleusercontent.com"
  }
}
```

Biến môi trường production/deployment:

```bash
Google__ClientId=826757086511-es3htk7un7lq7lvlpmkqppmp3h7nnjd2.apps.googleusercontent.com
```

Android Checkpoint 28:

```kotlin
const val GOOGLE_WEB_CLIENT_ID = "826757086511-es3htk7un7lq7lvlpmkqppmp3h7nnjd2.apps.googleusercontent.com"
```

Lưu ý: Android Credential Manager phải xin ID token bằng Web Client ID ở trên trong `serverClientId`. Android Client ID vẫn cần có trên Google Console để Google chấp nhận package name và SHA-1 debug của app.

## Môi Trường Chạy Cho Thành Viên Nhóm

Google Sign-In không chỉ phụ thuộc vào version Android của emulator. Nó còn phụ thuộc vào Google Play services bên trong máy ảo.

Yêu cầu tối thiểu nên dùng:

```text
Emulator image: Google Play
Không dùng image chỉ ghi Google APIs
Package name: com.example.shop
Backend local: http://localhost:5053
Android app base URL: http://10.0.2.2:5053/
```

Image nhẹ nhất đã cài và test được:

```text
Android 26 Google Play x86
```

Google hiện không liệt kê `Android 26 Google Play x86_64` trong SDK Manager. Nếu cần đúng `x86_64`, dùng bản Google Play mới hơn, ví dụ Android 36.1 Google Play x86_64.

Không commit các phần sau vào repository:

- Android SDK.
- AVD/emulator config trong `.android/avd`.
- Log/screenshot khi test.

Riêng `app/debug.keystore` là ngoại lệ có chủ đích: đây là keystore debug dùng chung cho nhóm, không phải keystore release.

Nếu Google Sign-In báo lỗi trên máy mới, kiểm tra theo thứ tự:

1. Máy ảo phải là image `Google Play`, không phải chỉ `Google APIs`.
2. App debug phải được build từ repo này để dùng `app/debug.keystore`.
3. Backend phải đang chạy ở `http://localhost:5053`.
4. Android vẫn dùng base URL `http://10.0.2.2:5053/`.
5. Không đổi Web Client ID trong code Android/backend nếu vẫn dùng cùng project Google Cloud.

## Xử Lý Secret

Google đã tạo client secret cho Web application client, nhưng secret đó không được ghi vào repository này.

Lý do:

- Luồng Google login đã chốt chỉ verify `id_token` do Android phát hành theo audience, nên chỉ cần Web Client ID.
- OAuth client secret là credential thật. Không commit secret kể cả khi repository private.
- Nếu sau này cần secret cho luồng OAuth server-side khác, lấy trực tiếp từ Google Cloud Console và lưu trong secret store local hoặc biến môi trường deployment, không lưu trong git.

## Kết Quả Verify

Fresh verification ngày 2026-05-21:

| Kiểm tra | Kết quả |
|---|---|
| Trang Clients có `Shop Backend`, type Web application, và Web Client ID | PASS |
| Trang Clients có `Shop Android Shared Debug`, type Android, package `com.example.shop`, và SHA-1 của `app/debug.keystore` | PASS |
| Trang Clients vẫn còn `Shop Android Debug` cũ để tương thích máy test trước đó | PASS |
| Trang Audience hiển thị `Testing`, `External`, `2 users`, `locv2659@gmail.com`, và `khoaduy2608@gmail.com` | PASS |
| Trang Data Access hiển thị `openid`, `userinfo.email`, và `userinfo.profile` | PASS |
| Output `keytool` của `app/debug.keystore` khớp SHA-1 Android OAuth | PASS |
| Android Google Sign-In E2E trên emulator Google Play, chọn tài khoản Google và vào Home | PASS |
