package com.gdavidpb.tuindice.ui.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.ui.adapters.QuarterAdapter
import com.gdavidpb.tuindice.utils.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_record.*
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.support.v4.indeterminateProgressDialog
import org.jetbrains.anko.support.v4.longToast
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.File

open class RecordFragment : Fragment() {

    private val viewModel by sharedViewModel<MainViewModel>()

    private val quarterManager = QuarterManager()

    private val quarterAdapter = QuarterAdapter(manager = quarterManager)

    private val cachedColors by lazy {
        mapOf(
                STATUS_QUARTER_CURRENT to requireContext().getCompatColor(R.color.quarter_current),
                STATUS_QUARTER_COMPLETED to requireContext().getCompatColor(R.color.quarter_completed),
                STATUS_QUARTER_GUESS to requireContext().getCompatColor(R.color.quarter_guess),
                STATUS_QUARTER_RETIRED to requireContext().getCompatColor(R.color.quarter_retired)
        )
    }

    private val cachedFonts by lazy {
        mapOf(
                "Code.ttf" to Typeface.createFromAsset(requireContext().assets, "fonts/Code.ttf")
        )
    }

    private val loadingDialog by lazy {
        indeterminateProgressDialog(message = R.string.dialog_enrollment_getting)
    }

    private val retrySnackBar by lazy {
        Snackbar.make(requireView(), R.string.snack_bar_enrollment_retry, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry) { viewModel.openEnrollmentProof() }
    }

    private val errorSnackBar by lazy {
        Snackbar.make(requireView(), R.string.snack_bar_enrollment_error, Snackbar.LENGTH_LONG)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        setMenuVisibility(false)

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
            observe(enrollment, ::enrollmentObserver)

            getQuarters()

            loadAccount(trySync = false)
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

    private fun quartersObserver(result: Result<List<Quarter>>?) {
        when (result) {
            is Result.OnLoading -> {
                pBarRecord.visibility = View.VISIBLE
            }
            is Result.OnSuccess -> {
                pBarRecord.visibility = View.GONE

                val quarters = result.value

                val hasCurrentQuarter = quarters.contains { quarter ->
                    quarter.status == STATUS_QUARTER_CURRENT
                }

                setMenuVisibility(hasCurrentQuarter)

                quarterAdapter.swapItems(new = quarters)

                if (quarters.isEmpty()) {
                    rViewRecord.visibility = View.GONE
                    tViewRecord.visibility = View.VISIBLE
                } else {
                    tViewRecord.visibility = View.GONE
                    rViewRecord.visibility = View.VISIBLE
                }
            }
            is Result.OnError -> {
                pBarRecord.visibility = View.GONE

                errorSnackBar.show()
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
            is Result.OnEmpty -> {
                loadingDialog.dismiss()

                setMenuVisibility(true)

                retrySnackBar.show()
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

        override fun resolveFont(asset: String): Typeface {
            return cachedFonts.getValue(asset)
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