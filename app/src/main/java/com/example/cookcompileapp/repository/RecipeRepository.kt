package com.example.cookcompileapp.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.cookcompileapp.model.InstructionStep
import com.example.cookcompileapp.model.Recipe
import com.example.cookcompileapp.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class RecipeRepository private constructor(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("cookcompile_prefs", Context.MODE_PRIVATE)

    private val allRecipes = mutableListOf<Recipe>()
    private var isInitialized = false

    private val cacheFile: File
        get() = File(context.filesDir, "recipes_cache.json")

    init {
        loadRecipes()
    }

    companion object {
        @Volatile
        private var INSTANCE: RecipeRepository? = null

        fun getInstance(context: Context): RecipeRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RecipeRepository(context.applicationContext).also { INSTANCE = it }
            }
        }

        private const val KEY_SAVED_IDS = "saved_recipe_ids"
        private const val KEY_INITIAL_SAVED_SET = "initial_saved_set"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_CATALOG_VERSION = "catalog_version"
        private const val CURRENT_CATALOG_VERSION = 2

        // Online repository URL for OTA recipe catalog updates
        private const val REMOTE_RECIPES_URL =
            "https://raw.githubusercontent.com/wigataLK/CookCompile/main/app/src/main/assets/recipes.json"
    }

    private fun loadRecipes() {
        if (isInitialized) return
        allRecipes.clear()

        try {
            if (prefs.getInt(KEY_CATALOG_VERSION, 0) < CURRENT_CATALOG_VERSION) {
                if (cacheFile.exists()) {
                    cacheFile.delete()
                }
                prefs.edit().putInt(KEY_CATALOG_VERSION, CURRENT_CATALOG_VERSION).apply()
            }

            val jsonString = if (cacheFile.exists() && cacheFile.length() > 0) {
                cacheFile.readText(Charsets.UTF_8)
            } else {
                val inputStream = context.assets.open("recipes.json")
                val content = inputStream.bufferedReader().use { it.readText() }
                // Initialize cache file
                try {
                    cacheFile.writeText(content, Charsets.UTF_8)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                content
            }

            allRecipes.addAll(parseJsonToRecipes(jsonString))

            // Check if initial favorites already set in prefs
            if (!prefs.getBoolean(KEY_INITIAL_SAVED_SET, false)) {
                // Initial 5 favorites matching Figma AS 05
                val initialSaved = HashSet(allRecipes.take(5).map { it.id })
                prefs.edit()
                    .putStringSet(KEY_SAVED_IDS, initialSaved)
                    .putBoolean(KEY_INITIAL_SAVED_SET, true)
                    .apply()
            }

            syncFavoriteState()
            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseJsonToRecipes(jsonString: String): List<Recipe> {
        val jsonArray = JSONArray(jsonString)
        val loaded = mutableListOf<Recipe>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val id = obj.optString("id", i.toString())
            val title = obj.optString("title", "")
            val category = obj.optString("category", "Lainnya")
            val type = obj.optString("type", if (category.contains("Camilan", true) || category.contains("Minuman", true)) "Cemilan" else "Masakan")
            val description = obj.optString("description", "")
            val imageUrl = obj.optString("imageUrl", "")
            val author = obj.optString("author", "Cookpad Chef")
            val sourceUrl = obj.optString("sourceUrl", "https://cookpad.com/id")
            val servings = obj.optString("servings", "2-3 Porsi")
            val cookTime = obj.optString("cookTime", "30 Menit")
            val difficulty = obj.optString("difficulty", "Mudah")
            val calories = obj.optString("calories", "350 kkal")
            val rating = obj.optDouble("rating", 4.8)
            val reviewCount = obj.optInt("reviewCount", 15)
            val isPopular = obj.optBoolean("isPopular", i < 10 || rating >= 4.8)

            val ingredientsList = mutableListOf<String>()
            val ingrArray = obj.optJSONArray("ingredients")
            if (ingrArray != null) {
                for (j in 0 until ingrArray.length()) {
                    ingredientsList.add(ingrArray.getString(j))
                }
            }

            val instructionsList = mutableListOf<InstructionStep>()
            val instArray = obj.optJSONArray("instructions")
            if (instArray != null) {
                for (j in 0 until instArray.length()) {
                    val stepObj = instArray.getJSONObject(j)
                    instructionsList.add(
                        InstructionStep(
                            stepNumber = stepObj.optInt("stepNumber", j + 1),
                            description = stepObj.optString("description", "")
                        )
                    )
                }
            }

            loaded.add(
                Recipe(
                    id = id,
                    title = title,
                    category = category,
                    type = type,
                    description = description,
                    imageUrl = imageUrl,
                    author = author,
                    sourceUrl = sourceUrl,
                    servings = servings,
                    cookTime = cookTime,
                    difficulty = difficulty,
                    calories = calories,
                    rating = rating,
                    reviewCount = reviewCount,
                    isPopular = isPopular,
                    isFavorite = false,
                    ingredients = ingredientsList,
                    instructions = instructionsList
                )
            )
        }

        return RecipeCatalogGenerator.ensureFullCatalog(loaded, 1050)
    }

    private fun syncFavoriteState() {
        val savedIds = getSavedRecipeIds()
        for (recipe in allRecipes) {
            recipe.isFavorite = savedIds.contains(recipe.id)
        }
    }

    fun getAllRecipes(): List<Recipe> {
        syncFavoriteState()
        return allRecipes.toList()
    }

    fun getPopularRecipes(): List<Recipe> {
        syncFavoriteState()
        return allRecipes.filter { it.isPopular || it.rating >= 4.8 }
    }

    fun getSavedRecipes(): List<Recipe> {
        syncFavoriteState()
        val savedIds = getSavedRecipeIds()
        return allRecipes.filter { savedIds.contains(it.id) }
    }

    fun getSavedCount(): Int {
        return getSavedRecipeIds().size
    }

    fun getRecipesByType(type: String): List<Recipe> {
        syncFavoriteState()
        return if (type.equals("Semua", ignoreCase = true)) {
            allRecipes
        } else {
            allRecipes.filter { it.type.equals(type, ignoreCase = true) }
        }
    }

    fun searchRecipes(query: String, filterType: String = "Semua"): List<Recipe> {
        syncFavoriteState()
        val baseList = getRecipesByType(filterType)
        if (query.isBlank()) return baseList

        val trimmed = query.trim().lowercase()
        return baseList.filter {
            it.title.lowercase().contains(trimmed) ||
            it.category.lowercase().contains(trimmed) ||
            it.author.lowercase().contains(trimmed) ||
            it.ingredients.any { ing -> ing.lowercase().contains(trimmed) }
        }
    }

    fun getRecipeById(id: String): Recipe? {
        syncFavoriteState()
        return allRecipes.find { it.id == id }
    }

    fun toggleFavorite(recipeId: String): Boolean {
        val currentSaved = getSavedRecipeIds().toMutableSet()
        val isNowSaved = if (currentSaved.contains(recipeId)) {
            currentSaved.remove(recipeId)
            false
        } else {
            currentSaved.add(recipeId)
            true
        }
        saveFavoriteIds(currentSaved)
        syncFavoriteState()
        return isNowSaved
    }

    fun clearAllFavorites() {
        saveFavoriteIds(emptySet())
        syncFavoriteState()
    }

    fun restoreInitialFavorites() {
        val initialSaved = allRecipes.take(5).map { it.id }.toSet()
        saveFavoriteIds(initialSaved)
        syncFavoriteState()
    }

    private fun saveFavoriteIds(ids: Set<String>) {
        prefs.edit().remove(KEY_SAVED_IDS).apply()
        prefs.edit().putStringSet(KEY_SAVED_IDS, HashSet(ids)).apply()
    }

    private fun getSavedRecipeIds(): Set<String> {
        return HashSet(prefs.getStringSet(KEY_SAVED_IDS, emptySet()) ?: emptySet())
    }

    // Dynamic Live Update & Online OTA Sync
    fun checkForUpdates(onComplete: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Try remote sync if network is available
                var updated = false
                var remoteRecipes: List<Recipe>? = null
                try {
                    val url = URL(REMOTE_RECIPES_URL)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 4000
                    conn.readTimeout = 4000
                    conn.requestMethod = "GET"

                    if (conn.responseCode == 200) {
                        val remoteJson = conn.inputStream.bufferedReader().use { it.readText() }
                        val isValidCatalog = remoteJson.isNotBlank() &&
                            remoteJson.trimStart().startsWith("[") &&
                            !remoteJson.contains("sajikan selagi hangat bersama nasi putih")
                        if (isValidCatalog) {
                            val parsed = parseJsonToRecipes(remoteJson)
                            if (parsed.isNotEmpty()) {
                                cacheFile.writeText(remoteJson, Charsets.UTF_8)
                                remoteRecipes = parsed
                                updated = true
                            }
                        }
                    }
                    conn.disconnect()
                } catch (_: Exception) {
                    // Remote server offline or no network, fallback to refreshed cache
                }

                prefs.edit().putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis()).apply()

                withContext(Dispatchers.Main) {
                    if (remoteRecipes != null) {
                        allRecipes.clear()
                        allRecipes.addAll(remoteRecipes)
                        syncFavoriteState()
                    }
                    val count = allRecipes.size
                    val msg = if (updated) {
                        "Katalog resep berhasil diperbarui dari cloud! Total $count resep."
                    } else {
                        "Katalog resep sudah versi terbaru ($count resep aktif)."
                    }
                    onComplete(true, msg)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "Gagal memperbarui: ${e.message}")
                }
            }
        }
    }

    // User authentication / profile data
    fun saveUser(name: String, email: String) {
        prefs.edit()
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getCurrentUser(): User {
        val name = prefs.getString(KEY_USER_NAME, "Medwin") ?: "Medwin"
        val email = prefs.getString(KEY_USER_EMAIL, "medwin@cookcompile.id") ?: "medwin@cookcompile.id"
        return User(name = name, email = email)
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }
}
