package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.toSpannable

class LinkTextView(context: Context, attrs: AttributeSet)
    : AppCompatTextView(context, attrs) {

    private val links = hashMapOf<String, ClickableSpan>()

    private var spansProvider = { listOf<Any>() }

    init {
        movementMethod = LinkMovementMethod.getInstance()
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