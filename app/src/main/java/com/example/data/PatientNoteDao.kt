package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientNoteDao {
    @Query("SELECT * FROM patient_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<PatientNote>>

    @Query("SELECT * FROM patient_notes WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteNotes(): Flow<List<PatientNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: PatientNote): Long

    @Update
    suspend fun updateNote(note: PatientNote)

    @Query("DELETE FROM patient_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)

    @Query("SELECT * FROM patient_notes WHERE id = :id")
    suspend fun getNoteById(id: Int): PatientNote?
}
