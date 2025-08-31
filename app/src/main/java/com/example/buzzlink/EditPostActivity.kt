package com.example.buzzlink

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class EditPostActivity : AppCompatActivity() {

    private lateinit var editPostText: EditText
    private lateinit var editImageUrl: EditText
    private lateinit var btnSave: Button
    private lateinit var postId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)

        editPostText = findViewById(R.id.editPostText)
        editImageUrl = findViewById(R.id.editImageUrl)
        btnSave = findViewById(R.id.btnSave)

        postId = intent.getStringExtra("POST_ID") ?: run {
            Toast.makeText(this, "Invalid post", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val postRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId)

        // Load existing post data
        postRef.get().addOnSuccessListener { snapshot ->
            editPostText.setText(snapshot.child("postText").getValue(String::class.java) ?: "")
            editImageUrl.setText(snapshot.child("imageUrl").getValue(String::class.java) ?: "")
        }

        btnSave.setOnClickListener {
            val updatedText = editPostText.text.toString().trim()
            val updatedImageUrl = editImageUrl.text.toString().trim()

            if (updatedText.isEmpty() || updatedImageUrl.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updateMap = mapOf(
                "postText" to updatedText,
                "imageUrl" to updatedImageUrl
            )

            postRef.updateChildren(updateMap).addOnSuccessListener {
                Toast.makeText(this, "Post updated", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}