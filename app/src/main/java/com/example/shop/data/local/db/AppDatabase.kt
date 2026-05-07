package com.example.shop.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.shop.data.local.dao.AddressDao
import com.example.shop.data.model.Address

@Database(
    entities = [
        Address::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun addressDao(): AddressDao


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
