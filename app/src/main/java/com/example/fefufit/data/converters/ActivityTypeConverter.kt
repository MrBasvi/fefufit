package com.example.fefufit.data.converters

import androidx.room.TypeConverter
import com.example.fefufit.model.ActivityType

class ActivityTypeConverter {
    @TypeConverter
    fun fromActivityType(activityType: ActivityType): String {
        return activityType.name
    }

    @TypeConverter
    fun toActivityType(value: String): ActivityType {
        return ActivityType.valueOf(value)
    }
} 