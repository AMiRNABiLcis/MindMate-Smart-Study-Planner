package com.example.mindmate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

data class FocusUiState(
    val mode: TimerMode = TimerMode.FOCUS,
    val remainingMs: Long = TimerMode.FOCUS.durationMs,
    val isRunning: Boolean = false,
    val hasStarted: Boolean = false,
    val completedToday: Int = 0
) {
    val totalMs: Long get() = mode.durationMs
}

class FocusViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val userId: String =
        savedStateHandle.get<String>(FocusActivity.EXTRA_USER_ID)?.takeIf { it.isNotBlank() }
            ?: UserSessionManager(application).getUserEmail().trim().lowercase()

    private val controller = FocusTimerController.get(application).also { it.bind(userId) }

    val state: StateFlow<FocusUiState> = controller.state
    val finished: Flow<TimerMode> = controller.finished

    fun start() = controller.start()
    fun pause() = controller.pause()
    fun reset() = controller.reset()
    fun selectMode(mode: TimerMode) = controller.selectMode(mode)
}