package com.example.cookcompileapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cookcompileapp.databinding.FragmentProfileBinding
import com.example.cookcompileapp.repository.RecipeRepository
import java.text.NumberFormat
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RecipeRepository
    private val numberFormat = NumberFormat.getInstance(Locale.forLanguageTag("id-ID"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = RecipeRepository.getInstance(requireContext())

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        val user = repository.getCurrentUser()
        binding.tvProfileName.text = user.name
        binding.tvProfileRole.text = user.role

        val totalRecipes = numberFormat.format(repository.getAllRecipes().size)
        val savedCount = numberFormat.format(repository.getSavedCount())

        binding.tvStatTotalRecipes.text = "$totalRecipes Resep & Cemilan"
        binding.tvStatSavedRecipes.text = "$savedCount Tersimpan"
        binding.tvMenuSavedCount.text = "Daftar Resep Tersimpan ($savedCount)"
    }

    private fun setupListeners() {
        binding.cardNavSaved.setOnClickListener {
            (activity as? MainActivity)?.selectNavigationTab(R.id.menu_saved)
        }

        binding.cardUpdateRecipes.setOnClickListener {
            binding.tvSyncSubtitle.text = "Sedang memeriksa resep baru..."
            repository.checkForUpdates { _, message ->
                if (_binding != null && isAdded) {
                    setupUI()
                    binding.tvSyncSubtitle.text = "Terakhir diperiksa baru saja"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.cardAbout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Tentang CookCompile")
                .setMessage(
                    "CookCompile adalah aplikasi katalog masakan dan cemilan rumahan terlengkap.\n\n" +
                    "Aplikasi ini menyajikan lebih dari 1.000 resep kuliner nusantara yang dikurasi langsung dari Cookpad Indonesia " +
                    "(Ayam, Daging, Seafood, Tahu & Tempe, Nasi, Mie, Sayur, Camilan, Aneka Sambal, hingga Minuman Segar) " +
                    "lengkap dengan takaran bahan, langkah-langkah, porsi, waktu masak, dan tingkat kesulitan.\n\n" +
                    "Fitur Update Otomatis memungkinkan resep baru langsung masuk ke aplikasi secara instan tanpa perlu build ulang APK."
                )
                .setPositiveButton("Tutup", null)
                .show()
        }

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Keluar Akun")
                .setMessage("Apakah Anda yakin ingin keluar dari akun CookCompile?")
                .setPositiveButton("Keluar") { _, _ ->
                    repository.logout()
                    Toast.makeText(requireContext(), "Berhasil keluar akun", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    activity?.finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && _binding != null) {
            setupUI()
        }
    }

    override fun onResume() {
        super.onResume()
        setupUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
