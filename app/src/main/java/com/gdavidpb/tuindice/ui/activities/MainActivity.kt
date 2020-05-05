package com.gdavidpb.tuindice.ui.activities

import android.app.ActivityManager
import android.os.Bundle
import android.view.View
import androidx.annotation.NavigationRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
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
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModel<MainViewModel>()

    private val activityManager by inject<ActivityManager>()

    private val isFirstStartUp by lazy {
        intent.getBooleanExtra(EXTRA_FIRST_START_UP, false)
    }

    private val navController by lazy {
        findNavController(R.id.navHostFragment)
    }

    private val appBarConfiguration by lazy {
        val destinations = setOf(
                R.id.nav_summary,
                R.id.nav_record,
                R.id.nav_pensum,
                R.id.nav_about
        )

        AppBarConfiguration(navController.graph).apply {
            topLevelDestinations.addAll(destinations)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)

        if (!isGoogleServicesAvailable()) return

        with(viewModel) {
            observe(sync, ::syncObserver)
            observe(fetchStartUpAction, ::startUpObserver)

            fetchStartUpAction(dataString = intent.dataString ?: "")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onBackPressed()

        return true
    }

    private fun initView(@NavigationRes navId: Int) {
        setContentView(R.layout.activity_main)

        NavigationUI.setupWithNavController(bottomNavView, navController)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        with(navController) {
            val isTopLevelDestination = appBarConfiguration.isTopLevelDestination(navId)
            val isStartDestination = graph.startDestination == navId

            if (isTopLevelDestination && !isStartDestination) {
                val navOptions = NavOptions.Builder()
                        .setPopUpTo(graph.startDestination, true)
                        .build()

                navigate(navId, null, navOptions)
            }

            addOnDestinationChangedListener { _, destination, _ ->
                onDestinationChanged(destination)
            }
        }

        viewModel.trySyncAccount()
    }

    private fun onDestinationChanged(destination: NavDestination) {
        val isTopLevelDestination = appBarConfiguration.isTopLevelDestination(navId = destination.id)

        bottomNavView.visibleIf(isTopLevelDestination)

        if (isTopLevelDestination) viewModel.setLastScreen(navId = destination.id)
    }

    private fun showLoading(value: Boolean) {
        if (isFirstStartUp) {
            bottomNavView.visibleIf(value = !value)
            navHostFragment.requireView().visibleIf(value = !value, elseValue = View.INVISIBLE)

            pBarStartUp
        } else {
            bottomNavView.visible()

            pBarSync
        }.also { progressBar ->
            progressBar.visibleIf(value)
        }
    }

    private fun fatalFailureRestart() {
        activityManager.clearApplicationUserData()

        recreate()
    }

    private fun fatalFailureDialog() {
        alert {
            titleResource = R.string.alert_title_fatal_failure
            messageResource = R.string.alert_message_fatal_failure

            isCancelable = false

            positiveButton(R.string.restart) {
                fatalFailureRestart()
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

    private fun syncObserver(result: Result<Boolean>?) {
        when (result) {
            is Result.OnLoading -> {
                showLoading(true)
            }
            is Result.OnSuccess -> {
                showLoading(false)
            }
            is Result.OnError -> {
                showLoading(false)

                when (result.throwable) {
                    is NoAuthenticatedException -> fatalFailureRestart()
                    is NoDataException -> dataFailureDialog()
                    is SynchronizationException -> syncFailureDialog()
                }
            }
            is Result.OnCancel -> {
                showLoading(false)
            }
        }
    }

    private fun startUpObserver(result: Result<StartUpAction>?) {
        when (result) {
            is Result.OnSuccess -> {
                when (val value = result.value) {
                    is StartUpAction.Main -> {
                        initView(navId = value.screen)
                    }
                    is StartUpAction.Reset -> {
                        startActivity<EmailSentActivity>(
                                EXTRA_AWAITING_STATE to FLAG_RESET,
                                EXTRA_AWAITING_EMAIL to value.email
                        )
                        finish()
                    }
                    is StartUpAction.Verify -> {
                        startActivity<EmailSentActivity>(
                                EXTRA_AWAITING_STATE to FLAG_VERIFY,
                                EXTRA_AWAITING_EMAIL to value.email
                        )
                        finish()
                    }
                    is StartUpAction.Login -> {
                        startActivity<LoginActivity>()
                        finish()
                    }
                }
            }
            is Result.OnError -> {
                fatalFailureDialog()
            }
        }
    }
}
