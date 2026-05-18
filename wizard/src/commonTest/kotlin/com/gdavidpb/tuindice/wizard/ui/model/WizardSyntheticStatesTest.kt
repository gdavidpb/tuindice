package com.gdavidpb.tuindice.wizard.ui.model

import com.gdavidpb.tuindice.pensum.presentation.model.PensumDisplayLayoutDefaults
import kotlin.test.Test
import kotlin.test.assertEquals

class WizardSyntheticStatesTest {
	@Test
	fun when_samplePensumIsBuilt_then_usesNormalPensumInternalSpacing() {
		val model = samplePensumState().model

		assertEquals(PensumDisplayLayoutDefaults.TermWidth, model.terms.first().width)
		assertEquals(PensumDisplayLayoutDefaults.TermWidth, model.terms[1].x - model.terms[0].x)

		model.nodes.groupBy { node -> node.termId }.values.forEach { termNodes ->
			val sortedNodes = termNodes.sortedBy { node -> node.y }

			assertEquals(PensumDisplayLayoutDefaults.FirstNodeTop, sortedNodes.first().y)
			sortedNodes.zipWithNext().forEach { (currentNode, nextNode) ->
				assertEquals(
					PensumDisplayLayoutDefaults.NodeVerticalGap,
					nextNode.y - (currentNode.y + currentNode.height)
				)
			}
		}
	}

	@Test
	fun when_samplePensumIsBuilt_then_usesNormalPensumCanvasPadding() {
		val model = samplePensumState().model
		val contentRight = model.terms.maxOf { term -> term.x + term.width }
		val contentBottom = model.nodes.maxOf { node -> node.y + node.height }

		assertEquals(contentRight + PensumDisplayLayoutDefaults.CanvasRightPadding, model.canvas.width)
		assertEquals(contentBottom + PensumDisplayLayoutDefaults.CanvasBottomPadding, model.canvas.height)
	}
}
