package com.example.cookcompileapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.cookcompileapp.adapter.IngredientAdapter
import com.example.cookcompileapp.adapter.InstructionAdapter
import com.example.cookcompileapp.databinding.ActivityDetailRecipeBinding
import com.example.cookcompileapp.model.Recipe
import com.example.cookcompileapp.repository.RecipeRepository

class DetailRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailRecipeBinding
    private lateinit var repository: RecipeRepository
    private var currentRecipe: Recipe? = null

    companion object {
        const val EXTRA_RECIPE_ID = "extra_recipe_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val density = resources.displayMetrics.density
        val topMarginPx = (16 * density).toInt()
        val baseBottomPadding = binding.detailContentContainer.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            binding.layoutTopActions.setPadding(
                binding.layoutTopActions.paddingStart,
                statusBars.top + topMarginPx,
                binding.layoutTopActions.paddingEnd,
                binding.layoutTopActions.paddingBottom
            )
            binding.detailContentContainer.setPadding(
                binding.detailContentContainer.paddingStart,
                binding.detailContentContainer.paddingTop,
                binding.detailContentContainer.paddingEnd,
                baseBottomPadding + navBars.bottom
            )
            insets
        }

        repository = RecipeRepository.getInstance(this)
        val recipeId = intent.getStringExtra(EXTRA_RECIPE_ID) ?: ""
        currentRecipe = repository.getRecipeById(recipeId)

        if (currentRecipe == null) {
            Toast.makeText(this, "Resep tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        populateUI(currentRecipe!!)
        setupListeners(currentRecipe!!)
    }

    private fun populateUI(recipe: Recipe) {
        binding.tvDetailTitle.text = recipe.title
        binding.tvDetailAuthor.text = "Resep oleh: ${recipe.author}"
        binding.tvDetailRating.text = String.format("%.1f (%d ulasan)", recipe.rating, recipe.reviewCount)
        binding.tvDetailCookTime.text = recipe.cookTime
        binding.tvDetailServings.text = recipe.servings
        binding.tvDetailDifficulty.text = recipe.difficulty
        binding.tvDetailCalories.text = recipe.calories
        binding.tvDetailDescription.text = recipe.description

        // Category badge
        if (recipe.type.equals("Cemilan", ignoreCase = true)) {
            binding.tvDetailCategory.text = "Cemilan"
            binding.tvDetailCategory.setBackgroundResource(R.drawable.bg_badge_cemilan)
            binding.tvDetailCategory.setTextColor(
                ContextCompat.getColor(this, R.color.badge_cemilan_text)
            )
        } else {
            binding.tvDetailCategory.text = "Masakan"
            binding.tvDetailCategory.setBackgroundResource(R.drawable.bg_badge_masakan)
            binding.tvDetailCategory.setTextColor(
                ContextCompat.getColor(this, R.color.badge_masakan_text)
            )
        }

        updateBookmarkIcon(recipe.isFavorite)

        // Hero Image
        if (recipe.imageUrl.isNotBlank()) {
            Glide.with(this)
                .load(recipe.imageUrl)
                .placeholder(R.drawable.bg_image_placeholder)
                .error(R.drawable.bg_image_placeholder)
                .centerCrop()
                .into(binding.ivDetailImage)
        }

        // Ingredients
        binding.tvIngredientsHeader.text = "Bahan-Bahan (${recipe.ingredients.size} Bahan)"
        binding.rvIngredients.adapter = IngredientAdapter(recipe.ingredients)

        // Instructions
        binding.tvInstructionsHeader.text = "Langkah-Langkah Pembuatan (${recipe.instructions.size} Langkah)"
        binding.rvInstructions.adapter = InstructionAdapter(recipe.instructions)
    }

    private fun updateBookmarkIcon(isFavorite: Boolean) {
        if (isFavorite) {
            binding.ivDetailBookmark.setImageResource(R.drawable.ic_bookmark_filled)
        } else {
            binding.ivDetailBookmark.setImageResource(R.drawable.ic_bookmark)
        }
    }

    private fun setupListeners(recipe: Recipe) {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnDetailBookmark.setOnClickListener {
            val isSaved = repository.toggleFavorite(recipe.id)
            recipe.isFavorite = isSaved
            updateBookmarkIcon(isSaved)
            val msg = if (isSaved) "Resep disimpan ke Favorit" else "Resep dihapus dari Favorit"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        binding.btnShare.setOnClickListener {
            val shareText = StringBuilder()
                .append("🍲 *${recipe.title}*\n\n")
                .append("⏱ Waktu: ${recipe.cookTime} | 🍽 Porsi: ${recipe.servings}\n")
                .append("👤 Oleh: ${recipe.author}\n\n")
                .append("📝 *Bahan-bahan:*\n")
                .append(recipe.ingredients.joinToString("\n") { "• $it" })
                .append("\n\n🔗 Lihat selengkapnya di Cookpad:\n${recipe.sourceUrl}\n\n")
                .append("Dibagikan melalui CookCompile App")
                .toString()

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(sendIntent, "Bagikan Resep"))
        }

        binding.btnOpenCookpad.setOnClickListener {
            try {
                val idNum = recipe.id.toLongOrNull() ?: 0L
                val targetUrl = if (idNum >= 30000000L && recipe.sourceUrl.contains("/resep/30000")) {
                    "https://cookpad.com/id/cari/${Uri.encode(recipe.title)}"
                } else {
                    recipe.sourceUrl
                }
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                startActivity(browserIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "Tidak dapat membuka link", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        currentRecipe?.let {
            val refreshed = repository.getRecipeById(it.id)
            if (refreshed != null) {
                currentRecipe = refreshed
                updateBookmarkIcon(refreshed.isFavorite)
            }
        }
    }
}
