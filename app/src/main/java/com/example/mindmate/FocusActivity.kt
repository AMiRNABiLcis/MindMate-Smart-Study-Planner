package com.example.mindmate

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mindmate.databinding.ActivityFocusBinding
import kotlinx.coroutines.launch

class FocusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFocusBinding
    private val viewModel: FocusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFocusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Insets بدل applySystemBarsPadding() عشان الشاشة متعتمدش على حاجة برا الملف
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.progressTimer.max = PROGRESS_MAX
        binding.btnBack.setOnClickListener { finish() }

        binding.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) viewModel.selectMode(modeForButton(checkedId))
        }

        binding.btnStartPause.setOnClickListener {
            if (viewModel.state.value.isRunning) viewModel.pause() else viewModel.start()
        }
        binding.btnReset.setOnClickListener { viewModel.reset() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collect { render(it) } }
                launch {
                    viewModel.finished.collect { mode ->
                        val message = if (mode == TimerMode.FOCUS) {
                            R.string.focus_session_complete
                        } else {
                            R.string.break_complete
                        }
                        Toast.makeText(this@FocusActivity, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun render(state: FocusUiState) {
        binding.tvTimer.text = formatTime(state.remainingMs)
        binding.progressTimer.progress = progressPermille(state.remainingMs, state.totalMs)
        binding.tvCompletedToday.text = state.completedToday.toString()

        binding.tvPhase.setText(
            when (state.mode) {
                TimerMode.FOCUS -> R.string.phase_focus
                TimerMode.SHORT_BREAK -> R.string.phase_short_break
                TimerMode.LONG_BREAK -> R.string.phase_long_break
            }
        )

        binding.btnStartPause.setText(
            when {
                state.isRunning -> R.string.btn_pause
                state.hasStarted -> R.string.btn_resume
                else -> R.string.btn_start
            }
        )
        binding.btnReset.isEnabled = state.hasStarted || state.isRunning

        // مينفعش نبدّل الوضع والتايمر شغال
        for (id in MODE_BUTTON_IDS) binding.toggleMode.findViewById<android.view.View>(id).isEnabled = !state.isRunning

        val checkedId = buttonForMode(state.mode)
        if (binding.toggleMode.checkedButtonId != checkedId) binding.toggleMode.check(checkedId)

        // الشاشة تفضل منورة أثناء التركيز
        binding.root.keepScreenOn = state.isRunning
    }

    private val MODE_BUTTON_IDS
        get() = intArrayOf(R.id.btnModeFocus, R.id.btnModeShortBreak, R.id.btnModeLongBreak)

    private fun modeForButton(id: Int): TimerMode = when (id) {
        R.id.btnModeShortBreak -> TimerMode.SHORT_BREAK
        R.id.btnModeLongBreak -> TimerMode.LONG_BREAK
        else -> TimerMode.FOCUS
    }

    private fun buttonForMode(mode: TimerMode): Int = when (mode) {
        TimerMode.FOCUS -> R.id.btnModeFocus
        TimerMode.SHORT_BREAK -> R.id.btnModeShortBreak
        TimerMode.LONG_BREAK -> R.id.btnModeLongBreak
    }

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
        private const val PROGRESS_MAX = 1000
    }
}
