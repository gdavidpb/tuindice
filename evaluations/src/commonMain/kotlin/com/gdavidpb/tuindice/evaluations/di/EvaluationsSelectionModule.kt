package com.gdavidpb.tuindice.evaluations.di

import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.evaluations.data.source.SelectedWeekWriter
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

// The week the Evaluations tab opens on again, and the one holder that writes it. It keeps state of the
// account, so it is a SessionMemory that the end of a session lets go of.
val evaluationsSelectionModule = module {
	singleOf(::SelectedWeekWriter) { bind<SessionMemory>() }
}
