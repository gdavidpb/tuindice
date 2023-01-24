package com.gdavidpb.tuindice.summary.ui.fragments

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.domain.usecase.base.Completable
import com.gdavidpb.tuindice.base.domain.usecase.base.Event
import com.gdavidpb.tuindice.base.domain.usecase.base.Result
import com.gdavidpb.tuindice.base.presentation.model.BottomMenuItem
import com.gdavidpb.tuindice.base.ui.dialogs.ConfirmationBottomSheetDialog
import com.gdavidpb.tuindice.base.ui.dialogs.MenuBottomSheetDialog
import com.gdavidpb.tuindice.base.ui.fragments.NavigationFragment
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.domain.error.GetAccountError
import com.gdavidpb.tuindice.summary.domain.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.mapping.formatLastUpdate
import com.gdavidpb.tuindice.summary.mapping.toCreditsSummaryItem
import com.gdavidpb.tuindice.summary.mapping.toShortName
import com.gdavidpb.tuindice.summary.mapping.toSubjectsSummaryItem
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.adapters.SummaryAdapter
import com.gdavidpb.tuindice.summary.utils.extensions.fileProviderUri
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.fragment_summary.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class SummaryFragment : NavigationFragment() {

	private val viewModel by viewModel<SummaryViewModel>()

	private val picasso by inject<Picasso>()

	private val summaryAdapter = SummaryAdapter()

	private val cameraOutputUri by lazy {
		runCatching {
			File(requireContext().filesDir, "profile_picture.jpg")
				.apply {
					if (exists()) delete()
					createNewFile()
				}
				.fileProviderUri(requireContext())
		}.getOrNull()
	}

	private val registerTakePicture =
		registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
			if (result) viewModel.uploadProfilePicture(path = "$cameraOutputUri")
		}

	private val registerPickVisualMedia =
		registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { result ->
			if (result != null) viewModel.uploadProfilePicture(path = "$result")
		}

	private object ProfilePictureMenu {
		const val ID_PICK_PICTURE = 0
		const val ID_TAKE_PICTURE = 1
		const val ID_REMOVE_PICTURE = 2
	}

	override fun onCreateView() = R.layout.fragment_summary

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		rViewSummary.adapter = summaryAdapter

		vProfilePicture.onClickOnce(::onEditProfilePictureClick)

		requireActivity().addMenuProvider(SummaryMenuProvider(), viewLifecycleOwner)

		viewModel.getAccount()
	}

	override fun onInitObservers() {
		with(viewModel) {
			observe(signOut, ::signOutObserver)
			observe(account, ::accountObserver)
			observe(uploadProfilePicture, ::uploadProfilePictureObserver)
			observe(removeProfilePicture, ::removeProfilePictureObserver)
		}
	}

	private fun onProfilePictureOptionSelected(itemId: Int) {
		when (itemId) {
			ProfilePictureMenu.ID_REMOVE_PICTURE -> {
				bottomSheetDialog<ConfirmationBottomSheetDialog> {
					titleResource = R.string.dialog_title_remove_profile_picture_failure
					messageResource = R.string.dialog_message_remove_profile_picture_failure

					positiveButton(R.string.remove) { viewModel.removeProfilePicture() }
					negativeButton(R.string.cancel)
				}
			}
			ProfilePictureMenu.ID_PICK_PICTURE -> {
				val input =
					PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

				registerPickVisualMedia.launch(input)
			}
			ProfilePictureMenu.ID_TAKE_PICTURE -> {
				val input = cameraOutputUri

				if (input != null)
					registerTakePicture.launch(input)
				else
					errorSnackBar()
			}
		}
	}

	private fun onEditProfilePictureClick() {
		val items = mutableListOf(
			BottomMenuItem(
				itemId = ProfilePictureMenu.ID_PICK_PICTURE,
				iconResource = R.drawable.ic_upload,
				textResource = R.string.menu_pick_profile_picture
			)
		).apply {
			if (vProfilePicture.hasProfilePicture)
				add(
					0,
					BottomMenuItem(
						itemId = ProfilePictureMenu.ID_REMOVE_PICTURE,
						iconResource = R.drawable.ic_delete,
						iconColor = R.color.color_error,
						textResource = R.string.menu_remove_profile_picture,
						textColor = R.color.color_error
					)
				)

			if (hasCamera())
				add(
					BottomMenuItem(
						itemId = ProfilePictureMenu.ID_TAKE_PICTURE,
						iconResource = R.drawable.ic_camera,
						textResource = R.string.menu_take_profile_picture
					)
				)
		}

		bottomSheetDialog<MenuBottomSheetDialog> {
			titleResource = R.string.title_menu_profile_picture

			setItems(items) { itemId ->
				onProfilePictureOptionSelected(itemId)
			}
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
		tViewLastUpdate.drawables(start = R.drawable.ic_sync)

		tViewLastUpdate.isVisible = true

		if (account.grade > 0.0) {
			tViewGrade.isVisible = true
			tViewGrade.animateGrade(value = account.grade.toFloat())
		} else
			tViewGrade.isVisible = false

		loadProfilePictureFromUrl(url = account.pictureUrl)

		/* Load summary */

		val subjectsSummary = account.toSubjectsSummaryItem(context)
		val creditsSummary = account.toCreditsSummaryItem(context)

		val items = listOf(subjectsSummary, creditsSummary)

		summaryAdapter.submitSummary(items)
	}

	private fun loadProfilePictureFromUrl(url: String?) {
		if (url == null) {
			vProfilePicture.setLoading(false)
			vProfilePicture.setDrawable(null)

			return
		}

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
					}

					override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
						vProfilePicture.setLoading(false)
						errorSnackBar()
					}

					override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
						vProfilePicture.setLoading(true)
					}
				})
		}
	}

	private fun navigateToSignIn() {
		findNavController().popStackToRoot()

		navigate(SummaryFragmentDirections.navToSignIn())
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

	private fun accountObserver(result: Result<Account, GetAccountError>?) {
		when (result) {
			is Result.OnLoading -> {
				pBarSummary.isVisible = true
			}
			is Result.OnSuccess -> {
				pBarSummary.isVisible = false

				loadProfile(account = result.value)
			}
			is Result.OnError -> {
				pBarSummary.isVisible = false

				accountErrorHandler(error = result.error)
			}
			else -> {}
		}
	}

	private fun uploadProfilePictureObserver(result: Event<String, ProfilePictureError>?) {
		when (result) {
			is Event.OnLoading -> vProfilePicture.setLoading(true)
			is Event.OnSuccess -> {
				loadProfilePictureFromUrl(url = result.value)

				snackBar(R.string.snack_profile_picture_updated)
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
			is Event.OnLoading -> vProfilePicture.setLoading(true)
			is Event.OnSuccess -> {
				loadProfilePictureFromUrl(null)

				snackBar(R.string.snack_profile_picture_removed)
			}
			is Event.OnError -> {
				vProfilePicture.setLoading(false)

				profilePictureErrorHandler(error = result.error)
			}
			else -> {}
		}
	}

	private fun accountErrorHandler(error: GetAccountError?) {
		when (error) {
			is GetAccountError.AccountDisabled -> viewModel.signOut()
			is GetAccountError.NoConnection -> connectionSnackBar(error.isNetworkAvailable) { viewModel.getAccount() }
			is GetAccountError.OutdatedPassword -> navigate(SummaryFragmentDirections.navToUpdatePassword())
			is GetAccountError.Timeout -> errorSnackBar(R.string.snack_timeout) { viewModel.getAccount() }
			is GetAccountError.Unavailable -> showCantUpdate()
			null -> errorSnackBar()
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

	private fun showCantUpdate() {
		tViewLastUpdate.drawables(start = R.drawable.ic_sync_problem)
		tViewLastUpdate.onClickOnce { snackBar(R.string.snack_no_service) }
	}

	inner class SummaryMenuProvider : MenuProvider {
		override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
			menuInflater.inflate(R.menu.menu_summary, menu)
		}

		override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
			return when (menuItem.itemId) {
				R.id.menu_sign_out -> {
					bottomSheetDialog<ConfirmationBottomSheetDialog> {
						titleResource = R.string.dialog_title_sign_out
						messageResource = R.string.dialog_message_sign_out

						positiveButton(R.string.menu_sign_out) { viewModel.signOut() }
						negativeButton(R.string.cancel)
					}

					true
				}
				else -> false
			}
		}
	}
}