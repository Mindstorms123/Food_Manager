package com.example.foodmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecipeAdapter(
    private val recipes: List<Recipe>,
    private val onRecipeClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount() = recipes.size

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.recipeTitle)
        private val difficultyTextView: TextView = itemView.findViewById(R.id.recipeDifficulty)
        private val durationTextView: TextView = itemView.findViewById(R.id.recipeDuration)

        fun bind(recipe: Recipe) {
            titleTextView.text = recipe.title
            difficultyTextView.text = recipe.difficulty
            durationTextView.text = "${recipe.duration} Minuten"

            itemView.setOnClickListener { onRecipeClick(recipe) }
        }
    }
}
