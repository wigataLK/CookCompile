package com.example.cookcompileapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cookcompileapp.adapter.RecipeVerticalAdapter
import com.example.cookcompileapp.databinding.FragmentSavedBinding
import com.example.cookcompileapp.model.Recipe
import com.example.cookcompileapp.repository.RecipeRepository

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RecipeRepository
    private lateinit var savedAdapter: RecipeVerticalAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = RecipeRepository.getInstance(requireContext())

        setupAdapter()
        setupListeners()
        loadData()
    }

    private fun setupAdapter() {
        savedAdapter = RecipeVerticalAdapter(
            recipes = emptyList(),
            onRecipeClick = { recipe -> openDetail(recipe) },
            onBookmarkClick = { recipe, _ ->
                repository.toggleFavorite(recipe.id)
                Toast.makeText(requireContext(), "Batal simpan ${recipe.title}", Toast.LENGTH_SHORT).show()
                loadData()
            }
        )
        binding.rvSavedRecipes.adapter = savedAdapter
    }

    private fun setupListeners() {
        binding.btnDeleteAll.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Hapus Semua Favorit")
                .setMessage("Apakah Anda yakin ingin menghapus semua resep dari daftar tersimpan?")
                .setPositiveButton("Hapus") { _, _ ->
                    repository.clearAllFavorites()
                    Toast.makeText(requireContext(), "Semua resep tersimpan telah dihapus", Toast.LENGTH_SHORT).show()
                    loadData()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        binding.btnAddMore.setOnClickListener {
            (activity as? MainActivity)?.selectNavigationTab(R.id.menu_home)
        }

        binding.btnExploreHome.setOnClickListener {
            (activity as? MainActivity)?.selectNavigationTab(R.id.menu_home)
        }

        binding.btnRestoreInitial.setOnClickListener {
            repository.restoreInitialFavorites()
            Toast.makeText(requireContext(), "5 resep favorit awal berhasil ditampilkan kembali!", Toast.LENGTH_SHORT).show()
            loadData()
        }
    }

    fun loadData() {
        if (_binding == null) return

        val savedList = repository.getSavedRecipes()
        savedAdapter.updateData(savedList)

        val count = savedList.size
        binding.tvSavedSubtitle.text = "$count favorit tersimpan • Klik ikon hijau untuk batal simpan:"

        if (count == 0) {
            binding.layoutSavedContent.visibility = View.GONE
            binding.btnDeleteAll.visibility = View.GONE
            binding.tvSavedSubtitle.visibility = View.GONE
            binding.layoutEmptySaved.visibility = View.VISIBLE
        } else {
            binding.layoutSavedContent.visibility = View.VISIBLE
            binding.btnDeleteAll.visibility = View.VISIBLE
            binding.tvSavedSubtitle.visibility = View.VISIBLE
            binding.layoutEmptySaved.visibility = View.GONE
        }
    }

    private fun openDetail(recipe: Recipe) {
        val intent = Intent(requireContext(), DetailRecipeActivity::class.java).apply {
            putExtra(DetailRecipeActivity.EXTRA_RECIPE_ID, recipe.id)
        }
        startActivity(intent)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && _binding != null) {
            loadData()
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
