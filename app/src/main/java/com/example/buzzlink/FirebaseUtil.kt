package com.example.buzzlink

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUtil {
    fun currentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun usersCollection(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("users")
    }

    fun chatsCollection(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("chats")
    }

    fun chatDocument(chatId: String): DocumentReference {
        return FirebaseFirestore.getInstance().collection("chats").document(chatId)
    }

    fun messagesCollection(chatId: String): CollectionReference {
        return chatDocument(chatId).collection("messages")
    }
}