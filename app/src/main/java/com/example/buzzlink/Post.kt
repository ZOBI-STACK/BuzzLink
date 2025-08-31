package com.example.buzzlink

data class Post(
    var postId: String? = null,
    var userId: String? = null,
    var username: String? = null,
    var profileImageUrl: String? = null,
    var postText: String? = null,
    var imageUrl: String? = null,
    var timestamp: Long? = null,
    var likes: Any? = null // Defensive type
)