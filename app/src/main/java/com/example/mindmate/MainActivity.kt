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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
}

@Composable
fun AppMainScreen() {
    val backStack = remember { mutableStateListOf<ScreenDestination>(ScreenDestination.Home) }
    val currentScreen = backStack.last()
    val subjectsList = remember { mutableStateListOf<SubjectModel>() }

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
                        isSelected = currentScreen == ScreenDestination.Subjects || currentScreen == ScreenDestination.AddSubject
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
        when (currentScreen) {
            ScreenDestination.Home -> HomeContent(
                subjectsList = subjectsList,
                onAddSubjectClick = { navigateTo(ScreenDestination.AddSubject) },
                onSeeAllClick = { navigateTo(ScreenDestination.Subjects) },
                modifier = Modifier.padding(innerPadding)
            )
            ScreenDestination.Subjects -> SubjectsContent(
                subjectsList = subjectsList,
                onAddSubjectClick = { navigateTo(ScreenDestination.AddSubject) },
                onBackClick = { navigateBack() },
                modifier = Modifier.padding(innerPadding)
            )
            ScreenDestination.AddSubject -> AddSubjectScreen(
                modifier = Modifier.padding(innerPadding),
                onBackClick = { navigateBack() },
                onSaveSubject = { subjectName ->
                    subjectsList.add(SubjectModel(name = subjectName, percentage = "0%"))
                    if (backStack.size > 1) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                    if (backStack.lastOrNull() != ScreenDestination.Subjects) {
                        backStack.add(ScreenDestination.Subjects)
                    }
                }
            )
            ScreenDestination.Tasks -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Tasks Screen", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            ScreenDestination.Profile -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Profile Screen", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}