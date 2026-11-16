package com.example.cookcompileapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cookcompileapp.R
import com.example.cookcompileapp.databinding.ItemRecipeHorizontalBinding
import com.example.cookcompileapp.model.Recipe

class RecipeHorizontalAdapter(
    private var recipes: List<Recipe>,
    private val onRecipeClick: (Recipe) -> Unit,
    private val onBookmarkClick: (Recipe, Int) -> Unit
) : RecyclerView.Adapter<RecipeHorizontalAdapter.ViewHolder>() {

    fun updateData(newRecipes: List<Recipe>) {
        this.recipes = newRecipes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecipeHorizontalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size

    inner class ViewHolder(private val binding: ItemRecipeHorizontalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            binding.tvRecipeTitle.text = recipe.title
            binding.tvAuthor.text = "Oleh ${recipe.author}"
            binding.tvCookTime.text = recipe.cookTime
            binding.tvRating.text = String.format("%.1f", recipe.rating)

            // Category badge styling
            if (recipe.type.equals("Cemilan", ignoreCase = true)) {
                binding.tvCategoryBadge.text = "Cemilan"
                binding.tvCategoryBadge.setBackgroundResource(R.drawable.bg_badge_cemilan)
                binding.tvCategoryBadge.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.badge_cemilan_text)
                )
            } else {
                binding.tvCategoryBadge.text = "Masakan"
                binding.tvCategoryBadge.setBackgroundResource(R.drawable.bg_badge_masakan)
                binding.tvCategoryBadge.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.badge_masakan_text)
                )
            }

            // Bookmark icon
            if (recipe.isFavorite) {
                binding.btnBookmark.setImageResource(R.drawable.ic_bookmark_filled)
            } else {
                binding.btnBookmark.setImageResource(R.drawable.ic_bookmark)
            }

            // Load food image
            if (recipe.imageUrl.isNotBlank()) {
                Glide.with(binding.root.context)
                    .load(recipe.imageUrl)
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .centerCrop()
                    .into(binding.ivRecipeImage)
            } else {
                binding.ivRecipeImage.setImageResource(R.drawable.bg_image_placeholder)
            }

            binding.root.setOnClickListener {
                onRecipeClick(recipe)
            }

            binding.btnBookmark.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onBookmarkClick(recipe, pos)
                }
            }
        }
    }
}
