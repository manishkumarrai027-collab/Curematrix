package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Billing
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the 'Billing' entity class.
 * Tracks medical costs, payment status, and dates for patient billing.
 */
@Dao
interface BillingDao {
    @Query("SELECT * FROM billing ORDER BY id DESC")
    fun getAllBills(): Flow<List<Billing>>

    @Query("SELECT * FROM billing WHERE id = :id LIMIT 1")
    fun getBillById(id: Long): Flow<Billing?>

    @Query("SELECT * FROM billing WHERE patientId = :patientId ORDER BY id DESC")
    fun getBillsForPatient(patientId: Long): Flow<List<Billing>>

    @Query("SELECT * FROM billing WHERE paymentStatus = :status ORDER BY id DESC")
    fun getBillsByStatus(status: String): Flow<List<Billing>>

    @Query("SELECT * FROM billing WHERE date = :date ORDER BY id DESC")
    fun getBillsByDate(date: String): Flow<List<Billing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Billing): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bills: List<Billing>)

    @Update
    suspend fun updateBill(bill: Billing)

    @Delete
    suspend fun deleteBill(bill: Billing)

    @Query("UPDATE billing SET paymentStatus = 'Paid' WHERE id = :billId")
    suspend fun markBillAsPaid(billId: Long)

    @Query("UPDATE billing SET paymentStatus = :status WHERE id = :billId")
    suspend fun updatePaymentStatus(billId: Long, status: String)

    @Query("SELECT SUM(CASE WHEN amount > 0 THEN amount ELSE totalAmount END) FROM billing WHERE paymentStatus = 'Paid'")
    fun getTotalPaidRevenue(): Flow<Double?>

    @Query("SELECT SUM(CASE WHEN amount > 0 THEN amount ELSE totalAmount END) FROM billing WHERE paymentStatus = 'Pending'")
    fun getTotalPendingAmount(): Flow<Double?>
}
