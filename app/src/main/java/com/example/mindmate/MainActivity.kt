package com.example.mindmate

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.unit.dp
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
    data object Schedule : ScreenDestination()
    data object Profile : ScreenDestination()
    data object AddSubject : ScreenDestination()

    // Subject details (Overview / Tasks / Notes); startTab lets us open straight on Tasks
    data class SubjectDetail(val subjectId: Int, val startTab: Int = 0) : ScreenDestination()
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

    // Saves a new subject (rejecting duplicate names) and returns to the Subjects list.
    // With openTasksTab = true it then opens the new subject on its Tasks tab.
    fun saveNewSubject(name: String, openTasksTab: Boolean) {
        scope.launch {
            if (dao.checkSubjectNameExists(userEmail, name, -1) > 0) {
                Toast.makeText(context, "A subject with this name already exists", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val newId = dao.insertSubject(Subject(userEmail = userEmail, name = name)).toInt()
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
            if (backStack.lastOrNull() != ScreenDestination.Subjects) {
                backStack.add(ScreenDestination.Subjects)
            }
            if (openTasksTab) {
                backStack.add(ScreenDestination.SubjectDetail(newId, startTab = 1))
            }
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
                        iconRes = R.drawable.schedule,
                        label = "Schedule",
                        isSelected = currentScreen == ScreenDestination.Schedule
                    ) { selectBottomTab(ScreenDestination.Schedule) }

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
                onAddTasksClick = { subjectName -> saveNewSubject(subjectName, openTasksTab = true) },
                // Saved to Room so it survives app restarts
                onSaveSubject = { subjectName -> saveNewSubject(subjectName, openTasksTab = false) }
            )

            is ScreenDestination.SubjectDetail -> {
                val subject = dbSubjects.find { it.id == screen.subjectId }
                SubjectDetailScreen(
                    subjectId = screen.subjectId,
                    subjectName = subject?.name ?: "",
                    onBackClick = { navigateBack() },
                    onViewAllTasksClick = { selectBottomTab(ScreenDestination.Tasks) },
                    onSubjectDeleted = { navigateBack() },
                    initialTab = screen.startTab,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            ScreenDestination.Tasks -> TasksScreen(
                dao = dao,
                modifier = Modifier.padding(innerPadding)
            )

            ScreenDestination.Schedule -> ScheduleScreen(
                dao = dao,
                modifier = Modifier.padding(innerPadding)
            )

            ScreenDestination.Profile -> ProfileContent(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
