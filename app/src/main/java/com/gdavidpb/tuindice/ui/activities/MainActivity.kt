package com.gdavidpb.tuindice.ui.activities

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.usecase.coroutines.Completable
import com.gdavidpb.tuindice.domain.usecase.coroutines.Event
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.errors.SyncError
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.ui.dialogs.UpdatePasswordBottomSheetDialog
import com.gdavidpb.tuindice.utils.IdempotentLocker
import com.gdavidpb.tuindice.utils.RequestCodes
import com.gdavidpb.tuindice.utils.TIME_EXIT_LOCKER
import com.gdavidpb.tuindice.utils.extensions.*
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val reviewManager by inject<ReviewManager>()

    private val updateManager by inject<AppUpdateManager>()

    private val viewModel by viewModel<MainViewModel>()

    private val inputMethodManager by inject<InputMethodManager>()

    private val backLocker = IdempotentLocker()

    private val topDestinations = listOf(
            R.id.fragment_summary,
            R.id.fragment_record,
            R.id.fragment_about,
            R.id.fragment_splash,
            R.id.fragment_sign_in
    )

    private val bottomDestinations = listOf(
            R.id.fragment_summary,
            R.id.fragment_record,
            R.id.fragment_about
    )

    private val appBarConfiguration by lazy {
        AppBarConfiguration(navController.graph).apply {
            topLevelDestinations.addAll(topDestinations)
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

        with(viewModel) {
            observe(sync, ::syncObserver)
            observe(requestReview, ::requestReviewObserver)
            observe(updateInfo, ::updateInfoObserver)
            observe(signOut, ::signOutObserver)
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.checkUpdate(updateManager)
    }

    override fun onBackPressed() {
        val isHomeDestination = topDestinations.contains(navController.currentDestination?.id)

        if (isHomeDestination) {
            toast(R.string.toast_repeat_to_exit)

            val locked = backLocker.lock(UnlockIn = TIME_EXIT_LOCKER)

            if (!locked) finish()
        } else {
            super.onBackPressed()
        }
    }

    private fun onDestinationChanged(destination: NavDestination) {
        hideSoftKeyboard(inputMethodManager)

        val showAppBar = (destination.id != R.id.fragment_splash)
        val showBottomNav = bottomDestinations.contains(destination.id)

        appBar.isVisible = showAppBar
        bottomNavView.isVisible = showBottomNav

        if (showBottomNav) viewModel.setLastScreen(navId = destination.id)
    }

    private fun showUpdatePasswordDialog() {
        UpdatePasswordBottomSheetDialog()
                .show(supportFragmentManager, "updatePasswordDialog")
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

    private fun requestReviewObserver(result: Event<ReviewInfo, Nothing>?) {
        when (result) {
            is Event.OnSuccess -> {
                val reviewInfo = result.value

                lifecycleScope.launchWhenResumed {
                    reviewManager.launchReview(
                            this@MainActivity,
                            reviewInfo
                    )
                }
            }
        }
    }

    private fun updateInfoObserver(result: Event<AppUpdateInfo, Nothing>?) {
        when (result) {
            is Event.OnSuccess -> {
                val updateInfo = result.value

                updateManager.startUpdateFlowForResult(
                        updateInfo,
                        AppUpdateType.IMMEDIATE,
                        this@MainActivity,
                        RequestCodes.APP_UPDATE_REQUEST
                )
            }
        }
    }

    private fun signOutObserver(result: Completable<Nothing>?) {
        when (result) {
            is Completable.OnComplete, is Completable.OnError -> {
                errorSnackBar(R.string.snack_account_disabled)

                recreate()
            }
        }
    }

    private fun syncErrorHandler(error: SyncError?) {
        when (error) {
            is SyncError.AccountDisabled -> viewModel.forceSignOut()
            is SyncError.OutdatedPassword -> showUpdatePasswordDialog()
        }
    }
}