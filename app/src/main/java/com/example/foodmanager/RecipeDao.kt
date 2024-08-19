package com.example.foodmanager

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipe WHERE id = :id")
    fun getRecipeById(id: Int): Recipe?

    @Query("SELECT * FROM recipe")
    fun getAllRecipes(): List<Recipe>

    @Insert
    fun insertRecipe(recipe: Recipe)

    @Update
    fun updateRecipe(recipe: Recipe)

    @Delete
    fun deleteRecipe(recipe: Recipe)
}
