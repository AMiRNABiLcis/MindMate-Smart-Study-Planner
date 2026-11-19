package com.example.mindmate

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// User
@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val password: String = "",
    val fullName: String = "",
    val username: String = "",
    val phone: String = "",
    val gender: String = "",
    val dateOfBirth: String = ""
)

// Subject
@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val name: String,
    val description: String = "",
    val notes: String = "",
    val color: Int = 0xFF6B4FD3.toInt(),
    val icon: String = "📚",
    val studyGoal: Int = 80
)

// Task
@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(
        entity = Subject::class,
        parentColumns = ["id"],
        childColumns = ["subjectId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val subjectId: Int? = null,
    val title: String,
    val priority: String = "Medium",   // Low / Medium / High
    val status: String = "Pending",    // Pending / In Progress / Completed
    val deadline: Long? = null,
    val completedAt: Long? = null
)

// StudySession
@Entity(
    tableName = "study_sessions",
    foreignKeys = [ForeignKey(
        entity = Subject::class,
        parentColumns = ["id"],
        childColumns = ["subjectId"],
        onDelete = ForeignKey.SET_NULL
    )]
)
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val subjectId: Int? = null,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val status: String = "Scheduled"   // Scheduled / Completed / Cancelled
)

// FocusSession
@Entity(
    tableName = "focus_sessions",
    foreignKeys = [ForeignKey(
        entity = Subject::class,
        parentColumns = ["id"],
        childColumns = ["subjectId"],
        onDelete = ForeignKey.SET_NULL
    )]
)
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val subjectId: Int? = null,
    val minutes: Int,
    val completedAt: Long = System.currentTimeMillis()
)
