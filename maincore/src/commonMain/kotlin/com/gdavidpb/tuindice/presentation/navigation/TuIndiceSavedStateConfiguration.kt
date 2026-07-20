package com.gdavidpb.tuindice.presentation.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.gdavidpb.tuindice.about.presentation.navigation.AboutNavContribution
import com.gdavidpb.tuindice.auth.presentation.navigation.AuthNavContribution
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavContribution
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.EnrollmentProofNavContribution
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsNavContribution
import com.gdavidpb.tuindice.pensum.presentation.navigation.PensumNavContribution
import com.gdavidpb.tuindice.record.presentation.navigation.RecordNavContribution
import com.gdavidpb.tuindice.subjects.presentation.navigation.SubjectsNavContribution
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryNavContribution
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private val navContributions: List<TuIndiceNavContribution> = listOf(
	MainNavContribution,
	AuthNavContribution,
	SummaryNavContribution,
	RecordNavContribution,
	EvaluationsNavContribution,
	SubjectsNavContribution,
	PensumNavContribution,
	AboutNavContribution,
	EnrollmentProofNavContribution
)

/**
 * Aggregated open-polymorphism registry for every serializable destination;
 * required by [androidx.navigation3.runtime.rememberNavBackStack] on non-JVM
 * targets to save and restore back stacks.
 */
val TuIndiceSavedStateConfiguration: SavedStateConfiguration = SavedStateConfiguration {
	serializersModule = SerializersModule {
		polymorphic(NavKey::class) {
			navContributions.forEach { contribution -> contribution.registerNavKeys(this) }
		}
	}
}
