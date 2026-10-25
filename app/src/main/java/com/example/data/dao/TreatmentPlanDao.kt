package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TreatmentPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface TreatmentPlanDao {
    @Query("SELECT * FROM treatment_plans ORDER BY startDateMillis DESC")
    fun getAllTreatmentPlans(): Flow<List<TreatmentPlan>>

    @Query("SELECT * FROM treatment_plans WHERE patientId = :patientId ORDER BY startDateMillis DESC")
    fun getTreatmentPlansForPatient(patientId: Long): Flow<List<TreatmentPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreatmentPlan(plan: TreatmentPlan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plans: List<TreatmentPlan>)

    @Update
    suspend fun updateTreatmentPlan(plan: TreatmentPlan)

    @Delete
    suspend fun deleteTreatmentPlan(plan: TreatmentPlan)
}
