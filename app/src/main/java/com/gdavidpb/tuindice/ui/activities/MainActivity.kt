package com.gdavidpb.tuindice.ui.activities

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavDestination
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.model.exception.FatalException
import com.gdavidpb.tuindice.domain.model.exception.NoAuthenticatedException
import com.gdavidpb.tuindice.domain.model.exception.NoDataException
import com.gdavidpb.tuindice.domain.model.exception.SynchronizationException
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.utils.FLAG_RESET
import com.gdavidpb.tuindice.utils.FLAG_VERIFY
import com.gdavidpb.tuindice.utils.extensions.*
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : NavigationActivity(navViewId = R.id.mainNavHostFragment) {

    private val viewModel by viewModel<MainViewModel>()

    private val appBarConfiguration by lazy {
        val destinations = setOf(
                R.id.fragment_summary,
                R.id.fragment_record,
                R.id.fragment_pensum,
                R.id.fragment_about
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

    private fun initView(@IdRes navId: Int) {
        setContentView(R.layout.activity_main)

        NavigationUI.setupWithNavController(bottomNavView, navController)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        bottomNavView.setOnNavigationItemReselectedListener { }

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

    private fun startUpObserver(result: Result<StartUpAction>?) {
        when (result) {
            is Result.OnSuccess -> {
                handleStartUpAction(action = result.value)
            }
            is Result.OnError -> {
                val exception = FatalException(cause = result.throwable)

                handleException(throwable = exception)
            }
        }
    }

    private fun handleStartUpAction(action: StartUpAction) {
        when (action) {
            is StartUpAction.Main -> {
                initView(navId = action.screen)
            }
            is StartUpAction.Reset -> {
                startActivity<EmailSentActivity>(
                        EXTRA_AWAITING_STATE to FLAG_RESET,
                        EXTRA_AWAITING_EMAIL to action.email
                )
                finish()
            }
            is StartUpAction.Verify -> {
                startActivity<EmailSentActivity>(
                        EXTRA_AWAITING_STATE to FLAG_VERIFY,
                        EXTRA_AWAITING_EMAIL to action.email
                )
                finish()
            }
            is StartUpAction.Login -> {
                startActivity<LoginActivity>()
                finish()
            }
        }
    }
}