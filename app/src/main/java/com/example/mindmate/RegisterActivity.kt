package com.example.mindmate

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmate.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: UserSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = UserSessionManager(this)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnCreateAccount.setOnClickListener {
            handleRegistration()
        }

        binding.tvSignIn.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun handleRegistration() {
        val fullName = binding.etFullName.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()
        val confirmPassword = binding.etConfirmPassword.text?.toString().orEmpty()
        val isTermsChecked = binding.cbTerms.isChecked

        // Reset field errors
        binding.tilFullName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        // 1. Validate Full Name
        if (fullName.isEmpty()) {
            binding.tilFullName.error = "Full Name is required"
            binding.etFullName.requestFocus()
            return
        }
        if (fullName.length < 3) {
            binding.tilFullName.error = "Full Name must be at least 3 characters"
            binding.etFullName.requestFocus()
            return
        }

        // 2. Validate Email
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email address is required"
            binding.etEmail.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email address"
            binding.etEmail.requestFocus()
            return
        }
        if (sessionManager.isEmailRegistered(email)) {
            binding.tilEmail.error = "This email is already registered. Please log in."
            binding.etEmail.requestFocus()
            return
        }

        // 3. Validate Password
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            binding.etPassword.requestFocus()
            return
        }
        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            binding.etPassword.requestFocus()
            return
        }

        // 4. Validate Confirm Password
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Please confirm your password"
            binding.etConfirmPassword.requestFocus()
            return
        }
        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            binding.etConfirmPassword.requestFocus()
            return
        }

        // 5. Validate Terms
        if (!isTermsChecked) {
            Toast.makeText(
                this@RegisterActivity,
                "Please accept the Terms of Service & Privacy Policy",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        setLoading(true)

        binding.root.postDelayed(
            /* action = */ {
                setLoading(false)

                // Save user account
                sessionManager.registerUser(fullName, email, password)

                Toast.makeText(
                    this@RegisterActivity,
                    "Account created! Welcome, $fullName 🎉",
                    Toast.LENGTH_LONG
                ).show()

                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                intent.putExtra("USER_NAME", fullName)
                intent.putExtra("USER_EMAIL", email)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            },
            /* delayMillis = */ 1000L
        )
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnCreateAccount.isEnabled = !isLoading
    }
}
