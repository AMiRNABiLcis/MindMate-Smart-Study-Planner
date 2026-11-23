package com.example.mindmate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindmate.data.AppDatabase
import com.example.mindmate.data.FocusRepository
import com.example.mindmate.data.Subject
import com.example.mindmate.data.SubjectWithTasks
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val session = UserSessionManager(app)
    private val db = AppDatabase.get(app)

    val userId = session.getUserEmail().trim().lowercase()

    var userName: String = session.getUserName()
        private set
    val userEmail: String = session.getUserEmail()

    val subjectsWithTasks: StateFlow<List<SubjectWithTasks>> =
        db.subjectDao().observeSubjectsWithTasks(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val subjectModels: StateFlow<List<SubjectModel>> =
        subjectsWithTasks.map { list ->
            list.map { SubjectModel(it.subject.name, "${it.progressPercent}%", it.subject.id) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val progress: StateFlow<Pair<Int, Int>> =
        subjectsWithTasks.map { list ->
            list.sumOf { it.doneCount } to list.sumOf { it.tasks.size }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0 to 0)

    private val focusRepo = FocusRepository(db.focusSessionDao())

    val focusToday: StateFlow<Int> =
        focusRepo.observeCompletedSince(userId, startOfDay())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val focusTotalMinutes: StateFlow<Int> =
        focusRepo.observeTotalMinutes(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun addSubject(name: String) {
        viewModelScope.launch {
            db.subjectDao().insert(Subject(name = name.trim(), studyGoal = 100, userId = userId))
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch { db.subjectDao().delete(subject) }
    }

    fun updateName(newName: String) {
        session.updateName(newName.trim())
        userName = newName.trim()
    }

    fun logout() {
        FocusTimerController.get(getApplication()).stopAndReset()
        session.logout()
    }
}