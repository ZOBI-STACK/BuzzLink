package com.example.buzzlink

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var usernameText: TextView
    private lateinit var profileBio: TextView
    private lateinit var bioText: TextView
    private lateinit var postsCount: TextView
    private lateinit var userPostRecyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var btnFollow: Button
    private val postList = ArrayList<Post>()

    private lateinit var postRef: DatabaseReference
    private lateinit var userRef: DatabaseReference
    private var userId: String = ""
    private lateinit var actualCurrentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        userId = intent.getStringExtra("USER_ID") ?: run {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        postRef = FirebaseDatabase.getInstance().getReference("Posts")
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

        profileImage = findViewById(R.id.profileImage)
        usernameText = findViewById(R.id.usernameText)
        profileBio = findViewById(R.id.profileBio)
        bioText = findViewById(R.id.bioText)
        postsCount = findViewById(R.id.postsCount)
        userPostRecyclerView = findViewById(R.id.userPostRecyclerView)
        btnFollow = findViewById(R.id.btnFollow)

        actualCurrentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Adapter disables Edit and Delete icons in user profile
        postAdapter = PostAdapter(postList, actualCurrentUserId,
            onEditClick = { /* No-op */ },
            onDeleteClick = { /* No-op */ },
            showDeleteIcon = false,
            showEditIcon = false
        )
        userPostRecyclerView.adapter = postAdapter
        userPostRecyclerView.layoutManager = LinearLayoutManager(this)
        userPostRecyclerView.setHasFixedSize(true)

        loadUserProfile()
        loadUserPosts()

        // Show/hide and handle follow button
        if (userId == actualCurrentUserId) {
            btnFollow.visibility = View.GONE
        } else {
            btnFollow.visibility = View.VISIBLE
            updateFollowButtonState()
            btnFollow.setOnClickListener {
                toggleFollowState()
            }
        }
    }

    private fun updateFollowButtonState() {
        val followersRef = FirebaseDatabase.getInstance().getReference("Users")
            .child(userId)
            .child("Followers")
        followersRef.child(actualCurrentUserId).get().addOnSuccessListener { snapshot ->
            val isFollowing = snapshot.exists()
            btnFollow.text = if (isFollowing) "Unfollow" else "Follow"
        }
    }

    private fun toggleFollowState() {
        val userRef = FirebaseDatabase.getInstance().getReference("Users")
        val followersRef = userRef.child(userId).child("Followers")
        val followingRef = userRef.child(actualCurrentUserId).child("Following")
        val isFollow = btnFollow.text == "Follow"

        if (isFollow) {
            // Add follower
            followersRef.child(actualCurrentUserId).setValue(true)
            followingRef.child(userId).setValue(true)
            btnFollow.text = "Unfollow"
            Toast.makeText(this, "You are now following this user", Toast.LENGTH_SHORT).show()
        } else {
            // Remove follower
            followersRef.child(actualCurrentUserId).removeValue()
            followingRef.child(userId).removeValue()
            btnFollow.text = "Follow"
            Toast.makeText(this, "Unfollowed user", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserProfile() {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.child("username").getValue(String::class.java) ?: ""
                val bio = snapshot.child("bio").getValue(String::class.java) ?: ""
                val extraBio = snapshot.child("extraBio").getValue(String::class.java) ?: ""
                val imageUrl = snapshot.child("profileImage").getValue(String::class.java) ?: ""

                usernameText.text = username
                profileBio.text = bio
                bioText.text = extraBio

                if (imageUrl.isNotEmpty()) {
                    // Use your image loader here (Picasso/Glide)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadUserPosts() {
        postRef.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList.clear()
                    for (postSnapshot in snapshot.children) {
                        val post = postSnapshot.getValue(Post::class.java)
                        if (post != null) {
                            val postTime = postSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                            post.timestamp = postTime
                            postList.add(post)
                        }
                    }
                    postList.sortByDescending { it.timestamp ?: 0L }
                    postAdapter.notifyDataSetChanged()
                    postsCount.text = "Posts: ${postList.size}"
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UserProfileActivity, "Failed to load posts", Toast.LENGTH_SHORT).show()
                }
            })
    }
}