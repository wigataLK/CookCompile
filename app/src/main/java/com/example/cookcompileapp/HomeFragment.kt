package com.example.cookcompileapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.example.cookcompileapp.adapter.RecipeHorizontalAdapter
import com.example.cookcompileapp.adapter.RecipeVerticalAdapter
import com.example.cookcompileapp.databinding.FragmentHomeBinding
import com.example.cookcompileapp.model.Recipe
import com.example.cookcompileapp.repository.RecipeRepository
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RecipeRepository
    private lateinit var popularAdapter: RecipeHorizontalAdapter
    private lateinit var verticalAdapter: RecipeVerticalAdapter

    private var currentFilter = "Semua"
    private var currentQuery = ""
    private val numberFormat = NumberFormat.getInstance(Locale.forLanguageTag("id-ID"))

    // 15-item chunk pagination controls
    private val CHUNK_SIZE = 15
    private var verticalLimit = CHUNK_SIZE
    private var popularOffset = 0
    private var isAutoLoadingVertical = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = RecipeRepository.getInstance(requireContext())

        setupUserHeader()
        setupAdapters()
        setupChips()
        setupSearch()
        setupSwipeRefresh()
        setupPaginationListeners()
        loadData()
    }

    private fun setupUserHeader() {
        val user = repository.getCurrentUser()
        binding.tvGreeting.text = "Halo, ${user.name}!"

        binding.ivUserAvatar.setOnClickListener {
            (activity as? MainActivity)?.selectNavigationTab(R.id.menu_profile)
        }
    }

    private fun setupAdapters() {
        popularAdapter = RecipeHorizontalAdapter(
            recipes = emptyList(),
            onRecipeClick = { recipe -> openDetail(recipe) },
            onBookmarkClick = { recipe, _ -> toggleBookmark(recipe) }
        )
        binding.rvPopular.adapter = popularAdapter

        verticalAdapter = RecipeVerticalAdapter(
            recipes = emptyList(),
            onRecipeClick = { recipe -> openDetail(recipe) },
            onBookmarkClick = { recipe, _ -> toggleBookmark(recipe) }
        )
        binding.rvAllRecipes.adapter = verticalAdapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary, R.color.primary_dark)
        binding.swipeRefresh.setOnRefreshListener {
            // Reset pagination to first 15 items on refresh
            verticalLimit = CHUNK_SIZE
            val popSize = repository.getPopularRecipes().size
            if (popSize > 0) {
                popularOffset = (popularOffset + CHUNK_SIZE) % popSize
            }

            repository.checkForUpdates { _, _ ->
                if (_binding != null && isAdded) {
                    loadData()
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(requireContext(), "Katalog disegarkan (15 resep per halaman)", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupPaginationListeners() {
        // Vertical Endless Scroll: detect when near bottom of NestedScrollView
        binding.nestedScrollView.setOnScrollChangeListener { v: NestedScrollView, _, scrollY, _, _ ->
            val child = v.getChildAt(0)
            if (child != null) {
                val diff = child.measuredHeight - v.measuredHeight - scrollY
                if (diff <= 400 && !isAutoLoadingVertical) {
                    loadNext15VerticalRecipes()
                }
            }
        }

        // Manual Load More Button
        binding.btnLoadMore.setOnClickListener {
            loadNext15VerticalRecipes()
        }

        // Horizontal Next 15 Hits button
        binding.btnRefreshPopular.setOnClickListener {
            loadNext15PopularRecipes()
        }
    }

    private fun loadNext15VerticalRecipes() {
        val totalAvailable = repository.searchRecipes(currentQuery, currentFilter).size
        if (verticalLimit < totalAvailable) {
            isAutoLoadingVertical = true
            binding.progressBarLoadMore.visibility = View.VISIBLE
            binding.btnLoadMore.isEnabled = false

            // Increase limit by 15
            verticalLimit += CHUNK_SIZE
            loadData()

            binding.progressBarLoadMore.postDelayed({
                if (_binding != null) {
                    binding.progressBarLoadMore.visibility = View.GONE
                    binding.btnLoadMore.isEnabled = true
                    isAutoLoadingVertical = false
                }
            }, 250)
        }
    }

    private fun loadNext15PopularRecipes() {
        val allPopular = repository.getPopularRecipes()
        if (allPopular.isEmpty()) return

        popularOffset = (popularOffset + CHUNK_SIZE)
        if (popularOffset >= allPopular.size) {
            popularOffset = 0
        }

        val next15 = allPopular.drop(popularOffset).take(CHUNK_SIZE)
        if (next15.isNotEmpty()) {
            popularAdapter.updateData(next15)
            binding.rvPopular.scrollToPosition(0)
            Toast.makeText(requireContext(), "Menampilkan 15 resep hits lainnya →", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupChips() {
        updateChipNumbers()

        binding.chipSemua.setOnClickListener {
            applyCategoryFilter("Semua")
        }
        binding.chipMasakan.setOnClickListener {
            applyCategoryFilter("Masakan")
        }
        binding.chipCemilan.setOnClickListener {
            applyCategoryFilter("Cemilan")
        }
    }

    private fun updateChipNumbers() {
        val allCount = numberFormat.format(repository.getAllRecipes().size)
        val masakanCount = numberFormat.format(repository.getRecipesByType("Masakan").size)
        val cemilanCount = numberFormat.format(repository.getRecipesByType("Cemilan").size)

        binding.chipSemua.text = "Semua ($allCount)"
        binding.chipMasakan.text = "Masakan ($masakanCount)"
        binding.chipCemilan.text = "Cemilan ($cemilanCount)"
    }

    private fun applyCategoryFilter(type: String) {
        currentFilter = type
        verticalLimit = CHUNK_SIZE // Reset to 15 on filter change for instant loading
        updateChipStyles()
        loadData()
    }

    private fun updateChipStyles() {
        val ctx = requireContext()

        fun setChipActive(chip: TextView, isActive: Boolean) {
            if (isActive) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected)
                chip.setTextColor(ContextCompat.getColor(ctx, R.color.white))
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected)
                chip.setTextColor(ContextCompat.getColor(ctx, R.color.text_secondary))
            }
        }

        setChipActive(binding.chipSemua, currentFilter == "Semua")
        setChipActive(binding.chipMasakan, currentFilter == "Masakan")
        setChipActive(binding.chipCemilan, currentFilter == "Cemilan")
    }

    private fun setupSearch() {
        binding.etSearch.hint = "Cari dari 1.000+ masakan & cemilan..."
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString()?.trim() ?: ""
                verticalLimit = CHUNK_SIZE // Reset to 15 on search
                binding.btnClearSearch.visibility = if (currentQuery.isNotEmpty()) View.VISIBLE else View.GONE
                loadData()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }
    }

    fun loadData() {
        if (_binding == null) return

        updateChipNumbers()

        // 1. Horizontal: Hide popular carousel during search so results appear right away
        if (currentQuery.isNotEmpty()) {
            binding.sectionPopularContainer.visibility = View.GONE
        } else {
            binding.sectionPopularContainer.visibility = View.VISIBLE
            val allPopular = repository.getPopularRecipes()
            val chunkPopular = allPopular.drop(popularOffset).take(CHUNK_SIZE)
            popularAdapter.updateData(chunkPopular.ifEmpty { allPopular.take(CHUNK_SIZE) })
        }

        // 2. Vertical: Load active chunk of 15 items
        val allFiltered = repository.searchRecipes(currentQuery, currentFilter)
        val totalCount = allFiltered.size
        val pagedList = allFiltered.take(verticalLimit)
        verticalAdapter.updateData(pagedList)

        val totalCountStr = numberFormat.format(totalCount)
        val currentLoadedStr = numberFormat.format(pagedList.size)

        // Section Title
        when (currentFilter) {
            "Masakan" -> {
                binding.tvSectionMoreTitle.text = "Koleksi Masakan Utama ($totalCountStr Menu)"
            }
            "Cemilan" -> {
                binding.tvSectionMoreTitle.text = "Koleksi Resep Cemilan ($totalCountStr Menu)"
            }
            else -> {
                binding.tvSectionMoreTitle.text = "Masakan & Cemilan Lainnya ($totalCountStr Menu)"
            }
        }

        // Pagination Footer status
        if (pagedList.size < totalCount) {
            binding.layoutLoadMore.visibility = View.VISIBLE
            binding.btnLoadMore.visibility = View.VISIBLE
            binding.btnLoadMore.text = "+ Muat 15 Resep Berikutnya"
            binding.tvPaginationStatus.text = "Menampilkan $currentLoadedStr dari $totalCountStr resep (Scroll ↓ untuk memuat)"
        } else if (totalCount > 0) {
            binding.layoutLoadMore.visibility = View.VISIBLE
            binding.btnLoadMore.visibility = View.GONE
            binding.tvPaginationStatus.text = "Semua $totalCountStr resep telah dimuat"
        } else {
            binding.layoutLoadMore.visibility = View.GONE
        }

        // Toggle visibility if search yields empty
        if (totalCount == 0 && currentQuery.isNotEmpty()) {
            binding.sectionMoreContainer.visibility = View.GONE
            binding.layoutEmptySearch.visibility = View.VISIBLE
        } else {
            binding.sectionMoreContainer.visibility = View.VISIBLE
            binding.layoutEmptySearch.visibility = View.GONE
        }
    }

    private fun toggleBookmark(recipe: Recipe) {
        val isSaved = repository.toggleFavorite(recipe.id)
        val msg = if (isSaved) "Resep disimpan ke Favorit" else "Resep dihapus dari Favorit"
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        loadData()
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
            setupUserHeader()
            loadData()
        }
    }

    override fun onResume() {
        super.onResume()
        setupUserHeader()
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
