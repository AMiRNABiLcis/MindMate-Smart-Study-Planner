package com.example.mindmate

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // Users
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUser(email: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    fun observeUser(email: String): Flow<User?>

    @Update
    suspend fun updateUser(user: User)

    // Subjects
    @Query("SELECT * FROM subjects WHERE userEmail = :email ORDER BY id")
    fun getSubjects(email: String): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: Int): Subject?

    @Query("SELECT * FROM subjects WHERE id = :id")
    fun getSubjectFlow(id: Int): Flow<Subject?>

    // to reject a second subject with the same name for the same user
    @Query("SELECT COUNT(*) FROM subjects WHERE userEmail = :email AND LOWER(name) = LOWER(:name) AND id != :exceptId")
    suspend fun checkSubjectNameExists(email: String, name: String, exceptId: Int): Int

    @Insert
    suspend fun insertSubject(subject: Subject): Long

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    // Tasks
    @Query("SELECT * FROM tasks WHERE userEmail = :email")
    fun getTasks(email: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE subjectId = :subjectId")
    fun getTasksForSubject(subjectId: Int): Flow<List<Task>>

    @Insert
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    // ── Study Sessions (Schedule team responsibility) ──────────────────────────
    @Query("SELECT * FROM study_sessions WHERE userEmail = :email ORDER BY startTime")
    fun getSessions(email: String): Flow<List<StudySession>>

    @Insert
    suspend fun insertSession(session: StudySession)

    @Update
    suspend fun updateSession(session: StudySession)

    @Delete
    suspend fun deleteSession(session: StudySession)

    // ── Focus Sessions (Pomodoro team responsibility) ──────────────────────────
    @Insert
    suspend fun insertFocusSession(session: FocusSession)

    @Query("SELECT * FROM focus_sessions WHERE userEmail = :email ORDER BY completedAt DESC")
    fun getFocusSessions(email: String): Flow<List<FocusSession>>
}
