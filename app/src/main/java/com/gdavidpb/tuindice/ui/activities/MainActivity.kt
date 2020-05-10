package com.gdavidpb.tuindice.ui.activities

import android.os.Bundle
import androidx.annotation.NavigationRes
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.model.exception.FatalException
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.utils.EXTRA_AWAITING_EMAIL
import com.gdavidpb.tuindice.utils.EXTRA_AWAITING_STATE
import com.gdavidpb.tuindice.utils.FLAG_RESET
import com.gdavidpb.tuindice.utils.FLAG_VERIFY
import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : NavigationActivity() {

    private val viewModel by viewModel<MainViewModel>()

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
            is Result.OnCancel -> {
                pBarSync.visibleIf(false)
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
                val exception = FatalException(cause = result.throwable)

                handleException(throwable = exception)
            }
        }
    }
}