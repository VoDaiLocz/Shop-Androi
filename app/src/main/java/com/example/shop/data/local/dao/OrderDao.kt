package com.example.shop.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.shop.data.model.Order
import com.example.shop.data.model.OrderItem
import com.example.shop.data.model.OrderWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    // 1. Thêm một đơn hàng mới
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    // 2. Thêm danh sách các sản phẩm trong đơn hàng đó
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItem>)

    // 3. Lấy lịch sử đơn hàng của một User (gồm cả chi tiết sản phẩm)
    @Transaction
    @Query("SELECT * FROM orders WHERE userId = :uId ORDER BY orderDate DESC")
    fun getOrdersWithItemsByUserId(uId: Int): Flow<List<OrderWithItems>>

    // 4. (Tùy chọn) Admin lấy toàn bộ đơn hàng
    @Transaction
    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    fun getALLOrders(): Flow<List<OrderWithItems>>
}
