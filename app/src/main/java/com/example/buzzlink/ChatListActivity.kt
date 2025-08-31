package com.example.buzzlink

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatListAdapter
    private val chats = mutableListOf<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)
        recyclerView = findViewById(R.id.recyclerChatList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with empty list
        adapter = ChatListAdapter(chats) { chat ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatId", chat.chatId)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        loadChats()
    }

    private fun loadChats() {
        Toast.makeText(this, "Loading chats...", Toast.LENGTH_SHORT).show()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show()
            chats.clear()
            adapter.notifyDataSetChanged()
            return
        }
        FirebaseFirestore.getInstance().collection("chats")
            .whereArrayContains("userIds", uid)
            .get()
            .addOnSuccessListener { docs ->
                chats.clear()
                for (document in docs) {
                    document.toObject(Chat::class.java)?.let { chat ->
                        chats.add(chat)
                    }
                }
                adapter.notifyDataSetChanged()
                if (chats.isEmpty()) {
                    Toast.makeText(this, "No chats found.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Loaded ${chats.size} chats", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                chats.clear()
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Failed to load chats: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("ChatListActivity", "Error loading chats", e)
            }
    }
}