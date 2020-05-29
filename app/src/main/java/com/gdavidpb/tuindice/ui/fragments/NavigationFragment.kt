package com.gdavidpb.tuindice.ui.fragments

import android.app.ActivityManager
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.gdavidpb.tuindice.MainNavigationDirections
import com.gdavidpb.tuindice.ui.activities.LoginActivity
import com.gdavidpb.tuindice.utils.extensions.contentView
import org.koin.android.ext.android.inject

abstract class NavigationFragment : Fragment() {
    private val activityManager by inject<ActivityManager>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.contentView?.background = view.background
    }

    protected fun clearApplicationUserData() {
        activityManager.clearApplicationUserData()
    }

    protected fun navigateToLogin() {
        val direction = MainNavigationDirections.actionToLogin()

        findNavController().navigate(direction)

        val activity = requireActivity()

        if (activity !is LoginActivity) activity.finish()
    }

    protected fun navigate(@IdRes resId: Int) = findNavController().navigate(resId)

    protected fun navigate(directions: NavDirections) = findNavController().navigate(directions)

    protected fun navigateUp() = findNavController().navigateUp()
}