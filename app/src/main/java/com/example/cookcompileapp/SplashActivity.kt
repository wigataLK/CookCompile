package com.example.cookcompileapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cookcompileapp.databinding.ActivitySplashBinding
import com.example.cookcompileapp.repository.RecipeRepository

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var repository: RecipeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val density = resources.displayMetrics.density
        val minInsetExtra = (20 * density).toInt()
        val baseStart = binding.splashContentContainer.paddingStart
        val baseEnd = binding.splashContentContainer.paddingEnd
        val baseTop = binding.splashContentContainer.paddingTop
        val baseBottom = binding.splashContentContainer.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.splashContentContainer.setPadding(
                baseStart + bars.left,
                maxOf(baseTop, bars.top + minInsetExtra),
                baseEnd + bars.right,
                maxOf(baseBottom, bars.bottom + minInsetExtra)
            )
            insets
        }

        repository = RecipeRepository.getInstance(this)

        // Directly display the exact Figma AS 01 splash image
        binding.ivSplashBg.setImageResource(R.drawable.img_splash_figma)

        binding.btnStartCooking.setOnClickListener {
            val intent = if (repository.isLoggedIn()) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
            startActivity(intent)
            finish()
        }
    }
}
