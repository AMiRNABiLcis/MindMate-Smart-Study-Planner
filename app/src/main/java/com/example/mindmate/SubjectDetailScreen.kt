package com.example.mindmate

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
private val BannerBlue   = Color(0xFF3B5B7A)
private val TabActive    = Color(0xFF1F1B3A)
private val TabInactive  = Color(0xFF6C63B8)
private val Magenta      = Color(0xFFD1198E)
private val RingPurple   = Color(0xFF6A2BD9)
private val RingBlue     = Color(0xFF2F3FE0)
private val RingTrack    = Color(0xFFBDBDC9)
private val TaskCountBlue = Color(0xFF4A90D9)
private val ScreenBg     = Color(0xFFF1F0F9)

private val DetailTabs = listOf("Overview", "Tasks", "Notes")
@Composable
fun SubjectDetailScreen(
    subjectId: Int,
    subjectName: String,
    onBackClick: () -> Unit,
    onViewAllTasksClick: () -> Unit,
    onSubjectDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    initialTab: Int = 0
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember { AppDatabase.getDatabase(context).appDao() }
    val userEmail = remember { currentUserEmail(context) }

    // Live tasks and the live subject row (for notes) – both come from Room
    val tasks by remember(subjectId) { dao.getTasksForSubject(subjectId) }
        .collectAsState(initial = emptyList())
    val subject by remember(subjectId) { dao.getSubjectFlow(subjectId) }
        .collectAsState(initial = null)

    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.status == "Completed" }
    val percent = if (totalTasks == 0) 0 else completedTasks * 100 / totalTasks

    val recentCompleted = remember(tasks) {
        tasks.filter { it.status == "Completed" }
            .sortedByDescending { it.completedAt ?: 0L }
            .take(4)
    }

    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Back arrow + edit / delete subject
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = "Back",
                    tint = Color(0xFF261D51),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { showRenameDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Rename subject", tint = Color(0xFF261D51))
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete subject", tint = DangerRed)
            }
        }

        Spacer(Modifier.height(4.dp))

        // Banner: subject name + percentage
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(14.dp),
            color = BannerBlue,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subjectName,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "$percent%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DetailTabs.forEachIndexed { index, title ->
                val selected = selectedTab == index
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) TabActive else TabInactive,
                    textDecoration = if (selected) TextDecoration.Underline else TextDecoration.None,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { selectedTab = index }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        when (selectedTab) {
            0 -> OverviewTab(
                percent = percent,
                completedTasks = completedTasks,
                totalTasks = totalTasks,
                recentCompleted = recentCompleted,
                onViewAllTasksClick = onViewAllTasksClick,
                modifier = Modifier.weight(1f)
            )

            1 -> TasksTab(
                dao = dao,
                subject = subject,
                subjectId = subjectId,
                tasks = tasks,
                modifier = Modifier.weight(1f)
            )

            else -> NotesTab(
                savedNotes = subject?.notes ?: "",
                enabled = subject != null,
                onSave = { newNotes ->
                    val current = subject
                    if (current != null) {
                        scope.launch {
                            dao.updateSubject(current.copy(notes = newNotes))
                            Toast.makeText(context, "Notes saved", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showRenameDialog) {
        RenameSubjectDialog(
            currentName = subjectName,
            onSave = { newName ->
                val current = subject
                if (current != null) {
                    scope.launch {
                        if (dao.checkSubjectNameExists(userEmail, newName, subjectId) > 0) {
                            Toast.makeText(context, "A subject with this name already exists", Toast.LENGTH_SHORT).show()
                        } else {
                            dao.updateSubject(current.copy(name = newName))
                            showRenameDialog = false
                        }
                    }
                }
            },
            onDismiss = { showRenameDialog = false }
        )
    }
    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete subject?",
            message = "\"$subjectName\" and all of its tasks will be deleted, and its study sessions will be unlinked. This can't be undone.",
            confirmText = "Delete",
            onConfirm = {
                showDeleteDialog = false
                val current = subject
                if (current != null) {
                    scope.launch {
                        dao.deleteSubject(current)
                        onSubjectDeleted()
                    }
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun TasksTab(
    dao: AppDao,
    subject: Subject?,
    subjectId: Int,
    tasks: List<Task>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    // Open tasks first, then by deadline
    val sortedTasks = remember(tasks) {
        tasks.sortedWith(compareBy<Task> { it.status == "Completed" }.thenBy { it.deadline ?: Long.MAX_VALUE })
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        item {
            Button(
                onClick = { taskToEdit = null; showDialog = true },
                enabled = subject != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6BA4E7))
            ) {
                Text("Add Task", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (sortedTasks.isEmpty()) {
            item {
                EmptyState("✅", "No tasks yet", "Add a task to start tracking this subject.")
            }
        }
        items(sortedTasks, key = { it.id }) { task ->
            TaskRow(
                task = task,
                subjectName = null,
                onToggle = { scope.launch { toggleTask(dao, task) } },
                onShare = { shareTask(context, task, subject?.name) },
                onEdit = { taskToEdit = task; showDialog = true },
                onDelete = { taskToDelete = task }
            )
        }
    }

    if (showDialog && subject != null) {
        TaskDialog(
            initial = taskToEdit,
            subjects = listOf(subject),
            defaultSubjectId = subjectId,
            onSave = { task ->
                scope.launch { saveTask(dao, task) }
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
    taskToDelete?.let { task ->
        ConfirmDialog(
            title = "Delete task?",
            message = "\"${task.title}\" will be removed permanently.",
            confirmText = "Delete",
            onConfirm = {
                scope.launch { dao.deleteTask(task) }
                taskToDelete = null
            },
            onDismiss = { taskToDelete = null }
        )
    }
}

@Composable
private fun NotesTab(
    savedNotes: String,
    enabled: Boolean,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var notes by remember(savedNotes) { mutableStateOf(savedNotes) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes for this subject") },
            minLines = 8,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        Button(
            onClick = { onSave(notes) },
            enabled = enabled && notes != savedNotes,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6BA4E7))
        ) {
            Text("Save Notes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RenameSubjectDialog(
    currentName: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Subject") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Subject name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isEmpty()) {
                        Toast.makeText(context, "Subject name is required", Toast.LENGTH_SHORT).show()
                    } else {
                        onSave(trimmed)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6BA4E7))
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun OverviewTab(
    percent: Int,
    completedTasks: Int,
    totalTasks: Int,
    recentCompleted: List<Task>,
    onViewAllTasksClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Donut + "Completed x/y Task"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProgressDonut(percent = percent, modifier = Modifier.size(120.dp))

                    Column {
                        Text(
                            text = "Completed",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Magenta
                        )
                        Text(
                            text = "$completedTasks/$totalTasks Task",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TaskCountBlue
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    text = "Recent Activity",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TabActive,
                    textDecoration = TextDecoration.Underline
                )

                Spacer(Modifier.height(12.dp))

                if (recentCompleted.isEmpty()) {
                    Text(
                        text = if (totalTasks == 0) "No tasks yet for this subject."
                        else "No completed tasks yet. Keep going!",
                        fontSize = 14.sp,
                        color = Color(0xFF7A7A7A)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        recentCompleted.forEach { task ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Magenta),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✓", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = task.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Magenta,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable { onViewAllTasksClick() },
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFF8AA8C7)),
                    shadowElevation = 3.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "View All Tasks",
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ProgressDonut(percent: Int, modifier: Modifier = Modifier) {
    val sweepFraction by animateFloatAsState(
        targetValue = percent.coerceIn(0, 100) / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "donut"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(inset, inset)

            drawArc(
                color = RingTrack,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            if (sweepFraction > 0f) {
                rotate(degrees = -90f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Magenta, RingPurple, RingBlue, Magenta)
                        ),
                        startAngle = 0f,
                        sweepAngle = 360f * sweepFraction,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
        }
        Text(
            text = "$percent%",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Magenta
        )
    }
}
