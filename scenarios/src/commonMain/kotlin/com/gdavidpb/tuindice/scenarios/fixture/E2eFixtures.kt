package com.gdavidpb.tuindice.scenarios.fixture

/**
 * Entities the scenarios act on; ids and codes are the ones the mocks serve (`EntityFixturesTest`
 * checks each against its files). Names follow how the scenarios use them.
 */
object E2eFixtures {
	private const val SYNC_SUCCESS = "mocks/__files/sync/post-sync-success.json"
	private const val SYNC_ANNULLED_PROVISIONAL = "mocks/__files/sync/post-sync-annulled-provisional.json"
	private const val SEARCH_CATALOG = "mocks/__files/subjects/search-subjects-catalog.json"
	private const val SEARCH_PRIORITY = "mocks/__files/subjects/search-subjects-priority.json"
	private const val SEARCH_SLOTS = "mocks/__files/subjects/search-subjects-slot-eligibility.json"
	private const val PENSUM_2016 = "mocks/__files/pensums/get-pensum-2016-degree_project.json"
	private const val PENSUM_2019 = "mocks/__files/pensums/get-pensum-2019-degree_project.json"

	/** The term of the canonical record that is in progress. */
	val CurrentTerm = E2eFixture("d377155d39da686f412622aaca9074cd", listOf(SYNC_SUCCESS))

	/** The synthetic term the record creation flows add after the current one (`2026-SEP_DEC`). */
	val NextTermKey = E2eFixture.derived("2026-SEP_DEC", "term key the app builds from the period and the year")

	val PrimaryAttempt = E2eFixture("e21f0d7e481d13d19f328d6818ce4d77", listOf(SYNC_SUCCESS))
	val SecondaryAttempt = E2eFixture("8322eaae7602f4749aef5691b9724af2", listOf(SYNC_SUCCESS))
	val ClashingAttempt = E2eFixture("722cc2cc935e0aefdc73f9ebf9d2f1b6", listOf(SYNC_SUCCESS))
	val UnscheduledAttempt = E2eFixture("f47ff0fa7342cdaec37d9e9cff489ebd", listOf(SYNC_SUCCESS))

	/** Only the annulled-provisional record has it. */
	val WithdrawnAttempt = E2eFixture("5288ee9acb2b3a199331d0dc908a494a", listOf(SYNC_ANNULLED_PROVISIONAL))

	val SyntheticDegreeProjectTerm = E2eFixture("synthetic-2026-jul-aug-degree-project", listOf(SYNC_SUCCESS))
	val SyntheticEp1308Attempt = E2eFixture("synthetic-2026-jul-aug-ep1308", listOf(SYNC_SUCCESS))

	val GradedEvaluation = E2eFixture(
		value = "741ee22ed3a76b1bbd7deb77e9490435",
		declaredIn = listOf("mocks/config/evaluations-base-state.json")
	)

	val SubjectEc5333 = E2eFixture("EC5333", listOf(SEARCH_CATALOG))
	val SubjectEc5201 = E2eFixture("EC5201", listOf(SEARCH_CATALOG))
	val SubjectCi2511 = E2eFixture("CI2511", listOf(SEARCH_CATALOG))
	val SubjectMa1111 = E2eFixture("MA1111", listOf(SYNC_SUCCESS, PENSUM_2016))
	val SubjectMa1112 = E2eFixture("MA1112", listOf(SYNC_SUCCESS, SEARCH_CATALOG))
	val SubjectMa1121 = E2eFixture("MA1121", listOf(SYNC_SUCCESS, SEARCH_CATALOG))
	val SubjectEp1308 = E2eFixture("EP1308", listOf(SEARCH_PRIORITY, PENSUM_2016))
	val SubjectEp2308 = E2eFixture("EP2308", listOf(SEARCH_PRIORITY, PENSUM_2016))
	val SubjectEp5855 = E2eFixture("EP5855", listOf(PENSUM_2016))
	val SubjectAa1001 = E2eFixture("AA1001", listOf(SEARCH_PRIORITY))
	val SubjectAb1001 = E2eFixture("AB1001", listOf(SEARCH_PRIORITY))
	val SubjectCi5312 = E2eFixture("CI5312", listOf(SEARCH_SLOTS))
	val SubjectEg1511 = E2eFixture("EG1511", listOf(SEARCH_SLOTS))
	val SubjectEg1114 = E2eFixture("EG1114", listOf(SYNC_SUCCESS))

	/** Pensum nodes, as the tags of the pensum canvas name them. */
	val PensumNodeMath1 = E2eFixture("0800-2019-degree_project-t1-math1-ma1111", listOf(PENSUM_2019))
	val PensumNodeMath2 = E2eFixture("0800-2019-degree_project-t2-math2-ma1112", listOf(PENSUM_2019))
	val PensumNodeLanguage1 = E2eFixture(
		"0800-2019-degree_project-t1-lla111",
		listOf("mocks/__files/pensums/get-pensum-equivalence-success.json")
	)
	val PensumNodeLongInternshipMath1 = E2eFixture(
		"0800-2018-long_internship-t1-math1-ma1111",
		listOf("mocks/__files/pensums/get-pensum-2018-long_internship.json")
	)

	/** Search queries and the subjects the search mocks return for them (the record search flows type these). */
	val recordSearches: List<SearchExpectation> = listOf(
		SearchExpectation("ec", listOf(SubjectEc5333.value)),
		SearchExpectation("ma", listOf(SubjectMa1112.value, SubjectMa1121.value)),
		SearchExpectation("ma1111", listOf(SubjectMa1111.value)),
		SearchExpectation(
			"pr",
			listOf(SubjectAa1001.value, SubjectAb1001.value, SubjectEp1308.value, SubjectEp2308.value)
		),
		SearchExpectation("slot", listOf(SubjectCi5312.value, SubjectEg1511.value))
	)

	/** Every fixture that names a mock file, for the tests that check them. */
	val all: List<E2eFixture> = listOf(
		CurrentTerm,
		NextTermKey,
		PrimaryAttempt,
		SecondaryAttempt,
		ClashingAttempt,
		UnscheduledAttempt,
		WithdrawnAttempt,
		SyntheticDegreeProjectTerm,
		SyntheticEp1308Attempt,
		GradedEvaluation,
		SubjectEc5333,
		SubjectEc5201,
		SubjectCi2511,
		SubjectMa1111,
		SubjectMa1112,
		SubjectMa1121,
		SubjectEp1308,
		SubjectEp2308,
		SubjectEp5855,
		SubjectAa1001,
		SubjectAb1001,
		SubjectCi5312,
		SubjectEg1511,
		SubjectEg1114,
		PensumNodeMath1,
		PensumNodeMath2,
		PensumNodeLanguage1,
		PensumNodeLongInternshipMath1
	)
}
