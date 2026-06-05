package com.gdavidpb.tuindice.pensum.ui

object PensumUiTags {
	const val PensumScreen = "pensum_screen"
	const val PensumContextSummary = "pensum_context_summary"
	const val PensumCurrentSelectionSummary = "pensum_current_selection_summary"
	const val PensumSelector = "pensum_selector"
	const val ModalitySelector = "pensum_modality_selector"
	const val Canvas = "pensum_canvas"
	const val CanvasLegend = "pensum_canvas_legend"
	const val FitToScreen = "pensum_fit_to_screen"
	const val FocusProgress = "pensum_focus_progress"
	const val MinimapToggle = "pensum_minimap_toggle"
	const val ZoomIn = "pensum_zoom_in"
	const val ZoomOut = "pensum_zoom_out"
	const val Minimap = "pensum_minimap"
	const val StickyTerms = "pensum_sticky_terms"
	fun versionOption(year: Int): String = "pensum_version_option_$year"
	fun modalityOption(id: String): String = "pensum_modality_option_$id"
	fun node(id: String): String = "pensum_node_$id"
	fun nodeSubjectStatsButton(id: String): String = "pensum_node_subject_stats_button_$id"
}
