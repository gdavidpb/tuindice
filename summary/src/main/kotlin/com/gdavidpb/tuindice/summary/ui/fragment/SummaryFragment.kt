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
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.base.NavigationBaseDirections
import com.gdavidpb.tuindice.base.presentation.model.BottomMenuItem
import com.gdavidpb.tuindice.base.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationBottomSheetDialog
import com.gdavidpb.tuindice.base.ui.dialog.MenuBottomSheetDialog
import com.gdavidpb.tuindice.base.ui.fragment.NavigationFragment
import com.gdavidpb.tuindice.base.utils.extension.bottomSheetDialog
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.base.utils.extension.connectionSnackBar
import com.gdavidpb.tuindice.base.utils.extension.drawables
import com.gdavidpb.tuindice.base.utils.extension.errorSnackBar
import com.gdavidpb.tuindice.base.utils.extension.hasCamera
import com.gdavidpb.tuindice.base.utils.extension.launchRepeatOnLifecycle
import com.gdavidpb.tuindice.base.utils.extension.snackBar
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.model.SummaryViewState
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.adapter.SummaryAdapter
import kotlinx.android.synthetic.main.fragment_summary.fViewSummary
import kotlinx.android.synthetic.main.fragment_summary.pBarSummary
import kotlinx.android.synthetic.main.fragment_summary.rViewSummary
import kotlinx.android.synthetic.main.fragment_summary.tViewCareer
import kotlinx.android.synthetic.main.fragment_summary.tViewGrade
import kotlinx.android.synthetic.main.fragment_summary.tViewLastUpdate
import kotlinx.android.synthetic.main.fragment_summary.tViewName
import kotlinx.android.synthetic.main.fragment_summary.vProfilePicture
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SummaryFragment : NavigationFragment() {

	private val mainViewModel by sharedViewModel<MainViewModel>()
	private val viewModel by viewModel<SummaryViewModel>()

	private val summaryAdapter = SummaryAdapter()

	private object Flipper {
		const val CONTENT = 0
		const val LOADING = 1
		// TODO const val FAILED = 2
	}

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

	override fun onCreateView() = R.layout.fragment_summary

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		rViewSummary.adapter = summaryAdapter

		vProfilePicture.setOnClickListener { onEditProfilePictureClick() }

		requireActivity().addMenuProvider(SummaryMenuProvider(), viewLifecycleOwner)

		launchRepeatOnLifecycle {
			with(viewModel) {
				collect(viewState, ::stateCollector)
				collect(viewEvent, ::eventCollector)
			}
		}

		viewModel.loadSummaryAction()
	}

	private fun stateCollector(state: Summary.State) {
		when (state) {
			is Summary.State.Loading -> fViewSummary.displayedChild = Flipper.LOADING
			is Summary.State.Loaded -> loadSummaryViewState(value = state.value)
			is Summary.State.Failed -> TODO()
		}
	}

	private fun eventCollector(event: Summary.Event) {
		when (event) {
			is Summary.Event.NavigateToAccountDisabled -> navigateToAccountDisabled()
			is Summary.Event.NavigateToOutdatedPassword -> navigateToOutdatedPassword()
			is Summary.Event.OpenCamera -> takeProfilePicture(event.output)
			is Summary.Event.OpenPicker -> pickProfilePicture()
			is Summary.Event.ShowProfilePictureUpdatedSnackBar -> snackBar(R.string.snack_profile_picture_updated)
			is Summary.Event.ShowProfilePictureRemovedSnackBar -> snackBar(R.string.snack_profile_picture_removed)
			is Summary.Event.ShowTryLaterSnackBar -> snackBar(R.string.snack_no_service)
			is Summary.Event.ShowTimeoutSnackBar -> errorSnackBar(R.string.snack_timeout)
			is Summary.Event.ShowNoConnectionSnackBar -> connectionSnackBar(event.isNetworkAvailable)
			is Summary.Event.ShowDefaultErrorSnackBar -> errorSnackBar()
		}
	}

	private fun loadSummaryViewState(value: SummaryViewState) {
		tViewName.text = value.name
		tViewLastUpdate.text = value.lastUpdate
		tViewCareer.text = value.careerName

		if (value.isUpdated) {
			tViewLastUpdate.drawables(start = R.drawable.ic_sync)
			tViewLastUpdate.setOnClickListener(null)
		} else {
			tViewLastUpdate.drawables(start = R.drawable.ic_sync_problem)
			tViewLastUpdate.setOnClickListener { onLastUpdateClick() }
		}

		tViewGrade.isVisible = value.isGradeVisible

		if (value.isGradeVisible) tViewGrade.animateGrade(value = value.grade)

		vProfilePicture.loadImage(
			url = value.profilePictureUrl,
			lifecycleOwner = viewLifecycleOwner
		)

		summaryAdapter.submitSummary(items = value.items)

		vProfilePicture.showLoading(value = value.isProfilePictureLoading)

		pBarSummary.isVisible = value.isUpdating

		fViewSummary.displayedChild = if (value.isLoading) Flipper.LOADING else Flipper.CONTENT
	}

	private fun navigateToAccountDisabled() {
		mainViewModel.signOut()
	}

	private fun navigateToOutdatedPassword() {
		navigate(NavigationBaseDirections.navToUpdatePassword())
	}

	private fun pickProfilePicture() {
		registerPickVisualMedia.launch(pickVisualMediaRequest)
	}

	private fun takeProfilePicture(output: String) {
		registerTakePicture.launch(Uri.parse(output))

		viewModel.setCameraOutput(output)
	}

	private fun onLastUpdateClick() {
		viewModel.showTryLaterAction()
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
			menuInflater.inflate(R.menu.menu_summary, menu)
		}

		override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
			return when (menuItem.itemId) {
				R.id.menu_sign_out -> {
					bottomSheetDialog<ConfirmationBottomSheetDialog> {
						titleResource = R.string.dialog_title_sign_out
						messageResource = R.string.dialog_message_sign_out

						positiveButton(R.string.menu_sign_out) { mainViewModel.signOut() }
						negativeButton(R.string.cancel)
					}

					true
				}

				else -> false
			}
		}
	}
}