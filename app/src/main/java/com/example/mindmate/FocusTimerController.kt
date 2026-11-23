package com.example.mindmate

import android.app.Application
import android.os.SystemClock
import com.example.mindmate.data.AppDatabase
import com.example.mindmate.data.FocusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FocusTimerController private constructor(app: Application) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val repository = FocusRepository(AppDatabase.get(app).focusSessionDao())

    private val _state = MutableStateFlow(FocusUiState())
    val state: StateFlow<FocusUiState> = _state.asStateFlow()

    private val _finished = Channel<TimerMode>(Channel.BUFFERED)
    val finished = _finished.receiveAsFlow()

    private var timerJob: Job? = null
    private var todayJob: Job? = null
    private var userId: String = ""

    fun bind(newUserId: String) {
        if (newUserId == userId && todayJob?.isActive == true) return
        if (newUserId != userId) stopAndReset()
        userId = newUserId
        todayJob?.cancel()
        todayJob = scope.launch {
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

        timerJob = scope.launch {
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
        _state.update { it.copy(remainingMs = it.mode.durationMs, isRunning = false, hasStarted = false) }
    }

    fun selectMode(mode: TimerMode) {
        if (mode == _state.value.mode) return
        timerJob?.cancel()
        _state.update {
            it.copy(mode = mode, remainingMs = mode.durationMs, isRunning = false, hasStarted = false)
        }
    }

    fun stopAndReset() {
        timerJob?.cancel()
        _state.update { FocusUiState(completedToday = it.completedToday) }
    }

    private suspend fun onFinished() {
        val finishedMode = _state.value.mode
        var completed = _state.value.completedToday

        if (finishedMode == TimerMode.FOCUS) {
            repository.recordCompletedFocus(userId, finishedMode.minutes)
            completed += 1
        }

        val next = nextMode(finishedMode, completed)
        _state.update {
            it.copy(mode = next, remainingMs = next.durationMs, isRunning = false, hasStarted = false)
        }
        _finished.send(finishedMode)
    }

    companion object {
        private const val TICK_MS = 200L

        @Volatile
        private var instance: FocusTimerController? = null

        fun get(app: Application): FocusTimerController =
            instance ?: synchronized(this) {
                instance ?: FocusTimerController(app).also { instance = it }
            }
    }
}