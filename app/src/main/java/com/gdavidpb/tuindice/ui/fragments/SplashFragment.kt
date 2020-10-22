package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.utils.FLAG_RESET
import com.gdavidpb.tuindice.utils.FLAG_VERIFY
import com.gdavidpb.tuindice.utils.extensions.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SplashFragment : NavigationFragment() {

    private val mainViewModel by sharedViewModel<MainViewModel>()

    override fun onCreateView() = R.layout.fragment_splash

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(mainViewModel) {
            observe(fetchStartUpAction, ::startUpObserver)
        }
    }

    private fun startUpObserver(result: Result<StartUpAction>?) {
        when (result) {
            is Result.OnSuccess -> {
                handleStartUpAction(action = result.value)
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