package com.gdavidpb.tuindice.summary.ui.share

// The tags of the share sheet that name a term by its key or a background by its id. The ring is drawn on the chosen
// background only, so a test of the choice finds one ring or none. The chosen term and background carry a tag of their
// own too, on a node not merged into its chip or tile: the E2E driver reads the merged tree, where a child's tag is
// not seen.
object ShareTermUiTags {
	fun term(termKey: String): String = "summary_share_term_term_$termKey"

	fun termSelected(termKey: String): String = "summary_share_term_term_selected_$termKey"

	fun background(backgroundId: String): String = "summary_share_term_background_$backgroundId"

	fun backgroundSelected(backgroundId: String): String = "summary_share_term_background_selected_$backgroundId"

	fun backgroundRing(backgroundId: String): String = "summary_share_term_background_ring_$backgroundId"
}
