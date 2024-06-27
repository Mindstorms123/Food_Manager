package com.example.foodmanager

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: Int,
    val title: String,
    val difficulty: String,
    val duration: Int // in minutes
)