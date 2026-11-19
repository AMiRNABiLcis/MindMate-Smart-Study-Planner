package com.example.mindmate.data

import kotlinx.coroutines.flow.Flow

class FocusRepository(private val dao: FocusSessionDao) {

    fun observeCompletedSince(userId: String, since: Long): Flow<Int> =
        dao.observeCountSince(userId, since)

    fun observeTotalMinutes(userId: String): Flow<Int> =
        dao.observeTotalMinutes(userId)

    suspend fun recordCompletedFocus(userId: String, minutes: Int) {
        dao.insert(FocusSession(userId = userId, durationMinutes = minutes))
    }
}
