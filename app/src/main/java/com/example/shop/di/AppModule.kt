package com.example.shop.diimport

import android.content.Context
import com.example.shop.data.local.dao.ProductDao
import com.example.shop.data.local.dao.UserDao
import com.example.shop.data.local.dao.CategoryDao // 1. Thêm import này
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

    //======================USER============================
    @Provides
    fun providerUserDao(db: AppDatabase): UserDao {
        return db.userDao()
    }

    //======================PRODUCT============================
    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao {
        return db.productDao()
    }

    //======================CATEGORY============================
    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao {
        return db.categoryDao()
    }
}