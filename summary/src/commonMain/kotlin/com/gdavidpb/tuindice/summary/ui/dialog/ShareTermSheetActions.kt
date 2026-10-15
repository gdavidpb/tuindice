package com.gdavidpb.tuindice.summary.ui.dialog

import com.gdavidpb.tuindice.summary.presentation.model.ShareCardPicture

/**
 * What the share sheet hands on when the student acts in it: the term that was tapped, the background that was tapped,
 * and the captured card when it is shared.
 */
data class ShareTermSheetActions(
	val onTermClick: (termKey: String) -> Unit,
	val onBackgroundClick: (backgroundId: String) -> Unit,
	val onShareClick: (picture: ShareCardPicture) -> Unit
)
