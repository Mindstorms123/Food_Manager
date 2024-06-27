package com.example.foodmanager

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface FoodItemDao {

    @Query("SELECT * FROM food_items")
    fun getAll(): LiveData<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE rowid = :index + 1 LIMIT 1")
    fun getNext(index: Int): FoodItem?

    @Query("SELECT * FROM food_items LIMIT 1")
    fun getFirst(): FoodItem?

    @Query("SELECT * FROM food_items WHERE name = :name LIMIT 1")
    fun findByName(name: String): FoodItem?

    // Weitere DAO-Funktionen für Insert, Delete, Update
    @Insert
    fun insert(foodItem: FoodItem)

    @Delete
    fun delete(foodItem: FoodItem)

    @Update
    fun update(foodItem: FoodItem)
}
