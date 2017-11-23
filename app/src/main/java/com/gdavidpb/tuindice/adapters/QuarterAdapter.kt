package com.gdavidpb.tuindice.adapters

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.text.style.TypefaceSpan
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.gdavidpb.tuindice.*
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.graphics.Typeface
import android.util.LongSparseArray

class QuarterAdapter(context: Context,
                     private val isDemo: Boolean = false,
                     var interaction: Boolean = !isDemo)
    : ArrayAdapter<DstQuarter>(context, 0) {

    private val demoQuarter = LongSparseArray<Int>()
    private val demoSubject = LongSparseArray<Int>()

    private var demoAnimator: ValueAnimator? = null

    private val colorWarning by lazy { ContextCompat.getColor(context, R.color.colorWarning) }
    private val typeface by lazy { Typeface.createFromAsset(context.assets, "fonts/Code.ttf") }

    @SuppressLint("ClickableViewAccessibility")
    @Suppress("UNCHECKED_CAST")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val quarter = getItem(position)
        val holder: SparseArray<View>

        val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.view_quarter,
                        parent,
                        false)

        if (convertView == null) {
            holder = SparseArray()

            holder.putAs(R.id.viewQuarterColor, view)
            holder.putAs(R.id.tViewQuarterTitle, view)
            holder.putAs(R.id.iViewQuarterRemove, view)
            holder.putAs(R.id.tViewQuarterGrade, view)
            holder.putAs(R.id.tViewQuarterGradeSum, view)
            holder.putAs(R.id.tViewQuarterCredits, view)
            holder.putAs(R.id.lLayoutQuarterContainer, view)

            view.tag = holder
        } else
            holder = convertView.tag as SparseArray<View>

        val container = holder.getAs<LinearLayout>(R.id.lLayoutQuarterContainer)

        /* Add children */
        while (container.childCount < quarter.subjects.size) {
            val child = LayoutInflater.from(context)
                    .inflate(R.layout.view_subject,
                            container,
                            false)

            val childrenHolder = SparseArray<View>()

            childrenHolder.putAs(R.id.sBarGrade, child)
            childrenHolder.putAs(R.id.tViewSubjectGrade, child)
            childrenHolder.putAs(R.id.tViewSubjectCode, child)
            childrenHolder.putAs(R.id.tViewSubjectName, child)
            childrenHolder.putAs(R.id.tViewSubjectCredits, child)

            child.tag = childrenHolder

            container.addView(child)
        }

        /* Remove children */
        while (container.childCount > quarter.subjects.size) {
            val child = container.getChildAt(container.childCount - 1)

            container.removeView(child)

            child.tag = null
        }

        val viewQuarterColor = holder.getAs<View>(R.id.viewQuarterColor)
        val tViewQuarterGrade = holder.getAs<TextView>(R.id.tViewQuarterGrade)
        val tViewQuarterTitle = holder.getAs<TextView>(R.id.tViewQuarterTitle)
        val iViewQuarterRemove = holder.getAs<ImageView>(R.id.iViewQuarterRemove)
        val tViewQuarterCredits = holder.getAs<TextView>(R.id.tViewQuarterCredits)
        val tViewQuarterGradeSum = holder.getAs<TextView>(R.id.tViewQuarterGradeSum)

        /* Start requested demo animation */
        if (isDemo) {
            val demoId = demoQuarter.get(quarter.id, -1)

            if (demoId != -1) {
                val demoView = holder.getAs<View>(demoId)

                demoView.demoAtMe()

                demoQuarter.remove(quarter.id)
            }
        }

        /* Set up quarter view */
        setUpQuarter(quarter,
                tViewQuarterGrade,
                tViewQuarterGradeSum,
                tViewQuarterCredits,
                tViewQuarterTitle,
                viewQuarterColor,
                iViewQuarterRemove)

        for (i in quarter.subjects.indices) {
            val subject = quarter.subjects[i]
            val child = container.getChildAt(i)

            val childHolder = child.tag as SparseArray<View>

            val sBarGrade = childHolder.getAs<SeekBar>(R.id.sBarGrade)
            val tViewSubjectCode = childHolder.getAs<TextView>(R.id.tViewSubjectCode)
            val tViewSubjectName = childHolder.getAs<TextView>(R.id.tViewSubjectName)
            val tViewSubjectGrade = childHolder.getAs<TextView>(R.id.tViewSubjectGrade)
            val tViewSubjectCredits = childHolder.getAs<TextView>(R.id.tViewSubjectCredits)

            /* Start requested demo animation */
            if (isDemo) {
                val demoId = demoSubject.get(subject.id, -1)

                if (demoId != -1) {
                    val demoView = childHolder.getAs<View>(demoId)

                    demoAnimator = demoView.demoAtMe()

                    demoSubject.remove(subject.id)
                }
            }

            if (interaction)
                sBarGrade.setOnTouchListener(null)
            else
                sBarGrade.setOnTouchListener { _, _ -> true }

            setUpSubject(quarter,
                    subject,
                    tViewQuarterGrade,
                    tViewQuarterGradeSum,
                    tViewQuarterCredits,
                    tViewSubjectName,
                    tViewSubjectGrade,
                    tViewSubjectCode,
                    tViewSubjectCredits,
                    sBarGrade)
        }

        return view
    }

    fun startDemoAtQuarter(quarter: DstQuarter, @IdRes idRes: Int) {
        demoQuarter.put(quarter.id, idRes)
    }

    fun startDemoAtSubject(subject: DstSubject, @IdRes idRes: Int) {
        demoSubject.put(subject.id, idRes)
    }

    private fun setUpQuarter(quarter: DstQuarter,
                             quarterGrade: TextView,
                             quarterGradeSum: TextView,
                             quarterCredits: TextView,
                             quarterTitle: TextView? = null,
                             quarterColor: View? = null,
                             quarterRemove: ImageView? = null) {
        val color = quarter.type.getColor(context)

        /* Set up quarter grade */
        val grade =
                if (quarter.type == QuarterType.COMPLETED || isDemo)
                    if (isDemo)
                        quarter.subjects[0].grade.toDouble()
                    else
                        quarter.grade
                else
                    context.getDatabase().computeGradeFromQuarter(quarter)

        quarterGrade.text = spanNumber(
                R.string.gradeDiff,
                grade,
                color,
                context)

        /* Set up quarter grade sum */
        val gradeSum = when {
            quarter.type == QuarterType.COMPLETED -> quarter.gradeSum
            isDemo -> (quarter.gradeSum + quarter.subjects[0].grade) / 2 /* Emulate grade sum */
            !isDemo -> context.getDatabase().computeGradeSumFromQuarter(quarter)
            else -> quarter.gradeSum
        }

        val gradeSumColor = when {
            quarter.type == QuarterType.COMPLETED -> color
            gradeSum < 3.0 -> colorWarning
            else -> color
        }

        if (gradeSumColor != color)
            quarterGradeSum.lookAtMe(1.5f)

        quarterGradeSum.text = spanNumber(R.string.gradeSum,
                gradeSum,
                gradeSumColor,
                context)

        /* Set up quarter credits */
        val credits = when {
            grade == 0.0 -> 0
            quarter.type == QuarterType.COMPLETED || isDemo -> quarter.subjects.sumBy { it.credits }
            else -> context.getDatabase().getQuarterCredits(quarter)
        }

        val creditsColor = when {
            quarter.type == QuarterType.COMPLETED -> color
            (!isDemo && !quarter.isOk(credits)) -> colorWarning
            (isDemo && credits == 0) -> colorWarning
            else -> color
        }

        if (creditsColor != color)
            quarterCredits.lookAtMe(1.5f)

        quarterCredits.text = spanNumber(R.string.quarterCredits,
                credits,
                creditsColor,
                context)

        /* Set up quarter color */
        quarterColor?.setBackgroundColor(color)

        /* Set up quarter title */
        quarterTitle?.text = quarter.getName()

        /* Set up remove */
        quarterRemove?.visibility =
                if (quarter.type == QuarterType.GUESS)
                    View.VISIBLE
                else
                    View.GONE
    }

    private fun setUpSubject(quarter: DstQuarter,
                             subject: DstSubject,
                             quarterGrade: TextView,
                             quarterGradeSum: TextView,
                             quarterCredits: TextView,
                             subjectName: TextView,
                             subjectGrade: TextView,
                             subjectCode: TextView,
                             subjectCredits: TextView,
                             barGrade: SeekBar) {
        if (quarter.type == QuarterType.COMPLETED) {
            barGrade.setOnSeekBarChangeListener(null)

            barGrade.visibility = View.GONE
        } else {
            barGrade.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    if (isDemo && demoAnimator != null) {
                        val animator = demoAnimator!!

                        animator.end()

                        animator.repeatCount = 0
                        animator.repeatMode = ValueAnimator.RESTART
                        animator.setFloatValues(animator.animatedValue as Float, 1f)

                        animator.start()

                        demoAnimator = null
                    }
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) { }

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    subject.grade = progress

                    if (progress > 0)
                        subject.status = SubjectStatus.OK
                    else
                        subject.status = SubjectStatus.RETIRED

                    if (!isDemo)
                        context.getDatabase().updateSubject(subject)

                    /* Set up quarter view */
                    setUpQuarter(quarter,
                            quarterGrade,
                            quarterGradeSum,
                            quarterCredits)

                    subjectGrade.text = formatGrade(progress)

                    subjectCode.text = spanTitle(subject, context)
                }
            })

            barGrade.progress = subject.grade

            barGrade.visibility = View.VISIBLE
        }

        subjectName.text = subject.name

        subjectGrade.text = formatGrade(subject.grade)

        subjectCode.text = spanTitle(subject, context)

        subjectCredits.text = context.getString(R.string.subjectCredits, subject.credits)
    }

    private fun spanTitle(subject: DstSubject, context: Context): SpannableString {
        if (subject.status == SubjectStatus.OK)
            return SpannableString(subject.code)

        val secondaryColor = ContextCompat.getColor(context, R.color.colorBackgroundDark)

        val content = context.getString(R.string.subjectTitle, subject.code, subject.status.toString(context))

        val index = content.indexOf(' ')

        val spannableString = SpannableString(content)

        spannableString.setSpan(TypefaceSpan("sans-serif-light"),
                index, content.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannableString.setSpan(ForegroundColorSpan(secondaryColor),
                index, content.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return spannableString
    }

    private fun spanNumber(@StringRes stringRes: Int, value: Number, color: Int, context: Context): SpannableString {
        val content = context.getString(stringRes, value)

        val index = content.indexOf(' ')

        val spannableString = SpannableString(content)

        spannableString.setSpan(TextAppearanceSpan(context, R.style.TextAppearance_AppCompat_Medium),
                0, index,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannableString.setSpan(ForegroundColorSpan(color),
                0, index,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        if (stringRes == R.string.quarterCredits)
            spannableString.setSpan(com.gdavidpb.tuindice.models.TypefaceSpan(typeface),
                    0, index,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return spannableString
    }

    private fun formatGrade(grade: Int): String = if (grade > 0) context.getString(R.string.grade, grade) else "-"

    @Suppress("unchecked_cast")
    private fun <T: View> SparseArray<View>.getAs(@IdRes idRes: Int): T = get(idRes) as T

    private fun <T: View> SparseArray<View>.putAs(@IdRes idRes: Int, view: T): T {
        val hold: T = view.findViewById(idRes)

        put(idRes, hold)

        return hold
    }
}