package com.example.foodmanager

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val difficulty: String,
    val duration: Int,
    val ingredients: String,
    val description: String,
    val steps: List<String> // Changed from Sequence<String> to List<String>
)
