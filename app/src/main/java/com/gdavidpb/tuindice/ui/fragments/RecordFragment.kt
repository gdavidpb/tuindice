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
import com.gdavidpb.tuindice.presentation.viewmodel.EnrollmentFragmentViewModel
import com.gdavidpb.tuindice.utils.observe
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_record.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

open class RecordFragment : Fragment() {

    private val viewModel: EnrollmentFragmentViewModel by viewModel()

    private val picasso: Picasso by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rViewRecord.layoutManager = LinearLayoutManager(context)

        with(viewModel) {
            observe(loadAccount, ::loadAccountObserver)

            loadAccount(trySync = false)
        }
    }

    private fun loadAccount(account: Account) {
        /*
        tViewName.text = account.fullName.toShortName()
        tViewCareer.text = account.careerName
        tViewGrade.text = (0.0).formatGrade()

        if (account.photoUrl.isNotEmpty())
            picasso.load(account.photoUrl).transform(CircleTransform()).into(iViewProfile)
        else
            iViewProfile.setImageResource(R.mipmap.ic_launcher_round)
        */
    }

    private fun loadAccountObserver(result: Result<Account>?) {
        when (result) {
            is Result.OnLoading -> {
                sRefreshRecord.isRefreshing = true
            }
            is Result.OnSuccess -> {
                sRefreshRecord.isRefreshing = false
            }
            is Result.OnError -> {
                sRefreshRecord.isRefreshing = false

                // todo failure show image on recycler
            }
        }
    }
}