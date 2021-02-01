package com.gdavidpb.tuindice.ui.fragments

import android.app.ActivityManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.extensions.*
import org.koin.android.ext.android.inject

abstract class NavigationFragment : Fragment() {
    private val activityManager by inject<ActivityManager>()
    private val inputMethodManager by inject<InputMethodManager>()
    private val connectivityManager by inject<ConnectivityManager>()

    @LayoutRes
    abstract fun onCreateView(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(onCreateView(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.contentView?.background = view.background
    }

    protected fun clearApplicationUserData() {
        activityManager.clearApplicationUserData()
    }

    protected fun hideSoftKeyboard() {
        inputMethodManager.hideSoftKeyboard(requireActivity())
    }

    protected fun navigate(directions: NavDirections) = findNavController().navigate(directions)

    protected fun navigateUp() = findNavController().navigateUp()

    protected fun credentialsChangedDialog() {
        activityManager.clearApplicationUserData()

        alert {
            titleResource = R.string.alert_title_credentials_failure
            messageResource = R.string.alert_message_credentials_failure

            isCancelable = false

            positiveButton(R.string.accept) {
                requireActivity().recreate()
            }
        }
    }

    protected fun disabledAccountDialog() {
        activityManager.clearApplicationUserData()

        alert {
            titleResource = R.string.alert_title_disabled_failure
            messageResource = R.string.alert_message_disabled_failure

            isCancelable = false

            positiveButton(R.string.accept) {
                requireActivity().finish()
            }
        }
    }

    protected fun noConnectionSnackBar(retry: (() -> Unit)? = null) {
        snackBar {
            messageResource = if (connectivityManager.isNetworkAvailable())
                R.string.snack_service_unreachable
            else
                R.string.snack_network_unavailable

            if (retry != null) action(R.string.retry) { retry() }
        }
    }

    protected fun defaultErrorSnackBar(retry: (() -> Unit)? = null) {
        snackBar {
            messageResource = R.string.snack_bar_error_occurred

            if (retry != null) action(R.string.retry) { retry() }
        }
    }
}