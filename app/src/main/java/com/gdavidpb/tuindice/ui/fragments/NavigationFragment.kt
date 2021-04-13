package com.gdavidpb.tuindice.ui.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.extensions.contentView
import com.gdavidpb.tuindice.utils.extensions.snackBar
import org.koin.android.ext.android.inject

abstract class NavigationFragment : Fragment() {
    protected val packageManager by inject<PackageManager>()

    @LayoutRes
    abstract fun onCreateView(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(onCreateView(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.contentView?.background = view.background
    }

    protected fun requireAppCompatActivity() = requireActivity() as AppCompatActivity

    protected fun navigate(directions: NavDirections) = findNavController().navigate(directions)

    protected fun navigateUp() = findNavController().navigateUp()

    @Deprecated("Migrate to errorSnackBar")
    protected fun noConnectionSnackBar(isNetworkAvailable: Boolean, retry: (() -> Unit)? = null) {
        snackBar {
            messageResource = if (isNetworkAvailable)
                R.string.snack_service_unreachable
            else
                R.string.snack_network_unavailable

            if (retry != null) action(R.string.retry) { retry() }
        }
    }

    protected fun errorSnackBar(@StringRes message: Int = R.string.snack_default_error, retry: (() -> Unit)? = null) {
        snackBar {
            messageResource = message

            if (retry != null) action(R.string.retry) { retry() }
        }
    }
}