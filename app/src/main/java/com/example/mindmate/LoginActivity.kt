package com.example.mindmate

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindmate.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: UserSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = UserSessionManager(this)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            handleLogin()
        }

        binding.tvSignup.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmailLogin.text?.toString()?.trim().orEmpty()
            if (email.isNotEmpty()) {
                Toast.makeText(
                    this@LoginActivity,
                    "Password reset link sent to $email",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    "Please enter your email first",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun handleLogin() {
        val email = binding.etEmailLogin.text?.toString()?.trim().orEmpty()
        val password = binding.etPasswordLogin.text?.toString().orEmpty()

        binding.tilEmailLogin.error = null
        binding.tilPasswordLogin.error = null

        if (email.isEmpty()) {
            binding.tilEmailLogin.error = "Email is required"
            binding.etEmailLogin.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmailLogin.error = "Please enter a valid email address"
            binding.etEmailLogin.requestFocus()
            return
        }
        if (password.isEmpty()) {
            binding.tilPasswordLogin.error = "Password is required"
            binding.etPasswordLogin.requestFocus()
            return
        }
        if (password.length < 6) {
            binding.tilPasswordLogin.error = "Password must be at least 6 characters"
            binding.etPasswordLogin.requestFocus()
            return
        }

        setLoading(true)

        binding.root.postDelayed(
            /* action = */ {
                setLoading(false)

                when (sessionManager.loginUser(email, password)) {
                    LoginResult.SUCCESS -> {
                        val userName = sessionManager.getUserName()
                        Toast.makeText(
                            this@LoginActivity,
                            "Welcome back, $userName! 👋",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("USER_NAME", userName)
                        intent.putExtra("USER_EMAIL", email)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }

                    LoginResult.EMAIL_NOT_FOUND -> {
                        binding.tilEmailLogin.error = "Email not found. Please create an account."
                        Toast.makeText(
                            this@LoginActivity,
                            "No account found with this email. Please Sign up.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    LoginResult.WRONG_PASSWORD -> {
                        binding.tilPasswordLogin.error = "Incorrect password"
                    }
                }
            },
            /* delayMillis = */ 1000L
        )
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
    }
}
