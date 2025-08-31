package com.example.buzzlink

data class Comment(
    var commentId: String? = null,
    var userId: String? = null,
    var username: String? = null,
    var text: String? = null,
    var timestamp: Long? = null
)