package com.example.foodmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
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
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_recipe_dynamic, null)

        val titleEditText = dialogView.findViewById<EditText>(R.id.etTitle)
        val difficultyEditText = dialogView.findViewById<EditText>(R.id.etDifficulty)
        val durationEditText = dialogView.findViewById<EditText>(R.id.etDuration)
        val ingredientsEditText = dialogView.findViewById<EditText>(R.id.etIngredients)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.etDescription)
        val stepsContainer = dialogView.findViewById<LinearLayout>(R.id.stepsContainer)

        // Fill in the existing data
        titleEditText.setText(recipe.title)
        difficultyEditText.setText(recipe.difficulty)
        durationEditText.setText(recipe.duration.toString())
        ingredientsEditText.setText(recipe.ingredients)
        descriptionEditText.setText(recipe.description)

        // Add steps to the container
        recipe.steps.forEachIndexed { index, step ->
            addStepField(stepsContainer, index + 1, step)
        }

        // Disable editing in view mode
        setEditMode(dialogView)

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(dialogView)
        alertDialogBuilder.setTitle("Rezeptinformationen")

        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton("Löschen") { _, _ ->
            showDeleteRecipeDialog(recipe)
        }

        alertDialogBuilder.setNeutralButton("Bearbeiten") { _, _ ->
            showModifyRecipeDialog(recipe)
        }

        alertDialogBuilder.create().show()
    }

    private fun showModifyRecipeDialog(recipe: Recipe) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_recipe_dynamic, null)

        val titleEditText = dialogView.findViewById<EditText>(R.id.etTitle)
        val difficultyEditText = dialogView.findViewById<EditText>(R.id.etDifficulty)
        val durationEditText = dialogView.findViewById<EditText>(R.id.etDuration)
        val ingredientsEditText = dialogView.findViewById<EditText>(R.id.etIngredients)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.etDescription)
        val stepsContainer = dialogView.findViewById<LinearLayout>(R.id.stepsContainer)
        val addStepButton = dialogView.findViewById<Button>(R.id.btnAddStep)

        // Fill in the existing data
        titleEditText.setText(recipe.title)
        difficultyEditText.setText(recipe.difficulty)
        durationEditText.setText(recipe.duration.toString())
        ingredientsEditText.setText(recipe.ingredients)
        descriptionEditText.setText(recipe.description)

        // Add existing steps to the container
        recipe.steps.forEachIndexed { index, step ->
            addStepField(stepsContainer, index + 1, step)
        }

        addStepButton.setOnClickListener {
            addStepField(stepsContainer, stepsContainer.childCount + 1)
        }

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(dialogView)
        alertDialogBuilder.setTitle("Rezept bearbeiten")

        alertDialogBuilder.setPositiveButton("Speichern") { _, _ ->
            if (validateRecipeFields(titleEditText, difficultyEditText, durationEditText, ingredientsEditText, descriptionEditText, stepsContainer)) {
                val updatedRecipe = recipe.copy(
                    title = titleEditText.text.toString(),
                    difficulty = difficultyEditText.text.toString(),
                    duration = durationEditText.text.toString().toIntOrNull() ?: 0,
                    ingredients = ingredientsEditText.text.toString(),
                    description = descriptionEditText.text.toString(),
                    steps = stepsContainer.children.map { (it as EditText).text.toString() }.toList() // Ensure this is List<String>
                )
                updateRecipeInDatabase(updatedRecipe)
            }
        }

        alertDialogBuilder.setNegativeButton("Abbrechen") { dialog, _ ->
            Toast.makeText(this, "Bearbeiten abgebrochen", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun validateRecipeFields(
        titleEditText: EditText,
        difficultyEditText: EditText,
        durationEditText: EditText,
        ingredientsEditText: EditText,
        descriptionEditText: EditText,
        stepsContainer: LinearLayout
    ): Boolean {
        val fields = arrayOf(titleEditText, difficultyEditText, durationEditText, ingredientsEditText, descriptionEditText)
        for (field in fields) {
            if (field.text.toString().isBlank()) {
                Toast.makeText(this, "${field.hint} darf nicht leer sein", Toast.LENGTH_SHORT).show()
                return false
            }
        }

        if (stepsContainer.childCount == 0 || (stepsContainer.getChildAt(0) as EditText).text.toString().isBlank()) {
            Toast.makeText(this, "Mindestens ein Schritt ist erforderlich", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun addStepField(stepsContainer: LinearLayout, stepNumber: Int, stepText: String = "") {
        val stepEditText = EditText(this).apply {
            hint = "Schritt $stepNumber"
            setText(stepText)
            setTextColor(resources.getColor(android.R.color.black, theme))
        }
        stepsContainer.addView(stepEditText)
    }

    private fun setEditMode(dialogView: View) {
        dialogView.findViewById<EditText>(R.id.etTitle).isEnabled = false
        dialogView.findViewById<EditText>(R.id.etDifficulty).isEnabled = false
        dialogView.findViewById<EditText>(R.id.etDuration).isEnabled = false
        dialogView.findViewById<EditText>(R.id.etIngredients).isEnabled = false
        dialogView.findViewById<EditText>(R.id.etDescription).isEnabled = false
        dialogView.findViewById<LinearLayout>(R.id.stepsContainer).children.forEach { view ->
            view.isEnabled = false
        }
        dialogView.findViewById<Button>(R.id.btnAddStep).visibility = Button.GONE
    }

    private fun updateRecipeInDatabase(recipe: Recipe) {
        lifecycleScope.launch(Dispatchers.IO) {
            recipeDao.updateRecipe(recipe)
            val updatedRecipes = recipeDao.getAllRecipes()
            withContext(Dispatchers.Main) {
                adapter.updateRecipes(updatedRecipes)
                Toast.makeText(this@RezepteActivity, "Rezept aktualisiert", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddRecipeDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_recipe_dynamic, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.etTitle)
        val difficultyEditText = dialogView.findViewById<EditText>(R.id.etDifficulty)
        val durationEditText = dialogView.findViewById<EditText>(R.id.etDuration)
        val ingredientsEditText = dialogView.findViewById<EditText>(R.id.etIngredients)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.etDescription)
        val stepsContainer = dialogView.findViewById<LinearLayout>(R.id.stepsContainer)
        val addStepButton = dialogView.findViewById<Button>(R.id.btnAddStep)

        addStepButton.setOnClickListener {
            addStepField(stepsContainer, stepsContainer.childCount + 1)
        }

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(dialogView)
        alertDialogBuilder.setTitle("Neues Rezept hinzufügen")

        alertDialogBuilder.setPositiveButton("Hinzufügen") { _, _ ->
            if (validateRecipeFields(titleEditText, difficultyEditText, durationEditText, ingredientsEditText, descriptionEditText, stepsContainer)) {
                val newRecipe = Recipe(
                    title = titleEditText.text.toString(),
                    difficulty = difficultyEditText.text.toString(),
                    duration = durationEditText.text.toString().toIntOrNull() ?: 0,
                    ingredients = ingredientsEditText.text.toString(),
                    description = descriptionEditText.text.toString(),
                    steps = stepsContainer.children.map { (it as EditText).text.toString() }.toList() // Ensure this is List<String>
                )
                addRecipeToDatabase(newRecipe)
            }
        }

        alertDialogBuilder.setNegativeButton("Abbrechen") { dialog, _ ->
            Toast.makeText(this, "Hinzufügen abgebrochen", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@RezepteActivity, "Rezept hinzugefügt", Toast.LENGTH_SHORT).show()
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

        alertDialogBuilder.create().show()
    }

    private fun deleteRecipeFromDatabase(recipe: Recipe) {
        lifecycleScope.launch(Dispatchers.IO) {
            recipeDao.deleteRecipe(recipe)
            val updatedRecipes = recipeDao.getAllRecipes()
            withContext(Dispatchers.Main) {
                adapter.updateRecipes(updatedRecipes)
                Toast.makeText(this@RezepteActivity, "Rezept gelöscht", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
