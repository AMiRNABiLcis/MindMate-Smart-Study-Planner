package com.example.mindmate.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val studyGoal: Int,                       // نسبة الهدف 0..100
    val createdAt: Long = System.currentTimeMillis(),
    val userId: String = ""
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
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

data class SubjectWithTasks(
    @Embedded val subject: Subject,
    @Relation(parentColumn = "id", entityColumn = "subjectId")
    val tasks: List<Task>
) {
    val doneCount: Int get() = tasks.count { it.isDone }
    val progressPercent: Int get() = if (tasks.isEmpty()) 0 else doneCount * 100 / tasks.size
}
