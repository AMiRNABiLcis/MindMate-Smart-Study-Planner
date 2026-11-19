package com.example.mindmate

import android.content.Context
import android.content.SharedPreferences

class ProfilePrefsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mindmate_profile_prefs", Context.MODE_PRIVATE)

    fun getFullName(): String     = prefs.getString("full_name", "") ?: ""
    fun setFullName(v: String)    { prefs.edit().putString("full_name", v).apply() }

    fun getUsername(): String     = prefs.getString("username", "") ?: ""
    fun setUsername(v: String)    { prefs.edit().putString("username", v).apply() }

    fun getPhone(): String        = prefs.getString("phone", "") ?: ""
    fun setPhone(v: String)       { prefs.edit().putString("phone", v).apply() }

    fun getGender(): String       = prefs.getString("gender", "Prefer not to say") ?: "Prefer not to say"
    fun setGender(v: String)      { prefs.edit().putString("gender", v).apply() }

    fun getDateOfBirth(): String  = prefs.getString("date_of_birth", "") ?: ""
    fun setDateOfBirth(v: String) { prefs.edit().putString("date_of_birth", v).apply() }
}
