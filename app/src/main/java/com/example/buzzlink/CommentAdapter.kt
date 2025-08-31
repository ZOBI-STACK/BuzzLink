package com.example.buzzlink

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.BaseAdapter

class CommentAdapter(
    private val context: Context,
    private val commentList: List<Comment>
) : BaseAdapter() {

    override fun getCount(): Int = commentList.size

    override fun getItem(position: Int): Any = commentList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false)
        val comment = commentList[position]

        val usernameText: TextView = view.findViewById(R.id.commentUsername)
        val commentText: TextView = view.findViewById(R.id.commentText)
        val timeText: TextView = view.findViewById(R.id.commentTime)

        usernameText.text = comment.username ?: ""
        commentText.text = comment.text ?: ""
        timeText.text = comment.timestamp?.let { formatTime(it) } ?: ""

        return view
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}