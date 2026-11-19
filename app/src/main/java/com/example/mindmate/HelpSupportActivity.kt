package com.example.mindmate

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class HelpSupportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFF8F9FA)
            ) {
                HelpSupportScreen(onBack = { finish() })
            }
        }
    }
}

// Colour palette
private val HSBg      = Color(0xFFF8F9FA)
private val HSCard    = Color.White
private val HSAccent  = Color(0xFF7C3AED)
private val HSAccentL = Color(0xFF7C6FCD)
private val HSText    = Color(0xFF1A1A1A)
private val HSSubtext = Color(0xFF7A7A7A)
private val HSDivider = Color(0xFFE3E3EC)

private const val SUPPORT_EMAIL = "support@mindmate.app"

private data class FaqItem(val question: String, val answer: String)

private val faqs = listOf(
    FaqItem(
        "How do I add a new subject?",
        "Go to the Subjects tab and tap the '+' or 'Add New Subject' button. Enter the subject name and save."
    ),
    FaqItem(
        "How do I track my tasks?",
        "Navigate to the Tasks tab. You can create, edit, and mark tasks as complete from there."
    ),
    FaqItem(
        "Can I change my email address?",
        "For security reasons, your email address cannot be changed once set. Contact support if you need help."
    ),
    FaqItem(
        "How does the progress tracking work?",
        "MindMate tracks the tasks you complete. Your overall progress is shown on the Home tab, and each subject shows its own progress."
    ),
    FaqItem(
        "I forgot my password. What should I do?",
        "Currently the app stores credentials locally. If you are locked out, please contact support and we will assist you."
    )
)

@Composable
fun HelpSupportScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HSBg)
            .statusBarsPadding()
    ) {
        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 8.dp, end = 8.dp, bottom = 4.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = "Back",
                    tint = HSText
                )
            }
            Text(
                text = "Help & Support",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = HSText,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        HorizontalDivider(color = HSDivider, thickness = 1.dp)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Frequently Asked Questions",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = HSText
            )

            Spacer(modifier = Modifier.height(4.dp))

            faqs.forEach { faq ->
                FaqCard(faq = faq)
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(color = HSDivider)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Still need help?",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = HSText
            )
            Text(
                text = "Our support team is happy to assist you. Tap below to send us an email.",
                fontSize = 13.sp,
                color = HSSubtext,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
                        putExtra(Intent.EXTRA_SUBJECT, "MindMate Support Request")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Support email", SUPPORT_EMAIL))
                        Toast.makeText(
                            context,
                            "No email app found. Address copied: $SUPPORT_EMAIL",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HSAccent)
            ) {
                Text(
                    text = "✉️  Contact Us",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FaqCard(faq: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        color = HSCard,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = HSText,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (expanded) "▲" else "▼",
                    fontSize = 12.sp,
                    color = HSAccentL
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = HSDivider)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = faq.answer,
                        fontSize = 13.sp,
                        color = HSSubtext,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}