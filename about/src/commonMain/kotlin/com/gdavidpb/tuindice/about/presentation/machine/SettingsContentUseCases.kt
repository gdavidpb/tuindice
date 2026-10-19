package com.gdavidpb.tuindice.about.presentation.machine

import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveProfileIdentityUseCase

// What the content of Settings is made of: the version it shows in the About row and the identity of the student for
// its card. Grouped to keep the machine's constructor short.
class SettingsContentUseCases(
	val loadVersion: LoadVersionUseCase,
	val observeProfileIdentity: ObserveProfileIdentityUseCase
)
