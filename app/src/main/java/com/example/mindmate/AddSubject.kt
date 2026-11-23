package com.example.mindmate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TaskDraft(
    val title: String,
    val priority: String = "Medium",
    val status: String = "Pending"
)

@Composable
fun AddSubjectScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaveSubject: (String, List<TaskDraft>) -> Unit = { _, _ -> }
) {
    var subjectName by remember { mutableStateOf("") }
    val draftTasks = remember { mutableStateListOf<TaskDraft>() }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { task ->
                draftTasks.add(task)
                showAddTaskDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                    contentDescription = null,
                    tint = Color(0xFF261D51),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.weight(0.6f))

            Text(
                text = "Add Subject",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(28.dp))

        Image(
            painter = painterResource(id = R.drawable.add_sub),
            contentDescription = null,
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(36.dp))

        OutlinedTextField(
            value = subjectName,
            onValueChange = { subjectName = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Subject Name",
                    fontSize = 16.sp,
                    color = Color(0xFF1A1A1A)
                )
            },
            leadingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.add_sub_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(22.dp)
                        .padding(start = 4.dp),
                    contentScale = ContentScale.Fit
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF2196F3),
                unfocusedBorderColor = Color(0xFF6BA4E7)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { showAddTaskDialog = true },
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFF6BA4E7))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.add_task_icon),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = if (draftTasks.isEmpty()) "Add Tasks" else "Add Another Task",
                    fontSize = 16.sp,
                    color = Color(0xFF1A1A1A)
                )
            }
        }

        if (draftTasks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Added Tasks (${draftTasks.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                draftTasks.forEachIndexed { index, task ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1A1A1A)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${task.priority} · ${task.status}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF7A7A7A)
                                )
                            }
                            IconButton(
                                onClick = { draftTasks.removeAt(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove task",
                                    tint = Color(0xFFD32F2F)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val trimmed = subjectName.trim()
                if (trimmed.isNotEmpty()) {
                    onSaveSubject(trimmed, draftTasks.toList())
                }
            },
            modifier = Modifier
                .width(150.dp)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6BA4E7),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Add",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (TaskDraft) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var status by remember { mutableStateOf("Pending") }
    var titleError by remember { mutableStateOf(false) }

    val priorities = listOf("High", "Medium", "Low")
    val statuses = listOf("Pending", "In Progress", "Done")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Task",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (titleError && it.isNotBlank()) titleError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Task Title") },
                    isError = titleError,
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                if (titleError) {
                    Text(
                        text = "Please enter task title",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Priority", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    priorities.forEach { p ->
                        val selected = p == priority
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { priority = p },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) Color(0xFF2196F3) else Color(0xFFF0F4F8),
                            border = BorderStroke(1.dp, if (selected) Color(0xFF2196F3) else Color(0xFFCCCCCC))
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p,
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) Color.White else Color(0xFF333333)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Status", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    statuses.forEach { s ->
                        val selected = s == status
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { status = s },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) Color(0xFF284B6E) else Color(0xFFF0F4F8),
                            border = BorderStroke(1.dp, if (selected) Color(0xFF284B6E) else Color(0xFFCCCCCC))
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = s,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) Color.White else Color(0xFF333333)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        onAddTask(TaskDraft(title.trim(), priority, status))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6BA4E7))
            ) {
                Text("Add", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF7A7A7A))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AddSubjectScreenPreview() {
    AddSubjectScreen()
}