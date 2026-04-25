package com.example.shop.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.shop.data.model.Address
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses WHERE userId = :userId")
    fun getAddressesByUserId(userId: Int): Flow<List<Address>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: Address)

    @Delete
    suspend fun deleteAddress(address: Address)

    @Query("UPDATE addresses SET isDefault = 0 WHERE userId = :userId")
    suspend fun resetDefaults(userId: Int)

    @Transaction
    suspend fun setAsDefault(addresId: Int, userId: Int) {
        resetDefaults(userId)
    }
}