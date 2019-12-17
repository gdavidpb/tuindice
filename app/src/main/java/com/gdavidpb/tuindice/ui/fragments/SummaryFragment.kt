package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.ui.adapters.SummaryAdapter
import com.gdavidpb.tuindice.utils.extensions.observe
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_summary.*
import org.koin.android.ext.android.inject
import android.view.*
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

open class SummaryFragment : Fragment() {

    private val viewModel by sharedViewModel<MainViewModel>()

    private val picasso by inject<Picasso>()

    private val adapter = SummaryAdapter(manager = SummaryManager())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(false)

        rViewSummary.layoutManager = LinearLayoutManager(context)
        rViewSummary.adapter = adapter

        with(viewModel) {
            observe(sync, ::syncObserver)
            observe(account, ::accountObserver)

            getAccount()
        }
    }

    private fun syncObserver(result: Result<Boolean>?) {
        when (result) {
            is Result.OnSuccess -> {
                val requireUpdate = result.value

                if (requireUpdate) viewModel.getAccount()
            }
        }
    }

    private fun accountObserver(result: Result<Account>?) {
        when (result) {
            is Result.OnSuccess -> {
                adapter.setAccount(account = result.value)
            }
        }
    }

    inner class SummaryManager : SummaryAdapter.AdapterManager {
        override fun provideImageLoader(provider: (picasso: Picasso) -> Unit) = provider(picasso)
    }
}