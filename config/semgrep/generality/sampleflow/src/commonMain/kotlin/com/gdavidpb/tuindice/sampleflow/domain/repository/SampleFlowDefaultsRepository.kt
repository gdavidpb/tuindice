package com.gdavidpb.tuindice.sampleflow.domain.repository

// Positivo de domain-repository-no-default-bodies: el contrato trae un cuerpo por defecto y un getter.
interface SampleFlowDefaultsRepository {
	fun readDefault(): String {
		return "default"
	}

	val computed: String
		get() = "default"
}
