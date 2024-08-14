package com.example.foodmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RezepteActivity : AppCompatActivity() {

    private lateinit var recipeDao: RecipeDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rezepte)

        // Set status and navigation bar colors
        window.statusBarColor = resources.getColor(android.R.color.white, theme)
        window.navigationBarColor = resources.getColor(android.R.color.white, theme)

        // Find toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val vorratsButton: Button = findViewById(R.id.btVorrat)
        vorratsButton.setOnClickListener {
            finish()
        }

        // Initialize the database and DAO
        val db = RecipeDatabase.getDatabase(this)
        recipeDao = db.recipeDao()

        // RecyclerView setup
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val recipes = loadRecipesFromDatabase()
            adapter = RecipeAdapter(recipes, ::showRecipeInfoDialog, ::showDeleteRecipeDialog)
            recyclerView.adapter = adapter
        }

        // Add button setup
        val addButton: Button = findViewById(R.id.btadd)
        addButton.setOnClickListener {
            showAddRecipeDialog()
        }
    }

    private suspend fun loadRecipesFromDatabase(): List<Recipe> {
        return withContext(Dispatchers.IO) {
            recipeDao.getAllRecipes()
        }
    }

    private fun showRecipeInfoDialog(recipe: Recipe) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Rezeptinformationen")

        val infoMessage = """
            Titel: ${recipe.title}
            Schwierigkeitsgrad: ${recipe.difficulty}
            Dauer: ${recipe.duration} Minuten
            Zutaten: ${recipe.ingredients}
            Beschreibung: ${recipe.description}
            Schritte: ${recipe.steps}
        """.trimIndent()

        alertDialogBuilder.setMessage(infoMessage)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showAddRecipeDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_recipe, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.etTitle)
        val difficultyEditText = dialogView.findViewById<EditText>(R.id.etDifficulty)
        val durationEditText = dialogView.findViewById<EditText>(R.id.etDuration)
        val ingredientsEditText = dialogView.findViewById<EditText>(R.id.etIngredients)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.etDescription)
        val stepsEditText = dialogView.findViewById<EditText>(R.id.etSteps)

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(dialogView)
        alertDialogBuilder.setTitle("Neues Rezept hinzufügen")

        alertDialogBuilder.setPositiveButton("Hinzufügen") { _, _ ->
            val title = titleEditText.text.toString()
            val difficulty = difficultyEditText.text.toString()
            val duration = durationEditText.text.toString().toIntOrNull() ?: 0
            val ingredients = ingredientsEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val steps = stepsEditText.text.toString()

            if (title.isNotBlank() && difficulty.isNotBlank() && duration > 0) {
                val newRecipe = Recipe(
                    title = title,
                    difficulty = difficulty,
                    duration = duration,
                    ingredients = ingredients,
                    description = description,
                    steps = steps
                )
                addRecipeToDatabase(newRecipe)
            }
        }

        alertDialogBuilder.setNegativeButton("Abbrechen") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun addRecipeToDatabase(recipe: Recipe) {
        lifecycleScope.launch(Dispatchers.IO) {
            recipeDao.insertRecipe(recipe)
            val updatedRecipes = recipeDao.getAllRecipes()
            withContext(Dispatchers.Main) {
                adapter.updateRecipes(updatedRecipes)
            }
        }
    }

    private fun showDeleteRecipeDialog(recipe: Recipe) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Rezept löschen")
        alertDialogBuilder.setMessage("Möchten Sie dieses Rezept wirklich löschen?")

        alertDialogBuilder.setPositiveButton("Löschen") { _, _ ->
            deleteRecipeFromDatabase(recipe)
        }

        alertDialogBuilder.setNegativeButton("Abbrechen") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun deleteRecipeFromDatabase(recipe: Recipe) {
        lifecycleScope.launch(Dispatchers.IO) {
            recipeDao.deleteRecipe(recipe)
            val updatedRecipes = recipeDao.getAllRecipes()
            withContext(Dispatchers.Main) {
                adapter.updateRecipes(updatedRecipes)
            }
        }
    }
}
