package com.example.buzzlink

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EditProfileActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var saveButton: Button

    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        usernameEditText = findViewById(R.id.editUsername)
        bioEditText = findViewById(R.id.editBio)
        saveButton = findViewById(R.id.saveButton)

        val auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId)

        // Load current username and bio from Firebase
        userRef.get().addOnSuccessListener { dataSnapshot ->
            usernameEditText.setText(dataSnapshot.child("username").getValue(String::class.java) ?: "")
            bioEditText.setText(dataSnapshot.child("bio").getValue(String::class.java) ?: "")
        }

        saveButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val bio = bioEditText.text.toString().trim()

            val updates = mapOf(
                "username" to username,
                "bio" to bio
            )
            userRef.updateChildren(updates).addOnSuccessListener {
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
                finish() // Go back to ProfileActivity
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }
}