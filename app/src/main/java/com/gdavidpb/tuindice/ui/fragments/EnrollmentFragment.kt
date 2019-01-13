package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.presentation.viewmodel.EnrollmentFragmentViewModel
import io.reactivex.observers.DisposableMaybeObserver
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

        sRefreshEnrollment.setOnRefreshListener { viewModel.loadAccount(ActiveAccountObserver(), true) }
    }

    override fun onResume() {
        super.onResume()

        viewModel.loadAccount(ActiveAccountObserver(), true)
    }

    inner class ActiveAccountObserver : DisposableMaybeObserver<Account>() {
        init {
            sRefreshEnrollment.isRefreshing = true
        }

        override fun onSuccess(t: Account) {
            sRefreshEnrollment.isRefreshing = false

            // todo load enrollment
        }

        override fun onComplete() {
            sRefreshEnrollment.isRefreshing = false

            /* Handled by MainActivity::StartUpObserver */
        }

        override fun onError(e: Throwable) {
            sRefreshEnrollment.isRefreshing = false

            // todo failure show image on recycler
        }
    }
}