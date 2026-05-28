package com.gdavidpb.tuindice.pensum.data.mapper

import com.gdavidpb.tuindice.pensum.data.model.GetPensumResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class PensumResponseMapperTest {
	@Test
	fun mapsIndexAndSelectedPensumResponse() {
		val response = sampleResponse()

		assertEquals("2019-degree_project", response.cacheKey())
		assertEquals(listOf(2016, 2019), response.toAvailablePensums().map { option -> option.year })
		assertEquals(listOf("degree_project", "long_internship"), response.toAvailableModalities().map { modality -> modality.id })
		assertEquals("computacion-2019-degree-project", response.toGraph().id)
		assertEquals(listOf("computacion-2019-degree-project"), response.toGraphs().map { graph -> graph.id })
	}

	private fun sampleResponse(): GetPensumResponse {
		return GetPensumResponse(
			careerName = "Computacion",
			selectedPensumId = "computacion-2019-degree-project",
			inferred = false,
			availablePensums = listOf(
				GetPensumResponse.AvailablePensum(year = 2019),
				GetPensumResponse.AvailablePensum(year = 2016)
			),
			availableModalities = listOf(
				GetPensumResponse.AvailableModality(
					id = "degree_project",
					name = "Proyecto de Grado",
					isDefault = true
				),
				GetPensumResponse.AvailableModality(
					id = "long_internship",
					name = "Pasantia Larga",
					isDefault = false
				)
			),
			pensum = GetPensumResponse.Pensum(
				id = "computacion-2019-degree-project",
				year = 2019,
				modalityId = "degree_project",
				modalityName = "Proyecto de Grado",
				totalCredits = 170,
				canvas = GetPensumResponse.Canvas(width = 1280.0, height = 1600.0),
				terms = listOf(
					GetPensumResponse.Term(id = "T1", label = "T1", x = 0.0, width = 200.0)
				),
				nodes = listOf(
					GetPensumResponse.Node(
						id = "ec5344",
						nodeType = "COURSE",
						displayCode = "EC5344",
						subjectCode = "EC5344",
						name = "Radiacion y Antenas",
						credits = 3,
						category = "PROFESSIONAL",
						termId = "T1",
						x = 40.0,
						y = 80.0,
						width = 160.0,
						height = 120.0
					)
				),
				edges = emptyList()
			)
		)
	}
}
