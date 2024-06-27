package com.example.foodmanager

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    var quantity: Int = 0,
    var amountInGrams: Int = 0
)
