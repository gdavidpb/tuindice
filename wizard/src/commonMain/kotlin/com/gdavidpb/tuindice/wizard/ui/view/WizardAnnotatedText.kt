package com.gdavidpb.tuindice.wizard.ui.view

import androidx.compose.ui.text.AnnotatedString
import com.gdavidpb.tuindice.base.ui.view.toBoldMarkerAnnotatedText

internal fun String.toWizardAnnotatedText(): AnnotatedString {
	return toBoldMarkerAnnotatedText()
}
