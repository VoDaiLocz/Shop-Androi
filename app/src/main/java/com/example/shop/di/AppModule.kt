package com.example.shop.di

import android.content.Context
import com.example.shop.data.local.dao.ProductDao
import com.example.shop.data.local.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao {
        return db.productDao() // Gọi hàm này từ AppDatabase của bạn
    }
}