package com.gdavidpb.tuindice.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.model.exception.NoAuthenticatedException
import com.gdavidpb.tuindice.domain.usecase.coroutines.Completable
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.response.SyncResponse
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModel<MainViewModel>()

    private val isFirstStartUp by lazy {
        intent.getBooleanExtra(EXTRA_FIRST_START_UP, false)
    }

    private val navHostFragment by lazy {
        nav_host_fragment as NavHostFragment
    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        data ?: return

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PROFILE_PICTURE) {
                viewModel.getProfilePictureFile(optionalUri = data.data)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onBackPressed()

        return true
    }

    private fun initView(@IdRes navId: Int) {
        setContentView(R.layout.activity_main)

        with(navHostFragment.navController) {
            NavigationUI.setupWithNavController(bottomNavView, this)

            if (navId.isStartDestination()) {
                popBackStack()

                navigate(navId)
            }

            addOnDestinationChangedListener { _, destination, _ ->
                onDestinationChanged(destination)
            }
        }

        viewModel.trySyncAccount()
    }

    private fun onDestinationChanged(destination: NavDestination) {
        val destId = destination.id
        val isStartDestination = destId.isStartDestination()

        bottomNavView.visibleIf(isStartDestination)

        supportActionBar?.apply {
            title = destination.label

            setDisplayHomeAsUpEnabled(!isStartDestination)
        }

        viewModel.setLastScreen(navId = destId)
    }

    private fun showLoading(value: Boolean) {
        if (isFirstStartUp) {
            bottomNavView.visibleIf(value = !value, elseValue = View.INVISIBLE)
            navHostFragment.requireView().visibleIf(value = !value, elseValue = View.INVISIBLE)

            pBarStartUp
        } else {
            bottomNavView.visible()

            pBarSync
        }.also { progressBar ->
            progressBar.visibleIf(value)
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

    private fun dataFailureDialog() {
        alert {
            titleResource = R.string.alert_title_data_failure
            messageResource = R.string.alert_message_data_failure

            isCancelable = false

            positiveButton(R.string.settings) {
                viewModel.signOut()
            }

            negativeButton(R.string.exit) {
                viewModel.signOut()
            }
        }.show()
    }

    private fun syncObserver(result: Result<SyncResponse>?) {
        when (result) {
            is Result.OnLoading -> {
                showLoading(true)
            }
            is Result.OnSuccess -> {
                showLoading(false)

                val response = result.value

                val invalidState = !response.cacheUpdated && !response.thereIsCache

                if (invalidState) dataFailureDialog()
            }
            is Result.OnError -> {
                showLoading(false)

                when (result.throwable) {
                    is NoAuthenticatedException -> {
                        viewModel.signOut()
                    }
                }
            }
            is Result.OnCancel -> {
                showLoading(false)
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
