package com.ayesha.guidechat.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "intern",
    val profileImage: String = "",
    val isOnline: Boolean = false,
    val createdAt: Long = 0L
)