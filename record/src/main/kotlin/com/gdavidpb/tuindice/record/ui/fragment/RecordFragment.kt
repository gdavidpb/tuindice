package com.gdavidpb.tuindice.record.ui.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import com.gdavidpb.tuindice.base.NavigationBaseDirections
import com.gdavidpb.tuindice.base.presentation.model.BottomMenuItem
import com.gdavidpb.tuindice.base.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.base.ui.dialog.MenuBottomSheetDialog
import com.gdavidpb.tuindice.base.ui.fragment.NavigationFragment
import com.gdavidpb.tuindice.base.utils.RequestKeys
import com.gdavidpb.tuindice.base.utils.ResultKeys
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
import kotlinx.android.synthetic.main.fragment_record.fViewRecord
import kotlinx.android.synthetic.main.fragment_record.pBarRecord
import kotlinx.android.synthetic.main.fragment_record.rViewRecord
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecordFragment : NavigationFragment() {

	private val mainViewModel by sharedViewModel<MainViewModel>()
	private val viewModel by viewModel<RecordViewModel>()

	private val quarterManager = QuarterManager()

	private val quarterAdapter = QuarterAdapter(manager = quarterManager)

	private object Flipper {
		const val CONTENT = 0
		const val EMPTY = 1
		const val LOADING = 2
		// TODO const val FAILED = 3
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

		requireActivity().addMenuProvider(RecordMenuProvider(), viewLifecycleOwner)

		setFragmentResultListener(RequestKeys.USE_PLAN_GRADE, ::onFragmentResult)

		launchRepeatOnLifecycle {
			with(viewModel) {
				collect(viewState, ::stateCollector)
				collect(viewEvent, ::eventCollector)
			}
		}

		viewModel.loadQuartersAction()
	}

	private fun stateCollector(state: Record.State) {
		when (state) {
			is Record.State.Loading -> fViewRecord.displayedChild = Flipper.LOADING
			is Record.State.Loaded -> loadRecordState(value = state.value)
			is Record.State.Failed -> TODO()
		}
	}

	private fun eventCollector(event: Record.Event) {
		when (event) {
			is Record.Event.NavigateToAccountDisabled -> navigateToAccountDisabled()
			is Record.Event.NavigateToOutdatedPassword -> navigateToOutdatedPassword()
			is Record.Event.NavigateToEnrollmentProof -> navigateToEnrollmentProof()
			is Record.Event.NavigateToEvaluationPlan -> navigateToEvaluationPlan(event.item)
			is Record.Event.ShowTimeoutSnackBar -> errorSnackBar(R.string.snack_timeout)
			is Record.Event.ShowNoConnectionSnackBar -> connectionSnackBar(event.isNetworkAvailable)
			is Record.Event.ShowUnavailableSnackBar -> errorSnackBar(R.string.snack_service_unavailable)
			is Record.Event.ShowDefaultErrorSnackBar -> errorSnackBar()
		}
	}

	private fun loadRecordState(value: RecordViewState) {
		pBarRecord.isVisible = false

		quarterAdapter.submitList(list = value.quarters)

		fViewRecord.displayedChild = if (value.isEmpty) Flipper.EMPTY else Flipper.CONTENT
	}

	private fun navigateToEvaluationPlan(item: SubjectItem) {
		navigate(
			RecordFragmentDirections.navToEvaluationPlan(
				quarterId = item.data.qid,
				subjectId = item.id,
				subjectCode = item.data.code,
				subjectName = item.data.name
			)
		)
	}

	private fun navigateToAccountDisabled() {
		mainViewModel.signOut()
	}

	private fun navigateToOutdatedPassword() {
		navigate(NavigationBaseDirections.navToUpdatePassword())
	}

	private fun navigateToEnrollmentProof() {
		navigate(RecordFragmentDirections.navToEnrollmentProof())
	}

	private fun useEvaluationPlanGrade(result: Bundle) {
		val subjectId = result.getString(ResultKeys.SUBJECT_ID, null)
		val grade = result.getInt(ResultKeys.GRADE, -1)

		if (subjectId == null || grade == -1) return

		viewModel.updateSubjectAction(
			UpdateSubjectParams(
				subjectId = subjectId,
				grade = grade,
				dispatchToRemote = true
			)
		)
	}

	private fun showSubjectMenuDialog(item: SubjectItem) {
		val title = getString(
			R.string.label_evaluation_plan_header,
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

	private fun onFragmentResult(requestKey: String, result: Bundle) {
		when (requestKey) {
			RequestKeys.USE_PLAN_GRADE -> useEvaluationPlanGrade(result)
		}
	}

	private fun onSubjectOptionSelected(itemId: Int, item: SubjectItem) {
		when (itemId) {
			SubjectMenu.ID_SHOW_SUBJECT_EVALUATIONS ->
				viewModel.openEvaluationPlanAction(item)

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