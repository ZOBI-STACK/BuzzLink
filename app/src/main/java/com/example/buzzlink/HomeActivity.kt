package com.example.buzzlink

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    private lateinit var homeRecyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val postList = ArrayList<Post>()

    private lateinit var postRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Firebase init
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""

        postRef = FirebaseDatabase.getInstance().getReference("Posts")

        homeRecyclerView = findViewById(R.id.homeRecyclerView)

        // Use icons instead of buttons, hide delete icon in Home
        postAdapter = PostAdapter(postList, currentUserId,
            onEditClick = { post ->
                val intent = Intent(this, EditPostActivity::class.java)
                intent.putExtra("POST_ID", post.postId)
                startActivity(intent)
            },
            onDeleteClick = { post ->
                // no-op for home screen, or show toast
                // Toast.makeText(this, "Delete only available in Profile", Toast.LENGTH_SHORT).show()
            },
            showDeleteIcon = false,
            showEditIcon = true
        )
        homeRecyclerView.adapter = postAdapter
        homeRecyclerView.layoutManager = LinearLayoutManager(this)
        homeRecyclerView.setHasFixedSize(true)

        loadFollowedPosts()

        // Navigation logic, chat etc. ...
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    // Load only posts from followed users
    private fun loadFollowedPosts() {
        val followingRef = FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(currentUserId)
            .child("Following")

        followingRef.get().addOnSuccessListener { followingSnapshot ->
            val followingUserIds = followingSnapshot.children.mapNotNull { it.key }
            postRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList.clear()
                    for (postSnapshot in snapshot.children) {
                        val post = postSnapshot.getValue(Post::class.java)
                        if (post != null && followingUserIds.contains(post.userId)) {
                            val postTime = postSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                            post.timestamp = postTime
                            postList.add(post)
                        }
                    }
                    postList.sortByDescending { it.timestamp ?: 0L }
                    postAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }
}