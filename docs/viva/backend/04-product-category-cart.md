# Endpoint backend cho product category cart

File này dùng để học backend theo action gọi vào API. Mỗi mục bên dưới gắn với một hành động cụ thể trong app. Trace từ UI đến database nằm ở `../shared/03-trace-chi-tiet-theo-chuc-nang.md`.

## Flow 10. Home load sản phẩm gọi `GET /api/products`

- Controller: `ProductsController.GetAll`.
- Quyền: public.
- Android gọi từ `ProductApi.getProducts()`.
- Backend đọc bảng `Products`.
- Backend include `Categories`.
- Response trả id, name, price, description, imageUrl, quantity, categoryId, categoryName.
- Điểm vấn đáp: endpoint này không cần JWT vì khách có thể xem sản phẩm.

## Flow 11. Home load danh mục gọi `GET /api/categories`

- Controller: `CategoriesController.GetAll`.
- Quyền: public.
- Android gọi từ `CategoryApi.getCategories()`.
- Backend đọc bảng `Categories`.
- Response trả danh sách category.
- Điểm vấn đáp: category dùng cho chip ở Home, form admin product, màn quản lý category.

## Flow 13. Bấm product card gọi `GET /api/products/{id}`

- Controller: `ProductsController.GetById`.
- Quyền: public.
- Android gọi khi cần product detail theo id.
- Backend đọc bảng `Products`.
- Backend include `Category`.
- Nếu không có product, backend trả `404 NotFound`.

## Flow 14. Bấm Add to Cart gọi `POST /api/cart/items`

- Controller: `CartController.AddItem`.
- Quyền: JWT user.
- Android gửi `productId`, `quantity`.
- Backend lấy user id từ `ClaimTypes.NameIdentifier`.
- Backend kiểm tra quantity lớn hơn 0.
- Backend kiểm tra product tồn tại.
- Backend tìm `CartItems` theo user id, product id.
- Nếu chưa có item, backend insert cart item.
- Nếu đã có item, backend cộng quantity.
- Backend trả cart mới.

## Flow 15. Bấm icon cart gọi `GET /api/cart`

- Controller: `CartController.GetCart`.
- Quyền: JWT user.
- Backend lấy user id từ JWT.
- Backend đọc `CartItems` thuộc user.
- Backend include `Products`.
- Backend trả cart response để Android render.

## Flow 16. Bấm nút cộng cart gọi `PUT /api/cart/items/{id}`

- Controller: `CartController.UpdateItem`.
- Quyền: JWT user.
- Android gửi cart item id, quantity mới.
- Backend chỉ đọc cart item theo id kèm user id.
- Nếu item không thuộc user, backend trả `404 NotFound`.
- Backend update quantity.
- Backend trả cart mới.

## Flow 17. Bấm nút trừ cart gọi `PUT /api/cart/items/{id}`

- Controller: `CartController.UpdateItem`.
- Quyền: JWT user.
- Action này chỉ gọi PUT khi quantity sau khi trừ vẫn lớn hơn 0.
- Backend kiểm tra item thuộc user hiện tại.
- Backend update quantity.
- Backend trả cart mới.
- Nếu quantity bằng 1, Android chuyển sang Flow 18 thay vì gọi PUT.

## Flow 18. Bấm icon delete cart gọi `DELETE /api/cart/items/{id}`

- Controller: `CartController.DeleteItem`.
- Quyền: JWT user.
- Backend chỉ xóa cart item theo id kèm user id.
- Nếu item không thuộc user, backend trả `404 NotFound`.
- Backend xóa cart item.
- Backend trả cart mới.
- Điểm vấn đáp: dùng cart item id để xóa đúng dòng giỏ hàng, không dùng product id.

## Flow 30. Bấm Lưu sản phẩm admin gọi `POST /api/products`

- Controller: `ProductsController.Create`.
- Quyền: ADMIN.
- Backend validate name, description, price, quantity.
- Backend kiểm tra category tồn tại.
- Backend ghi `Products`.
- Nếu Android upload ảnh sau đó, endpoint tiếp theo là `POST /api/products/{id}/image`.

## Flow 32. Bấm Cập nhật sản phẩm admin gọi `PUT /api/products/{id}`

- Controller: `ProductsController.Update`.
- Quyền: ADMIN.
- Backend tìm product theo id.
- Backend validate request.
- Backend kiểm tra category tồn tại.
- Backend update `Products`.
- Nếu Android upload ảnh mới, backend update `ImageUrl`.

## Flow 33. Bấm xóa sản phẩm admin gọi `DELETE /api/products/{id}`

- Controller: `ProductsController.Delete`.
- Quyền: ADMIN.
- Backend tìm product theo id.
- Backend kiểm tra `OrderItems`.
- Backend kiểm tra `CartItems`.
- Nếu product đang được tham chiếu, backend trả `409 Conflict`.
- Nếu an toàn, backend xóa product.

## Flow 36. Bấm Lưu danh mục admin gọi `POST /api/categories`

- Controller: `CategoriesController.Create`.
- Quyền: ADMIN.
- Backend validate tên category.
- Backend ghi `Categories`.
- Backend trả category mới.

## Flow 38. Bấm Cập nhật danh mục admin gọi `PUT /api/categories/{id}`

- Controller: `CategoriesController.Update`.
- Quyền: ADMIN.
- Backend tìm category theo id.
- Backend validate tên category.
- Backend update `Categories`.

## Flow 39. Bấm xóa danh mục admin gọi `DELETE /api/categories/{id}`

- Controller: `CategoriesController.Delete`.
- Quyền: ADMIN.
- Backend tìm category theo id.
- Backend kiểm tra category còn product không.
- Nếu còn product, backend trả `409 Conflict`.
- Nếu không còn product, backend xóa category.

## Câu hỏi hay bị hỏi

Vì sao cart item dùng id riêng?

> Vì cart item là một dòng dữ liệu riêng trong giỏ hàng. Một product chỉ nói sản phẩm nào, còn cart item id nói đúng dòng giỏ hàng của đúng user.

Vì sao mọi cart endpoint phải kiểm tra user id?

> Để user không sửa hoặc xóa cart item của người khác bằng cách đoán id.
