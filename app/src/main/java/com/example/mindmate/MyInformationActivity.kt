package com.example.mindmate

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Calendar

class MyInformationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF8F9FA)) {
                MyInformationScreen(dao = db.appDao(), onBack = { finish() })
            }
        }
    }
}

private val MIBg      = Color(0xFFF8F9FA)
private val MICard    = Color.White
private val MIAccent  = Color(0xFF7C3AED)
private val MIAccentL = Color(0xFF7C6FCD)
private val MIText    = Color(0xFF1A1A1A)
private val MISubtext = Color(0xFF7A7A7A)
private val MIBorder  = Color(0xFFD0D7DE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyInformationScreen(dao: AppDao, onBack: () -> Unit = {}) {
    val context      = LocalContext.current
    val sessionMgr   = remember { UserSessionManager(context) }
    val profilePrefs = remember { ProfilePrefsManager(context) }
    val scope        = rememberCoroutineScope()
    val email = sessionMgr.getUserEmail()
    var fullName       by remember { mutableStateOf("") }
    var username       by remember { mutableStateOf("") }
    var dateOfBirth    by remember { mutableStateOf("") }
    var phone          by remember { mutableStateOf("") }
    var gender         by remember { mutableStateOf("Prefer not to say") }
    var genderExpanded by remember { mutableStateOf(false) }
    val genderOptions  = listOf("Female", "Male", "Prefer not to say")

    LaunchedEffect(email) {
        var user = dao.getUser(email)
        if (user == null) {
            user = User(
                email    = email,
                fullName = sessionMgr.getUserName(),
                password = ""
            )
            dao.insertUser(user)
        }
        fullName    = user.fullName.ifBlank { sessionMgr.getUserName() }
        username    = user.username
        dateOfBirth = user.dateOfBirth
        phone       = user.phone
        gender      = user.gender.ifBlank { "Prefer not to say" }
    }

    Column(modifier = Modifier.fillMaxSize().background(MIBg).statusBarsPadding()) {

        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 8.dp, end = 8.dp, bottom = 4.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = "Back",
                    tint = MIText
                )
            }
            Text(
                "My Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MIText,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        HorizontalDivider(color = MIBorder, thickness = 1.dp)

        // Form
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Full Name
            MITextField(label = "Full name", value = fullName, onValueChange = { fullName = it })

            // Username
            MITextField(label = "Username", value = username, onValueChange = { username = it.trim() })

            // Date of Birth
            Column {
                Text("Date of birth", fontSize = 13.sp, color = MISubtext, modifier = Modifier.padding(bottom = 6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MICard)
                        .border(1.dp, MIBorder, RoundedCornerShape(10.dp))
                        .clickable {
                            val parts = dateOfBirth.split("/")
                            val cal   = Calendar.getInstance()
                            val day   = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.DAY_OF_MONTH)
                            val month = (parts.getOrNull(1)?.toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)) - 1
                            val year  = parts.getOrNull(2)?.toIntOrNull() ?: (cal.get(Calendar.YEAR) - 20)
                            DatePickerDialog(context, { _, y, m, d ->
                                dateOfBirth = "%02d/%02d/%04d".format(d, m + 1, y)
                            }, year, month, day).show()
                        }
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Text(
                        text  = "Date of birth: ${dateOfBirth.ifBlank { "Not set" }}",
                        fontSize = 15.sp,
                        color = if (dateOfBirth.isBlank()) MISubtext else MIText
                    )
                }
            }

            // Gender dropdown
            Column {
                Text("Gender", fontSize = 13.sp, color = MISubtext, modifier = Modifier.padding(bottom = 6.dp))
                ExposedDropdownMenuBox(
                    expanded = genderExpanded,
                    onExpandedChange = { genderExpanded = !genderExpanded }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MICard)
                            .border(1.dp, MIBorder, RoundedCornerShape(10.dp))
                            .menuAnchor()
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Gender: $gender", fontSize = 15.sp, color = MIText)
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded)
                        }
                    }
                    ExposedDropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false },
                        containerColor = MICard
                    ) {
                        genderOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        option,
                                        color = if (option == gender) MIAccentL else MIText
                                    )
                                },
                                onClick = {
                                    gender = option
                                    genderExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Email — read-only
            Column {
                Text("Email (can't be changed)", fontSize = 13.sp, color = MISubtext, modifier = Modifier.padding(bottom = 6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEFEFEF))
                        .border(1.dp, MIBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Text(email, fontSize = 15.sp, color = MISubtext)
                }
            }

            // Phone
            MITextField(
                label         = "Phone number",
                value         = phone,
                onValueChange = { phone = it },
                keyboardType  = KeyboardType.Phone
            )
        }

        // Save Changes button
        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Button(
                onClick = {
                    if (fullName.trim().length < 2) {
                        Toast.makeText(context, "Enter your full name (at least 2 characters)", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val digits = phone.filter { it.isDigit() }.length
                    if (phone.isNotBlank() && digits !in 7..15) {
                        Toast.makeText(context, "Enter a valid phone number", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch {
                        val existing = dao.getUser(email)
                        val updated  = (existing ?: User(email = email, password = "")).copy(
                            fullName    = fullName.trim(),
                            username    = username.trim(),
                            dateOfBirth = dateOfBirth,
                            gender      = gender,
                            phone       = phone.trim()
                        )
                        if (existing == null) dao.insertUser(updated) else dao.updateUser(updated)
                        // Mirror to ProfilePrefsManager so Profile avatar initial updates live
                        profilePrefs.setFullName(fullName.trim())
                        Toast.makeText(context, "Profile updated ✓", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = MIAccent)
            ) {
                Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

@Composable
private fun MITextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(label, fontSize = 13.sp, color = MISubtext, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(10.dp),
            singleLine    = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = MIAccentL,
                unfocusedBorderColor    = MIBorder,
                focusedTextColor        = MIText,
                unfocusedTextColor      = MIText,
                cursorColor             = MIAccentL,
                focusedContainerColor   = MICard,
                unfocusedContainerColor = MICard
            )
        )
    }
}