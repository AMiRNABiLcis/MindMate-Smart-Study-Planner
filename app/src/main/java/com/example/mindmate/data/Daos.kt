package com.example.mindmate.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Insert
    suspend fun insert(subject: Subject): Long

    @Update
    suspend fun update(subject: Subject)

    @Delete
    suspend fun delete(subject: Subject)

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getById(id: Long): Subject?

    @Query("SELECT * FROM subjects ORDER BY id LIMIT 1")
    suspend fun getFirst(): Subject?

    @Query("SELECT * FROM subjects ORDER BY name")
    fun observeAll(): Flow<List<Subject>>

    /** للـ Home: كل مادة مع تاسكاتها (عشان تحسبوا التقدم) */
    @Transaction
    @Query("SELECT * FROM subjects ORDER BY name")
    fun observeSubjectsWithTasks(): Flow<List<SubjectWithTasks>>
}

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM tasks WHERE subjectId = :subjectId ORDER BY createdAt DESC")
    fun observeBySubject(subjectId: Long): Flow<List<Task>>

    @Query("SELECT COUNT(*) FROM tasks")
    fun observeTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE isDone = 1")
    fun observeDoneCount(): Flow<Int>
}

