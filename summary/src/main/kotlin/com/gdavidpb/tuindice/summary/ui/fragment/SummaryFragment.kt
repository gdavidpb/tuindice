package com.gdavidpb.tuindice.summary.ui.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.domain.usecase.baseV2.UseCaseState
import com.gdavidpb.tuindice.base.presentation.model.BottomMenuItem
import com.gdavidpb.tuindice.base.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationBottomSheetDialog
import com.gdavidpb.tuindice.base.ui.dialog.MenuBottomSheetDialog
import com.gdavidpb.tuindice.base.ui.fragment.NavigationFragment
import com.gdavidpb.tuindice.base.utils.extension.*
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.domain.error.GetAccountError
import com.gdavidpb.tuindice.summary.domain.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.presentation.mapper.formatLastUpdate
import com.gdavidpb.tuindice.summary.presentation.mapper.toCreditsSummaryItem
import com.gdavidpb.tuindice.summary.presentation.mapper.toShortName
import com.gdavidpb.tuindice.summary.presentation.mapper.toSubjectsSummaryItem
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.adapter.SummaryAdapter
import com.gdavidpb.tuindice.summary.utils.extension.fileProviderUri
import kotlinx.android.synthetic.main.fragment_summary.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class SummaryFragment : NavigationFragment() {

	private val mainViewModel by sharedViewModel<MainViewModel>()

	private val viewModel by viewModel<SummaryViewModel>()

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
			if (result) uploadProfilePicture(path = "$cameraOutputUri")
		}

	private val registerPickVisualMedia =
		registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { result ->
			if (result != null) uploadProfilePicture(path = "$result")
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

		launchRepeatOnLifecycle {
			with(viewModel) {
				collect(getAccount, ::getAccountCollector)
				collect(removeProfilePicture, ::removeProfilePictureCollector)
				collect(uploadProfilePicture, ::uploadProfilePictureCollector)
			}
		}
	}

	private fun onProfilePictureOptionSelected(itemId: Int) {
		when (itemId) {
			ProfilePictureMenu.ID_REMOVE_PICTURE -> {
				bottomSheetDialog<ConfirmationBottomSheetDialog> {
					titleResource = R.string.dialog_title_remove_profile_picture_failure
					messageResource = R.string.dialog_message_remove_profile_picture_failure

					positiveButton(R.string.remove) { removeProfilePicture() }
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
		val shortName = account.toShortName()
		val lastUpdate = getString(R.string.text_last_update, account.lastUpdate.formatLastUpdate())

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

		vProfilePicture.loadImage(url = account.pictureUrl, lifecycleOwner = this)
	}

	private fun loadSummary(account: Account) {
		val context = requireContext()

		val subjectsSummary = account.toSubjectsSummaryItem(context)
		val creditsSummary = account.toCreditsSummaryItem(context)

		val items = listOf(subjectsSummary, creditsSummary)

		summaryAdapter.submitSummary(items)
	}

	private fun getAccountCollector(result: UseCaseState<Account, GetAccountError>?) {
		when (result) {
			is UseCaseState.Loading -> {
				pBarSummary.isVisible = true
			}
			is UseCaseState.Data -> {
				pBarSummary.isVisible = false

				val account = result.value

				loadProfile(account)
				loadSummary(account)
			}
			is UseCaseState.Error -> {
				pBarSummary.isVisible = false

				accountErrorHandler(error = result.error)
			}
			else -> {}
		}
	}

	private fun removeProfilePictureCollector(result: UseCaseState<Unit, ProfilePictureError>?) {
		when (result) {
			is UseCaseState.Loading -> {
				vProfilePicture.setLoading(true)
			}
			is UseCaseState.Data -> {
				snackBar(R.string.snack_profile_picture_removed)
			}
			is UseCaseState.Error -> {
				vProfilePicture.setLoading(false)

				profilePictureErrorHandler(error = result.error)
			}
			else -> {}
		}
	}

	private fun uploadProfilePictureCollector(result: UseCaseState<String, ProfilePictureError>?) {
		when (result) {
			is UseCaseState.Loading -> {
				vProfilePicture.setLoading(true)
			}
			is UseCaseState.Data -> {
				snackBar(R.string.snack_profile_picture_updated)
			}
			is UseCaseState.Error -> {
				vProfilePicture.setLoading(false)

				profilePictureErrorHandler(error = result.error)
			}
			else -> {}
		}
	}

	private fun accountErrorHandler(error: GetAccountError?) {
		when (error) {
			is GetAccountError.AccountDisabled -> signOut()
			is GetAccountError.NoConnection -> connectionSnackBar(error.isNetworkAvailable)
			is GetAccountError.OutdatedPassword -> outdatedPassword()
			is GetAccountError.Timeout -> errorSnackBar(R.string.snack_timeout)
			is GetAccountError.Unavailable -> showCantUpdate()
			null -> errorSnackBar()
		}
	}

	private fun profilePictureErrorHandler(error: ProfilePictureError?) {
		when (error) {
			is ProfilePictureError.Timeout -> errorSnackBar(R.string.snack_timeout)
			is ProfilePictureError.NoConnection -> connectionSnackBar(error.isNetworkAvailable)
			else -> errorSnackBar()
		}
	}

	private fun showCantUpdate() {
		tViewLastUpdate.drawables(start = R.drawable.ic_sync_problem)
		tViewLastUpdate.onClickOnce { snackBar(R.string.snack_no_service) }
	}

	private fun uploadProfilePicture(path: String) {
		requestOn(viewModel) {
			uploadProfilePictureParams.emit(path)
		}
	}

	private fun removeProfilePicture() {
		requestOn(viewModel) {
			removeProfilePictureParams.emit(Unit)
		}
	}

	private fun signOut() {
		requestOn(mainViewModel) {
			signOutParams.emit(Unit)
		}
	}

	private fun outdatedPassword() {
		requestOn(mainViewModel) {
			outdatedPassword.emit(Unit)
		}
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

						positiveButton(R.string.menu_sign_out) { signOut() }
						negativeButton(R.string.cancel)
					}

					true
				}
				else -> false
			}
		}
	}
}