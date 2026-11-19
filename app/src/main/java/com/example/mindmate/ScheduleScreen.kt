package com.example.mindmate

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private const val HOUR_MS = 3_600_000L

@Composable
fun ScheduleScreen(dao: AppDao, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userEmail = remember { currentUserEmail(context) }
    val sessions by remember(userEmail) { dao.getSessions(userEmail) }.collectAsState(initial = emptyList())
    val subjects by remember(userEmail) { dao.getSubjects(userEmail) }.collectAsState(initial = emptyList())

    var mode by remember { mutableStateOf("Daily") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDialog by remember { mutableStateOf(false) }
    var sessionToEdit by remember { mutableStateOf<StudySession?>(null) }
    var sessionToDelete by remember { mutableStateOf<StudySession?>(null) }

    val subjectNames = subjects.associate { it.id to it.name }
    val rangeStart = if (mode == "Daily") startOfDay(selectedDate) else startOfWeek(selectedDate)
    val rangeEnd = if (mode == "Daily") addDays(rangeStart, 1) else addDays(rangeStart, 7)
    val visibleSessions = sessions.filter { it.startTime >= rangeStart && it.startTime < rangeEnd }
    val rangeText = if (mode == "Daily") {
        formatDate(rangeStart)
    } else {
        "${formatShortDate(rangeStart)} – ${formatShortDate(addDays(rangeEnd, -1))}"
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color(0xFFF8F9FA),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { sessionToEdit = null; showDialog = true },
                containerColor = AppBlue,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New session") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Study Schedule", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Daily", "Weekly").forEach { option ->
                        AppFilterChip(selected = mode == option, label = option) { mode = option }
                    }
                }
            }
            item {
                val step = if (mode == "Daily") 1 else 7
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { selectedDate = addDays(selectedDate, -step) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous")
                    }
                    Text(rangeText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    TextButton(onClick = { selectedDate = System.currentTimeMillis() }) { Text("Today") }
                    IconButton(onClick = { selectedDate = addDays(selectedDate, step) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
                    }
                }
            }
            if (visibleSessions.isEmpty()) {
                item {
                    EmptyState(
                        emoji = "🗓",
                        title = "Nothing planned",
                        message = "Add a study session to block out time for a subject."
                    )
                }
            }
            // The sessions are already sorted by start time, so the days stay in order
            visibleSessions.groupBy { startOfDay(it.startTime) }.forEach { (day, daySessions) ->
                if (mode == "Weekly") {
                    item(key = "header-$day") {
                        Text(formatDate(day), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppBlue)
                    }
                }
                items(daySessions, key = { "session-${it.id}" }) { studySession ->
                    val subjectName = studySession.subjectId?.let { subjectNames[it] }
                    SessionCard(
                        session = studySession,
                        subjectName = subjectName,
                        onToggleDone = {
                            val newStatus = if (studySession.status == "Completed") "Scheduled" else "Completed"
                            scope.launch { saveSession(dao, studySession.copy(status = newStatus)) }
                        },
                        onToggleCancel = {
                            val newStatus = if (studySession.status == "Cancelled") "Scheduled" else "Cancelled"
                            scope.launch { saveSession(dao, studySession.copy(status = newStatus)) }
                        },
                        // Implicit intent: hand the session to the device calendar app
                        onAddToCalendar = {
                            val intent = Intent(Intent.ACTION_INSERT)
                            intent.data = CalendarContract.Events.CONTENT_URI
                            intent.putExtra(CalendarContract.Events.TITLE, studySession.title)
                            if (subjectName != null) intent.putExtra(CalendarContract.Events.DESCRIPTION, "MindMate study session: $subjectName")
                            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, studySession.startTime)
                            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, studySession.endTime)
                            try {
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(context, "No calendar app found", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onEdit = { sessionToEdit = studySession; showDialog = true },
                        onDelete = { sessionToDelete = studySession }
                    )
                }
            }
        }
    }

    if (showDialog) {
        SessionDialog(
            initial = sessionToEdit,
            subjects = subjects,
            defaultDate = selectedDate,
            onSave = { studySession ->
                scope.launch { saveSession(dao, studySession) }
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
    sessionToDelete?.let { studySession ->
        ConfirmDialog(
            title = "Delete session?",
            message = "\"${studySession.title}\" will be removed from your schedule.",
            confirmText = "Delete",
            onConfirm = {
                scope.launch { dao.deleteSession(studySession) }
                sessionToDelete = null
            },
            onDismiss = { sessionToDelete = null }
        )
    }
}

@Composable
fun SessionCard(
    session: StudySession,
    subjectName: String?,
    onToggleDone: () -> Unit,
    onToggleCancel: () -> Unit,
    onAddToCalendar: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(session.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    if (subjectName != null) {
                        Text(subjectName, fontSize = 12.sp, color = AppBlue)
                    }
                    Text(
                        "${formatTime(session.startTime)} – ${formatTime(session.endTime)}",
                        fontSize = 12.sp,
                        color = Color(0xFF7A7A7A)
                    )
                }
                Pill(session.status, sessionStatusColor(session.status))
            }
            Row {
                IconButton(onClick = onToggleDone) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Mark completed", tint = SuccessGreen)
                }
                IconButton(onClick = onToggleCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel session", tint = Color(0xFF7A7A7A))
                }
                IconButton(onClick = onAddToCalendar) {
                    Icon(Icons.Default.DateRange, contentDescription = "Add to calendar", tint = AppBlue)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit session", tint = AppBlue)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete session", tint = DangerRed)
                }
            }
        }
    }
}

