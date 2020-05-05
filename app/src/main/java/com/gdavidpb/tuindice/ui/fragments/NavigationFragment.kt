package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.gdavidpb.tuindice.utils.extensions.contentView

abstract class NavigationFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.contentView?.background = view.background
    }
}