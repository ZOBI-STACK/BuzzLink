package com.example.buzzlink

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CommentsActivity : AppCompatActivity() {

    private lateinit var postId: String
    private lateinit var commentListView: ListView
    private lateinit var commentEditText: EditText
    private lateinit var btnSendComment: Button
    private lateinit var commentAdapter: CommentAdapter
    private val commentList = ArrayList<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        postId = intent.getStringExtra("POST_ID") ?: ""
        commentListView = findViewById(R.id.commentListView)
        commentEditText = findViewById(R.id.commentEditText)
        btnSendComment = findViewById(R.id.btnSendComment)

        commentAdapter = CommentAdapter(this, commentList)
        commentListView.adapter = commentAdapter

        val commentsRef = FirebaseDatabase.getInstance().getReference("Posts")
            .child(postId).child("comments")

        // Load comments
        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentList.clear()
                for (commentSnap in snapshot.children) {
                    val comment = commentSnap.getValue(Comment::class.java)
                    if (comment != null) commentList.add(comment)
                }
                commentAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // Send comment
        btnSendComment.setOnClickListener {
            val text = commentEditText.text.toString().trim()
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val username = "User" // Fetch from profile if needed
            if (text.isNotEmpty()) {
                val commentId = commentsRef.push().key ?: return@setOnClickListener
                val comment = Comment(commentId, userId, username, text, System.currentTimeMillis())
                commentsRef.child(commentId).setValue(comment)
                commentEditText.setText("")
            }
        }
    }
}