package com.gdavidpb.tuindice.ui.fragments

import android.app.ActivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.gdavidpb.tuindice.utils.extensions.contentView
import com.gdavidpb.tuindice.utils.extensions.hideSoftKeyboard
import org.koin.android.ext.android.inject

abstract class NavigationFragment : Fragment() {
    private val activityManager by inject<ActivityManager>()
    private val inputMethodManager by inject<InputMethodManager>()

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
}