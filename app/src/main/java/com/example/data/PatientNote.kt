package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patient_notes")
data class PatientNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: String,
    val gestationalWeeks: Int,
    val conditionType: String, // e.g., "GDM", "Preeclampsia", "ICP", "Other"
    val bloodPressureSystolic: Int? = null,
    val bloodPressureDiastolic: Int? = null,
    val bloodGlucoseFasting: Double? = null,
    val bloodGlucose1h: Double? = null,
    val bloodGlucose2h: Double? = null,
    val bileAcidValue: Double? = null,
    val clinicalNotes: String,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
