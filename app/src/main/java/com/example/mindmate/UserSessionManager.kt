package com.example.mindmate

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.MessageDigest

/**
 * Manages user authentication state and session data locally using SharedPreferences.
 * Supports multiple registered user accounts keyed by email.
 */
class UserSessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "mindmate_user_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_CURRENT_NAME = "current_user_name"
        private const val KEY_CURRENT_EMAIL = "current_user_email"

        private fun nameKey(email: String) = "user_${email.trim().lowercase()}_name"
        private fun hashKey(email: String) = "user_${email.trim().lowercase()}_hash"
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    fun registerUser(name: String, email: String, password: String) {
        val normalizedEmail = email.trim().lowercase()
        prefs.edit().apply {
            putString(nameKey(normalizedEmail), name)
            putString(hashKey(normalizedEmail), hashPassword(password))

            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_CURRENT_NAME, name)
            putString(KEY_CURRENT_EMAIL, email.trim())
            apply()
        }
    }

    fun loginUser(email: String, password: String): LoginResult {
        val normalizedEmail = email.trim().lowercase()
        val storedName = prefs.getString(nameKey(normalizedEmail), null)
        val storedHash = prefs.getString(hashKey(normalizedEmail), null)

        return when {
            storedHash == null -> {
                LoginResult.EMAIL_NOT_FOUND
            }
            hashPassword(password) != storedHash -> {
                LoginResult.WRONG_PASSWORD
            }
            else -> {
                prefs.edit().apply {
                    putBoolean(KEY_IS_LOGGED_IN, true)
                    putString(KEY_CURRENT_NAME, storedName ?: normalizedEmail.substringBefore("@"))
                    putString(KEY_CURRENT_EMAIL, email.trim())
                    apply()
                }
                LoginResult.SUCCESS
            }
        }
    }

    fun isEmailRegistered(email: String): Boolean {
        val normalizedEmail = email.trim().lowercase()
        return prefs.contains(hashKey(normalizedEmail))
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserName(): String = prefs.getString(KEY_CURRENT_NAME, "") ?: ""

    fun getUserEmail(): String = prefs.getString(KEY_CURRENT_EMAIL, "") ?: ""

    fun updateName(newName: String) {
        val email = getUserEmail().trim().lowercase()
        if (email.isEmpty()) return
        prefs.edit().apply {
            putString(nameKey(email), newName)
            putString(KEY_CURRENT_NAME, newName)
            apply()
        }
    }

    fun logout() {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_CURRENT_NAME)
            remove(KEY_CURRENT_EMAIL)
            apply()
        }
    }

    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}

enum class LoginResult {
    SUCCESS,
    EMAIL_NOT_FOUND,
    WRONG_PASSWORD
}
