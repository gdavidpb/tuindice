package com.gdavidpb.tuindice.ui.activities

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.usecase.coroutines.Completable
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.isGoogleServicesAvailable
import com.gdavidpb.tuindice.utils.extensions.observe
import com.gdavidpb.tuindice.utils.extensions.openSettings
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)

        if (!isGoogleServicesAvailable(activity = this)) return

        with(viewModel) {
            observe(sync, ::syncObserver)
            observe(signOut, ::signOutObserver)
            observe(fetchStartUpAction, ::startUpObserver)

            fetchStartUpAction(dataString = intent.dataString ?: "")
        }
    }

    private fun initView(@IdRes navId: Int) {
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.navHostFragment)

        NavigationUI.setupWithNavController(bottomNavView, navController)

        runCatching {
            navController.popBackStack()

            navController.navigate(navId)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            resetLiveData(destination.id)

            supportActionBar?.title = destination.label

            viewModel.setLastScreen(navId = destination.id)
        }

        viewModel.trySyncAccount()
    }

    private fun resetLiveData(destination: Int) {
        with(viewModel) {
            when (destination) {
                R.id.nav_record -> {
                    enrollment.value = null
                }
            }
        }
    }

    private fun fatalFailureDialog() {
        alert {
            titleResource = R.string.alert_title_fatal_failure
            messageResource = R.string.alert_message_fatal_failure

            isCancelable = false

            positiveButton(R.string.settings) {
                openSettings()

                finish()
            }

            negativeButton(R.string.exit) {
                finish()
            }
        }.show()
    }

    private fun syncObserver(result: Result<Boolean>?) {
        when (result) {
            is Result.OnLoading -> {
                pBarSync.visibility = View.VISIBLE
            }
            is Result.OnSuccess -> {
                pBarSync.visibility = View.GONE
            }
            is Result.OnError -> {
                pBarSync.visibility = View.GONE
            }
            is Result.OnCancel -> {
                pBarSync.visibility = View.GONE
            }
        }
    }

    private fun signOutObserver(result: Completable?) {
        when (result) {
            is Completable.OnComplete -> {
                startActivity<LoginActivity>()
                finish()
            }
            is Completable.OnError -> {
                fatalFailureDialog()
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
