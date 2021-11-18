package com.example.isthisahangout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.isthisahangout.databinding.ActivityMainBinding
import com.example.isthisahangout.databinding.NavHeaderBinding
import com.example.isthisahangout.utils.ConnectionLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var connectionLiveData: ConnectionLiveData
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    companion object {
        var username: String? = null
        var userId: String? = null
        var userpfp: String? = null
        val userNameObv = MutableStateFlow("abc")
        val userPfpObv = MutableStateFlow("https://firebasestorage.googleapis.com/v0/b/isthisahangout-61d93.appspot.com/o/pfp%2Fpfp_placeholder.jpg?alt=media&token=35fa14c3-6451-41f6-a8be-448a59996f75")
        val userIdObv = MutableStateFlow("abc1")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this) {
            binding.bottomSheet.isVisible = !it
        }
        setContentView(binding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment2,
                R.id.chatsFragment2,
                R.id.videosFragment2,
                R.id.postsFragment2,
                R.id.songFragment,
                R.id.loginFragment
            ),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)
        binding.navView.setupWithNavController(navController)
        val headerView = binding.navView.getHeaderView(0)
        val headerBinding = NavHeaderBinding.bind(headerView)
        this.lifecycleScope.launchWhenStarted {
            userNameObv.collectLatest { name ->
                headerBinding.navHeaderUsernameTextView.text = name
            }
        }
        this.lifecycleScope.launchWhenStarted {
            userPfpObv.collectLatest { image ->
                Glide.with(applicationContext)
                    .load(image)
                    .into(headerBinding.navHeaderPfpImageview)
            }
        }
        navHostFragment.findNavController()
            .addOnDestinationChangedListener { controller, destination, arguments ->
                when (destination.id) {
                    R.id.homeFragment2 -> {
                        binding.bottomNav.isVisible = true
                    }
                    R.id.chatsFragment2 -> {
                        binding.bottomNav.isVisible = true
                    }
                    R.id.videosFragment2 -> {
                        binding.bottomNav.isVisible = true
                    }
                    R.id.postsFragment2 -> {
                        binding.bottomNav.isVisible = true
                    }
                    R.id.songFragment -> {
                        binding.bottomNav.isVisible = true
                    }
                    else -> {
                        binding.bottomNav.isVisible = false
                    }
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}