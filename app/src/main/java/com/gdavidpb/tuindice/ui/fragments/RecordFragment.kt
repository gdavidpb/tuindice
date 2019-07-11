package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.ui.adapters.QuarterAdapter
import com.gdavidpb.tuindice.utils.*
import kotlinx.android.synthetic.main.fragment_record.*
import org.jetbrains.anko.design.longSnackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

open class RecordFragment : Fragment() {

    private val viewModel: RecordViewModel by viewModel()

    private val quarterManager = QuarterManager()

    private val quarterAdapter = QuarterAdapter(manager = quarterManager)

    private val cachedColors by lazy {
        mapOf(
                STATUS_QUARTER_CURRENT to ContextCompat.getColor(requireContext(), R.color.quarterCurrent),
                STATUS_QUARTER_COMPLETED to ContextCompat.getColor(requireContext(), R.color.quarterCompleted),
                STATUS_QUARTER_GUESS to ContextCompat.getColor(requireContext(), R.color.quarterGuess),
                STATUS_QUARTER_RETIRED to ContextCompat.getColor(requireContext(), R.color.quarterRetired)
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(rViewRecord) {
            layoutManager = LinearLayoutManager(context)
            adapter = quarterAdapter

            ItemTouchHelper(quarterManager).attachToRecyclerView(this)
        }

        /*
        rViewRecord.onScrollStateChanged { newState ->
            if (newState == SCROLL_STATE_IDLE)
                btnAddQuarter.show()
            else
                btnAddQuarter.hide()
        }

        btnAddQuarter.onClickOnce(::onAddQuarterClicked)
        */

        with(viewModel) {
            observe(quarters, ::quartersObserver)

            getQuarters()
        }
    }

    private fun quartersObserver(result: Result<List<Quarter>>?) {
        when (result) {
            is Result.OnLoading -> {
                pBarRecord.visibility = View.VISIBLE
            }
            is Result.OnSuccess -> {
                pBarRecord.visibility = View.GONE

                val quarters = result.value

                quarterAdapter.swapItems(new = quarters)

                //todo empty
            }
            is Result.OnError -> {
                pBarRecord.visibility = View.GONE

                //todo handle error
            }
        }
    }

    /*
    private fun onAddQuarterClicked() {

    }
    */

    inner class QuarterManager : QuarterAdapter.AdapterManager, ItemTouchHelper.Callback() {
        override fun onSubjectChanged(item: Subject) {
            viewModel.updateSubject(subject = item)
        }

        override fun onQuarterChanged(item: Quarter, position: Int) {
            quarterAdapter.replaceItemAt(item, position, false)
        }

        override fun computeGradeSum(quarter: Quarter): Double {
            val quarters = quarterAdapter.getQuarters()

            return quarters.computeGradeSum(until = quarter)
        }

        override fun resolveColor(item: Quarter): Int {
            return cachedColors.getValue(item.status)
        }

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val position = viewHolder.adapterPosition
            val item = quarterAdapter.getItem(position)

            /* Let swipes over the first with "guess" status */
            return if (item.status == STATUS_QUARTER_GUESS && position == 0)
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

            view?.longSnackbar(
                    getString(R.string.snackBar_message_quarter_removed, item.toQuarterTitle()),
                    getString(R.string.snackBar_action_quarter_removed)) {
                quarterAdapter.addItemAt(item, position)

                rViewRecord.scrollToPosition(0)
            }?.show()
        }
    }
}