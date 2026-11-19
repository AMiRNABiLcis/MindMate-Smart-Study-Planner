package com.example.mindmate

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

/**
 * Subject details page.
 * Only the **Overview** tab is implemented here. The Tasks and Notes tabs are
 * placeholders because they belong to other team members.
 */
@Composable
fun SubjectDetailScreen(
    subjectId: Int,
    subjectName: String,
    onBackClick: () -> Unit,
    onViewAllTasksClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).appDao() }

    // Live tasks of this subject (read-only here – Tasks team owns CRUD)
    val tasks by remember(subjectId) { dao.getTasksForSubject(subjectId) }
        .collectAsState(initial = emptyList())

    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.status == "Completed" }
    val percent = if (totalTasks == 0) 0 else completedTasks * 100 / totalTasks

    val recentCompleted = remember(tasks) {
        tasks.filter { it.status == "Completed" }
            .sortedByDescending { it.completedAt ?: 0L }
            .take(4)
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Back arrow
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

            else -> PlaceholderTab(
                title = DetailTabs[selectedTab],
                modifier = Modifier.weight(1f)
            )
        }
    }
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

            // grey track
            drawArc(
                color = RingTrack,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            // gradient progress arc, starting at 12 o'clock
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

@Composable
private fun PlaceholderTab(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title will be added soon",
            fontSize = 15.sp,
            color = Color(0xFF7A7A7A),
            textAlign = TextAlign.Center
        )
    }
}