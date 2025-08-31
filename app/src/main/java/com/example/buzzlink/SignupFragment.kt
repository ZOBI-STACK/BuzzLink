package com.example.buzzlink

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().getReference("Users")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup, container, false)

        val usernameEditText = view.findViewById<EditText>(R.id.signupUsername)
        val emailEditText = view.findViewById<EditText>(R.id.signupEmail)
        val passwordEditText = view.findViewById<EditText>(R.id.signupPassword)
        val signupBtn = view.findViewById<Button>(R.id.btnSignup)

        auth = FirebaseAuth.getInstance()

        signupBtn.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid

                            val userMap = mapOf(
                                "username" to username,
                                "email" to email,
                                "bio" to "Hey there! I'm new to BuzzLink 😊",
                                "profileImageUrl" to "" // Empty or default
                            )

                            if (userId != null) {
                                database.child(userId).setValue(userMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            requireContext(),
                                            "Signup successful!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            requireContext(),
                                            "Failed to save user data",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Signup failed: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
