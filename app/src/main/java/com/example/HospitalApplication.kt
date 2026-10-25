package com.example

import android.app.Application
import com.example.data.database.HospitalDatabase
import com.example.data.repository.HospitalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class HospitalApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { HospitalDatabase.getDatabase(this, applicationScope) }
    val repository by lazy {
        HospitalRepository(
            patientDao = database.patientDao(),
            doctorDao = database.doctorDao(),
            appointmentDao = database.appointmentDao(),
            wardBedDao = database.wardBedDao(),
            billingDao = database.billingDao(),
            prescriptionDao = database.prescriptionDao(),
            medicalRecordDao = database.medicalRecordDao(),
            labResultDao = database.labResultDao(),
            treatmentPlanDao = database.treatmentPlanDao()
        )
    }
}
