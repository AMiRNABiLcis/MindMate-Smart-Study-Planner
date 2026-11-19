package com.example.mindmate.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** جلسة تركيز (Pomodoro) مكتملة. userId = إيميل صاحب الحساب زي باقي الجداول. */
@Entity(
    tableName = "focus_sessions",
    indices = [Index("userId")]
)
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val durationMinutes: Int,
    val completedAt: Long = System.currentTimeMillis()
)

@Dao
interface FocusSessionDao {
    @Insert
    suspend fun insert(session: FocusSession): Long

    /** عدد الجلسات المكتملة من وقت معين (بداية اليوم مثلاً) */
    @Query("SELECT COUNT(*) FROM focus_sessions WHERE userId = :userId AND completedAt >= :since")
    fun observeCountSince(userId: String, since: Long): Flow<Int>

    /** إجمالي دقايق التركيز (للـ Dashboard والإحصائيات بعدين) */
    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM focus_sessions WHERE userId = :userId")
    fun observeTotalMinutes(userId: String): Flow<Int>
}
