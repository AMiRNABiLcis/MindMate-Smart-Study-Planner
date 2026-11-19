package com.example.mindmate

import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Light colour palette
private val ProfileBg     = Color(0xFFF8F9FA)
private val CardBg        = Color.White
private val TextPrimary   = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF7A7A7A)
private val AvatarBg      = Color(0xFFB0A4E3)

@Composable
fun ProfileContent(modifier: Modifier = Modifier) {
    val context        = LocalContext.current
    val sessionManager = remember { UserSessionManager(context) }
    val profilePrefs   = remember { ProfilePrefsManager(context) }
    val db             = remember { AppDatabase.getDatabase(context) }
    val dao            = remember { db.appDao() }

    val sessionEmail = sessionManager.getUserEmail()
    val sessionName  = sessionManager.getUserName()
    val roomUser by dao.observeUser(sessionEmail).collectAsState(initial = null)

    LaunchedEffect(sessionEmail) {
        if (sessionEmail.isNotBlank() && dao.getUser(sessionEmail) == null) {
            dao.insertUser(
                User(
                    email    = sessionEmail,
                    fullName = sessionName,
                    password = ""
                )
            )
        }
    }
    val displayName = roomUser?.fullName?.ifBlank { null }
        ?: profilePrefs.getFullName().ifBlank { null }
        ?: sessionName.ifBlank { "User" }
    val initial = displayName.firstOrNull()?.uppercaseChar() ?: 'U'

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ProfileBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        // Avatar
        Box(
            modifier = Modifier.size(88.dp).clip(CircleShape).background(AvatarBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial.toString(),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(displayName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text(sessionEmail, fontSize = 13.sp, color = TextSecondary)

        Spacer(Modifier.height(28.dp))

        // My Information
        ProfileRow(emoji = "👤", label = "My Information", showArrow = true) {
            context.startActivity(Intent(context, MyInformationActivity::class.java))
        }
        Spacer(Modifier.height(10.dp))

        // Help & Support
        ProfileRow(emoji = "❓", label = "Help & Support", showArrow = true) {
            context.startActivity(Intent(context, HelpSupportActivity::class.java))
        }
        Spacer(Modifier.height(24.dp))

        // Log Out
        Button(
            onClick = {
                sessionManager.logout()
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
        ) {
            Text("Log Out", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Composable
private fun ProfileRow(
    emoji: String,
    label: String,
    showArrow: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = CardBg,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            if (showArrow) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_forward),
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}