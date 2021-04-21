package com.gdavidpb.tuindice.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.usecase.coroutines.Event
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.errors.GetEnrollmentError
import com.gdavidpb.tuindice.domain.usecase.errors.SyncError
import com.gdavidpb.tuindice.presentation.model.QuarterItem
import com.gdavidpb.tuindice.presentation.model.SubjectItem
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.ui.adapters.QuarterAdapter
import com.gdavidpb.tuindice.ui.dialogs.EnrollmentDownloadingBottomSheetDialog
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.toQuarterItem
import com.gdavidpb.tuindice.utils.mappers.toUpdateRequest
import com.google.android.material.snackbar.Snackbar
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

    override fun onCreateView() = R.layout.fragment_record

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        setMenuVisibility(false)

        with(rViewRecord) {
            layoutManager = LinearLayoutManager(context)
            adapter = quarterAdapter

            ItemTouchHelper(quarterManager).attachToRecyclerView(this)
        }

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

    private fun notFoundSnackBar() {
        snackBar {
            messageResource = R.string.snack_enrollment_not_found
        }
    }

    private fun openEnrollmentProof() {
        val currentQuarterItem = quarterAdapter.getCurrentQuarter()

        if (currentQuarterItem != null)
            viewModel.openEnrollmentProof(quarter = currentQuarterItem.data)
        else
            notFoundSnackBar()
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
                    snackBar {
                        messageResource = R.string.snack_enrollment_unsupported
                    }
                }
            }
            is Event.OnError -> {
                downloadingDialog.dismiss()

                setMenuVisibility(true)

                enrollmentErrorHandler(error = result.error)
            }
            is Event.OnCancel -> {
                downloadingDialog.dismiss()

                setMenuVisibility(true)
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
            is GetEnrollmentError.NotEnrolled -> notFoundSnackBar()
            is GetEnrollmentError.NotFound -> notFoundSnackBar()
            else -> errorSnackBar { openEnrollmentProof() }
        }
    }

    inner class QuarterManager : QuarterAdapter.AdapterManager, ItemTouchHelper.Callback() {
        override fun onSubjectOptionsClicked(quarterItem: QuarterItem, subjectItem: SubjectItem) {
            navigate(RecordFragmentDirections.navToSubject(
                    quarterId = quarterItem.id,
                    subjectId = subjectItem.id,
                    subjectCode = subjectItem.code
            ))
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

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val position = viewHolder.absoluteAdapterPosition
            val item = quarterAdapter.getQuarter(position)

            /* Let swipes over the first with "mock" status */
            return if (item.isMock && position == 0)
                makeMovementFlags(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT)
            else
                0
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.absoluteAdapterPosition
            val item = quarterAdapter.getQuarter(position)

            quarterAdapter.removeQuarter(item)

            snackBar {
                message = getString(R.string.snack_item_removed, item.TitleText)

                action(R.string.snack_action_undone) {
                    rViewRecord.scrollToPosition(0)

                    val updatedItem = item.copy(isSwiping = false)

                    quarterAdapter.addQuarter(item = updatedItem, position = position)
                }

                onDismissed { event ->
                    if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                        viewModel.removeQuarter(id = item.id)
                    }
                }
            }
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)

            viewHolder ?: return

            if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) return

            val position = viewHolder.absoluteAdapterPosition

            if (position == RecyclerView.NO_POSITION) return

            val item = quarterAdapter.getQuarter(position)

            quarterAdapter.updateQuarter(item.copy(isSwiping = true))
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)

            val position = viewHolder.absoluteAdapterPosition

            if (position == RecyclerView.NO_POSITION) return

            val item = quarterAdapter.getQuarter(position)

            quarterAdapter.updateQuarter(item.copy(isSwiping = false))
        }
    }
}