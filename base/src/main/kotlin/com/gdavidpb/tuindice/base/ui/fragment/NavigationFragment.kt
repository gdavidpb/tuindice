package com.gdavidpb.tuindice.base.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController

abstract class NavigationFragment : Fragment() {
	@LayoutRes
	abstract fun onCreateView(): Int

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return inflater.inflate(onCreateView(), container, false)
	}

	protected fun navigate(directions: NavDirections, navOptions: NavOptions? = null) =
		findNavController().navigate(directions, navOptions)

	protected fun navigateUp(requestKey: String? = null, result: Bundle? = null) {
		if (requestKey != null && result != null)
			setFragmentResult(requestKey, result)

		findNavController().navigateUp()
	}
}