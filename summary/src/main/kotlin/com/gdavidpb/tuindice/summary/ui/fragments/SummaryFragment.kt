package com.gdavidpb.tuindice.summary.ui.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.domain.usecase.base.Completable
import com.gdavidpb.tuindice.base.domain.usecase.base.Event
import com.gdavidpb.tuindice.base.domain.usecase.base.Result
import com.gdavidpb.tuindice.base.domain.usecase.error.SyncError
import com.gdavidpb.tuindice.base.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.base.ui.dialogs.ConfirmationBottomSheetDialog
import com.gdavidpb.tuindice.base.ui.fragments.NavigationFragment
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.domain.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.mapping.formatLastUpdate
import com.gdavidpb.tuindice.summary.mapping.toCreditsSummaryItem
import com.gdavidpb.tuindice.summary.mapping.toShortName
import com.gdavidpb.tuindice.summary.mapping.toSubjectsSummaryItem
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.adapters.SummaryAdapter
import com.gdavidpb.tuindice.summary.utils.Actions
import com.gdavidpb.tuindice.summary.utils.Extras
import com.gdavidpb.tuindice.summary.utils.RequestCodes
import com.gdavidpb.tuindice.summary.utils.extensions.fileProviderUri
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.fragment_summary.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class SummaryFragment : NavigationFragment() {

	private val mainViewModel by sharedViewModel<MainViewModel>()

	private val viewModel by viewModel<SummaryViewModel>()

	private val picasso by inject<Picasso>()

	private val summaryAdapter = SummaryAdapter()

	private val profilePictureUri by lazy {
		File("profile_picture.jpg")
			.fileProviderUri(requireContext())
	}

	override fun onCreateView() = R.layout.fragment_summary

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		rViewSummary.adapter = summaryAdapter

		vProfilePicture.onClickOnce(::onEditProfilePictureClick)

		requireActivity().addMenuProvider(SummaryMenuProvider(), viewLifecycleOwner)

		with(viewModel) {
			getAccount()
		}
	}

	override fun onInitObservers() {
		with(mainViewModel) {
			observe(sync, ::syncObserver)
		}

		with(viewModel) {
			observe(signOut, ::signOutObserver)
			observe(account, ::accountObserver)
			observe(profilePicture, ::profilePictureObserver)
			observe(removeProfilePicture, ::removeProfilePictureObserver)
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		data ?: return

		if (resultCode == Activity.RESULT_OK) {
			val removeProfilePicture = data.hasExtra(Extras.REMOVE_PROFILE_PICTURE)
			val requestProfilePicture = (requestCode == RequestCodes.PROFILE_PICTURE_REQUEST)

			when {
				removeProfilePicture ->
					showRemoveProfilePictureDialog()
				requestProfilePicture ->
					viewModel.uploadProfilePicture(path = "${data.data ?: profilePictureUri}")
			}
		}
	}

	private fun onEditProfilePictureClick() {
		val removeIntent = Intent(Actions.REMOVE_PROFILE_PICTURE)

		val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
			.putExtra(MediaStore.EXTRA_OUTPUT, profilePictureUri)

		val galleryIntent = Intent(
			Intent.ACTION_PICK,
			MediaStore.Images.Media.EXTERNAL_CONTENT_URI
		)

		val chooser =
			Intent.createChooser(galleryIntent, getString(R.string.label_profile_picture_chooser))

		val hasCamera = hasCamera()
		val hasProfilePicture = vProfilePicture.hasProfilePicture

		val intents = mutableListOf<Intent>().apply {
			if (hasCamera) add(cameraIntent)
			if (hasProfilePicture) add(removeIntent)
		}.toTypedArray()

		if (intents.isNotEmpty())
			chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents)

		startActivityForResult(chooser, RequestCodes.PROFILE_PICTURE_REQUEST)
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

	private fun loadProfile(account: Account) {
		val context = requireContext()

		/* Load account */

		val shortName = account.toShortName()
		val lastUpdate =
			context.getString(R.string.text_last_update, account.lastUpdate.formatLastUpdate())

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
			else -> {}
		}
	}

	private fun syncObserver(result: Result<Boolean, SyncError>?) {
		when (result) {
			is Result.OnSuccess -> {
				val pendingUpdate = result.value

				if (pendingUpdate)
					viewModel.getAccount()
			}
			is Result.OnError -> {
				syncErrorHandler(error = result.error)
			}
			else -> {}
		}
	}

	private fun accountObserver(result: Result<Account, Nothing>?) {
		when (result) {
			is Result.OnSuccess -> {
				loadProfile(account = result.value)
			}
			else -> {}
		}
	}

	private fun profilePictureObserver(result: Event<String, ProfilePictureError>?) {
		when (result) {
			is Event.OnLoading -> {
				vProfilePicture.setLoading(true)
			}
			is Event.OnSuccess -> {
				loadProfilePictureFromUrl(url = result.value)
			}
			is Event.OnError -> {
				vProfilePicture.setLoading(false)

				profilePictureErrorHandler(error = result.error)
			}
			else -> {}
		}
	}

	private fun removeProfilePictureObserver(result: Event<Unit, ProfilePictureError>?) {
		when (result) {
			is Event.OnLoading -> {
				vProfilePicture.setLoading(true)
			}
			is Event.OnSuccess -> {
				vProfilePicture.setLoading(false)
				vProfilePicture.setDrawable(null)

				snackBar(R.string.snack_profile_picture_removed)
			}
			is Event.OnError -> {
				vProfilePicture.setLoading(false)

				profilePictureErrorHandler(error = result.error)
			}
			else -> {}
		}
	}

	private fun loadProfilePictureFromUrl(url: String) {
		with(picasso) {
			invalidate(url)

			load(url)
				.noFade()
				.stableKey(url)
				.into(object : Target {
					override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
						val drawable = bitmap.toDrawable(resources)

						vProfilePicture.setLoading(false)
						vProfilePicture.setDrawable(drawable)

						snackBar(R.string.snack_profile_picture_updated)
					}

					override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
						vProfilePicture.setLoading(false)
						errorSnackBar()
					}

					override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
					}
				})
		}
	}

	private fun syncErrorHandler(error: SyncError?) {
		when (error) {
			is SyncError.Unavailable -> {
				tViewLastUpdate.drawables(start = R.drawable.ic_sync_problem)
				tViewLastUpdate.onClickOnce { snackBar(R.string.snack_no_service) }
			}
			else -> {}
		}
	}

	private fun profilePictureErrorHandler(error: ProfilePictureError?) {
		when (error) {
			is ProfilePictureError.Timeout -> errorSnackBar(R.string.snack_timeout)
			is ProfilePictureError.NoData -> vProfilePicture.setDrawable(null)
			is ProfilePictureError.NoConnection -> connectionSnackBar(error.isNetworkAvailable)
			else -> errorSnackBar()
		}
	}

	inner class SummaryMenuProvider : MenuProvider {
		override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
			menuInflater.inflate(R.menu.menu_summary, menu)
		}

		override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
			return when (menuItem.itemId) {
				R.id.menu_sign_out -> {
					showSignOutDialog()
					true
				}
				else -> false
			}
		}
	}
}