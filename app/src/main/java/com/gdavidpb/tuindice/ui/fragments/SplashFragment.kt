package com.gdavidpb.tuindice.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.errors.StartUpError
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.SplashViewModel
import com.gdavidpb.tuindice.utils.extensions.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashFragment : NavigationFragment() {

    private val viewModel by viewModel<SplashViewModel>()

    private val mainViewModel by sharedViewModel<MainViewModel>()

    override fun onCreateView() = R.layout.fragment_splash

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val intent = requireActivity().intent

        viewModel.fetchStartUpAction(dataString = intent.dataString ?: "")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        with(viewModel) {
            observe(startUpAction, ::startUpObserver)
        }
    }

    private fun navToAccountDisabled() {
        val navOptions = findNavController().navOptionsClean()

        navigate(SplashFragmentDirections.navToAccountDisabled(), navOptions)
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
        with(requireActivity() as AppCompatActivity) {
            when (error) {
                is StartUpError.InvalidLink -> linkFailureDialog()
                is StartUpError.UnableToStart -> fatalFailureDialog()
                is StartUpError.NoConnection -> connectionSnackBar(error.isNetworkAvailable)
                is StartUpError.AccountDisabled -> navToAccountDisabled()
            }
        }
    }

    private fun handleStartUpAction(action: StartUpAction) {
        when (action) {
            is StartUpAction.Main -> {
                val navOptions = NavOptions.Builder()
                        .setPopUpTo(action.screen, true)
                        .build()

                runCatching {
                    findNavController().navigate(action.screen, null, navOptions)
                }.onFailure {
                    navigate(SplashFragmentDirections.navToSummary())
                }

                mainViewModel.trySyncAccount()
            }
            is StartUpAction.ResetPassword -> {
                navigate(SplashFragmentDirections.navToResetPassword(
                        email = action.email
                ))
            }
            is StartUpAction.SignIn -> {
                navigate(SplashFragmentDirections.navToSignIn())
            }
        }
    }
}