package com.gdavidpb.tuindice.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.usecase.coroutines.Event
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.errors.GetEnrollmentError
import com.gdavidpb.tuindice.domain.usecase.errors.SyncError
import com.gdavidpb.tuindice.presentation.model.BottomMenuItem
import com.gdavidpb.tuindice.presentation.model.QuarterItem
import com.gdavidpb.tuindice.presentation.model.SubjectItem
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.ui.adapters.QuarterAdapter
import com.gdavidpb.tuindice.ui.dialogs.EnrollmentDownloadingBottomSheetDialog
import com.gdavidpb.tuindice.ui.dialogs.MenuBottomSheetDialog
import com.gdavidpb.tuindice.ui.dialogs.UpdatePasswordBottomSheetDialog
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.toQuarterItem
import com.gdavidpb.tuindice.utils.mappers.toUpdateRequest
import kotlinx.android.synthetic.main.fragment_record.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class RecordFragment : NavigationFragment() {

    private val mainViewModel by sharedViewModel<MainViewModel>()

    private val viewModel by viewModel<RecordViewModel>()

    private val quarterManager = QuarterManager()

    private val quarterAdapter = QuarterAdapter(manager = quarterManager)

    private val downloadingDialog by lazy {
        EnrollmentDownloadingBottomSheetDialog()
    }

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

        setHasOptionsMenu(true)
        setMenuVisibility(false)

        rViewRecord.adapter = quarterAdapter

        viewModel.getQuarters()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        with(mainViewModel) {
            observe(sync, ::syncObserver)
        }

        with(viewModel) {
            observe(quarters, ::quartersObserver)
            observe(enrollment, ::enrollmentObserver)
            observe(quarterUpdate, ::quarterObserver)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_record, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_enrollment -> {
                val currentQuarterItem = quarterAdapter.getCurrentQuarter()

                if (currentQuarterItem != null)
                    viewModel.openEnrollmentProof(quarter = currentQuarterItem.data)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSubjectMenuDialog(quarterItem: QuarterItem, subjectItem: SubjectItem) {
        val title = getString(R.string.label_evaluation_plan_header, subjectItem.code, subjectItem.data.name)

        val items = mutableListOf(
                BottomMenuItem(
                        itemId = SubjectMenu.ID_SHOW_SUBJECT_EVALUATIONS,
                        iconResource = R.drawable.ic_list,
                        textResource = R.string.menu_subject_show_evaluations
                )
        ).apply {
            if (!subjectItem.isRetired)
                add(BottomMenuItem(
                        itemId = SubjectMenu.ID_REMOVE_SUBJECT,
                        iconResource = R.drawable.ic_not_interested,
                        textResource = R.string.menu_subject_remove
                ))
        }

        bottomSheetDialog<MenuBottomSheetDialog> {
            titleText = title

            onItemSelected(items) { itemId ->
                onSubjectOptionSelected(quarterItem, subjectItem, itemId)
            }
        }
    }

    private fun showUpdatePasswordDialog() {
        UpdatePasswordBottomSheetDialog()
            .show(childFragmentManager, "updatePasswordDialog")
    }

    private fun onSubjectOptionSelected(
        quarterItem: QuarterItem,
        subjectItem: SubjectItem,
        itemId: Int
    ) {
        when (itemId) {
            SubjectMenu.ID_SHOW_SUBJECT_EVALUATIONS -> showSubjectEvaluations(
                quarterItem,
                subjectItem
            )
            SubjectMenu.ID_REMOVE_SUBJECT -> removeSubject(quarterItem, subjectItem)
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
        val request = quarterItem.data.toUpdateRequest(
                sid = subjectItem.id,
                grade = 0,
                dispatchChanges = true
        )

        viewModel.updateQuarter(request)
    }

    private fun openEnrollmentProof() {
        val currentQuarterItem = quarterAdapter.getCurrentQuarter()

        if (currentQuarterItem != null)
            viewModel.openEnrollmentProof(quarter = currentQuarterItem.data)
        else
            snackBar(R.string.snack_enrollment_not_found)
    }

    private fun syncObserver(result: Result<Boolean, SyncError>?) {
        when (result) {
            is Result.OnSuccess -> {
                val pendingUpdate = result.value

                if (pendingUpdate)
                    viewModel.getQuarters()
            }
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
        }
    }

    private fun enrollmentObserver(result: Event<File, GetEnrollmentError>?) {
        when (result) {
            is Event.OnLoading -> {
                downloadingDialog.show(childFragmentManager, "downloadingDialog")

                setMenuVisibility(false)
            }
            is Event.OnSuccess -> {
                downloadingDialog.dismiss()

                setMenuVisibility(true)

                val enrollmentFile = result.value

                runCatching {
                    openPdf(file = enrollmentFile)
                }.onFailure {
                    snackBar(R.string.snack_enrollment_unsupported)
                }
            }
            is Event.OnError -> {
                downloadingDialog.dismiss()

                setMenuVisibility(true)

                enrollmentErrorHandler(error = result.error)
            }
        }
    }

    private fun quarterObserver(result: Result<Quarter, Nothing>?) {
        when (result) {
            is Result.OnSuccess -> {
                val context = requireContext()
                val quarter = result.value
                val item = quarter.toQuarterItem(context)

                quarterAdapter.updateQuarter(item)
            }
        }
    }

    private fun enrollmentErrorHandler(error: GetEnrollmentError?) {
        when (error) {
            is GetEnrollmentError.Timeout -> errorSnackBar(R.string.snack_timeout) { openEnrollmentProof() }
            is GetEnrollmentError.NoConnection -> connectionSnackBar(error.isNetworkAvailable) { openEnrollmentProof() }
            is GetEnrollmentError.NotFound -> snackBar(R.string.snack_enrollment_not_found)
            is GetEnrollmentError.AccountDisabled -> mainViewModel.signOut()
            is GetEnrollmentError.OutdatedPassword -> showUpdatePasswordDialog()
            is GetEnrollmentError.Unavailable -> errorSnackBar(R.string.snack_service_unavailable) { openEnrollmentProof() }
            else -> errorSnackBar { openEnrollmentProof() }
        }
    }

    inner class QuarterManager : QuarterAdapter.AdapterManager {
        override fun onSubjectOptionsClicked(quarterItem: QuarterItem, subjectItem: SubjectItem) {
            showSubjectMenuDialog(quarterItem = quarterItem, subjectItem = subjectItem)
        }

        override fun onSubjectGradeChanged(quarterItem: QuarterItem, subjectItem: SubjectItem, grade: Int, dispatchChanges: Boolean) {
            val request = quarterItem.data.toUpdateRequest(
                    sid = subjectItem.id,
                    grade = grade,
                    dispatchChanges = dispatchChanges
            )

            viewModel.updateQuarter(request)
        }

        override fun onSubmitQuarters(items: List<QuarterItem>) {
            fViewRecord.displayedChild = if (items.isNotEmpty()) Flipper.CONTENT else Flipper.EMPTY
        }
    }
}