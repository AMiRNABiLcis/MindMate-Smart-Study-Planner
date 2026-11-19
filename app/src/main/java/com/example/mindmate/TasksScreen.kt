package com.example.mindmate

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun TasksScreen(dao: AppDao, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userEmail = remember { currentUserEmail(context) }
    val tasks by remember(userEmail) { dao.getTasks(userEmail) }.collectAsState(initial = emptyList())
    val subjects by remember(userEmail) { dao.getSubjects(userEmail) }.collectAsState(initial = emptyList())

    var searchText by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") }
    var priorityFilter by remember { mutableStateOf("Any priority") }
    var subjectFilter by remember { mutableStateOf<Subject?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    val filterHelper = remember { TaskFilter() }

    val subjectNames = subjects.associate { it.id to it.name }
    val filteredTasks = filterHelper.filterTasks(tasks) { task ->
        task.title.contains(searchText, ignoreCase = true) &&
                (statusFilter == "All" || task.status == statusFilter) &&
                (priorityFilter == "Any priority" || task.priority == priorityFilter) &&
                (subjectFilter == null || task.subjectId == subjectFilter?.id)
    }.sortedWith(compareBy<Task> { it.status == "Completed" }.thenBy { it.deadline ?: Long.MAX_VALUE })

    Scaffold(
        modifier = modifier,
        containerColor = Color(0xFFF8F9FA),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { taskToEdit = null; showDialog = true },
                containerColor = AppBlue,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New task") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Tasks", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            }
            item {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    placeholder = { Text("Search tasks") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = AppBlue,
                        unfocusedBorderColor = Color(0xFFD0D7DE)
                    )
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("All") + TASK_STATUSES) { status ->
                        AppFilterChip(selected = statusFilter == status, label = status) { statusFilter = status }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        DropdownField(
                            label = "Priority",
                            options = listOf("Any priority") + PRIORITIES,
                            selected = priorityFilter,
                            optionLabel = { it },
                            onSelected = { priorityFilter = it }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        DropdownField(
                            label = "Subject",
                            options = listOf<Subject?>(null) + subjects,
                            selected = subjectFilter,
                            optionLabel = { it?.name ?: "Any subject" },
                            onSelected = { subjectFilter = it }
                        )
                    }
                }
            }
            if (filteredTasks.isEmpty()) {
                item {
                    EmptyState(
                        emoji = "✅",
                        title = if (tasks.isEmpty()) "No tasks yet" else "No tasks match your filters",
                        message = if (tasks.isEmpty()) "Tap “New task” to plan your first assignment." else "Try clearing a filter or search term."
                    )
                }
            }
            items(filteredTasks, key = { it.id }) { task ->
                val subjectName = task.subjectId?.let { subjectNames[it] }
                TaskRow(
                    task = task,
                    subjectName = subjectName,
                    onToggle = { scope.launch { toggleTask(dao, task) } },
                    onShare = { shareTask(context, task, subjectName) },
                    onEdit = { taskToEdit = task; showDialog = true },
                    onDelete = { taskToDelete = task }
                )
            }
        }
    }

    if (showDialog) {
        TaskDialog(
            initial = taskToEdit,
            subjects = subjects,
            defaultSubjectId = subjectFilter?.id,
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
