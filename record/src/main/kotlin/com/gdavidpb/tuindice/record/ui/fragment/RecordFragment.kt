package com.gdavidpb.tuindice.record.ui.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.base.NavigationBaseDirections
import com.gdavidpb.tuindice.base.presentation.model.BottomMenuItem
import com.gdavidpb.tuindice.base.ui.dialog.MenuBottomSheetDialog
import com.gdavidpb.tuindice.base.ui.fragment.NavigationFragment
import com.gdavidpb.tuindice.base.utils.extension.bottomSheetDialog
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.base.utils.extension.connectionSnackBar
import com.gdavidpb.tuindice.base.utils.extension.errorSnackBar
import com.gdavidpb.tuindice.base.utils.extension.launchRepeatOnLifecycle
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.domain.usecase.param.UpdateSubjectParams
import com.gdavidpb.tuindice.record.domain.usecase.param.WithdrawSubjectParams
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.model.RecordViewState
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.record.ui.adapter.QuarterAdapter
import kotlinx.android.synthetic.main.fragment_record.eViewRecord
import kotlinx.android.synthetic.main.fragment_record.fViewRecord
import kotlinx.android.synthetic.main.fragment_record.pBarRecord
import kotlinx.android.synthetic.main.fragment_record.rViewRecord
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecordFragment : NavigationFragment() {

	private val viewModel by viewModel<RecordViewModel>()

	private val quarterManager = QuarterManager()

	private val quarterAdapter = QuarterAdapter(manager = quarterManager)

	private object Flipper {
		const val CONTENT = 0
		const val EMPTY = 1
		const val LOADING = 2
		const val FAILED = 3
	}

	private object SubjectMenu {
		const val ID_SHOW_SUBJECT_EVALUATIONS = 0
		const val ID_WITHDRAW_SUBJECT = 1
	}

	override fun onCreateView() = R.layout.fragment_record

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		setMenuVisibility(false)

		rViewRecord.adapter = quarterAdapter

		eViewRecord.setOnRetryClick { initialLoad() }
		requireActivity().addMenuProvider(RecordMenuProvider(), viewLifecycleOwner)

		launchRepeatOnLifecycle {
			with(viewModel) {
				collect(viewState, ::stateCollector)
				collect(viewEvent, ::eventCollector)
			}
		}

		initialLoad()
	}

	private fun stateCollector(state: Record.State) {
		when (state) {
			is Record.State.Loading -> fViewRecord.displayedChild = Flipper.LOADING
			is Record.State.Loaded -> loadRecordState(value = state.value)
			is Record.State.Failed -> fViewRecord.displayedChild = Flipper.FAILED
		}
	}

	private fun eventCollector(event: Record.Event) {
		when (event) {
			is Record.Event.NavigateToOutdatedPassword -> navigateToOutdatedPassword()
			is Record.Event.NavigateToEnrollmentProof -> navigateToEnrollmentProof()
			is Record.Event.ShowTimeoutSnackBar -> errorSnackBar(R.string.snack_timeout)
			is Record.Event.ShowNoConnectionSnackBar -> connectionSnackBar(event.isNetworkAvailable)
			is Record.Event.ShowUnavailableSnackBar -> errorSnackBar(R.string.snack_service_unavailable)
			is Record.Event.ShowDefaultErrorSnackBar -> errorSnackBar()
		}
	}

	private fun initialLoad() {
		viewModel.loadQuartersAction()
	}

	private fun loadRecordState(value: RecordViewState) {
		pBarRecord.isVisible = false

		quarterAdapter.submitList(list = value.quarters)

		fViewRecord.displayedChild = if (value.isEmpty) Flipper.EMPTY else Flipper.CONTENT
	}

	private fun navigateToOutdatedPassword() {
		navigate(NavigationBaseDirections.navToUpdatePassword())
	}

	private fun navigateToEnrollmentProof() {
		navigate(RecordFragmentDirections.navToEnrollmentProof())
	}

	private fun showSubjectMenuDialog(item: SubjectItem) {
		val title = getString(
			R.string.label_evaluation_header,
			item.code,
			item.data.name
		)

		val items = mutableListOf(
			BottomMenuItem(
				itemId = SubjectMenu.ID_SHOW_SUBJECT_EVALUATIONS,
				iconResource = R.drawable.ic_list,
				textResource = R.string.menu_subject_show_evaluations
			)
		).apply {
			if (!item.isRetired)
				add(
					BottomMenuItem(
						itemId = SubjectMenu.ID_WITHDRAW_SUBJECT,
						iconResource = R.drawable.ic_not_interested,
						textResource = R.string.menu_subject_withdraw
					)
				)
		}

		bottomSheetDialog<MenuBottomSheetDialog> {
			titleText = title

			setItems(items) { itemId ->
				onSubjectOptionSelected(itemId, item)
			}
		}
	}

	private fun onSubjectOptionSelected(itemId: Int, item: SubjectItem) {
		when (itemId) {
			SubjectMenu.ID_SHOW_SUBJECT_EVALUATIONS ->
				TODO()

			SubjectMenu.ID_WITHDRAW_SUBJECT ->
				viewModel.withdrawSubjectAction(
					WithdrawSubjectParams(
						subjectId = item.id
					)
				)
		}
	}

	inner class QuarterManager : QuarterAdapter.AdapterManager {
		override fun onSubjectOptionsClicked(item: SubjectItem) {
			showSubjectMenuDialog(item)
		}

		override fun onSubjectGradeChanged(item: SubjectItem, grade: Int, isSelected: Boolean) {
			viewModel.updateSubjectAction(
				UpdateSubjectParams(
					subjectId = item.id,
					grade = grade,
					dispatchToRemote = isSelected
				)
			)
		}
	}

	inner class RecordMenuProvider : MenuProvider {
		override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
			menuInflater.inflate(R.menu.menu_record, menu)
		}

		override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
			return when (menuItem.itemId) {
				R.id.menu_enrollment -> {
					viewModel.openEnrollmentProofAction()

					true
				}

				else -> false
			}
		}
	}
}