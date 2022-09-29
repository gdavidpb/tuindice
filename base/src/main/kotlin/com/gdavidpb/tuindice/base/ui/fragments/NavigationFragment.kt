package com.gdavidpb.tuindice.base.ui.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.gdavidpb.tuindice.base.utils.extensions.contentView
import org.koin.android.ext.android.inject

abstract class NavigationFragment : Fragment() {
    protected val packageManager by inject<PackageManager>()

    open fun onInitObservers() {}

    @LayoutRes
    abstract fun onCreateView(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(onCreateView(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.contentView?.background = view.background

        onInitObservers()
    }

    protected fun navigate(directions: NavDirections, navOptions: NavOptions? = null) = findNavController().navigate(directions, navOptions)

    protected fun navigateUp() = findNavController().navigateUp()
}