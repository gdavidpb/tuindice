package com.gdavidpb.tuindice.record.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.usecase.base.Result
import com.gdavidpb.tuindice.base.presentation.model.BottomMenuItem
import com.gdavidpb.tuindice.base.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.base.ui.dialogs.MenuBottomSheetDialog
import com.gdavidpb.tuindice.base.ui.fragments.NavigationFragment
import com.gdavidpb.tuindice.base.utils.extension.bottomSheetDialog
import com.gdavidpb.tuindice.base.utils.extension.connectionSnackBar
import com.gdavidpb.tuindice.base.utils.extension.errorSnackBar
import com.gdavidpb.tuindice.base.utils.extension.observe
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.domain.error.GetQuartersError
import com.gdavidpb.tuindice.record.domain.param.UpdateSubjectParams
import com.gdavidpb.tuindice.record.presentation.mapper.toQuarterItem
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.record.ui.adapters.QuarterAdapter
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
		const val ID_WITHDRAW_SUBJECT = 1
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
		with(viewModel) {
			observe(quarters, ::quartersObserver)
		}
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

	private fun onSubjectOptionSelected(itemId: Int, item: SubjectItem) {
		when (itemId) {
			SubjectMenu.ID_SHOW_SUBJECT_EVALUATIONS ->
				navigate(
					RecordFragmentDirections.navToEvaluationPlan(
						subjectId = item.id,
						subjectCode = item.data.code,
						subjectName = item.data.name
					)
				)
			SubjectMenu.ID_WITHDRAW_SUBJECT ->
				viewModel.withdrawSubject(subjectId = item.id)
		}
	}

	private fun quartersObserver(result: Result<List<Quarter>, GetQuartersError>?) {
		when (result) {
			is Result.OnLoading -> {
				pBarRecord.isVisible = true
			}
			is Result.OnSuccess -> {
				pBarRecord.isVisible = false

				val context = requireContext()

				val quarters = result.value

				val items = quarters.map { quarter ->
					quarter.toQuarterItem(context)
				}

				val hasCurrentQuarter = items.any { quarter -> quarter.isCurrent }

				setMenuVisibility(hasCurrentQuarter)

				quarterAdapter.submitQuarters(items)
			}
			is Result.OnError -> {
				pBarRecord.isVisible = false

				quartersErrorHandler(error = result.error)
			}
			else -> {}
		}
	}

	private fun quartersErrorHandler(error: GetQuartersError?) {
		when (error) {
			is GetQuartersError.AccountDisabled -> mainViewModel.signOut()
			is GetQuartersError.NoConnection -> connectionSnackBar(error.isNetworkAvailable) { viewModel.getQuarters() }
			is GetQuartersError.OutdatedPassword -> mainViewModel.outdatedPassword()
			is GetQuartersError.Timeout -> errorSnackBar(R.string.snack_timeout) { viewModel.getQuarters() }
			is GetQuartersError.Unavailable -> errorSnackBar(R.string.snack_service_unavailable)
			else -> errorSnackBar()
		}
	}

	inner class QuarterManager : QuarterAdapter.AdapterManager {
		override fun onSubjectOptionsClicked(item: SubjectItem) {
			showSubjectMenuDialog(item)
		}

		override fun onSubjectGradeChanged(item: SubjectItem, grade: Int, isSelected: Boolean) {
			viewModel.updateSubject(
				UpdateSubjectParams(
					subjectId = item.id,
					grade = grade,
					dispatchToRemote = isSelected
				)
			)
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
				R.id.menu_enrollment -> {
					navigate(RecordFragmentDirections.navToEnrollmentProof())

					true
				}
				else -> false
			}
		}
	}
}