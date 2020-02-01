package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.response.SyncResponse
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.ui.adapters.SummaryAdapter
import com.gdavidpb.tuindice.utils.CircleTransform
import com.gdavidpb.tuindice.utils.extensions.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_summary.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

open class SummaryFragment : Fragment() {

    private val viewModel by sharedViewModel<MainViewModel>()

    private val picasso by inject<Picasso>()

    private val summaryAdapter = SummaryAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(false)

        with(rViewSummary) {
            layoutManager = LinearLayoutManager(context)
            adapter = summaryAdapter
        }

        with(viewModel) {
            observe(sync, ::syncObserver)
            observe(account, ::accountObserver)

            getAccount()
        }
    }

    private fun syncObserver(result: Result<SyncResponse>?) {
        when (result) {
            is Result.OnSuccess -> {
                val requireUpdate = result.value.isDataUpdated

                if (requireUpdate) viewModel.getAccount()
            }
        }
    }

    private fun accountObserver(result: Result<Account>?) {
        when (result) {
            is Result.OnSuccess -> {
                loadAccount(account = result.value)
            }
        }
    }

    private fun loadAccount(account: Account) {
        val context = requireContext()

        summaryAdapter.setAccount(account)

        val shortName = account.fullName.toShortName()
        val lastUpdate = context.getString(R.string.text_last_update, account.lastUpdate.formatLastUpdate())

        tViewName.text = shortName
        tViewCareer.text = account.careerName

        tViewLastUpdate.text = lastUpdate
        tViewLastUpdate.drawables(left = context.getCompatDrawable(R.drawable.ic_sync, R.color.color_secondary_text))

        if (account.grade > 0.0) {
            groupGrade.visible()

            tViewGrade.animateGrade(account.grade)
        } else
            groupGrade.gone()

        if (account.photoUrl.isNotEmpty())
            picasso.load(account.photoUrl)
                    .tag(account.uid)
                    .transform(CircleTransform())
                    .into(iViewProfile)
        else
            iViewProfile.setImageResource(R.mipmap.ic_launcher_round)
    }
}