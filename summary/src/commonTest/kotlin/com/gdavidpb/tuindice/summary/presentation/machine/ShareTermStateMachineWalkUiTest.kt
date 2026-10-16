package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.domain.model.ShareTermSource
import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.presentation.model.ShareCardPicture
import com.gdavidpb.tuindice.summary.testing.otherShareBackground
import com.gdavidpb.tuindice.summary.testing.shareTermMachineOf
import com.gdavidpb.tuindice.summary.testing.statisticsTerm
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * Seeded random walk for the share sheet's machine. UiTest-suffixed like the Statistics walk, so it runs on the iOS
 * gate with the Compose-backed resources; the static validators stay on the host in ShareTermStateMachineContractTest.
 *
 * The walk leaves out the failed observation. It ends the sheet on the Unavailable state, which nothing leaves, so a
 * walk that met it would stop visiting the rows of the terms. That path is covered by ShareTermViewModelContractTest.
 */
class ShareTermStateMachineWalkUiTest {
	@Test
	fun when_seededRandomWalkRuns_then_machineSurvives() = runTest {
		runShareTermMachineWalk(seed = 0x5A4E7E41)
	}

	@Test
	fun when_alternateSeededRandomWalkRuns_then_machineSurvives() = runTest {
		runShareTermMachineWalk(seed = 0x7E4B1D05)
	}

	private suspend fun TestScope.runShareTermMachineWalk(seed: Long) {
		val source = ShareTermSource(
			closedTerms = listOf(
				statisticsTerm(year = 2024),
				statisticsTerm(year = 2025, periodCode = AcademicTermPeriod.JAN_MAR, index = 3.5)
			),
			careerName = "Ingeniería de Computación",
			backgrounds = listOf(ShareBackground.included, otherShareBackground),
			preferredBackgroundId = null
		)

		assertMachineRandomWalk(
			screenMachine = shareTermMachineOf(),
			seed = seed,
			sampleEvents = listOf(
				ShareTerm.Action.ObserveShareTerm(termKey = null),
				ShareTerm.Action.SelectTerm(termKey = source.closedTerms.first().termKey),
				ShareTerm.Action.SelectBackground(backgroundId = otherShareBackground.id),
				ShareTerm.Action.ShareImage(picture = ShareCardPicture { ByteArray(0) }),
				ShareTermInternalEvent.TermsObserved(source = source),
				ShareTermInternalEvent.ImageShared,
				ShareTermInternalEvent.ImageShareFailed
			),
			coroutineScope = backgroundScope,
			// Conservative floor, as the Statistics walk: raise it to the observed coverage once the walk has run on CI.
			minRowCoverage = 0.4
		)
	}
}
