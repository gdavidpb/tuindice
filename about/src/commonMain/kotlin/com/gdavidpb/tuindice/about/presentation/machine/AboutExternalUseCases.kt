package com.gdavidpb.tuindice.about.presentation.machine

import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase

// The use cases that hand the student over to another app (mail, browser), grouped to keep the machine's constructor
// short.
class AboutExternalUseCases(
	val sendSupportEmail: SendSupportEmailUseCase,
	val openExternalUrl: OpenExternalUrlUseCase
)