@Composable
fun SessionDialog(
    initial: StudySession?,
    subjects: List<Subject>,
    defaultDate: Long,
    onSave: (StudySession) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val userEmail = remember { currentUserEmail(context) }
    val defaultStart = remember { startOfDay(defaultDate) + 9 * HOUR_MS }   // 9:00 on the shown day
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var subject by remember { mutableStateOf(subjects.find { it.id == initial?.subjectId }) }
    var startTime by remember { mutableLongStateOf(initial?.startTime ?: defaultStart) }
    var endTime by remember { mutableLongStateOf(initial?.endTime ?: (defaultStart + HOUR_MS)) }
    var status by remember { mutableStateOf(initial?.status ?: "Scheduled") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New Study Session" else "Edit Session") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownField(
                    label = "Subject",
                    options = listOf<Subject?>(null) + subjects,
                    selected = subject,
                    optionLabel = { it?.name ?: "No subject" },
                    onSelected = { subject = it }
                )
                DateTimeField(
                    label = "Starts",
                    value = startTime,
                    onValueChange = { picked ->
                        if (picked != null) {
                            startTime = picked
                            if (endTime <= picked) endTime = picked + HOUR_MS
                        }
                    }
                )
                DateTimeField(
                    label = "Ends",
                    value = endTime,
                    onValueChange = { picked ->
                        if (picked != null) endTime = picked
                    }
                )
                if (initial != null) {
                    DropdownField(
                        label = "Status",
                        options = SESSION_STATUSES,
                        selected = status,
                        optionLabel = { it },
                        onSelected = { status = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Title is required", Toast.LENGTH_SHORT).show()
                    } else if (endTime <= startTime) {
                        Toast.makeText(context, "End time must be after the start time", Toast.LENGTH_SHORT).show()
                    } else {
                        val base = initial ?: StudySession(userEmail = userEmail, title = "", startTime = startTime, endTime = endTime)
                        onSave(
                            base.copy(
                                title = title.trim(),
                                subjectId = subject?.id,
                                startTime = startTime,
                                endTime = endTime,
                                status = status
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppBlue)
            ) { Text(if (initial == null) "Add Session" else "Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
