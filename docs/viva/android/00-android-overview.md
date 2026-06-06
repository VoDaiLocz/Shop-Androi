# Android overview

## Công nghệ

- Kotlin.
- Jetpack Compose.
- Navigation Compose.
- Hilt Dependency Injection.
- ViewModel.
- StateFlow.
- Retrofit.
- Gson converter.
- Coil/AsyncImage để load ảnh.

## Kiến trúc Android

```text
Screen
-> ViewModel
-> Repository
-> Retrofit API interface
-> Backend
```

## Các package chính

```text
ui/              màn hình Compose
admin/ui/        màn hình admin
viewmodel/       ViewModel user flow
admin/viewmodel/ ViewModel admin flow
data/repository/ Repository gọi API và giữ state
data/remote/api/ Retrofit interface
data/remote/dto/ DTO request/response
data/model/      Model dùng trong app
navigation/      Routes và MainNavGraph
di/              Hilt module
utils/           Constants, extension, resource
```

## App startup

```text
MainActivity
-> MainScreen
-> MainNavGraph
-> LoginScreen
```

## DI

`AppModule` cung cấp:

- `OkHttpClient`
- `Retrofit`
- `AuthApi`
- `ProductApi`
- `CartApi`
- `CategoryApi`
- `OrderApi`
- `UserApi`

Repository được Hilt tạo qua constructor injection:

```kotlin
class CartRepository @Inject constructor(...)
```

## StateFlow

Repository giữ dữ liệu:

- `AuthRepository.currentUser`
- `AuthRepository.currentToken`
- `CartRepository._cartItems`
- `ProductRepository._products`
- `CategoryRepository._categories`
- `OrderRepository._myOrders`
- `OrderRepository._allOrders`

UI dùng `collectAsState()` để tự cập nhật khi state thay đổi.

## Câu trả lời mẫu

> Android dùng MVVM. Screen chỉ hiển thị UI và gọi ViewModel. ViewModel xử lý event và gọi Repository. Repository gọi Retrofit API, lấy token từ AuthRepository, map DTO sang model và cập nhật StateFlow để UI tự render lại.
