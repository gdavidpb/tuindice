package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.utils.observe
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.presentation.viewmodel.EnrollmentFragmentViewModel
import kotlinx.android.synthetic.main.fragment_enrollment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

open class EnrollmentFragment : Fragment() {

    private val viewModel: EnrollmentFragmentViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_enrollment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rViewRecycler.layoutManager = LinearLayoutManager(context)

        with(viewModel) {
            observe(loadAccount, ::loadAccountObserver)
        }

        sRefreshEnrollment.setOnRefreshListener { viewModel.loadAccount(tryRefresh = true) }
    }

    override fun onResume() {
        super.onResume()

        viewModel.loadAccount(tryRefresh = true)
    }

    private fun loadAccountObserver(result: Result<Account>?) {
        when (result) {
            is Result.OnLoading -> {
                sRefreshEnrollment.isRefreshing = true
            }
            is Result.OnSuccess -> {
                sRefreshEnrollment.isRefreshing = false

                // todo load enrollment
            }
            is Result.OnEmpty -> {
                sRefreshEnrollment.isRefreshing = false

                /* Handled by MainActivity::StartUpObserver */
            }
            is Result.OnError -> {
                sRefreshEnrollment.isRefreshing = false

                // todo failure show image on recycler
            }
        }
    }
}