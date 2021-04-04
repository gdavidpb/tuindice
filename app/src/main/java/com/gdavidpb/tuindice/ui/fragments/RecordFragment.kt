package com.gdavidpb.tuindice.ui.fragments

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
import com.gdavidpb.tuindice.ui.dialogs.credentialsChangedDialog
import com.gdavidpb.tuindice.utils.STATUS_QUARTER_CURRENT
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.toQuarterItem
import com.gdavidpb.tuindice.utils.mappers.toUpdateRequest
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.dialog_progress.view.*
import kotlinx.android.synthetic.main.fragment_record.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.File

class RecordFragment : NavigationFragment() {

    private val mainViewModel by sharedViewModel<MainViewModel>()

    private val viewModel by sharedViewModel<RecordViewModel>()

    private val quarterManager = QuarterManager()

    private val quarterAdapter = QuarterAdapter(manager = quarterManager)

    private val loadingDialog by lazy {
        alert {
            createView(R.layout.dialog_progress) {
                tViewDialogTitle.text = getString(R.string.dialog_enrollment_downloading)
            }

            isCancelable = false
        }
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

            /*
            onScrollStateChanged { newState ->
                if (newState == SCROLL_STATE_IDLE)
                    btnAddQuarter.show()
                else
                    btnAddQuarter.hide()
            }
            */

            ItemTouchHelper(quarterManager).attachToRecyclerView(this)
        }

        // btnAddQuarter.onClickOnce(::onAddQuarterClicked)

        with(mainViewModel) {
            observe(sync, ::syncObserver)
        }

        with(viewModel) {
            observe(quarters, ::quartersObserver)
            observe(enrollment, ::enrollmentObserver)
            observe(quarterUpdate, ::quarterObserver)

            getQuarters()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_record, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_enrollment -> {
                viewModel.openEnrollmentProof()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun notFoundSnackBar() {
        snackBar {
            messageResource = R.string.snack_bar_enrollment_not_found
        }
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

                val hasCurrentQuarter = quarters.any { quarter ->
                    quarter.status == STATUS_QUARTER_CURRENT
                }

                setMenuVisibility(hasCurrentQuarter)

                val items = quarters.map { quarter ->
                    quarter.toQuarterItem(context)
                }

                quarterAdapter.submitQuarters(items)
            }
        }
    }

    private fun enrollmentObserver(result: Event<File, GetEnrollmentError>?) {
        when (result) {
            is Event.OnLoading -> {
                loadingDialog.show()

                setMenuVisibility(false)
            }
            is Event.OnSuccess -> {
                loadingDialog.dismiss()

                setMenuVisibility(true)

                val enrollmentFile = result.value

                runCatching {
                    requireContext().openPdf(file = enrollmentFile)
                }.onFailure {
                    snackBar {
                        messageResource = R.string.snack_bar_enrollment_unsupported
                    }
                }
            }
            is Event.OnError -> {
                loadingDialog.dismiss()

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
            is GetEnrollmentError.InvalidCredentials -> requireAppCompatActivity().credentialsChangedDialog()
            is GetEnrollmentError.NoConnection -> noConnectionSnackBar(error.isNetworkAvailable) { viewModel.openEnrollmentProof() }
            is GetEnrollmentError.NotEnrolled -> notFoundSnackBar()
            is GetEnrollmentError.NotFound -> notFoundSnackBar()
            else -> defaultErrorSnackBar { viewModel.openEnrollmentProof() }
        }
    }

    /*
    private fun onAddQuarterClicked() {

    }
    */

    inner class QuarterManager : QuarterAdapter.AdapterManager, ItemTouchHelper.Callback() {
        override fun onSubjectClicked(quarterItem: QuarterItem, subjectItem: SubjectItem) {
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
            val position = viewHolder.adapterPosition
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
            val position = viewHolder.adapterPosition
            val item = quarterAdapter.getQuarter(position)

            quarterAdapter.removeQuarter(item)

            snackBar {
                message = getString(R.string.snack_bar_message_item_removed, item.startEndDateText)

                action(R.string.snack_bar_action_undone) {
                    rViewRecord.scrollToPosition(0)

                    // TODO swiping status

                    quarterAdapter.addQuarter(item, position)
                }

                onDismissed { event ->
                    if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                        // TODO view model remove quarter
                    }
                }
            }
        }
    }
}