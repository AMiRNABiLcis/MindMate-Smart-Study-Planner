package com.example.mindmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFF8F9FA)
            ) {
                AppMainScreen()
            }
        }
    }
}

private sealed class ScreenDestination {
    data object Home : ScreenDestination()
    data object Subjects : ScreenDestination()
    data object Tasks : ScreenDestination()
    data object Profile : ScreenDestination()
    data object AddSubject : ScreenDestination()

    // NEW: subject details (Overview / Tasks / Notes)
    data class SubjectDetail(val subjectId: Int) : ScreenDestination()
}

@Composable
fun AppMainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember { AppDatabase.getDatabase(context).appDao() }

    // Subjects are stored per user. Email is normalised so "A@x.com" and "a@x.com"
    // always map to the same rows.
    val userEmail = remember {
        UserSessionManager(context).getUserEmail().trim().lowercase()
    }

    val backStack = remember { mutableStateListOf<ScreenDestination>(ScreenDestination.Home) }
    val currentScreen = backStack.last()

    // Persistent data (Room)
    val dbSubjects by remember(userEmail) { dao.getSubjects(userEmail) }
        .collectAsState(initial = emptyList())
    val dbTasks by remember(userEmail) { dao.getTasks(userEmail) }
        .collectAsState(initial = emptyList())

    val subjectsList: List<SubjectModel> = dbSubjects.map { subject ->
        val subjectTasks = dbTasks.filter { it.subjectId == subject.id }
        val done = subjectTasks.count { it.status == "Completed" }
        val percent = if (subjectTasks.isEmpty()) 0 else done * 100 / subjectTasks.size
        SubjectModel(id = subject.id, name = subject.name, percentage = "$percent%")
    }

    val totalTasks = dbTasks.size
    val completedTasks = dbTasks.count { it.status == "Completed" }

    fun navigateTo(destination: ScreenDestination) {
        if (backStack.lastOrNull() != destination) {
            backStack.add(destination)
        }
    }

    fun selectBottomTab(tabDestination: ScreenDestination) {
        backStack.clear()
        backStack.add(ScreenDestination.Home)
        if (tabDestination != ScreenDestination.Home) {
            backStack.add(tabDestination)
        }
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        } else if (currentScreen != ScreenDestination.Home) {
            backStack.clear()
            backStack.add(ScreenDestination.Home)
        }
    }

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomBarItem(
                        iconRes = R.drawable.home,
                        label = "Home",
                        isSelected = currentScreen == ScreenDestination.Home
                    ) { selectBottomTab(ScreenDestination.Home) }

                    BottomBarItem(
                        iconRes = R.drawable.subjects,
                        label = "Subjects",
                        isSelected = currentScreen == ScreenDestination.Subjects ||
                                currentScreen == ScreenDestination.AddSubject ||
                                currentScreen is ScreenDestination.SubjectDetail
                    ) { selectBottomTab(ScreenDestination.Subjects) }

                    BottomBarItem(
                        iconRes = R.drawable.tasks,
                        label = "Tasks",
                        isSelected = currentScreen == ScreenDestination.Tasks
                    ) { selectBottomTab(ScreenDestination.Tasks) }

                    BottomBarItem(
                        iconRes = R.drawable.profile,
                        label = "Profile",
                        isSelected = currentScreen == ScreenDestination.Profile
                    ) { selectBottomTab(ScreenDestination.Profile) }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        when (val screen = currentScreen) {
            ScreenDestination.Home -> HomeContent(
                subjectsList = subjectsList,
                completedTasks = completedTasks,
                totalTasks = totalTasks,
                onAddSubjectClick = { navigateTo(ScreenDestination.AddSubject) },
                onSeeAllClick = { navigateTo(ScreenDestination.Subjects) },
                onSubjectClick = { subject -> navigateTo(ScreenDestination.SubjectDetail(subject.id)) },
                modifier = Modifier.padding(innerPadding)
            )

            ScreenDestination.Subjects -> SubjectsContent(
                subjectsList = subjectsList,
                onAddSubjectClick = { navigateTo(ScreenDestination.AddSubject) },
                onSubjectClick = { subject -> navigateTo(ScreenDestination.SubjectDetail(subject.id)) },
                onBackClick = { navigateBack() },
                modifier = Modifier.padding(innerPadding)
            )

            ScreenDestination.AddSubject -> AddSubjectScreen(
                modifier = Modifier.padding(innerPadding),
                onBackClick = { navigateBack() },
                onSaveSubject = { subjectName ->
                    // Save to Room so it survives app restarts
                    scope.launch {
                        dao.insertSubject(Subject(userEmail = userEmail, name = subjectName))
                    }
                    if (backStack.size > 1) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                    if (backStack.lastOrNull() != ScreenDestination.Subjects) {
                        backStack.add(ScreenDestination.Subjects)
                    }
                }
            )

            is ScreenDestination.SubjectDetail -> {
                val subject = dbSubjects.find { it.id == screen.subjectId }
                SubjectDetailScreen(
                    subjectId = screen.subjectId,
                    subjectName = subject?.name ?: "",
                    onBackClick = { navigateBack() },
                    onViewAllTasksClick = { selectBottomTab(ScreenDestination.Tasks) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            ScreenDestination.Tasks -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Tasks Screen", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            ScreenDestination.Profile -> ProfileContent(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}