package com.example.buzzlink

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.FirebaseDatabase

class PostAdapter(
    private val postList: List<Post>,
    private val currentUserId: String,
    private val onEditClick: (Post) -> Unit,
    private val onDeleteClick: (Post) -> Unit,
    private val showDeleteIcon: Boolean = true,
    private val showEditIcon: Boolean = true
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    // Cache for usernames
    private val usernameCache = HashMap<String, String>()

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val username: TextView = itemView.findViewById(R.id.username)
        val time: TextView = itemView.findViewById(R.id.time)
        val content: TextView = itemView.findViewById(R.id.content)
        val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val editIcon: ImageView = itemView.findViewById(R.id.iconEditPost)
        val deleteIcon: ImageView = itemView.findViewById(R.id.iconDeletePost)
        val likeIcon: ImageView = itemView.findViewById(R.id.iconLike)
        val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        val commentIcon: ImageView = itemView.findViewById(R.id.iconComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        // --- Username logic ---
        val userId = post.userId
        if (!post.username.isNullOrEmpty()) {
            holder.username.text = post.username
        } else if (!userId.isNullOrEmpty()) {
            val cached = usernameCache[userId]
            if (cached != null) {
                holder.username.text = cached
            } else {
                val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
                userRef.child("username").get().addOnSuccessListener {
                    val username = it.getValue(String::class.java) ?: "Unknown"
                    holder.username.text = username
                    usernameCache[userId] = username
                }.addOnFailureListener {
                    holder.username.text = "Unknown"
                }
            }
        } else {
            holder.username.text = "Unknown"
        }

        // Username click: open user profile
        holder.username.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, UserProfileActivity::class.java)
            intent.putExtra("USER_ID", post.userId)
            context.startActivity(intent)
        }

        holder.time.text = post.timestamp?.let { formatTime(it) } ?: ""
        holder.content.text = post.postText ?: ""

        Glide.with(holder.itemView.context)
            .load(post.profileImageUrl)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(R.drawable.ic_profile)
            .into(holder.profileImage)

        // Show post image if available
        if (!post.imageUrl.isNullOrEmpty()) {
            holder.postImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(post.imageUrl)
                .placeholder(R.drawable.ic_home)
                .into(holder.postImage)
        } else {
            holder.postImage.visibility = View.GONE
        }

        // Show Edit icon only for posts by current user AND if showEditIcon is true
        if (post.userId == currentUserId && currentUserId.isNotBlank() && showEditIcon) {
            holder.editIcon.visibility = View.VISIBLE
            holder.editIcon.setOnClickListener { onEditClick(post) }
        } else {
            holder.editIcon.visibility = View.GONE
        }

        // Show Delete icon only for posts by current user AND if showDeleteIcon is true
        if (post.userId == currentUserId && currentUserId.isNotBlank() && showDeleteIcon) {
            holder.deleteIcon.visibility = View.VISIBLE
            holder.deleteIcon.setOnClickListener { onDeleteClick(post) }
        } else {
            holder.deleteIcon.visibility = View.GONE
        }

        // Like logic
        var likeMapSize = 0
        var isLiked = false
        val likesMap = post.likes as? Map<*, *>
        if (likesMap != null) {
            likeMapSize = likesMap.size
            isLiked = likesMap[currentUserId] == true
        }
        holder.likeCount.text = "Likes: $likeMapSize"
        holder.likeIcon.setImageResource(if (isLiked) R.drawable.ic_like_filled else R.drawable.ic_like_outline)
        holder.likeIcon.setOnClickListener {
            val postId = post.postId ?: return@setOnClickListener
            val postRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("likes")
            if (isLiked) {
                postRef.child(currentUserId).removeValue()
            } else {
                postRef.child(currentUserId).setValue(true)
            }
        }

        // Comment icon
        holder.commentIcon.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, CommentsActivity::class.java)
            intent.putExtra("POST_ID", post.postId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = postList.size

    private fun formatTime(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}