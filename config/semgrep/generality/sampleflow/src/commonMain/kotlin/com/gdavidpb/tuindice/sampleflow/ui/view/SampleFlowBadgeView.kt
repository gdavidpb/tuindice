package com.gdavidpb.tuindice.sampleflow.ui.view

// Disparo de ui-view-no-shared-data-class: un tipo de ayuda público de una vista va en ui/model.
data class SampleFlowBadge(val label: String)

// Privado al archivo de su vista: no dispara la regla.
private data class SampleFlowBadgeParts(val text: String)
