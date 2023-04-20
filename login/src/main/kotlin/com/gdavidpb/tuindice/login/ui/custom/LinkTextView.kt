package com.gdavidpb.tuindice.login.ui.custom

import android.content.Context
import android.graphics.Rect
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import androidx.core.text.toSpannable
import com.google.android.material.textview.MaterialTextView

open class LinkTextView(context: Context, attrs: AttributeSet)
    : MaterialTextView(context, attrs) {

    private val links = hashMapOf<String, ClickableSpan>()

    private var spansProvider = { listOf<Any>() }

    init {
        movementMethod = LinkMovementMethod.getInstance()
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        return false
    }

    fun setSpans(provider: () -> List<Any>) {
        spansProvider = provider
    }

    fun setLink(text: String, onClick: () -> Unit) {
        links[text.trim()] = object : ClickableSpan() {
            override fun onClick(widget: View) {
                onClick()
            }
        }
    }

    fun build() {
        val inputText = "$text".trim()
        val outputText = inputText.toSpannable().apply {
            links.forEach { (link, clickSpan) ->
                val pattern = link.toPattern()
                val matcher = pattern.matcher(inputText)

                while (matcher.find())
                    (spansProvider() + clickSpan).forEach { span ->
                        setSpan(span, matcher.start(), matcher.end(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                    }
            }
        }

        text = outputText
    }
}