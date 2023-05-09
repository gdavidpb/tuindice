package com.gdavidpb.tuindice.summary.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ViewFlipper
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.base.NavigationBaseDirections
import androidx.recyclerview.widget.RecyclerView
import com.gdavidpb.tuindice.base.presentation.model.BottomMenuItem
import com.gdavidpb.tuindice.base.ui.custom.ErrorView
import com.gdavidpb.tuindice.base.ui.custom.TopProgressBar
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
import com.gdavidpb.tuindice.base.utils.extension.view
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.model.SummaryViewState
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.adapter.SummaryAdapter
import com.gdavidpb.tuindice.summary.ui.custom.GradeTextView
import com.gdavidpb.tuindice.summary.ui.custom.ProfilePictureView
import com.google.android.material.textview.MaterialTextView
import org.koin.androidx.viewmodel.ext.android.viewModel

class SummaryFragment : NavigationFragment() {

	private val pBarSummary by view<TopProgressBar>(R.id.pBarSummary)
	private val fViewSummary by view<ViewFlipper>(R.id.fViewSummary)
	private val vProfilePicture by view<ProfilePictureView>(R.id.vProfilePicture)
	private val tViewGrade by view<GradeTextView>(R.id.tViewGrade)
	private val tViewName by view<MaterialTextView>(R.id.tViewName)
	private val tViewCareer by view<MaterialTextView>(R.id.tViewCareer)
	private val tViewLastUpdate by view<MaterialTextView>(R.id.tViewLastUpdate)
	private val rViewSummary by view<RecyclerView>(R.id.rViewSummary)
	private val eViewSummary by view<ErrorView>(R.id.eViewSummary)

	private val viewModel by viewModel<SummaryViewModel>()

	private val summaryAdapter = SummaryAdapter()

	private object Flipper {
		const val CONTENT = 0
		const val LOADING = 1
		const val FAILED = 2
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

		eViewSummary.setOnRetryClick { initialLoad() }
		vProfilePicture.setOnClickListener { onEditProfilePictureClick() }

		requireActivity().addMenuProvider(SummaryMenuProvider(), viewLifecycleOwner)

		launchRepeatOnLifecycle {
			with(viewModel) {
				collect(viewState, ::stateCollector)
				collect(viewEvent, ::eventCollector)
			}
		}

		initialLoad()
	}

	private fun stateCollector(state: Summary.State) {
		when (state) {
			is Summary.State.Loading -> fViewSummary.displayedChild = Flipper.LOADING
			is Summary.State.Loaded -> loadSummaryViewState(value = state.value)
			is Summary.State.Failed -> fViewSummary.displayedChild = Flipper.FAILED
		}
	}

	private fun eventCollector(event: Summary.Event) {
		when (event) {
			is Summary.Event.NavigateToOutdatedPassword -> navigateToOutdatedPassword()
			is Summary.Event.NavigateToSignIn -> navigateToSignIn()
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

	private fun initialLoad() {
		viewModel.loadSummaryAction()
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

	private fun navigateToOutdatedPassword() {
		navigate(NavigationBaseDirections.navToUpdatePassword())
	}

	private fun navigateToSignIn() {
		navigate(NavigationBaseDirections.navToSignIn())
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

						positiveButton(R.string.menu_sign_out) { viewModel.signOutAction() }
						negativeButton(R.string.cancel)
					}

					true
				}

				else -> false
			}
		}
	}
}