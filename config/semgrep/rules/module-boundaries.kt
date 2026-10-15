package com.gdavidpb.tuindice.alphaflow.presentation

// ok: feature-module-no-cross-import
import com.gdavidpb.tuindice.alphaflow.domain.AlphaModel
// ok: feature-module-no-cross-import
import com.gdavidpb.tuindice.base.presentation.ViewState
// ok: feature-module-no-cross-import
import com.gdavidpb.tuindice.persistence.domain.repository.SharedReadRepository
// ok: feature-module-no-cross-import
import com.gdavidpb.tuindice.academiccore.domain.engine.SharedEngine
// ok: feature-module-no-cross-import
import com.gdavidpb.tuindice.security.data.source.SecureStore
// ok: feature-module-no-cross-import
import com.gdavidpb.tuindice.testkit.koin.assertResolves
// ok: feature-module-no-cross-import
import com.gdavidpb.tuindice.scenariokit.Step
// ruleid: feature-module-no-cross-import
import com.gdavidpb.tuindice.betaflow.presentation.contract.Beta
// ruleid: feature-module-no-cross-import
import com.gdavidpb.tuindice.gammaflow.domain.model.Gamma
// ok: feature-module-no-cross-import
import kotlinx.coroutines.flow.Flow

class AlphaMapper
