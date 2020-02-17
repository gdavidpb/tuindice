package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.response.SyncResponse
import com.gdavidpb.tuindice.presentation.model.QuarterItem
import com.gdavidpb.tuindice.presentation.model.SubjectItem
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.ui.adapters.QuarterAdapter
import com.gdavidpb.tuindice.utils.STATUS_QUARTER_CURRENT
import com.gdavidpb.tuindice.utils.STATUS_QUARTER_GUESS
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.toQuarterItem
import com.gdavidpb.tuindice.utils.mappers.toSubject
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.dialog_progress.view.*
import kotlinx.android.synthetic.main.fragment_record.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.File

open class RecordFragment : Fragment() {

    private val viewModel by sharedViewModel<MainViewModel>()

    private val quarterManager = QuarterManager()

    private val quarterAdapter = QuarterAdapter(manager = quarterManager)

    private val loadingDialog by lazy {
        alert {
            createView(R.layout.dialog_progress) {
                tViewDialogTitle.text = getString(R.string.dialog_enrollment_getting)
            }

            isCancelable = false
        }.create()
    }

    private val retrySnackBar by lazy {
        Snackbar.make(requireView(), R.string.snack_bar_enrollment_retry, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry) { viewModel.openEnrollmentProof() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        with(viewModel) {
            observe(sync, ::syncObserver)
            observe(quarters, ::quartersObserver)
            observe(enrollment, ::enrollmentObserver)

            getQuarters()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
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

    private fun syncObserver(result: Result<SyncResponse>?) {
        when (result) {
            is Result.OnSuccess -> {
                val requireUpdate = result.value.isDataUpdated

                if (requireUpdate) viewModel.getQuarters()
            }
        }
    }

    private fun quartersObserver(result: Result<List<Quarter>>?) {
        when (result) {
            is Result.OnSuccess -> {
                val context = requireContext()

                val quarters = result.value

                val hasCurrentQuarter = quarters.contains { quarter ->
                    quarter.status == STATUS_QUARTER_CURRENT
                }

                setMenuVisibility(hasCurrentQuarter)

                val items = quarters.map { quarter ->
                    quarter.toQuarterItem(context)
                }

                quarterAdapter.swapItems(new = items)

                if (quarters.isEmpty()) {
                    rViewRecord.gone()
                    tViewRecord.visible()
                } else {
                    tViewRecord.gone()
                    rViewRecord.visible()
                }
            }
        }
    }

    private fun enrollmentObserver(result: Result<File>?) {
        when (result) {
            is Result.OnLoading -> {
                loadingDialog.show()

                setMenuVisibility(false)
            }
            is Result.OnSuccess -> {
                loadingDialog.dismiss()

                setMenuVisibility(true)

                val enrollmentFile = result.value

                runCatching {
                    requireContext().openPdf(file = enrollmentFile)
                }.onFailure {
                    longToast(R.string.toast_enrollment_unsupported)
                }
            }
            is Result.OnError -> {
                loadingDialog.dismiss()

                setMenuVisibility(true)

                retrySnackBar.show()
            }
        }
    }

    /*
    private fun onAddQuarterClicked() {

    }
    */

    inner class QuarterManager : QuarterAdapter.AdapterManager, ItemTouchHelper.Callback() {
        override fun onSubjectClicked(item: SubjectItem) {
            val action = RecordFragmentDirections
                    .actionNavRecordToNavSubject(subjectId = item.id)

            findNavController().navigate(action)
        }

        override fun onSubjectChanged(item: SubjectItem, dispatchChanges: Boolean) {
            if (dispatchChanges)
                viewModel.updateSubject(subject = item.toSubject())
        }

        override fun onQuarterChanged(item: QuarterItem, position: Int) {
            quarterAdapter.replaceItemAt(item, position, false)
        }

        override fun getItem(position: Int): QuarterItem {
            return quarterAdapter.getItem(position)
        }

        override fun computeGradeSum(quarter: QuarterItem): Double {
            return quarterAdapter.computeGradeSum(until = quarter)
        }

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val position = viewHolder.adapterPosition
            val item = quarterAdapter.getItem(position)

            /* Let swipes over the first with "guess" status */
            return if (item.data.status == STATUS_QUARTER_GUESS && position == 0)
                makeMovementFlags(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT)
            else
                0
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val item = quarterAdapter.getItem(position)

            quarterAdapter.removeItemAt(position)

            snackBar {
                message = getString(R.string.snack_bar_message_item_removed, item.startEndDateText)

                action(getString(R.string.snack_bar_action_undone)) {
                    quarterAdapter.addItemAt(item, position)

                    rViewRecord.scrollToPosition(0)
                }
            }.build().show()
        }
    }
}