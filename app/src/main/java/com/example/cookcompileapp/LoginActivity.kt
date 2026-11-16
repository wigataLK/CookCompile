package com.example.cookcompileapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cookcompileapp.databinding.ActivityLoginBinding
import com.example.cookcompileapp.repository.RecipeRepository

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var repository: RecipeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(0, statusBars.top, 0, navBars.bottom)
            insets
        }

        repository = RecipeRepository.getInstance(this)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.etEmail.error = "Masukkan alamat email"
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Masukkan format email yang valid"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = "Masukkan kata sandi"
                return@setOnClickListener
            }

            // Extract clean display name from email (e.g. budi.santoso@cookcompile.id -> Budi Santoso)
            val localPart = email.substringBefore("@").replace('.', ' ').replace('_', ' ').trim()
            val name = localPart.split(Regex("\\s+"))
                .filter { it.isNotBlank() }
                .joinToString(" ") { word ->
                    word.lowercase().replaceFirstChar { it.uppercase() }
                }
                .ifBlank { "Chef" }
            repository.saveUser(name, email)

            Toast.makeText(this, "Selamat datang, $name!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.btnSkipLogin.setOnClickListener {
            repository.saveUser("Medwin", "medwin@cookcompile.id")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
