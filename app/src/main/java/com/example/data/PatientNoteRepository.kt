package com.example.data

import kotlinx.coroutines.flow.Flow

class PatientNoteRepository(private val dao: PatientNoteDao) {
    val allNotes: Flow<List<PatientNote>> = dao.getAllNotes()
    val favoriteNotes: Flow<List<PatientNote>> = dao.getFavoriteNotes()

    suspend fun insert(note: PatientNote): Long {
        return dao.insertNote(note)
    }

    suspend fun update(note: PatientNote) {
        dao.updateNote(note)
    }

    suspend fun deleteById(id: Int) {
        dao.deleteNoteById(id)
    }

    suspend fun getById(id: Int): PatientNote? {
        return dao.getNoteById(id)
    }
}
