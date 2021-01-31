package com.gdavidpb.tuindice.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.usecase.coroutines.Completable
import com.gdavidpb.tuindice.domain.usecase.coroutines.Event
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.errors.SyncError
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

open class SummaryFragment : NavigationFragment() {

    private val mainViewModel by sharedViewModel<MainViewModel>()

    private val viewModel by viewModel<SummaryViewModel>()

    private val picasso by inject<Picasso>()

    private val loadProfilePicture = LiveCompletable<Any>() //todo define error

    private val summaryAdapter = SummaryAdapter()

    override fun onCreateView() = R.layout.fragment_summary

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

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
            observe(getProfilePictureFile, ::getProfilePictureFileObserver)
            observe(createProfilePictureFile, ::createProfilePictureFileObserver)

            observe(signOut, ::signOutObserver)
            observe(profile, ::profileObserver)
            observe(profilePicture, ::profilePictureObserver)
            observe(loadProfilePicture, ::loadProfilePictureObserver)
            observe(updateProfilePicture, ::updateProfilePictureObserver)
            observe(removeProfilePicture, ::removeProfilePictureObserver)

            getProfile()
            getProfilePicture()
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

    private fun signOutObserver(result: Completable<Any>?) {
        when (result) {
            is Completable.OnComplete -> {
                navigateToLogin()
            }
            is Completable.OnError -> {
                clearApplicationUserData()

                navigateToLogin()
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
        }
    }

    private fun profileObserver(result: Result<Account, Nothing>?) {
        when (result) {
            is Result.OnSuccess -> {
                loadAccount(account = result.value)
            }
        }
    }

    private fun getProfilePictureFileObserver(result: Event<Uri, Any>?) {
        when (result) {
            is Event.OnSuccess -> {
                val outputUri = result.value

                viewModel.updateProfilePicture(outputUri)
            }
            is Event.OnError -> {
                //todo requireActivity().showSnackBarException(throwable = result.throwable)
            }
        }
    }

    private fun createProfilePictureFileObserver(result: Event<Uri, Any>?) {
        when (result) {
            is Event.OnSuccess -> {
                val outputUri = result.value
                        .fileProviderUri(requireContext())

                requestProfilePictureInput(outputUri)
            }
            is Event.OnError -> {
                //todo requireActivity().showSnackBarException(throwable = result.throwable)
            }
        }
    }

    private fun updateProfilePictureObserver(result: Event<String, Any>?) {
        when (result) {
            is Event.OnLoading -> {
                showProfilePictureLoading()
            }
            is Event.OnSuccess -> {
                loadProfilePicture(url = result.value, invalidate = true)

                snackBar {
                    messageResource = R.string.snack_bar_profile_picture_updated
                }
            }
            is Event.OnError -> {
                hideProfilePictureLoading()

                //todo requireActivity().showSnackBarException(throwable = result.throwable)
            }
        }
    }

    private fun loadProfilePictureObserver(result: Completable<Any>?) {
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

                //todo requireActivity().showSnackBarException(throwable = result.throwable)
            }
        }
    }

    private fun profilePictureObserver(result: Result<String, Any>?) {
        when (result) {
            is Result.OnLoading -> {
                showProfilePictureLoading()
            }
            is Result.OnSuccess -> {
                loadProfilePicture(url = result.value, invalidate = false)
            }
            is Result.OnError -> {
                hideProfilePictureLoading()

                //todo requireActivity().showSnackBarException(throwable = result.throwable)

                iViewProfile.setImageResource(R.mipmap.ic_launcher_round)
            }
        }
    }

    private fun navigateToLogin() {
        findNavController().popStackToRoot()

        SummaryFragmentDirections.navToLogin().let(::navigate)
    }

    private fun removeProfilePictureObserver(result: Event<Unit, Any>?) {
        when (result) {
            is Event.OnLoading -> {
                showProfilePictureLoading()
            }
            is Event.OnSuccess -> {
                hideProfilePictureLoading()

                iViewProfile.tag = false

                iViewProfile.setImageResource(R.mipmap.ic_launcher_round)

                snackBar {
                    messageResource = R.string.snack_bar_profile_picture_removed
                }
            }
            is Event.OnError -> {
                hideProfilePictureLoading()

                //todo requireActivity().showSnackBarException(throwable = result.throwable)
            }
        }
    }

    private fun showSignOutDialog() {
        alert {
            titleResource = R.string.alert_title_sign_out
            messageResource = R.string.alert_message_sign_out

            positiveButton(R.string.yes) {
                viewModel.signOut()
            }

            negativeButton(R.string.cancel)
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

        arrayOf(iViewProfile, iViewEditProfile).forEach { view ->
            view.disable()
            view.animateScaleDown()
        }
    }

    private fun hideProfilePictureLoading() {
        pBarProfile.invisible()

        arrayOf(iViewProfile, iViewEditProfile).forEach { view ->
            view.enable()
            view.animateScaleUp()
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

        val shortName = account.toShortName()
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
    }

    private fun loadProfilePicture(url: String, invalidate: Boolean) {
        if (url.isNotEmpty()) {
            with(picasso) {
                if (invalidate) invalidate(url)

                load(url)
                        .noFade()
                        .stableKey(url)
                        .transform(CircleTransform())
                        .error(R.mipmap.ic_launcher_round)
                        .into(iViewProfile, loadProfilePicture)
            }
        } else {
            hideProfilePictureLoading()

            iViewProfile.setImageResource(R.mipmap.ic_launcher_round)
        }
    }
}