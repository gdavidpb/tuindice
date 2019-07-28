package com.gdavidpb.tuindice.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.*
import kotlinx.android.synthetic.main.activity_calendar.*
import org.jetbrains.anko.design.longSnackbar
import java.util.*

class CalendarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        setSupportActionBar(toolbar)

        with(rViewCalendar) {
            //todo adapter

            layoutManager = LinearLayoutManager(this@CalendarActivity)

            onScrollStateChanged { newState ->
                if (newState == SCROLL_STATE_IDLE)
                    btnAddEvent.show()
                else
                    btnAddEvent.hide()
            }
        }

        app_bar.onStateChanged { newState ->
            when (newState) {
                STATE_IDLE -> {
                    tViewCalendar.visibility = View.INVISIBLE
                    cViewCalendar.visibility = View.VISIBLE
                }
                STATE_EXPANDED -> {
                    tViewCalendar.visibility = View.INVISIBLE
                    cViewCalendar.visibility = View.VISIBLE
                }
                STATE_COLLAPSED -> {
                    val date = Date().format("MMMM yyyy")?.capitalize()

                    tViewCalendar.text = getString(R.string.label_calendar_activity, date)

                    tViewCalendar.visibility = View.VISIBLE
                    cViewCalendar.visibility = View.INVISIBLE
                }
            }
        }

        btnAddEvent.onClickOnce(::onAddEventClicked)

        window.decorView.longSnackbar(R.string.snack_bar_calendar_disclaimer)
    }

    private fun onAddEventClicked() {

    }
}
