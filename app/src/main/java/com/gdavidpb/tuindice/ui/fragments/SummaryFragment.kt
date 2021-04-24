package com.gdavidpb.tuindice.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.usecase.coroutines.Completable
import com.gdavidpb.tuindice.domain.usecase.coroutines.Event
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.errors.ProfilePictureError
import com.gdavidpb.tuindice.domain.usecase.errors.SyncError
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.ui.adapters.SummaryAdapter
import com.gdavidpb.tuindice.ui.dialogs.ConfirmationBottomSheetDialog
import com.gdavidpb.tuindice.utils.Actions
import com.gdavidpb.tuindice.utils.Extras
import com.gdavidpb.tuindice.utils.RequestCodes
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.toCreditsSummaryItem
import com.gdavidpb.tuindice.utils.mappers.toSubjectsSummaryItem
import kotlinx.android.synthetic.main.fragment_summary.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SummaryFragment : NavigationFragment() {

    private val mainViewModel by sharedViewModel<MainViewModel>()

    private val viewModel by viewModel<SummaryViewModel>()

    private val loadProfilePicture = LiveCompletable<ProfilePictureError>()

    private val summaryAdapter = SummaryAdapter()

    override fun onCreateView() = R.layout.fragment_summary

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        rViewSummary.adapter = summaryAdapter

        vProfilePicture.onClickOnce(::onEditProfilePictureClick)

        with(viewModel) {
            getProfile()
            getProfilePicture()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        with(mainViewModel) {
            observe(sync, ::syncObserver)
        }

        with(viewModel) {
            observe(getProfilePictureFile, ::getProfilePictureFileObserver)
            observe(createProfilePictureFile, ::createProfilePictureFileObserver)

            observe(signOut, ::signOutObserver)
            observe(profile, ::profileObserver)
            observe(profilePicture, ::profilePictureObserver)
            observe(loadProfilePicture, ::loadProfilePictureObserver)
            observe(updateProfilePicture, ::updateProfilePictureObserver)
            observe(removeProfilePicture, ::removeProfilePictureObserver)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_summary, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_sign_out -> {
                showSignOutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data ?: return

        if (resultCode == Activity.RESULT_OK) {
            val removeProfilePicture = data.hasExtra(Extras.REMOVE_PROFILE_PICTURE)
            val requestProfilePicture = (requestCode == RequestCodes.PROFILE_PICTURE)

            when {
                removeProfilePicture -> showRemoveProfilePictureDialog()
                requestProfilePicture -> viewModel.getProfilePictureFile(optionalUri = data.data)
            }
        }
    }

    private fun onEditProfilePictureClick() {
        viewModel.createProfilePictureFile()
    }

    private fun navigateToSignIn() {
        findNavController().popStackToRoot()

        navigate(SummaryFragmentDirections.navToSignIn())
    }

    private fun showSignOutDialog() {
        bottomSheetDialog<ConfirmationBottomSheetDialog> {
            titleResource = R.string.dialog_title_sign_out
            messageResource = R.string.dialog_message_sign_out

            positiveButton(R.string.menu_sign_out) { viewModel.signOut() }
            negativeButton(R.string.cancel)
        }
    }

    private fun showRemoveProfilePictureDialog() {
        bottomSheetDialog<ConfirmationBottomSheetDialog> {
            titleResource = R.string.dialog_title_remove_profile_picture_failure
            messageResource = R.string.dialog_message_remove_profile_picture_failure

            positiveButton(R.string.remove) { viewModel.removeProfilePicture() }
            negativeButton(R.string.cancel)
        }
    }

    private fun requestProfilePictureInput(outputUri: Uri) {
        val removeIntent = Intent(Actions.REMOVE_PROFILE_PICTURE)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(MediaStore.EXTRA_OUTPUT, outputUri)

        val galleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        val chooser = Intent.createChooser(galleryIntent, getString(R.string.label_profile_picture_chooser))

        val hasCamera = packageManager.hasCamera()
        val hasProfilePicture = vProfilePicture.hasProfilePicture

        val intents = mutableListOf<Intent>().apply {
            if (hasCamera) add(cameraIntent)
            if (hasProfilePicture) add(removeIntent)
        }.toTypedArray()

        if (intents.isNotEmpty())
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents)

        startActivityForResult(chooser, RequestCodes.PROFILE_PICTURE)
    }

    private fun loadProfile(account: Account) {
        val context = requireContext()

        /* Load account */

        val shortName = account.toShortName()
        val lastUpdate = context.getString(R.string.text_last_update, account.lastUpdate.formatLastUpdate())

        tViewName.text = shortName
        tViewCareer.text = account.careerName

        tViewLastUpdate.text = lastUpdate
        tViewLastUpdate.isVisible = true

        if (account.grade > 0.0) {
            tViewGrade.isVisible = true
            tViewGrade.animateGrade(value = account.grade.toFloat())
        } else
            tViewGrade.isVisible = false

        /* Load summary */

        val subjectsSummary = account.toSubjectsSummaryItem(context)
        val creditsSummary = account.toCreditsSummaryItem(context)

        val items = listOf(subjectsSummary, creditsSummary)

        summaryAdapter.submitSummary(items)
    }

    private fun signOutObserver(result: Completable<Nothing>?) {
        when (result) {
            is Completable.OnComplete -> {
                navigateToSignIn()
            }
            is Completable.OnError -> {
                requireActivity().recreate()
            }
        }
    }

    private fun syncObserver(result: Result<Boolean, SyncError>?) {
        when (result) {
            is Result.OnSuccess -> {
                val pendingUpdate = result.value

                if (pendingUpdate)
                    viewModel.getProfile()
            }
            is Result.OnError -> {
                syncErrorHandler(error = result.error)
            }
        }
    }

    private fun profileObserver(result: Result<Account, Nothing>?) {
        when (result) {
            is Result.OnSuccess -> {
                val account = result.value

                loadProfile(account)
            }
        }
    }

    private fun getProfilePictureFileObserver(result: Event<Uri, Nothing>?) {
        when (result) {
            is Event.OnSuccess -> {
                val outputUri = result.value

                viewModel.updateProfilePicture(outputUri)
            }
        }
    }

    private fun createProfilePictureFileObserver(result: Event<Uri, ProfilePictureError>?) {
        when (result) {
            is Event.OnSuccess -> {
                val outputUri = result.value
                        .fileProviderUri(requireContext())

                requestProfilePictureInput(outputUri)
            }
            is Event.OnError -> {
                profilePictureErrorHandler(error = result.error)
            }
        }
    }

    private fun updateProfilePictureObserver(result: Event<String, ProfilePictureError>?) {
        when (result) {
            is Event.OnLoading -> {
                vProfilePicture.setLoading(true)
            }
            is Event.OnSuccess -> {
                vProfilePicture.loadProfilePicture(
                        url = result.value,
                        invalidate = true,
                        liveData = loadProfilePicture
                )

                snackBar(R.string.snack_profile_picture_updated)
            }
            is Event.OnError -> {
                vProfilePicture.setLoading(false)

                profilePictureErrorHandler(error = result.error)
            }
        }
    }

    private fun loadProfilePictureObserver(result: Completable<ProfilePictureError>?) {
        when (result) {
            is Completable.OnLoading -> {
                vProfilePicture.setLoading(true)
            }
            is Completable.OnComplete -> {
                vProfilePicture.setLoading(false)
            }
            is Completable.OnError -> {
                vProfilePicture.setLoading(false)

                profilePictureErrorHandler(error = result.error)
            }
        }
    }

    private fun profilePictureObserver(result: Result<String, ProfilePictureError>?) {
        when (result) {
            is Result.OnLoading -> {
                vProfilePicture.setLoading(true)
            }
            is Result.OnSuccess -> {
                vProfilePicture.loadProfilePicture(
                        url = result.value,
                        invalidate = false,
                        liveData = loadProfilePicture
                )
            }
            is Result.OnError -> {
                vProfilePicture.setLoading(false)
                vProfilePicture.loadDefaultProfilePicture()

                profilePictureErrorHandler(error = result.error)
            }
        }
    }

    private fun removeProfilePictureObserver(result: Event<Unit, ProfilePictureError>?) {
        when (result) {
            is Event.OnLoading -> {
                vProfilePicture.setLoading(true)
            }
            is Event.OnSuccess -> {
                vProfilePicture.setLoading(false)
                vProfilePicture.loadDefaultProfilePicture()

                snackBar(R.string.snack_profile_picture_removed)
            }
            is Event.OnError -> {
                vProfilePicture.setLoading(false)

                profilePictureErrorHandler(error = result.error)
            }
        }
    }

    private fun syncErrorHandler(error: SyncError?) {
        when (error) {
            is SyncError.NoConnection -> {
                if (error.isNetworkAvailable) {
                    tViewLastUpdate.drawables(start = R.drawable.ic_sync_problem)
                    tViewLastUpdate.onClickOnce { snackBar(R.string.snack_no_service) }
                }
            }
        }
    }

    private fun profilePictureErrorHandler(error: ProfilePictureError?) {
        when (error) {
            is ProfilePictureError.Timeout -> errorSnackBar(R.string.snack_timeout)
            is ProfilePictureError.NoData -> vProfilePicture.loadDefaultProfilePicture()
            is ProfilePictureError.NoConnection -> connectionSnackBar(error.isNetworkAvailable)
            else -> errorSnackBar()
        }
    }
}