package com.gdavidpb.tuindice.record.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.usecase.base.Result
import com.gdavidpb.tuindice.base.domain.usecase.error.SyncError
import com.gdavidpb.tuindice.base.presentation.model.BottomMenuItem
import com.gdavidpb.tuindice.base.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.base.ui.dialogs.MenuBottomSheetDialog
import com.gdavidpb.tuindice.base.ui.fragments.NavigationFragment
import com.gdavidpb.tuindice.base.utils.extensions.bottomSheetDialog
import com.gdavidpb.tuindice.base.utils.extensions.observe
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.record.ui.adapters.QuarterAdapter
import com.gdavidpb.tuindice.record.utils.mappers.toQuarterItem
import kotlinx.android.synthetic.main.fragment_record.*
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
	}

	private object SubjectMenu {
		const val ID_SHOW_SUBJECT_EVALUATIONS = 0
		const val ID_REMOVE_SUBJECT = 1
	}

	override fun onCreateView() = R.layout.fragment_record

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		setMenuVisibility(false)

		rViewRecord.adapter = quarterAdapter

		requireActivity().addMenuProvider(RecordMenuProvider(), viewLifecycleOwner)

		viewModel.getQuarters()
	}

	override fun onInitObservers() {
		with(mainViewModel) {
			observe(sync, ::syncObserver)
		}

		with(viewModel) {
			observe(quarters, ::quartersObserver)
		}
	}

	private fun showSubjectMenuDialog(quarterItem: QuarterItem, subjectItem: SubjectItem) {
		val title = getString(
			R.string.label_evaluation_plan_header,
			subjectItem.code,
			subjectItem.data.name
		)

		val items = mutableListOf(
			BottomMenuItem(
				itemId = SubjectMenu.ID_SHOW_SUBJECT_EVALUATIONS,
				iconResource = R.drawable.ic_list,
				textResource = R.string.menu_subject_show_evaluations
			)
		).apply {
			if (!subjectItem.isRetired)
				add(
					BottomMenuItem(
						itemId = SubjectMenu.ID_REMOVE_SUBJECT,
						iconResource = R.drawable.ic_not_interested,
						textResource = R.string.menu_subject_remove
					)
				)
		}

		bottomSheetDialog<MenuBottomSheetDialog> {
			titleText = title

			setItems(items) { itemId ->
				onSubjectOptionSelected(quarterItem, subjectItem, itemId)
			}
		}
	}

	private fun onSubjectOptionSelected(
		quarterItem: QuarterItem,
		subjectItem: SubjectItem,
		itemId: Int
	) {
		when (itemId) {
			SubjectMenu.ID_SHOW_SUBJECT_EVALUATIONS -> showSubjectEvaluations(
				quarterItem = quarterItem,
				subjectItem = subjectItem
			)
			SubjectMenu.ID_REMOVE_SUBJECT -> removeSubject(
				quarterItem = quarterItem,
				subjectItem = subjectItem
			)
		}
	}

	private fun showSubjectEvaluations(quarterItem: QuarterItem, subjectItem: SubjectItem) {
		navigate(
			RecordFragmentDirections.navToEvaluationPlan(
				quarterId = quarterItem.id,
				subjectId = subjectItem.id,
				subjectCode = subjectItem.data.code,
				subjectName = subjectItem.data.name
			)
		)
	}

	private fun removeSubject(quarterItem: QuarterItem, subjectItem: SubjectItem) {
		TODO("Not yet implemented")
	}

	private fun syncObserver(result: Result<Boolean, SyncError>?) {
		when (result) {
			is Result.OnSuccess -> {
				val pendingUpdate = result.value

				if (pendingUpdate)
					viewModel.getQuarters()
			}
			else -> {}
		}
	}

	private fun quartersObserver(result: Result<List<Quarter>, Nothing>?) {
		when (result) {
			is Result.OnSuccess -> {
				val context = requireContext()

				val quarters = result.value

				val items = quarters.map { quarter ->
					quarter.toQuarterItem(context)
				}

				val hasCurrentQuarter = items.any { quarter -> quarter.isCurrent }

				setMenuVisibility(hasCurrentQuarter)

				quarterAdapter.submitQuarters(items)
			}
			else -> {}
		}
	}

	inner class QuarterManager : QuarterAdapter.AdapterManager {
		override fun onSubjectOptionsClicked(quarterItem: QuarterItem, subjectItem: SubjectItem) {
			showSubjectMenuDialog(quarterItem = quarterItem, subjectItem = subjectItem)
		}

		override fun onSubjectGradeChanged(
			quarterItem: QuarterItem,
			subjectItem: SubjectItem,
			grade: Int,
			dispatchChanges: Boolean
		) {
			TODO("Not yet implemented")
		}

		override fun onSubmitQuarters(items: List<QuarterItem>) {
			fViewRecord.displayedChild = if (items.isNotEmpty()) Flipper.CONTENT else Flipper.EMPTY
		}
	}

	inner class RecordMenuProvider : MenuProvider {
		override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
			menuInflater.inflate(R.menu.menu_record, menu)
		}

		override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
			return when (menuItem.itemId) {
				R.id.menu_enrollment -> { // TODO visibility determined by current quarter existence
					navigate(RecordFragmentDirections.navToEnrollmentProof())

					true
				}
				else -> false
			}
		}
	}
}