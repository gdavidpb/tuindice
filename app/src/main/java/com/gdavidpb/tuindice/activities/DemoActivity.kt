package com.gdavidpb.tuindice.activities

import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v7.app.AppCompatDelegate
import android.support.v7.content.res.AppCompatResources
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import com.gdavidpb.tuindice.*
import com.gdavidpb.tuindice.abstracts.Initializer
import com.gdavidpb.tuindice.adapters.QuarterAdapter
import kotlinx.android.synthetic.main.activity_demo.*
import java.util.*

class DemoActivity : AppCompatActivity(), Initializer {

    private var index = 0

    private lateinit var quarter: DstQuarter
    private lateinit var adapter: QuarterAdapter

    private val messages by lazy { resources.getStringArray(R.array.demoMessages) }
    private val keys by lazy { resources.getStringArray(R.array.demoKeys) }
    private val colors by lazy { resources.getIntArray(R.array.demoColors) }
    private val typeface by lazy { Typeface.createFromAsset(assets, "fonts/Code.ttf") }

    override fun onInitialize(view: View?) {
        /* Set up welcome */
        val drawableLauncher = AppCompatResources.getDrawable(this, R.drawable.ic_launcher)

        tViewDemoTitle.setText(R.string.demoWelcome)
        tViewDemoTitle.setCompoundDrawablesWithIntrinsicBounds(null, drawableLauncher, null, null)

        /* Start demo */
        startDemo()

        /* Set up listeners */
        bDemoGotIt.setOnClickListener { nextDemo() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Set Spanish-Venezuela as default locale */
        Locale.setDefault(Locale("es", "VE"))

        /* Set up vector compatibility */
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        /* Set up activity */
        setContentView(R.layout.activity_demo)

        onInitialize()
    }

    override fun onBackPressed() { }

    private fun startDemo() {
        tViewDemoMessage.text = spanMessage(
                messages[index],
                keys[index],
                colors[index])

        /* Set up completed quarter sample */
        quarter = getDatabase().getQuarterSample(QuarterType.COMPLETED)

        displayDemoAtQuarter()

        index++
    }

    private fun nextDemo() {
        tViewDemoTitle.visibility = View.GONE

        lLayoutDemo.updateAtMe {
            if (index < messages.size) {
                tViewDemoMessage.text = spanMessage(
                        messages[index],
                        keys[index],
                        colors[index])

                when (index++) {
                    1 -> {
                        /* Set up current quarter sample */
                        quarter = getDatabase().getQuarterSample(QuarterType.CURRENT)

                        displayDemoAtQuarter()
                    }
                    2 -> displayDemoAtQuarter(R.id.tViewQuarterGrade)
                    3 -> displayDemoAtQuarter(R.id.tViewQuarterGradeSum)
                    4 -> displayDemoAtQuarter(R.id.tViewQuarterCredits)
                    5 -> displayDemoAtSubject(R.id.sBarGrade)
                    6 -> {
                        quarter.subjects[0].grade = 0
                        quarter.subjects[0].status = SubjectStatus.RETIRED

                        displayDemoAtSubject(R.id.tViewSubjectCode)
                    }
                    7 -> {
                        quarter.subjects[0].grade = 0
                        quarter.subjects[0].status = SubjectStatus.RETIRED

                        displayDemoAtQuarter(R.id.tViewQuarterCredits)
                    }
                    8 -> {
                        quarter.subjects[0].grade = 1
                        quarter.subjects[0].status = SubjectStatus.OK

                        displayDemoAtQuarter(R.id.tViewQuarterGradeSum)
                    }
                }
            } else {
                getPreferences().firstRun()

                finish()
            }
        }
    }

    private fun displayDemoAtSubject(@IdRes view: Int = 0) {
        adapter = QuarterAdapter(this, true)

        adapter.add(quarter)

        if (view == R.id.sBarGrade)
            adapter.interaction = true

        if (view != 0)
            adapter.startDemoAtSubject(quarter.subjects[0], view)

        lViewDemoQuarter.adapter = adapter
    }

    private fun displayDemoAtQuarter(@IdRes view: Int = 0) {
        adapter = QuarterAdapter(this, true)

        adapter.add(quarter)

        if (view != 0)
            adapter.startDemoAtQuarter(quarter, view)

        lViewDemoQuarter.adapter = adapter
    }

    private fun spanMessage(message: String,
                            key: String,
                            color: Int): SpannableString {
        val spannableString = SpannableString(message)

        val start = message.indexOf(key)

        spannableString.setSpan(ForegroundColorSpan(color),
                start, start + key.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannableString.setSpan(StyleSpan(Typeface.BOLD),
                start, start + key.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val dot = message.indexOf("⦿")

        if (dot != -1) {
            spannableString.setSpan(com.gdavidpb.tuindice.models.TypefaceSpan(typeface),
                    dot, dot + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            spannableString.setSpan(ForegroundColorSpan(color),
                    dot, dot + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return spannableString
    }
}
