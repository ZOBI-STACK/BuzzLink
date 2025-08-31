package com.example.buzzlink

data class Chat(
    val chatId: String = "",
    val userIds: List<String> = emptyList()
)