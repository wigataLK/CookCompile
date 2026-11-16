package com.example.cookcompileapp.model

import java.io.Serializable

data class InstructionStep(
    val stepNumber: Int,
    val description: String
) : Serializable

data class Recipe(
    val id: String,
    val title: String,
    val category: String,
    val type: String, // "Masakan" or "Cemilan"
    val description: String,
    val imageUrl: String,
    val author: String,
    val sourceUrl: String,
    val servings: String,
    val cookTime: String,
    val difficulty: String,
    val calories: String,
    val rating: Double,
    val reviewCount: Int,
    val isPopular: Boolean,
    var isFavorite: Boolean,
    val ingredients: List<String>,
    val instructions: List<InstructionStep>
) : Serializable
