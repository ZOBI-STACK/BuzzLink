package com.example.buzzlink

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CreatePostActivity : AppCompatActivity() {

    private lateinit var editPostText: EditText
    private lateinit var editImageUrl: EditText
    private lateinit var btnPost: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        editPostText = findViewById(R.id.editPostText)
        editImageUrl = findViewById(R.id.editImageUrl)
        btnPost = findViewById(R.id.btnPost)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        btnPost.setOnClickListener {
            val postText = editPostText.text.toString().trim()
            val imageUrl = editImageUrl.text.toString().trim()
            val userId = auth.currentUser?.uid

            if (postText.isEmpty()) {
                Toast.makeText(this, "Please enter post text", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (imageUrl.isEmpty()) {
                Toast.makeText(this, "Please enter image URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (userId == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔥 Use "Posts" instead of "posts" everywhere!
            val postId = database.reference.child("Posts").push().key ?: return@setOnClickListener
            val timestamp = System.currentTimeMillis()

            val postMap = mapOf(
                "postId" to postId,
                "userId" to userId,
                "postText" to postText,
                "imageUrl" to imageUrl,
                "timestamp" to timestamp
            )

            // Save post in Firebase
            database.reference.child("Posts").child(postId).setValue(postMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Post created successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to create post: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}