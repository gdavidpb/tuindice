package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.viewmodel.PensumViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

open class PensumFragment : Fragment() {

    private val viewModel by viewModel<PensumViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pensum, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(false)
    }
}