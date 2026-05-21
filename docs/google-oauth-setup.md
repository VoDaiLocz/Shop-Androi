# Google OAuth Setup

Ngay cap nhat: 2026-05-21
Trang thai: Checkpoint 26 da cau hinh xong tren Google Cloud Console

## Google Cloud Project

| Truong | Gia tri |
|---|---|
| Project name | `shop-android` |
| Project ID | `zippy-nexus-497009-c3` |
| Console | `https://console.cloud.google.com/auth/clients?project=zippy-nexus-497009-c3` |
| Tai khoan cau hinh | `locv2659@gmail.com` |

## OAuth Consent

| Muc | Gia tri |
|---|---|
| Platform | Google Auth Platform |
| User type | External |
| Publishing status | Testing |
| Test user | `locv2659@gmail.com` |
| OAuth user cap | `1 user (1 test, 0 other) / 100 user cap` |

Non-sensitive scopes da luu:

| Scope | Mo ta tren Google Console |
|---|---|
| `openid` | Associate you with your personal info on Google |
| `https://www.googleapis.com/auth/userinfo.email` | See your primary Google Account email address |
| `https://www.googleapis.com/auth/userinfo.profile` | See your personal info, including any personal info you've made publicly available |

## OAuth Clients

### Web Application

| Truong | Gia tri |
|---|---|
| Name | `Shop Backend` |
| Type | Web application |
| Client ID | `826757086511-es3htk7un7lq7lvlpmkqppmp3h7nnjd2.apps.googleusercontent.com` |
| Muc dich | Backend verify Google `id_token`; Android Credential Manager dung lam server client ID |

### Android Debug

| Truong | Gia tri |
|---|---|
| Name | `Shop Android Debug` |
| Type | Android |
| Client ID | `826757086511-gn16lrjbrlu8ml1mtodmulv81qbi7o8v.apps.googleusercontent.com` |
| Package name | `com.example.shop` |
| SHA-1 | `33:DB:2C:01:F2:D8:E2:70:27:39:2F:EB:1B:5A:BF:8B:EA:A9:A2:86` |
| SHA-256 | `69:3F:6B:B7:90:DF:57:95:5B:3C:5F:84:D1:14:A9:9B:07:8C:91:C2:CF:41:79:C6:FC:DC:B9:C2:A7:18:89:48` |

## Local Debug Keystore

Keystore da dung de lay fingerprint:

```text
/home/vodailoc/.android/debug.keystore
```

Lenh kiem tra:

```bash
keytool -list -v \
  -keystore /home/vodailoc/.android/debug.keystore \
  -alias androiddebugkey \
  -storepass android \
  -keypass android
```

Ket qua fingerprint da verify ngay 2026-05-21:

```text
Alias name: androiddebugkey
Owner: CN=Android Debug, O=Android, C=US
SHA1: 33:DB:2C:01:F2:D8:E2:70:27:39:2F:EB:1B:5A:BF:8B:EA:A9:A2:86
SHA256: 69:3F:6B:B7:90:DF:57:95:5B:3C:5F:84:D1:14:A9:9B:07:8C:91:C2:CF:41:79:C6:FC:DC:B9:C2:A7:18:89:48
```

## Where To Use These Values

Backend Checkpoint 27:

```json
{
  "Google": {
    "ClientId": "826757086511-es3htk7un7lq7lvlpmkqppmp3h7nnjd2.apps.googleusercontent.com"
  }
}
```

Production/deployment environment variable:

```bash
Google__ClientId=826757086511-es3htk7un7lq7lvlpmkqppmp3h7nnjd2.apps.googleusercontent.com
```

Android Checkpoint 28:

```kotlin
const val GOOGLE_WEB_CLIENT_ID = "826757086511-es3htk7un7lq7lvlpmkqppmp3h7nnjd2.apps.googleusercontent.com"
```

Important: Android Credential Manager should request the ID token with the Web Client ID above as `serverClientId`. The Android Client ID is still needed in Google Console so Google accepts the app package and debug SHA-1.

## Secret Handling

Google created a client secret for the Web application client, but it is not recorded in this repository.

Reason:

- The planned Google login flow verifies Android-issued `id_token` by audience and only needs the Web Client ID.
- OAuth client secrets are credentials. Do not commit them even in a private repository.
- If the secret is ever needed for a different server-side OAuth flow, read it directly from Google Cloud Console and store it in a local secret store or deployment environment variable, not in git.

## Verification Performed

Fresh verification on 2026-05-21:

| Check | Result |
|---|---|
| Clients page contains `Shop Backend`, Web application type, and Web Client ID | PASS |
| Clients page contains `Shop Android Debug`, Android type, and Android Client ID | PASS |
| Audience page shows `Testing`, `External`, `1 user`, and `locv2659@gmail.com` | PASS |
| Data Access page shows `openid`, `userinfo.email`, and `userinfo.profile` | PASS |
| Local `keytool` output matches Android OAuth SHA-1 | PASS |

Not yet tested in this checkpoint:

- Backend `/api/auth/google`, because it is Checkpoint 27 and has not been implemented yet.
- Android Credential Manager Google login, because it is Checkpoint 28 and has not been implemented yet.
- End-to-end Google login, because it depends on Checkpoints 27 and 28.
