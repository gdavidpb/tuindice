package com.gdavidpb.tuindice.summary.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.Guideline
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.base.utils.extension.animatePercent
import com.gdavidpb.tuindice.base.utils.extension.loadAttributes
import com.gdavidpb.tuindice.base.utils.extension.view
import com.gdavidpb.tuindice.summary.R
import com.google.android.material.textview.MaterialTextView

class StatusCardView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

	private val tViewHeader by view<MaterialTextView>(R.id.tViewHeader)
	private val guidelineApproved by view<Guideline>(R.id.guidelineApproved)
	private val guidelineFailed by view<Guideline>(R.id.guidelineFailed)
	private val guidelineRetired by view<Guideline>(R.id.guidelineRetired)
	private val vRetired by view<View>(R.id.vRetired)
	private val tViewApprovedLabel by view<MaterialTextView>(R.id.tViewApprovedLabel)
	private val tViewFailedLabel by view<MaterialTextView>(R.id.tViewFailedLabel)
	private val tViewRetiredLabel by view<MaterialTextView>(R.id.tViewRetiredLabel)
	private val tViewApprovedCount by view<MaterialTextView>(R.id.tViewApprovedCount)
	private val tViewFailedCount by view<MaterialTextView>(R.id.tViewFailedCount)
	private val tViewRetiredCount by view<MaterialTextView>(R.id.tViewRetiredCount)

	private object Defaults {
		const val TEXT = ""
		const val COUNT = 0
	}

	var headerText: CharSequence = Defaults.TEXT
		get() = tViewHeader.text
		set(value) {
			field = value
			tViewHeader.text = value
		}

	var approvedCount: Int = Defaults.COUNT
	var failedCount: Int = Defaults.COUNT
	var retiredCount: Int = Defaults.COUNT

	private val approvedText: CharSequence
	private val failedText: CharSequence
	private val retiredText: CharSequence

	init {
		inflate(context, R.layout.view_status_card_view, this)

		loadAttributes(R.styleable.StatusCardView, attrs).apply {
			headerText = getString(R.styleable.StatusCardView_headerText) ?: Defaults.TEXT
			approvedText = getString(R.styleable.StatusCardView_approvedText) ?: Defaults.TEXT
			failedText = getString(R.styleable.StatusCardView_failedText) ?: Defaults.TEXT
			retiredText = getString(R.styleable.StatusCardView_retiredText) ?: Defaults.TEXT

			approvedCount = getInt(R.styleable.StatusCardView_approvedCount, Defaults.COUNT)
			failedCount = getInt(R.styleable.StatusCardView_failedCount, Defaults.COUNT)
			retiredCount = getInt(R.styleable.StatusCardView_retiredCount, Defaults.COUNT)
		}.recycle()

		tViewApprovedLabel.text = approvedText
		tViewFailedLabel.text = failedText
		tViewRetiredLabel.text = retiredText

		notifyChanges(animate = false)
	}

	fun notifyChanges(animate: Boolean = true) {
		val total = (approvedCount + failedCount + retiredCount).toFloat()

		if (total != 0f) {
			vRetired.alpha = 1f
		} else {
			vRetired.alpha = 0.25f
			return
		}

		val approvedPercent = approvedCount / total
		val failedPercent = failedCount / total

		if (animate) {
			guidelineApproved.animatePercent(approvedPercent)
			guidelineFailed.animatePercent(approvedPercent + failedPercent)
			guidelineRetired.animatePercent(1f)
		} else {
			guidelineApproved.setGuidelinePercent(approvedPercent)
			guidelineFailed.setGuidelinePercent(approvedPercent + failedPercent)
			guidelineRetired.setGuidelinePercent(1f)
		}

		mapOf(
			tViewApprovedCount to approvedCount,
			tViewFailedCount to failedCount,
			tViewRetiredCount to retiredCount
		).forEach { (textView, value) ->
			if (value > 0) {
				textView.text = "$value"
				textView.isVisible = true
			} else
				textView.isInvisible = true
		}
	}
}