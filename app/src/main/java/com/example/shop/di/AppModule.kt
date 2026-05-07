package com.example.shop.di

import android.content.Context
import com.example.shop.data.local.dao.AddressDao
import com.example.shop.data.local.dao.CartDao
import com.example.shop.data.local.dao.ProductDao
import com.example.shop.data.local.dao.UserDao
import com.example.shop.data.local.dao.CategoryDao
import com.example.shop.data.local.dao.OrderDao
import com.example.shop.data.local.db.AppDatabase
import com.example.shop.data.repository.AddressRepository
import com.example.shop.data.repository.AuthRepository
import com.example.shop.data.repository.CartRepository
import com.example.shop.data.repository.OrderRepository
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

    //======================CartItem====================================
    @Provides
    fun provideCartDao(db: AppDatabase): CartDao {
        return db.cartDao()
    }

    //======================Order====================================
    @Provides
    fun provideOrderDao(db: AppDatabase): OrderDao {
        return db.orderDao()
    }

    @Provides
    fun provideAddressDao(db: AppDatabase): AddressDao {
        return db.addressDao()
    }

    // --- REPOSITORIES---

    @Provides
    @Singleton // Giúp giữ thông tin User khi chuyển màn hình
    fun provideAuthRepository(userDao: UserDao): AuthRepository {
        return AuthRepository(userDao)
    }

    @Provides
    @Singleton
    fun provideCartRepository(cartDao: CartDao): CartRepository {
        return CartRepository(cartDao)
    }

    @Provides
    @Singleton
    fun provideOrderRepository(orderDao: OrderDao, cartDao: CartDao): OrderRepository {
        return OrderRepository(orderDao, cartDao)
    }

    @Provides
    @Singleton
    fun provideAddressRepository(addressDao: AddressDao): AddressRepository {
        return AddressRepository(addressDao)
    }
}