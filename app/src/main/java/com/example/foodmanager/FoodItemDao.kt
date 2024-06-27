package com.example.foodmanager

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FoodItemDao {

    @Query("SELECT * FROM food_items")
    fun getAll(): LiveData<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE rowid = :index + 1 LIMIT 1")
    fun getNext(index: Int): FoodItem?

    @Query("SELECT * FROM food_items WHERE name = :name LIMIT 1")
    fun findByName(name: String): FoodItem?

    @Insert
    fun insert(foodItem: FoodItem)

    @Delete
    fun delete(foodItem: FoodItem)

    @Update
    fun update(foodItem: FoodItem)
}
