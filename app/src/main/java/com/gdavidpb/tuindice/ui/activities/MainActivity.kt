package com.gdavidpb.tuindice.ui.activities

import android.app.ActivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.model.exception.NoAuthenticatedException
import com.gdavidpb.tuindice.domain.model.exception.NoDataException
import com.gdavidpb.tuindice.domain.model.exception.SynchronizationException
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.utils.IdempotentLocker
import com.gdavidpb.tuindice.utils.TIME_EXIT_LOCKER
import com.gdavidpb.tuindice.utils.extensions.*
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val activityManager by inject<ActivityManager>()

    private val viewModel by viewModel<MainViewModel>()

    private val backLocker = IdempotentLocker()

    /* true = show bottom nav, false = hide bottom nav */
    private val destinations = mapOf(
            R.id.fragment_summary to true,
            R.id.fragment_record to true,
            R.id.fragment_pensum to true,
            R.id.fragment_about to true,
            R.id.fragment_splash to false,
            R.id.fragment_login to false,
            R.id.fragment_email to false
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

        NavigationUI.setupWithNavController(bottomNavView, navController)
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration)

        bottomNavView.setOnNavigationItemReselectedListener { }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            onDestinationChanged(destination)
        }

        if (!isGoogleServicesAvailable()) return

        with(viewModel) {
            observe(sync, ::syncObserver)
            observe(fetchStartUpAction, ::startUpObserver)

            fetchStartUpAction(dataString = intent.dataString ?: "")
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

    private fun handleException(throwable: Throwable): Boolean {
        val causes = throwable.causes()

        return when {
            throwable is NoAuthenticatedException -> {
                fatalFailureRestart()
                true
            }
            throwable is NoDataException -> {
                dataFailureDialog()
                true
            }
            throwable is SynchronizationException -> {
                syncFailureDialog()
                true
            }
            causes.contains<FirebaseAuthInvalidUserException>() -> {
                activityManager.clearApplicationUserData()

                disabledFailureDialog()
                true
            }
            causes.contains<FirebaseAuthActionCodeException>() -> {
                linkFailureDialog()
                true
            }
            else -> {
                false
            }
        }
    }

    private fun fatalFailureRestart() {
        activityManager.clearApplicationUserData()

        recreate()
    }

    private fun disabledFailureDialog() {
        alert {
            titleResource = R.string.alert_title_disabled_failure
            messageResource = R.string.alert_message_disabled_failure

            isCancelable = false

            /* TODO
            if (allowDisabledAccount())
                positiveButton(R.string.accept)
            else
                positiveButton(R.string.exit) {
                    finish()
                }
            */
        }
    }

    private fun linkFailureDialog() {
        alert {
            titleResource = R.string.alert_title_link_failure
            messageResource = R.string.alert_message_link_failure

            isCancelable = false

            positiveButton(R.string.exit) {
                finish()
            }
        }
    }

    private fun syncFailureDialog() {
        alert {
            titleResource = R.string.alert_title_sync_failure
            messageResource = R.string.alert_message_sync_failure

            isCancelable = false

            positiveButton(R.string.open_settings) {
                openDataTime()
            }

            negativeButton(R.string.exit) {
                finish()
            }
        }
    }

    private fun dataFailureDialog() {
        alert {
            titleResource = R.string.alert_title_data_failure
            messageResource = R.string.alert_message_data_failure

            isCancelable = false

            positiveButton(R.string.restart) {
                fatalFailureRestart()
            }

            negativeButton(R.string.exit) {
                finish()
            }
        }
    }

    private fun fatalFailureDialog() {
        alert {
            titleResource = R.string.alert_title_fatal_failure
            messageResource = R.string.alert_message_fatal_failure

            isCancelable = false

            positiveButton(R.string.restart) {
                activityManager.clearApplicationUserData()

                recreate()
            }

            negativeButton(R.string.exit) {
                finish()
            }
        }
    }


    private fun onDestinationChanged(destination: NavDestination) {
        val showBottomNav = destinations[destination.id] ?: false

        bottomNavView.isVisible = showBottomNav

        if (showBottomNav) viewModel.setLastScreen(navId = destination.id)
    }

    private fun startUpObserver(result: Result<StartUpAction>?) {
        when (result) {
            is Result.OnSuccess -> {
                appBar.visible()
            }
            is Result.OnError -> {
                fatalFailureDialog()
            }
        }
    }

    private fun syncObserver(result: Result<Boolean>?) {
        when (result) {
            is Result.OnLoading -> {
                pBarSync.visibleIf(true)
            }
            is Result.OnSuccess -> {
                pBarSync.visibleIf(false)
            }
            is Result.OnError -> {
                pBarSync.visibleIf(false)

                handleException(throwable = result.throwable)
            }
        }
    }
}