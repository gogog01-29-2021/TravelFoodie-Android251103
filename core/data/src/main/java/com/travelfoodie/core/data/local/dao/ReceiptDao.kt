package com.travelfoodie.core.data.local.dao

import androidx.room.*
import com.travelfoodie.core.data.local.entity.ReceiptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {

    @Query("SELECT * FROM receipts ORDER BY createdAt DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE restaurantId = :restaurantId ORDER BY createdAt DESC")
    fun getReceiptsByRestaurant(restaurantId: String): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE receiptId = :receiptId")
    suspend fun getReceiptById(receiptId: String): ReceiptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity)

    @Update
    suspend fun updateReceipt(receipt: ReceiptEntity)

    @Delete
    suspend fun deleteReceipt(receipt: ReceiptEntity)

    @Query("DELETE FROM receipts WHERE receiptId = :receiptId")
    suspend fun deleteReceiptById(receiptId: String)
}
