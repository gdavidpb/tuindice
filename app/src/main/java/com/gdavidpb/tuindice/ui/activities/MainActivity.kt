package com.gdavidpb.tuindice.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.errors.SyncError
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.ui.dialogs.credentialsChangedDialog
import com.gdavidpb.tuindice.ui.dialogs.disabledFailureDialog
import com.gdavidpb.tuindice.ui.dialogs.fatalFailureRestart
import com.gdavidpb.tuindice.utils.IdempotentLocker
import com.gdavidpb.tuindice.utils.TIME_EXIT_LOCKER
import com.gdavidpb.tuindice.utils.extensions.*
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val reviewManager by inject<ReviewManager>()

    private val viewModel by viewModel<MainViewModel>()

    private val backLocker = IdempotentLocker()

    /* true = show bottom nav, false = hide bottom nav */
    private val destinations = mapOf(
            R.id.fragment_summary to true,
            R.id.fragment_record to true,
            R.id.fragment_pensum to true,
            R.id.fragment_about to true,
            R.id.fragment_splash to false,
            R.id.fragment_sign_in to false,
            R.id.fragment_reset_password to false
    )

    private val appBarConfiguration by lazy {
        AppBarConfiguration(navController.graph).apply {
            topLevelDestinations.addAll(destinations.keys)
        }
    }

    private val navController by lazy {
        findNavController(R.id.mainNavHostFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        NavigationUI.setupWithNavController(bottomNavView, navController)
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration)

        bottomNavView.setOnNavigationItemReselectedListener { }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            onDestinationChanged(destination)
        }

        if (!isGoogleServicesAvailable()) return

        with(viewModel) {
            observe(sync, ::syncObserver)
            observe(requestReview, ::requestReviewObserver)
        }
    }

    override fun onBackPressed() {
        val isHomeDestination = destinations[navController.currentDestination?.id]

        if (isHomeDestination == true) {
            toast(R.string.toast_repeat_to_exit)

            val locked = backLocker.lock(UnlockIn = TIME_EXIT_LOCKER)

            if (!locked) finish()
        } else {
            super.onBackPressed()
        }
    }

    private fun onDestinationChanged(destination: NavDestination) {
        val showBottomNav = destinations[destination.id] ?: false
        val showAppBar = destination.id != R.id.fragment_splash

        bottomNavView.isVisible = showBottomNav
        appBar.isVisible = showAppBar

        if (showBottomNav) viewModel.setLastScreen(navId = destination.id)
    }

    private fun syncObserver(result: Result<Boolean, SyncError>?) {
        when (result) {
            is Result.OnLoading -> {
                pBarSync.isVisible = true
            }
            is Result.OnSuccess -> {
                pBarSync.isVisible = false

                viewModel.checkReview(reviewManager)
            }
            is Result.OnError -> {
                pBarSync.isVisible = false

                syncErrorHandler(error = result.error)
            }
        }
    }

    private fun requestReviewObserver(result: Result<ReviewInfo, Nothing>?) {
        when (result) {
            is Result.OnSuccess -> {
                val reviewInfo = result.value

                lifecycleScope.launchWhenResumed {
                    reviewManager.launchReview(this@MainActivity, reviewInfo)
                }
            }
        }
    }

    private fun syncErrorHandler(error: SyncError?) {
        when (error) {
            is SyncError.Unauthenticated -> fatalFailureRestart()
            is SyncError.AccountDisabled -> disabledFailureDialog()
            is SyncError.InvalidCredentials -> credentialsChangedDialog()
        }
    }
}