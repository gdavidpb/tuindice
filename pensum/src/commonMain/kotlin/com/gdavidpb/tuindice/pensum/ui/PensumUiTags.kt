package com.gdavidpb.tuindice.pensum.ui

object PensumUiTags {
	const val PensumScreen = "pensum_screen"
	const val PensumSelector = "pensum_selector"
	const val ModalitySelector = "pensum_modality_selector"
	const val Canvas = "pensum_canvas"
	const val ZoomIn = "pensum_zoom_in"
	const val ZoomOut = "pensum_zoom_out"
	const val Minimap = "pensum_minimap"
	fun versionOption(year: Int): String = "pensum_version_option_$year"
	fun node(id: String): String = "pensum_node_$id"
	fun nodeSubjectStatsButton(id: String): String = "pensum_node_subject_stats_button_$id"
}
