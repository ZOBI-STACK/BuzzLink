package com.example.buzzlink

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var resetButton: Button
    private lateinit var messageTextView: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        emailEditText = findViewById(R.id.emailEditText)
        resetButton = findViewById(R.id.resetButton)
        messageTextView = findViewById(R.id.messageTextView)
        auth = FirebaseAuth.getInstance()

        resetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                messageTextView.text = "Please enter your email."
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        messageTextView.text = "A password reset email has been sent."
                    } else {
                        messageTextView.text = "Failed to send reset email: ${task.exception?.localizedMessage}"
                    }
                }
        }

        // Add this inside your onCreate in ForgotPasswordActivity
        val backToLoginButton: Button = findViewById(R.id.backToLoginButton)
        backToLoginButton.setOnClickListener {
            finish() // This will just go back to the previous (Login) screen
        }
    }
}