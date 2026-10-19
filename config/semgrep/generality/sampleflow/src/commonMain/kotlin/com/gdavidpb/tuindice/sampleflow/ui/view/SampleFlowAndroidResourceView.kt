package com.gdavidpb.tuindice.sampleflow.ui.view

import androidx.compose.runtime.Composable

// Positivo de no-android-string-resources: lee un texto de R.string en lugar de composeResources.
@Composable
fun SampleFlowAndroidResourceView(): String {
	return R.string.sample_label.toString()
}
