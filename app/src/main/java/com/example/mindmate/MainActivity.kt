package com.example.mindmate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.lifecycle.viewmodel.compose.viewModel

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
    data object MyInformation : ScreenDestination()
    data class SubjectDetails(val subjectId: Long) : ScreenDestination()
}

@Composable
fun AppMainScreen(vm: AppViewModel = viewModel()) {
    val context = LocalContext.current
    val backStack = remember { mutableStateListOf<ScreenDestination>(ScreenDestination.Home) }
    val currentScreen = backStack.last()

    val subjectModels by vm.subjectModels.collectAsState()
    val subjectsWithTasks by vm.subjectsWithTasks.collectAsState()
    val progress by vm.progress.collectAsState()
    var userName by remember { mutableStateOf(vm.userName) }

    fun navigateTo(destination: ScreenDestination) {
        if (backStack.lastOrNull() != destination) backStack.add(destination)
    }

    fun selectBottomTab(tab: ScreenDestination) {
        backStack.clear()
        backStack.add(ScreenDestination.Home)
        if (tab != ScreenDestination.Home) backStack.add(tab)
    }

    fun navigateBack() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    fun openTasks(subjectId: Long) {
        context.startActivity(
            Intent(context, TasksActivity::class.java).putExtra("subject_id", subjectId)
        )
    }

    fun editSubject(subjectId: Long) {
        context.startActivity(
            Intent(context, EditSubjectActivity::class.java).putExtra("subject_id", subjectId)
        )
    }

    fun logout() {
        vm.logout()
        context.startActivity(
            Intent(context, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    BackHandler(enabled = backStack.size > 1) { navigateBack() }

    val openSubject: (SubjectModel) -> Unit = { navigateTo(ScreenDestination.SubjectDetails(it.id)) }

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
                                currentScreen is ScreenDestination.SubjectDetails
                    ) { selectBottomTab(ScreenDestination.Subjects) }

                    BottomBarItem(
                        iconRes = R.drawable.tasks,
                        label = "Tasks",
                        isSelected = currentScreen == ScreenDestination.Tasks
                    ) { selectBottomTab(ScreenDestination.Tasks) }

                    BottomBarItem(
                        iconRes = R.drawable.profile,
                        label = "Profile",
                        isSelected = currentScreen == ScreenDestination.Profile ||
                                currentScreen == ScreenDestination.MyInformation
                    ) { selectBottomTab(ScreenDestination.Profile) }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)
        when (val screen = currentScreen) {
            ScreenDestination.Home -> HomeContent(
                userName = userName,
                subjectsList = subjectModels,
                completedTasks = progress.first,
                totalTasks = progress.second,
                onAddSubjectClick = { navigateTo(ScreenDestination.AddSubject) },
                onSeeAllClick = { navigateTo(ScreenDestination.Subjects) },
                onSubjectClick = openSubject,
                modifier = screenModifier
            )

            ScreenDestination.Subjects -> SubjectsContent(
                subjectsList = subjectModels,
                onAddSubjectClick = { navigateTo(ScreenDestination.AddSubject) },
                onSubjectClick = openSubject,
                onBackClick = { navigateBack() },
                modifier = screenModifier
            )

            ScreenDestination.AddSubject -> AddSubjectScreen(
                modifier = screenModifier,
                onBackClick = { navigateBack() },
                onSaveSubject = { subjectName ->
                    vm.addSubject(subjectName)
                    if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                    if (backStack.lastOrNull() != ScreenDestination.Subjects) {
                        backStack.add(ScreenDestination.Subjects)
                    }
                }
            )

            is ScreenDestination.SubjectDetails -> {
                val data = subjectsWithTasks.firstOrNull { it.subject.id == screen.subjectId }
                SubjectDetailsScreen(
                    data = data,
                    onBack = { navigateBack() },
                    onEdit = { editSubject(screen.subjectId) },
                    onDelete = {
                        data?.let { vm.deleteSubject(it.subject) }
                        navigateBack()
                    },
                    onViewTasks = { openTasks(screen.subjectId) },
                    modifier = screenModifier
                )
            }

            ScreenDestination.Tasks -> TasksHubScreen(
                subjects = subjectModels,
                onSubjectClick = { openTasks(it.id) },
                modifier = screenModifier
            )

            ScreenDestination.Profile -> ProfileScreen(
                name = userName,
                email = vm.userEmail,
                onMyInformation = { navigateTo(ScreenDestination.MyInformation) },
                onLogout = { logout() },
                modifier = screenModifier
            )

            ScreenDestination.MyInformation -> MyInformationScreen(
                name = userName,
                email = vm.userEmail,
                onSaveName = { vm.updateName(it); userName = it.trim() },
                onBack = { navigateBack() },
                modifier = screenModifier
            )
        }
    }
}
