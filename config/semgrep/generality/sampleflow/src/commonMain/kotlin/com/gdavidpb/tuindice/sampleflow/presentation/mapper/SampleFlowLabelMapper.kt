package com.gdavidpb.tuindice.sampleflow.presentation.mapper

import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.sampleflow.ui.view.SampleFlowBadge

// Disparos de presentation-no-persistence-import y presentation-no-ui-layer-import.
fun sampleFlowLabel(progress: PensumCreditsProgress, badge: SampleFlowBadge): Any = progress to badge
