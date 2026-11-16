package com.example.cookcompileapp

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.cookcompileapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var homeFragment: HomeFragment
    private lateinit var savedFragment: SavedFragment
    private lateinit var profileFragment: ProfileFragment

    private lateinit var activeFragment: Fragment

    companion object {
        private const val TAG_HOME = "HOME"
        private const val TAG_SAVED = "SAVED"
        private const val TAG_PROFILE = "PROFILE"
        private const val KEY_ACTIVE_TAG = "active_fragment_tag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle both status bar (jam hp, baterai, internet) and navigation bar (gesture pill)
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainLayout) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            // Push content below the status bar so headers are never covered
            binding.fragmentContainer.setPadding(0, statusBars.top, 0, 0)
            // Push bottom navigation above the system gesture bar
            binding.bottomNavigation.setPadding(0, 0, 0, navBars.bottom)

            insets
        }

        setupFragments(savedInstanceState)
        setupBottomNav()
        setupBackNavigation()
    }

    private fun setupFragments(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            homeFragment = HomeFragment()
            savedFragment = SavedFragment()
            profileFragment = ProfileFragment()

            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, profileFragment, TAG_PROFILE).hide(profileFragment)
                .add(R.id.fragmentContainer, savedFragment, TAG_SAVED).hide(savedFragment)
                .add(R.id.fragmentContainer, homeFragment, TAG_HOME)
                .commit()
            activeFragment = homeFragment
        } else {
            homeFragment = (supportFragmentManager.findFragmentByTag(TAG_HOME) as? HomeFragment)
                ?: HomeFragment()
            savedFragment = (supportFragmentManager.findFragmentByTag(TAG_SAVED) as? SavedFragment)
                ?: SavedFragment()
            profileFragment = (supportFragmentManager.findFragmentByTag(TAG_PROFILE) as? ProfileFragment)
                ?: ProfileFragment()

            val activeTag = savedInstanceState.getString(KEY_ACTIVE_TAG, TAG_HOME)
            activeFragment = when (activeTag) {
                TAG_SAVED -> savedFragment
                TAG_PROFILE -> profileFragment
                else -> homeFragment
            }

            val tx = supportFragmentManager.beginTransaction()
            listOf(homeFragment to TAG_HOME, savedFragment to TAG_SAVED, profileFragment to TAG_PROFILE).forEach { (frag, tag) ->
                if (!frag.isAdded) {
                    tx.add(R.id.fragmentContainer, frag, tag)
                }
                if (frag == activeFragment) {
                    tx.show(frag)
                } else {
                    tx.hide(frag)
                }
            }
            tx.commit()
        }
    }

    private fun setupBottomNav() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    switchFragment(homeFragment)
                    true
                }
                R.id.menu_saved -> {
                    switchFragment(savedFragment)
                    true
                }
                R.id.menu_profile -> {
                    switchFragment(profileFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (activeFragment != homeFragment) {
                    selectNavigationTab(R.id.menu_home)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun switchFragment(target: Fragment) {
        if (activeFragment != target) {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit()
            activeFragment = target
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val activeTag = when (activeFragment) {
            savedFragment -> TAG_SAVED
            profileFragment -> TAG_PROFILE
            else -> TAG_HOME
        }
        outState.putString(KEY_ACTIVE_TAG, activeTag)
    }

    fun selectNavigationTab(menuId: Int) {
        binding.bottomNavigation.selectedItemId = menuId
    }
}
