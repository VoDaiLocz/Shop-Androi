# Cấu Hình Google OAuth

Ngày cập nhật: 2026-05-21
Trạng thái: Checkpoint 26 đã cấu hình xong trên Google Cloud Console

## Google Cloud Project

| Trường | Giá trị |
|---|---|
| Tên project | `shop-android` |
| Project ID | `zippy-nexus-497009-c3` |
| Console | `https://console.cloud.google.com/auth/clients?project=zippy-nexus-497009-c3` |
| Tài khoản cấu hình | `locv2659@gmail.com` |

## OAuth Consent

| Mục | Giá trị |
|---|---|
| Nền tảng | Google Auth Platform |
| User type | External |
| Publishing status | Testing |
| Test user | `locv2659@gmail.com` |
| OAuth user cap | `1 user (1 test, 0 other) / 100 user cap` |

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

### Android Debug

| Trường | Giá trị |
|---|---|
| Name | `Shop Android Debug` |
| Type | Android |
| Client ID | `826757086511-gn16lrjbrlu8ml1mtodmulv81qbi7o8v.apps.googleusercontent.com` |
| Package name | `com.example.shop` |
| SHA-1 | `33:DB:2C:01:F2:D8:E2:70:27:39:2F:EB:1B:5A:BF:8B:EA:A9:A2:86` |
| SHA-256 | `69:3F:6B:B7:90:DF:57:95:5B:3C:5F:84:D1:14:A9:9B:07:8C:91:C2:CF:41:79:C6:FC:DC:B9:C2:A7:18:89:48` |

## Debug Keystore Local

Keystore đã dùng để lấy fingerprint:

```text
/home/vodailoc/.android/debug.keystore
```

Lệnh kiểm tra:

```bash
keytool -list -v \
  -keystore /home/vodailoc/.android/debug.keystore \
  -alias androiddebugkey \
  -storepass android \
  -keypass android
```

Kết quả fingerprint đã verify ngày 2026-05-21:

```text
Alias name: androiddebugkey
Owner: CN=Android Debug, O=Android, C=US
SHA1: 33:DB:2C:01:F2:D8:E2:70:27:39:2F:EB:1B:5A:BF:8B:EA:A9:A2:86
SHA256: 69:3F:6B:B7:90:DF:57:95:5B:3C:5F:84:D1:14:A9:9B:07:8C:91:C2:CF:41:79:C6:FC:DC:B9:C2:A7:18:89:48
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
| Trang Clients có `Shop Android Debug`, type Android, và Android Client ID | PASS |
| Trang Audience hiển thị `Testing`, `External`, `1 user`, và `locv2659@gmail.com` | PASS |
| Trang Data Access hiển thị `openid`, `userinfo.email`, và `userinfo.profile` | PASS |
| Output `keytool` local khớp SHA-1 Android OAuth | PASS |

Chưa test trong checkpoint này:

- Backend `/api/auth/google`, vì đây là Checkpoint 27 và chưa implement.
- Android Credential Manager Google login, vì đây là Checkpoint 28 và chưa implement.
- End-to-end Google login, vì cần hoàn thành Checkpoint 27 và Checkpoint 28 trước.
