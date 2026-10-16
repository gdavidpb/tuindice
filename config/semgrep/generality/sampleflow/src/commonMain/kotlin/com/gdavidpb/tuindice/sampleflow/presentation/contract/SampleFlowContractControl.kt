package com.gdavidpb.tuindice.sampleflow.presentation.contract

// Control negativo: una data class private es detalle de implementación del contrato. La regla
// presentation-no-toplevel-data-class no la marca, y este control comprueba que la exención por visibilidad se mantiene.
private data class SampleFlowPrivateHelper(val label: String)

fun sampleFlowPrivateHelper(label: String): Any = SampleFlowPrivateHelper(label)
