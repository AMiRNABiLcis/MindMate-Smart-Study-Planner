package com.example.mindmate.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/** جدول المواد */
@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val studyGoal: Int,                       // نسبة الهدف 0..100
    val createdAt: Long = System.currentTimeMillis()
)

/** جدول التاسكات: كل تاسك تابع لمادة (علاقة one-to-many) */
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE      // لو اتمسحت المادة، تاسكاتها تتمسح
        )
    ],
    indices = [Index("subjectId")]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val title: String,
    val priority: String = "Medium",          // High / Medium / Low
    val status: String = "Pending",           // Pending / In Progress / Done
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/** مادة + كل تاسكاتها في استعلام واحد */
data class SubjectWithTasks(
    @Embedded val subject: Subject,
    @Relation(parentColumn = "id", entityColumn = "subjectId")
    val tasks: List<Task>
)
