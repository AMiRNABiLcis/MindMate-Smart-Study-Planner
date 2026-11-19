package com.example.mindmate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindmate.data.SubjectWithTasks

private val Navy = Color(0xFF261D51)
private val Blue = Color(0xFF2196F3)
private val PageBg = Color(0xFFF8F9FA)
private val Pink = Color(0xFFD81B9A)

@Composable
private fun BackArrow(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.CenterStart
    ) {
        Icon(
            painter = painterResource(id = R.drawable.arrow_back),
            contentDescription = "Back",
            tint = Navy,
            modifier = Modifier.size(22.dp)
        )
    }
}

/** Tasks tab: بيختار المادة وبعدين يفتح TasksActivity */
@Composable
fun TasksHubScreen(
    subjects: List<SubjectModel>,
    onSubjectClick: (SubjectModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PageBg)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text("Tasks", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
        Spacer(Modifier.height(4.dp))
        Text("Choose a subject to manage its tasks", fontSize = 14.sp, color = Color(0xFF7A7A7A))
        Spacer(Modifier.height(16.dp))
        if (subjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEFEFEF)),
                contentAlignment = Alignment.Center
            ) {
                Text("Add a subject first.", fontSize = 14.sp, color = Color(0xFF7A7A7A))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(subjects, key = { it.id }) { s ->
                    SubjectListCard(s.name, s.percentage) { onSubjectClick(s) }
                }
            }
        }
    }
}

/** Subject Details (Overview) حسب التصميم */
@Composable
fun SubjectDetailsScreen(
    data: SubjectWithTasks?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (data == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Subject not found", color = Color(0xFF7A7A7A))
        }
        return
    }
    var confirmDelete by remember { mutableStateOf(false) }
    val pct = data.progressPercent
    val recent = data.tasks.filter { it.isDone }.sortedByDescending { it.createdAt }.take(3)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PageBg)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackArrow(onBack)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit subject", tint = Navy) }
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(Icons.Default.Delete, "Delete subject", tint = Color(0xFFD32F2F))
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF284B6E),
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    data.subject.name, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    color = Color.White, maxLines = 1, modifier = Modifier.weight(1f)
                )
                Text("$pct%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            Text("Overview", fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp)
            Text(
                "Tasks", fontWeight = FontWeight.Bold, color = Color(0xFF7B68C8), fontSize = 16.sp,
                modifier = Modifier.clickable { onViewTasks() }
            )
            Text("Notes", fontWeight = FontWeight.Bold, color = Color(0xFFBDB5E6), fontSize = 16.sp)
        }
        HorizontalDivider(Modifier.padding(top = 6.dp), color = Color(0xFFE0E0E0))

        Spacer(Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                        CircularProgressIndicator(
                            progress = { pct / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = Pink,
                            trackColor = Color(0xFFB9B9C9),
                            strokeWidth = 12.dp
                        )
                        Text("$pct%", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Pink)
                    }
                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text("Completed", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Pink)
                        Text(
                            "${data.doneCount}/${data.tasks.size} Task",
                            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Blue
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("Recent Activity", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Navy)
                Spacer(Modifier.height(10.dp))
                if (recent.isEmpty()) {
                    Text("No completed tasks yet.", fontSize = 14.sp, color = Color(0xFF7A7A7A))
                } else {
                    recent.forEach { t ->
                        Row(
                            modifier = Modifier.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(24.dp).clip(CircleShape).background(Pink),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(t.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Pink, maxLines = 1)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clickable { onViewTasks() },
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFF8AA8C7)),
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("View All Tasks", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Navy)
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete subject?") },
            text = { Text("\"${data.subject.name}\" and its ${data.tasks.size} task(s) will be deleted.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text("Delete", color = Color(0xFFD32F2F))
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun Avatar(name: String, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(0xFF284B6E)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.trim().firstOrNull()?.uppercase() ?: "?",
            color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size / 2.4f).sp
        )
    }
}

@Composable
fun ProfileScreen(
    name: String,
    email: String,
    onMyInformation: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmLogout by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("My Profile", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Navy)
        Spacer(Modifier.height(16.dp))
        Avatar(name, 96)
        Spacer(Modifier.height(10.dp))
        Text(name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Navy)
        Text(email, fontSize = 14.sp, color = Color(0xFF7A7A7A))
        Spacer(Modifier.height(28.dp))

        ProfileRow("My Information", Navy, onMyInformation)
        Spacer(Modifier.height(12.dp))
        ProfileRow("Logout", Color(0xFFE53935)) { confirmLogout = true }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Log out?") },
            confirmButton = {
                TextButton(onClick = { confirmLogout = false; onLogout() }) { Text("Logout", color = Color(0xFFE53935)) }
            },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ProfileRow(label: String, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFD0D0D0))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = color)
            Text(">", fontSize = 18.sp, color = Color(0xFF9E9E9E))
        }
    }
}

/** My Information: الاسم قابل للتعديل والإيميل مقفول (باقي الحقول محتاجة تخزين) */
@Composable
fun MyInformationScreen(
    name: String,
    email: String,
    onSaveName: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf(name) }
    var error by remember { mutableStateOf<String?>(null) }
    var saved by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        BackArrow(onBack)
        Text("My Information", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Navy)
        Spacer(Modifier.height(24.dp))

        Text("Full name", fontWeight = FontWeight.Bold, color = Navy)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it; error = null; saved = false },
            singleLine = true,
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Text("Email", fontWeight = FontWeight.Bold, color = Navy)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {},
            enabled = false,
            singleLine = true,
            trailingIcon = { Icon(Icons.Default.Lock, null) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(28.dp))

        Button(
            onClick = {
                if (fullName.trim().length < 3) error = "Name must be at least 3 characters"
                else { onSaveName(fullName); saved = true }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF558DE6))
        ) { Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        if (saved) {
            Spacer(Modifier.height(8.dp))
            Text("Saved", color = Color(0xFF2E7D32), fontSize = 14.sp)
        }
    }
}
