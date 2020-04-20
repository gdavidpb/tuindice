package com.gdavidpb.tuindice.ui.fragments

import android.app.Activity
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
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.ui.adapters.SummaryAdapter
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_summary.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

open class SummaryFragment : Fragment() {

    private val mainViewModel by sharedViewModel<MainViewModel>()

    private val viewModel by viewModel<SummaryViewModel>()

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

        with(mainViewModel) {
            observe(sync, ::syncObserver)
        }

        with(viewModel) {
            observe(account, ::accountObserver)
            observe(getProfilePictureFile, ::getProfilePictureFileObserver)
            observe(createProfilePictureFile, ::createProfilePictureFileObserver)
            observe(updateProfilePicture, ::updateProfilePictureObserver)
            observe(loadProfilePicture, ::loadProfilePictureObserver)
            observe(profilePicture, ::profilePictureObserver)
            observe(removeProfilePicture, ::removeProfilePictureObserver)

            getAccount()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data ?: return

        if (resultCode == Activity.RESULT_OK) {
            val removeProfilePicture = data.hasExtra(EXTRA_REMOVE_PROFILE_PICTURE)
            val requestProfilePicture = requestCode == REQUEST_CODE_PROFILE_PICTURE

            when {
                removeProfilePicture -> {
                    showRemoveProfilePictureDialog()
                }
                requestProfilePicture -> {
                    viewModel.getProfilePictureFile(optionalUri = data.data)
                }
            }
        }
    }

    private fun onEditProfilePictureClick() {
        viewModel.createProfilePictureFile()
    }

    private fun syncObserver(result: Result<Boolean>?) {
        when (result) {
            is Result.OnSuccess -> {
                val pendingUpdate = result.value

                if (pendingUpdate)
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
            is Event.OnError -> requireActivity().showSnackBarException(throwable = result.throwable)
        }
    }

    private fun createProfilePictureFileObserver(result: Event<Uri>?) {
        when (result) {
            is Event.OnSuccess -> {
                val outputUri = result.value
                        .fileProviderUri(requireContext())

                requestProfilePictureInput(outputUri)
            }
            is Event.OnError -> requireActivity().showSnackBarException(throwable = result.throwable)
        }
    }

    private fun updateProfilePictureObserver(result: Event<String>?) {
        when (result) {
            is Event.OnLoading -> {
                showProfilePictureLoading()
            }
            is Event.OnSuccess -> {
                loadProfilePicture(url = result.value)

                snackBar {
                    messageResource = R.string.snack_bar_profile_picture_updated
                }
            }
            is Event.OnError -> {
                hideProfilePictureLoading()

                requireActivity().showSnackBarException(throwable = result.throwable)
            }
        }
    }

    private fun loadProfilePictureObserver(result: Completable?) {
        when (result) {
            is Completable.OnLoading -> {
                showProfilePictureLoading()
            }
            is Completable.OnComplete -> {
                hideProfilePictureLoading()

                iViewProfile.tag = true
            }
            is Completable.OnError -> {
                hideProfilePictureLoading()

                requireActivity().showSnackBarException(throwable = result.throwable)
            }
        }
    }

    private fun profilePictureObserver(result: Result<String>?) {
        when (result) {
            is Result.OnLoading -> {
                showProfilePictureLoading()
            }
            is Result.OnSuccess -> {
                hideProfilePictureLoading()

                loadProfilePicture(url = result.value)
            }
            is Result.OnError -> {
                hideProfilePictureLoading()

                requireActivity().showSnackBarException(throwable = result.throwable)

                iViewProfile.setImageResource(R.mipmap.ic_launcher_round)
            }
        }
    }

    private fun removeProfilePictureObserver(result: Event<Unit>?) {
        when (result) {
            is Event.OnLoading -> {
                showProfilePictureLoading()
            }
            is Event.OnSuccess -> {
                hideProfilePictureLoading()

                iViewProfile.setImageResource(R.mipmap.ic_launcher_round)

                snackBar {
                    messageResource = R.string.snack_bar_profile_picture_removed
                }
            }
            is Event.OnError -> {
                hideProfilePictureLoading()

                requireActivity().showSnackBarException(throwable = result.throwable)
            }
        }
    }

    private fun showRemoveProfilePictureDialog() {
        alert {
            titleResource = R.string.alert_title_remove_profile_picture_failure
            messageResource = R.string.alert_message_remove_profile_picture_failure

            positiveButton(R.string.yes) {
                viewModel.removeProfilePicture()
            }

            negativeButton(R.string.cancel)
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
        val removeIntent = Intent(ACTION_REMOVE_PROFILE_PICTURE)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(MediaStore.EXTRA_OUTPUT, outputUri)

        val galleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        val chooser = Intent.createChooser(galleryIntent, getString(R.string.label_profile_picture_chooser))

        val hasProfilePicture = iViewProfile.tag as? Boolean ?: false

        val intents = if (hasProfilePicture)
            arrayOf(removeIntent, cameraIntent)
        else
            arrayOf(cameraIntent)

        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents)

        startActivityForResult(chooser, REQUEST_CODE_PROFILE_PICTURE)
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

        loadProfilePicture(url = account.profilePicture)
    }

    private fun loadProfilePicture(url: String) {
        if (url.isNotEmpty()) {
            with(picasso) {
                load(url)
                        .noFade()
                        .transform(CircleTransform())
                        .error(R.mipmap.ic_launcher_round)
                        .into(iViewProfile, loadProfilePicture)
            }
        } else {
            iViewProfile.setImageResource(R.mipmap.ic_launcher_round)

            hideProfilePictureLoading()
        }
    }
}