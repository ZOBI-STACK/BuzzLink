package com.example.buzzlink

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var editText: EditText
    private lateinit var sendBtn: Button
    private lateinit var otherUserNameText: TextView
    private val messages = mutableListOf<Message>()
    private lateinit var adapter: ChatMessageAdapter
    private var otherUserId: String? = null
    private var chatId: String = ""
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        otherUserId = intent.getStringExtra("otherUserId")

        recyclerView = findViewById(R.id.recyclerChatMessages)
        editText = findViewById(R.id.editMessage)
        sendBtn = findViewById(R.id.btnSend)
        otherUserNameText = findViewById(R.id.textOtherUserName)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ChatMessageAdapter(messages)
        recyclerView.adapter = adapter

        loadOtherUserName()
        getChatIdAndLoadMessages()

        sendBtn.setOnClickListener {
            sendMessage()
        }
    }

    // Get user display name
    private fun loadOtherUserName() {
        val ref = FirebaseDatabase.getInstance().getReference("Users").child(otherUserId ?: return)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.child("username").getValue(String::class.java) ?: "Unknown"
                otherUserNameText.text = "Chat with $username"
            }
            override fun onCancelled(error: DatabaseError) {
                otherUserNameText.text = "Chat"
            }
        })
    }

    // Make a unique chatId per user pair, then load messages
    private fun getChatIdAndLoadMessages() {
        val currentId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userPair = listOf(currentId, otherUserId ?: "").sorted()
        chatId = userPair.joinToString("_")
        dbRef = FirebaseDatabase.getInstance().getReference("Chats").child(chatId)
        loadMessages()
    }

    private fun loadMessages() {
        dbRef.child("messages").orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for (msgSnap in snapshot.children) {
                        val msg = msgSnap.getValue(Message::class.java)
                        if (msg != null) messages.add(msg)
                    }
                    adapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messages.size - 1)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun sendMessage() {
        val text = editText.text.toString().trim()
        if (text.isNotEmpty()) {
            val senderId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val msg = Message(
                senderId = senderId,
                receiverId = otherUserId ?: "",
                text = text,
                timestamp = System.currentTimeMillis()
            )
            dbRef.child("messages").push().setValue(msg)
            editText.setText("")
        }
    }
}