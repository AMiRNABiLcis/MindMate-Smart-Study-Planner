package com.example.mindmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFF8F9FA)
            ) {
                MainShellScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainShellScreen(
    subjectsList: List<SubjectModel> = emptyList(),
    completedTasks: Int = 0,
    totalTasks: Int = 0,
    initialTab: Int = 0,
    onAddSubjectClick: () -> Unit = {},
    onSubjectClick: (SubjectModel) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }

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
                        isSelected = selectedTab == 0
                    ) { selectedTab = 0 }

                    BottomBarItem(
                        iconRes = R.drawable.subjects,
                        label = "Subjects",
                        isSelected = selectedTab == 1
                    ) { selectedTab = 1 }

                    BottomBarItem(
                        iconRes = R.drawable.tasks,
                        label = "Tasks",
                        isSelected = selectedTab == 2
                    ) { selectedTab = 2 }

                    BottomBarItem(
                        iconRes = R.drawable.profile,
                        label = "Profile",
                        isSelected = selectedTab == 3
                    ) { selectedTab = 3 }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeContent(
                subjectsList = subjectsList,
                completedTasks = completedTasks,
                totalTasks = totalTasks,
                onAddSubjectClick = onAddSubjectClick,
                onSeeAllClick = { selectedTab = 1 },
                onSubjectClick = onSubjectClick,
                modifier = Modifier.padding(innerPadding)
            )
            1 -> SubjectsContent(
                subjectsList = subjectsList,
                onAddSubjectClick = onAddSubjectClick,
                onSubjectClick = onSubjectClick,
                onBackClick = { selectedTab = 0 },
                modifier = Modifier.padding(innerPadding)
            )
            2 -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Tasks Screen", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            3 -> Box(
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

@Composable
fun HomeContent(
    subjectsList: List<SubjectModel> = emptyList(),
    completedTasks: Int = 0,
    totalTasks: Int = 0,
    onAddSubjectClick: () -> Unit = {},
    onSeeAllClick: () -> Unit = {},
    onSubjectClick: (SubjectModel) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val progressFraction = if (totalTasks > 0) {
        (completedTasks.toFloat() / totalTasks.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val progressPercentage = (progressFraction * 100).toInt()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hello, ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Let's Make Today Count!",
                    fontSize = 14.sp,
                    color = Color(0xFF7A7A7A)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(text = "Your Progress", fontSize = 14.sp, color = Color(0xFFE3F2FD))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$progressPercentage%",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(text = "Overall Completion", fontSize = 12.sp, color = Color(0xFFE3F2FD))

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF2196F3),
                            trackColor = Color(0xFFE0E0E0),
                            strokeCap = StrokeCap.Butt,
                            gapSize = 0.dp,
                            drawStopIndicator = {}
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Complete $completedTasks/$totalTasks",
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Subjects",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            if (subjectsList.isNotEmpty()) {
                Row(
                    modifier = Modifier.clickable { onSeeAllClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "See all",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_forward),
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (subjectsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEFEFEF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No subjects yet. Tap below to add one!",
                    fontSize = 14.sp,
                    color = Color(0xFF7A7A7A),
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                subjectsList.forEach { subject ->
                    SubjectCard(
                        name = subject.name,
                        percentage = subject.percentage,
                        onClick = { onSubjectClick(subject) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clickable { onAddSubjectClick() },
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFF8AA8C7)),
            shadowElevation = 2.dp
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Add New Subject",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
        }
    }
}

@Composable
fun BottomBarItem(iconRes: Int, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) Color(0xFF2196F3) else Color(0xFF1A1A1A)

    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = color,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SubjectCard(
    name: String = "Mathematics",
    percentage: String = "80%",
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .width(140.dp)
            .height(74.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF284B6E),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = percentage,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

data class SubjectModel(
    val id: Int = 0,
    val name: String,
    val percentage: String
)