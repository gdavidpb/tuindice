package com.gdavidpb.tuindice.summary.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import com.gdavidpb.tuindice.base.presentation.model.BottomMenuItem
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationBottomSheetDialog
import com.gdavidpb.tuindice.base.ui.dialog.MenuBottomSheetDialog
import com.gdavidpb.tuindice.base.ui.fragment.NavigationFragment
import com.gdavidpb.tuindice.base.utils.extension.bottomSheetDialog
import com.gdavidpb.tuindice.base.utils.extension.hasCamera
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SummaryFragment : NavigationFragment() {

	private val viewModel by viewModel<SummaryViewModel>()

	private object ProfilePictureMenu {
		const val ID_PICK_PICTURE = 0
		const val ID_TAKE_PICTURE = 1
		const val ID_REMOVE_PICTURE = 2
	}

	private val pickVisualMediaRequest =
		PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

	private val registerTakePicture =
		registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
			if (result) viewModel.uploadTakenProfilePictureAction()
		}

	private val registerPickVisualMedia =
		registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { result ->
			if (result != null) viewModel.uploadProfilePictureAction(path = "$result")
		}

	override fun onCreateView(): Int {
		TODO("Not yet implemented")
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		//vProfilePicture.setOnClickListener { onEditProfilePictureClick() }

		requireActivity().addMenuProvider(SummaryMenuProvider(), viewLifecycleOwner)
	}

	private fun pickProfilePicture() {
		registerPickVisualMedia.launch(pickVisualMediaRequest)
	}

	private fun takeProfilePicture(output: String) {
		registerTakePicture.launch(Uri.parse(output))

		viewModel.setCameraOutput(output)
	}

	private fun onEditProfilePictureClick() {
		val items = mutableListOf(
			BottomMenuItem(
				itemId = ProfilePictureMenu.ID_PICK_PICTURE,
				iconResource = R.drawable.ic_upload,
				textResource = R.string.menu_pick_profile_picture
			)
		).apply {
			if (true) //vProfilePicture.hasProfilePicture
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

	private fun onProfilePictureOptionSelected(itemId: Int) {
		when (itemId) {
			ProfilePictureMenu.ID_REMOVE_PICTURE -> {
				bottomSheetDialog<ConfirmationBottomSheetDialog> {
					titleResource = R.string.dialog_title_remove_profile_picture_failure
					messageResource = R.string.dialog_message_remove_profile_picture_failure

					positiveButton(R.string.remove) { viewModel.removeProfilePictureAction() }
					negativeButton(R.string.cancel)
				}
			}

			ProfilePictureMenu.ID_PICK_PICTURE -> {
				viewModel.pickProfilePictureAction()
			}

			ProfilePictureMenu.ID_TAKE_PICTURE -> {
				viewModel.takeProfilePictureAction()
			}
		}
	}

	inner class SummaryMenuProvider : MenuProvider {
		override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
			//menuInflater.inflate(R.menu.menu_summary, menu)
		}

		override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
			return when (menuItem.itemId) {
				0 -> { //R.id.menu_sign_out
					bottomSheetDialog<ConfirmationBottomSheetDialog> {
						titleResource = R.string.dialog_title_sign_out
						messageResource = R.string.dialog_message_sign_out

						//positiveButton(R.string.dialog_button_sign_out) { viewModel.confirmSignOutAction() }
						negativeButton(R.string.cancel)
					}

					true
				}

				else -> false
			}
		}
	}
}