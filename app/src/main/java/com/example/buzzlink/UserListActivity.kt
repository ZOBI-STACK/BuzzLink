package com.example.buzzlink

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserListAdapter
    private val users = mutableListOf<User>()
    private lateinit var textEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        recyclerView = findViewById(R.id.
        recyclerUserList)
        textEmpty = findViewById(R.id.textEmpty)
        adapter = UserListAdapter(users) { user ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("otherUserId", user.id)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadUsers()
    }

    private fun loadUsers() {
        val currentId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Users")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                for (userSnap in snapshot.children) {
                    val user = userSnap.getValue(User::class.java)
                    val userId = userSnap.key ?: continue
                    if (userId != currentId && user != null) {
                        users.add(user.copy(id = userId))
                    }
                }
                adapter.notifyDataSetChanged()
                textEmpty.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserListActivity, "Failed to load users", Toast.LENGTH_SHORT).show()
            }
        })
    }
}