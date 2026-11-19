package com.example.mindmate

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
val AppBlue = Color(0xFF2196F3)
val SuccessGreen = Color(0xFF2EAD6D)
val WarningAmber = Color(0xFFF59E0B)
val DangerRed = Color(0xFFE5484D)
val InfoBlue = Color(0xFF3B82F6)
val GrayText = Color(0xFF8B86A3)

val PRIORITIES = listOf("Low", "Medium", "High")
val TASK_STATUSES = listOf("Pending", "In Progress", "Completed")
val SESSION_STATUSES = listOf("Scheduled", "Completed", "Cancelled")

// Same normalisation as MainActivity so every screen reads/writes the same user's rows
fun currentUserEmail(context: Context): String =
    UserSessionManager(context).getUserEmail().trim().lowercase()

// Date / time
fun formatDate(time: Long): String = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(time))
fun formatShortDate(time: Long): String = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(time))
fun formatTime(time: Long): String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(time))
fun formatDateTime(time: Long): String = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(time))
fun startOfDay(time: Long): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = time
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

fun addDays(time: Long, days: Int): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = time
    c.add(Calendar.DAY_OF_YEAR, days)
    return c.timeInMillis
}

fun startOfWeek(time: Long): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = startOfDay(time)
    val diff = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7
    c.add(Calendar.DAY_OF_YEAR, -diff)
    return c.timeInMillis
}
fun priorityColor(priority: String): Color = when (priority) {
    "High" -> DangerRed
    "Medium" -> WarningAmber
    else -> SuccessGreen
}

fun taskStatusColor(status: String): Color = when (status) {
    "Completed" -> SuccessGreen
    "In Progress" -> InfoBlue
    else -> GrayText
}

fun sessionStatusColor(status: String): Color = when (status) {
    "Completed" -> SuccessGreen
    "Cancelled" -> GrayText
    else -> InfoBlue
}

// Implicit intent
fun shareTask(context: Context, task: Task, subjectName: String?) {
    val text = buildString {
        append("Task: ${task.title}")
        if (subjectName != null) append("\nSubject: $subjectName")
        append("\nPriority: ${task.priority}")
        append("\nStatus: ${task.status}")
        task.deadline?.let { append("\nDue: ${formatDateTime(it)}") }
    }
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_TEXT, text)
    context.startActivity(Intent.createChooser(intent, "Share task"))
}

suspend fun saveTask(dao: AppDao, task: Task) {
    val completedAt = if (task.status == "Completed") (task.completedAt ?: System.currentTimeMillis()) else null
    val taskToSave = task.copy(completedAt = completedAt)
    if (task.id == 0) dao.insertTask(taskToSave) else dao.updateTask(taskToSave)
}

// Tick the checkbox of a task
suspend fun toggleTask(dao: AppDao, task: Task) {
    val newStatus = if (task.status == "Completed") "Pending" else "Completed"
    saveTask(dao, task.copy(status = newStatus))
}

suspend fun saveSession(dao: AppDao, session: StudySession) {
    if (session.id == 0) dao.insertSession(session) else dao.updateSession(session)
}

// Small reusable pieces
@Composable
fun Pill(text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(16.dp)) {
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun EmptyState(emoji: String, title: String, message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 48.sp)
        Spacer(Modifier.height(8.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(message, color = Color(0xFF7A7A7A), fontSize = 14.sp)
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText, color = Color.Red) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AppFilterChip(selected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppBlue.copy(alpha = 0.18f),
            selectedLabelColor = Color(0xFF0D5FA8)
        )
    )
}

@Composable
fun <T> DropdownField(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("$label: ${optionLabel(selected)}", modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DateTimeField(
    label: String,
    value: Long?,
    onValueChange: (Long?) -> Unit,
    clearable: Boolean = false
) {
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(
            onClick = {
                val cal = Calendar.getInstance()
                cal.timeInMillis = value ?: System.currentTimeMillis()
                DatePickerDialog(context, { _, year, month, day ->
                    TimePickerDialog(context, { _, hour, minute ->
                        val picked = Calendar.getInstance()
                        picked.set(year, month, day, hour, minute, 0)
                        picked.set(Calendar.MILLISECOND, 0)
                        onValueChange(picked.timeInMillis)
                    }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "$label: ${if (value == null) "Not set" else formatDateTime(value)}",
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (clearable && value != null) {
            TextButton(onClick = { onValueChange(null) }) { Text("✕") }
        }
    }
}

@Composable
fun TaskRow(
    task: Task,
    subjectName: String?,
    onToggle: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val completed = task.status == "Completed"
    val deadline = task.deadline
    val overdue = !completed && deadline != null && deadline < System.currentTimeMillis()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.Top) {
            Checkbox(
                checked = completed,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = AppBlue)
            )
            Column(modifier = Modifier.weight(1f).padding(top = 12.dp)) {
                Text(
                    task.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (completed) Color(0xFF7A7A7A) else Color(0xFF1A1A1A)
                )
                if (subjectName != null) {
                    Text(subjectName, fontSize = 12.sp, color = AppBlue)
                }
                if (deadline != null) {
                    Text(
                        "📅 " + formatDateTime(deadline),
                        fontSize = 12.sp,
                        color = if (overdue) DangerRed else Color(0xFF7A7A7A)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill(task.priority, priorityColor(task.priority))
                    Pill(task.status, taskStatusColor(task.status))
                }
            }
            Row {
                IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Share task", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit task", modifier = Modifier.size(18.dp), tint = AppBlue)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete task", modifier = Modifier.size(18.dp), tint = DangerRed)
                }
            }
        }
    }
}

@Composable
fun TaskDialog(
    initial: Task?,
    subjects: List<Subject>,
    defaultSubjectId: Int?,
    onSave: (Task) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val userEmail = remember { currentUserEmail(context) }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var subject by remember { mutableStateOf(subjects.find { it.id == (initial?.subjectId ?: defaultSubjectId) }) }
    var priority by remember { mutableStateOf(initial?.priority ?: "Medium") }
    var status by remember { mutableStateOf(initial?.status ?: "Pending") }
    var deadline by remember { mutableStateOf(initial?.deadline) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add a New Task" else "Edit Task") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task title") },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownField(
                    label = "Subject",
                    options = listOf<Subject?>(null) + subjects,
                    selected = subject,
                    optionLabel = { it?.name ?: "No subject" },
                    onSelected = { subject = it }
                )
                Text("Priority", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PRIORITIES.forEach { option ->
                        AppFilterChip(selected = priority == option, label = option) { priority = option }
                    }
                }
                DropdownField(
                    label = "Status",
                    options = TASK_STATUSES,
                    selected = status,
                    optionLabel = { it },
                    onSelected = { status = it }
                )
                DateTimeField(
                    label = "Deadline",
                    value = deadline,
                    onValueChange = { deadline = it },
                    clearable = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Title is required", Toast.LENGTH_SHORT).show()
                    } else {
                        val base = initial ?: Task(userEmail = userEmail, title = "")
                        onSave(
                            base.copy(
                                title = title.trim(),
                                subjectId = subject?.id,
                                priority = priority,
                                status = status,
                                deadline = deadline
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppBlue)
            ) { Text(if (initial == null) "Add Task" else "Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
