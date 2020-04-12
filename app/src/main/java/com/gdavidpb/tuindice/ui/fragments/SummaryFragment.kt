package com.gdavidpb.tuindice.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.usecase.coroutines.Completable
import com.gdavidpb.tuindice.domain.usecase.coroutines.Event
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.response.SyncResponse
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.ui.adapters.SummaryAdapter
import com.gdavidpb.tuindice.utils.CircleTransform
import com.gdavidpb.tuindice.utils.DECIMALS_GRADE_QUARTER
import com.gdavidpb.tuindice.utils.REQUEST_CODE_PROFILE_PICTURE
import com.gdavidpb.tuindice.utils.extensions.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_summary.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

open class SummaryFragment : Fragment() {

    private val viewModel by sharedViewModel<MainViewModel>()

    private val picasso by inject<Picasso>()

    private val loadProfilePicture = LiveCompletable()

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

        iViewProfile.onClickOnce(::onEditProfilePictureClick)
        iViewEditProfile.onClickOnce(::onEditProfilePictureClick)

        with(viewModel) {
            observe(sync, ::syncObserver)
            observe(account, ::accountObserver)
            observe(getProfilePictureFile, ::getProfilePictureFileObserver)
            observe(createProfilePictureFile, ::createProfilePictureFileObserver)
            observe(updateProfilePicture, ::updateProfilePictureObserver)
            observe(loadProfilePicture, ::loadProfilePictureObserver)

            getAccount()
        }
    }

    private fun onEditProfilePictureClick() {
        viewModel.createProfilePictureFile()
    }

    private fun syncObserver(result: Result<SyncResponse>?) {
        when (result) {
            is Result.OnSuccess -> {
                val response = result.value

                if (response.cacheUpdated)
                    viewModel.getAccount()
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

    private fun getProfilePictureFileObserver(result: Event<Uri>?) {
        when (result) {
            is Event.OnSuccess -> {
                val outputUri = result.value

                viewModel.updateProfilePicture(outputUri)
            }
            is Event.OnError -> requireActivity().showSnackBarError(throwable = result.throwable)
        }
    }

    private fun createProfilePictureFileObserver(result: Event<Uri>?) {
        when (result) {
            is Event.OnSuccess -> {
                val outputUri = result.value
                        .fileProviderUri(requireContext())

                requestProfilePictureInput(outputUri)
            }
            is Event.OnError -> requireActivity().showSnackBarError(throwable = result.throwable)
        }
    }

    private fun updateProfilePictureObserver(result: Event<String>?) {
        when (result) {
            is Event.OnLoading -> {
                showProfilePictureLoading()
            }
            is Event.OnSuccess -> {
                val stableKey = result.value

                picasso.invalidate(stableKey)

                viewModel.getAccount()

                snackBar {
                    messageResource = R.string.snack_bar_profile_picture_updated
                }
            }
            is Event.OnError -> {
                hideProfilePictureLoading()

                requireActivity().showSnackBarError(throwable = result.throwable)
            }
        }
    }

    private fun loadProfilePictureObserver(result: Completable?) {
        when (result) {
            is Completable.OnLoading -> {
                pBarProfile.visible()
            }
            is Completable.OnComplete -> {
                hideProfilePictureLoading()
            }
            is Completable.OnError -> {
                hideProfilePictureLoading()

                requireActivity().showSnackBarError(throwable = result.throwable)
            }
        }
    }

    private fun showProfilePictureLoading() {
        pBarProfile.visible()

        arrayOf(iViewProfile, iViewEditProfile).forEach {
            it.disable()
            it.animateScaleDown()
        }
    }

    private fun hideProfilePictureLoading() {
        pBarProfile.gone()

        arrayOf(iViewProfile, iViewEditProfile).forEach {
            it.enable()
            it.animateScaleUp()
        }
    }

    private fun requestProfilePictureInput(outputUri: Uri) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(MediaStore.EXTRA_OUTPUT, outputUri)

        val galleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        val chooser = Intent.createChooser(galleryIntent, getString(R.string.label_profile_picture_chooser))

        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        requireActivity().startActivityForResult(chooser, REQUEST_CODE_PROFILE_PICTURE)
    }

    private fun loadAccount(account: Account) {
        val context = requireContext()

        summaryAdapter.setAccount(account)

        val shortName = account.fullName.toShortName()
        val lastUpdate = context.getString(R.string.text_last_update, account.lastUpdate.formatLastUpdate())

        tViewName.text = shortName
        tViewCareer.text = account.careerName

        tViewLastUpdate.text = lastUpdate
        tViewLastUpdate.drawables(start = context.getCompatDrawable(R.drawable.ic_sync, R.color.color_secondary_text))

        if (account.grade > 0.0) {
            groupGrade.visible()

            tViewGrade.animateGrade(value = account.grade, decimals = DECIMALS_GRADE_QUARTER)
        } else
            groupGrade.gone()

        if (account.photoUrl.isNotEmpty())
            with(picasso) {
                load(account.photoUrl)
                        .noFade()
                        .stableKey(account.uid)
                        .transform(CircleTransform())
                        .error(R.mipmap.ic_launcher_round)
                        .into(iViewProfile, loadProfilePicture)
            }
        else
            iViewProfile.setImageResource(R.mipmap.ic_launcher_round)
    }
}