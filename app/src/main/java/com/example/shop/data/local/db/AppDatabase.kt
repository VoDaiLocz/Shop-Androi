package com.example.shop.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.shop.data.local.dao.CartDao
import com.example.shop.data.local.dao.CategoryDao
import com.example.shop.data.local.dao.OrderDao
import com.example.shop.data.local.dao.ProductDao
import com.example.shop.data.local.dao.UserDao
import com.example.shop.data.model.CartItem
import com.example.shop.data.model.Category
import com.example.shop.data.model.Order
import com.example.shop.data.model.OrderItem
import com.example.shop.data.model.Product
import com.example.shop.data.model.User

@Database(
    entities = [
        Product::class,
        User::class,
        Category::class,
        CartItem::class,
        Order::class,
        OrderItem::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_db"
                )
                .fallbackToDestructiveMigration() //tự động xóa db cũ và tạo mới
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}