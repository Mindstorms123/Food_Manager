package com.example.foodmanager

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
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

        val Vorratsbutton: Button = findViewById(R.id.btVorrat)
        Vorratsbutton.setOnClickListener{
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
            adapter = RecipeAdapter(recipes) { recipe ->
                showRecipeInfoDialog(
                    recipe.title,
                    recipe.difficulty,
                    "${recipe.duration} Minuten",
                    ""
                )
            }
            recyclerView.adapter = adapter
        }
    }

    private suspend fun loadRecipesFromDatabase(): List<Recipe> {
        return withContext(Dispatchers.IO) {
            recipeDao.getAllRecipes()
        }
    }

    private fun showRecipeInfoDialog(title: String, difficulty: String, duration: String, message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Rezeptinformationen")

        val infoMessage = "Titel: $title\nSchwierigkeitsgrad: $difficulty\nDauer: $duration\n\n$message"

        alertDialogBuilder.setMessage(infoMessage)
        alertDialogBuilder.setPositiveButton("Mit Vorrat vergleichen") { dialog, _ ->
            // Funktionalität für Vorratsvergleich hinzufügen
        }
        alertDialogBuilder.setNegativeButton("Abbrechen") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}
