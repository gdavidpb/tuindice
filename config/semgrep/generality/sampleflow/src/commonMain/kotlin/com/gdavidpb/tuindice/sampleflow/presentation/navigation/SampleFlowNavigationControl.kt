package com.gdavidpb.tuindice.sampleflow.presentation.navigation

import androidx.compose.ui.text.TextStyle
import com.gdavidpb.tuindice.sampleflow.ui.view.SampleFlowBadge

// Control negativo: navigation monta pantallas y queda fuera del alcance de las reglas de importación de presentación.
fun sampleFlowNavigationControl(style: TextStyle, badge: SampleFlowBadge): Any = style to badge
