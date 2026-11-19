package com.example.mindmate

import java.util.Calendar
import java.util.Locale

/** أوضاع التايمر: تركيز 25 دقيقة، راحة قصيرة 5، راحة طويلة 15 */
enum class TimerMode(val minutes: Int) {
    FOCUS(25),
    SHORT_BREAK(5),
    LONG_BREAK(15);

    val durationMs: Long get() = minutes * 60_000L
}

const val FOCUS_SESSIONS_BEFORE_LONG_BREAK = 4

/** 1500000 ms -> "25:00". بنقرّب لفوق عشان التايمر ميوريش 00:00 قبل ما يخلص فعلاً. */
fun formatTime(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L) + 999L) / 1000L
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

/** الوقت المتبقي كنسبة من 0 لـ 1000 (للـ CircularProgressIndicator). */
fun progressPermille(remainingMs: Long, totalMs: Long): Int {
    if (totalMs <= 0L) return 0
    return (remainingMs.coerceIn(0L, totalMs) * 1000L / totalMs).toInt()
}

/** بعد كل 4 جلسات تركيز بتيجي راحة طويلة، وبعد أي راحة بنرجع للتركيز. */
fun nextMode(finished: TimerMode, completedFocusCount: Int): TimerMode = when (finished) {
    TimerMode.FOCUS ->
        if (completedFocusCount > 0 && completedFocusCount % FOCUS_SESSIONS_BEFORE_LONG_BREAK == 0)
            TimerMode.LONG_BREAK
        else
            TimerMode.SHORT_BREAK
    TimerMode.SHORT_BREAK, TimerMode.LONG_BREAK -> TimerMode.FOCUS
}

/** بداية اليوم (00:00) بتوقيت الجهاز. */
fun startOfDay(now: Long = System.currentTimeMillis()): Long =
    Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
