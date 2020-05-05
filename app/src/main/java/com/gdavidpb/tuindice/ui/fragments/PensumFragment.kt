package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.viewmodel.PensumViewModel
import com.gdavidpb.tuindice.utils.extensions.onClickOnce
import kotlinx.android.synthetic.main.fragment_pensum.*
import org.koin.androidx.viewmodel.ext.android.viewModel

open class PensumFragment : NavigationFragment() {

    private val viewModel by viewModel<PensumViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pensum, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(false)

        pensumNetworkReset.onClickOnce(::onResetNetworkClicked)
    }

    private fun onResetNetworkClicked() {
        pensumNetwork.reset()
    }
}