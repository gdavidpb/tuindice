package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * The indicator the sheet shows while its terms are read: a room of the height the terms take, so that the sheet does
 * not shrink when they arrive.
 */
@Composable
fun ShareTermSheetLoading() {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(LoadingAreaHeight),
		contentAlignment = Alignment.Center
	) {
		CircularProgressIndicator(
			modifier = Modifier.testTag(SummaryUiTags.ShareTermLoadingIndicator)
		)
	}
}

// The room the sheet keeps while its terms are read, so that it does not shrink when they arrive.
private val LoadingAreaHeight = 120.dp
