package com.example.mindmate

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.mindmate.data.AppDatabase
import com.example.mindmate.data.FocusRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FocusUiState(
    val mode: TimerMode = TimerMode.FOCUS,
    val remainingMs: Long = TimerMode.FOCUS.durationMs,
    val isRunning: Boolean = false,
    /** true لو التايمر اشتغل وبعدين اتوقف (الزرار يبقى Resume) */
    val hasStarted: Boolean = false,
    val completedToday: Int = 0
) {
    val totalMs: Long get() = mode.durationMs
}

/**
 * الـ userId بييجي من Intent extra اسمه FocusActivity.EXTRA_USER_ID
 * (SavedStateHandle بياخد extras الـ Activity تلقائياً).
 */
class FocusViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val userId: String = savedStateHandle.get<String>(FocusActivity.EXTRA_USER_ID).orEmpty()
    private val repository = FocusRepository(AppDatabase.get(application).focusSessionDao())

    private val _state = MutableStateFlow(FocusUiState())
    val state: StateFlow<FocusUiState> = _state.asStateFlow()

    /** بيبعت الوضع اللي خلص (عشان الـ Activity تعرض رسالة) */
    private val _finished = Channel<TimerMode>(Channel.BUFFERED)
    val finished = _finished.receiveAsFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.observeCompletedSince(userId, startOfDay()).collect { count ->
                _state.update { it.copy(completedToday = count) }
            }
        }
    }

    fun start() {
        val current = _state.value
        if (current.isRunning || current.remainingMs <= 0L) return

        val endAt = SystemClock.elapsedRealtime() + current.remainingMs
        _state.update { it.copy(isRunning = true, hasStarted = true) }

        timerJob = viewModelScope.launch {
            while (true) {
                val left = endAt - SystemClock.elapsedRealtime()
                if (left <= 0L) break
                _state.update { it.copy(remainingMs = left) }
                delay(TICK_MS)
            }
            onFinished()
        }
    }

    fun pause() {
        timerJob?.cancel()
        _state.update { it.copy(isRunning = false) }
    }

    fun reset() {
        timerJob?.cancel()
        _state.update {
            it.copy(remainingMs = it.mode.durationMs, isRunning = false, hasStarted = false)
        }
    }

    fun selectMode(mode: TimerMode) {
        if (mode == _state.value.mode) return
        timerJob?.cancel()
        _state.update {
            it.copy(
                mode = mode,
                remainingMs = mode.durationMs,
                isRunning = false,
                hasStarted = false
            )
        }
    }

    private suspend fun onFinished() {
        val finishedMode = _state.value.mode
        var completed = _state.value.completedToday

        if (finishedMode == TimerMode.FOCUS) {
            repository.recordCompletedFocus(userId, finishedMode.minutes)
            completed += 1 // الـ Flow بيتحدّث بعد الـ insert، فبنحسبها هنا للوضع الجاي
        }

        val next = nextMode(finishedMode, completed)
        _state.update {
            it.copy(
                mode = next,
                remainingMs = next.durationMs,
                isRunning = false,
                hasStarted = false
            )
        }
        _finished.send(finishedMode)
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val TICK_MS = 200L
    }
}
