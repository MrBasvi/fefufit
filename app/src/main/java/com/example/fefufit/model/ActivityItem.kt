package com.example.fefufit.model

sealed class ActivityItem {
    data class Activity(
        val id: String,
        val distance: String,
        val duration: String,
        val type: String,
        val timeAgo: String,
        val startTime: String,
        val endTime: String,
        val isMyActivity: Boolean
    ) : ActivityItem()
}
