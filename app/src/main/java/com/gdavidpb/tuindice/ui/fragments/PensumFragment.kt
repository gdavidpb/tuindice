package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.view.View
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.viewmodel.PensumViewModel
import com.gdavidpb.tuindice.utils.extensions.onClickOnce
import kotlinx.android.synthetic.main.fragment_pensum.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class PensumFragment : NavigationFragment() {

    private val viewModel by viewModel<PensumViewModel>()

    override fun onCreateView() = R.layout.fragment_pensum

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pensumNetworkReset.onClickOnce(::onResetNetworkClick)
    }

    private fun onResetNetworkClick() {
        pensumNetwork.reset()
    }
}