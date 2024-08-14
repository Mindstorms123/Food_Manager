package com.example.foodmanager

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val difficulty: String,
    val duration: Int, // in minutes
    val ingredients: String,
    val description: String,
    val steps: String
)
