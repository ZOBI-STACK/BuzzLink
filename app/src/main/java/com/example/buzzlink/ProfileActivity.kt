package com.example.buzzlink

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var usernameText: TextView
    private lateinit var profileBio: TextView
    private lateinit var bioText: TextView
    private lateinit var postsCount: TextView
    private lateinit var profilePostRecyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val postList = ArrayList<Post>()

    private lateinit var postRef: DatabaseReference
    private lateinit var userRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Firebase init
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        postRef = FirebaseDatabase.getInstance().getReference("Posts")
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId)

        val createPostButton: Button = findViewById(R.id.createPostButton)
        val logoutButton: Button = findViewById(R.id.logoutButton)
        val btnSettings: ImageButton = findViewById(R.id.btnSettings)

        profileImage = findViewById(R.id.profileImage)
        usernameText = findViewById(R.id.usernameText)
        profileBio = findViewById(R.id.profileBio)
        bioText = findViewById(R.id.bioText)
        postsCount = findViewById(R.id.postsCount)
        profilePostRecyclerView = findViewById(R.id.profilePostRecyclerView)

        // --- CHAT ICON LOGIC ---
        val chatIcon: ImageView? = findViewById(R.id.chatIcon)
        chatIcon?.setOnClickListener {
            Toast.makeText(this, "Opening ChatActivity", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        // --- SEARCH ICON LOGIC ---
        val searchIcon: ImageView? = findViewById(R.id.searchIcon)
        searchIcon?.setOnClickListener {
            showUserSearchDialog()
        }

        // RecyclerView setup
        postAdapter = PostAdapter(postList, currentUserId,
            onEditClick = { post ->
                val intent = Intent(this, EditPostActivity::class.java)
                intent.putExtra("POST_ID", post.postId)
                startActivity(intent)
            },
            onDeleteClick = { post ->
                post.postId?.let { postId ->
                    val postRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId)
                    postRef.removeValue().addOnSuccessListener {
                        Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to delete post", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            showDeleteIcon = true,
            showEditIcon = true
        )
        profilePostRecyclerView.adapter = postAdapter
        profilePostRecyclerView.layoutManager = LinearLayoutManager(this)
        profilePostRecyclerView.setHasFixedSize(true)

        // Load user data and posts
        loadUserProfile()
        loadUserPosts()

        createPostButton.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Settings button logic
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

        // Bottom navigation
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_profile -> true // Already here
                else -> false
            }
        }
    }

    private fun showUserSearchDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Search User by Username")

        val input = EditText(this)
        input.hint = "Enter username"
        builder.setView(input)

        builder.setPositiveButton("Search") { dialog, _ ->
            val username = input.text.toString().trim()
            if (username.isNotEmpty()) {
                searchUserByUsername(username)
            } else {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun searchUserByUsername(username: String) {
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")
        val query = usersRef.orderByChild("username").equalTo(username)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnap in snapshot.children) {
                        val userId = userSnap.key
                        val intent = Intent(this@ProfileActivity, UserProfileActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)
                        return
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Search failed: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
                    Picasso.get().load(imageUrl).into(profileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadUserPosts() {
        postRef.orderByChild("userId").equalTo(currentUserId)
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
                    Toast.makeText(this@ProfileActivity, "Failed to load posts", Toast.LENGTH_SHORT).show()
                }
            })
    }
}