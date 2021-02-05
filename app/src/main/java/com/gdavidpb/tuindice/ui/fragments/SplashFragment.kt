package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.errors.StartUpError
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.SplashViewModel
import com.gdavidpb.tuindice.ui.dialogs.disabledFailureDialog
import com.gdavidpb.tuindice.ui.dialogs.fatalFailureDialog
import com.gdavidpb.tuindice.ui.dialogs.linkFailureDialog
import com.gdavidpb.tuindice.ui.dialogs.networkFailureDialog
import com.gdavidpb.tuindice.utils.FLAG_RESET
import com.gdavidpb.tuindice.utils.FLAG_VERIFY
import com.gdavidpb.tuindice.utils.extensions.observe
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashFragment : NavigationFragment() {

    private val viewModel by viewModel<SplashViewModel>()

    private val mainViewModel by sharedViewModel<MainViewModel>()

    override fun onCreateView() = R.layout.fragment_splash

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val intent = requireActivity().intent

        with(viewModel) {
            observe(startUpAction, ::startUpObserver)

            fetchStartUpAction(dataString = intent.dataString ?: "")
        }
    }

    private fun startUpObserver(result: Result<StartUpAction, StartUpError>?) {
        when (result) {
            is Result.OnSuccess -> {
                handleStartUpAction(action = result.value)
            }
            is Result.OnError -> {
                startUpErrorHandler(error = result.error)
            }
        }
    }

    private fun startUpErrorHandler(error: StartUpError?) {
        with(requireAppCompatActivity()) {
            when (error) {
                is StartUpError.InvalidLink -> linkFailureDialog()
                is StartUpError.UnableToStart -> fatalFailureDialog()
                is StartUpError.AccountDisabled -> disabledFailureDialog()
                is StartUpError.NoConnection -> networkFailureDialog()
            }
        }
    }

    private fun handleStartUpAction(action: StartUpAction) {
        when (action) {
            is StartUpAction.Main -> {
                val navOptions = NavOptions.Builder()
                        .setPopUpTo(action.screen, true)
                        .build()

                findNavController().navigate(action.screen, null, navOptions)

                mainViewModel.trySyncAccount()
            }
            is StartUpAction.Reset -> {
                SplashFragmentDirections.navToEmail(
                        awaitingEmail = action.email,
                        awaitingState = FLAG_RESET
                ).let(::navigate)
            }
            is StartUpAction.Verify -> {
                SplashFragmentDirections.navToEmail(
                        awaitingEmail = action.email,
                        awaitingState = FLAG_VERIFY
                ).let(::navigate)
            }
            is StartUpAction.Login -> {
                SplashFragmentDirections.navToLogin()
                        .let(::navigate)
            }
        }
    }
}