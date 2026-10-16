package com.gdavidpb.tuindice.scenarios.fixture

/**
 * Entities the scenarios act on; ids and codes are the ones the mocks serve (`EntityFixturesTest`
 * checks each against its files). Names follow how the scenarios use them.
 */
object E2eFixtures {
	private const val SYNC_SUCCESS = "mocks/__files/sync/post-sync-success.json"
	private const val SYNC_ANNULLED_PROVISIONAL = "mocks/__files/sync/post-sync-annulled-provisional.json"
	private const val SYNC_SUMMARY_HUB = "mocks/__files/sync/post-sync-summary-hub.json"
	private const val EVALUATIONS_SUMMARY_HUB = "mocks/__files/evaluations/get-evaluations-summary-hub.json"
	private const val SHARE_BACKGROUNDS_CATALOG = "mocks/__files/share/get-share-backgrounds-catalog.json"
	private val HUB_FILES = listOf(SYNC_SUMMARY_HUB, EVALUATIONS_SUMMARY_HUB)
	private const val SEARCH_CATALOG = "mocks/__files/subjects/search-subjects-catalog.json"
	private const val SEARCH_PRIORITY = "mocks/__files/subjects/search-subjects-priority.json"
	private const val SEARCH_SLOTS = "mocks/__files/subjects/search-subjects-slot-eligibility.json"
	private const val PENSUM_2016 = "mocks/__files/pensums/get-pensum-2016-degree_project.json"
	private const val PENSUM_2019 = "mocks/__files/pensums/get-pensum-2019-degree_project.json"

	/**
	 * The new password the update scenarios type for `E2eAccounts.UpdatePassword`; the reissue mapping
	 * `auth-update-password-reissue-success.json` accepts it (`AccountFixturesTest` checks that).
	 */
	const val UpdatedPassword = "123456"

	/**
	 * The new password the failure scenario types for `E2eAccounts.UpdatePasswordFailure`; the mapping
	 * `auth-update-password-failure-reissue-unauthorized.json` answers it with 401.
	 */
	const val RejectedPassword = "000000"

	/**
	 * What "now" is for the app in every scenario (`DebugLaunchArguments.NOW`), so the dates the app derives from
	 * the clock never depend on the day the run happens. It falls in the September - December 2026 term:
	 * the first one the app offers when planning a term on top of the canonical record (whose latest
	 * term is July - August 2026), which is what [NextTermKey] and `Copy.TermSepDec2026` name.
	 * `E2eClockFixtureTest` checks both against this date.
	 */
	const val Now = "2026-10-15T12:00:00Z"

	/** The term of the canonical record that is in progress. */
	val CurrentTerm = E2eFixture("d377155d39da686f412622aaca9074cd", listOf(SYNC_SUCCESS))

	/** The synthetic term the record creation scenarios add after the latest one of the record (`2026-SEP_DEC`). */
	val NextTermKey = E2eFixture.derived("2026-SEP_DEC", "term key the app builds from the period and the year")

	val PrimaryAttempt = E2eFixture("e21f0d7e481d13d19f328d6818ce4d77", listOf(SYNC_SUCCESS))
	val SecondaryAttempt = E2eFixture("8322eaae7602f4749aef5691b9724af2", listOf(SYNC_SUCCESS))
	val ClashingAttempt = E2eFixture("722cc2cc935e0aefdc73f9ebf9d2f1b6", listOf(SYNC_SUCCESS))
	val UnscheduledAttempt = E2eFixture("f47ff0fa7342cdaec37d9e9cff489ebd", listOf(SYNC_SUCCESS))

	/** Only the annulled-provisional record has it. */
	val WithdrawnAttempt = E2eFixture("5288ee9acb2b3a199331d0dc908a494a", listOf(SYNC_ANNULLED_PROVISIONAL))

	/**
	 * The current term of the hub accounts (`E2eAccounts.SummaryHub`): September - December 2026, whose classes start on
	 * Monday 2026-09-28, so the frozen clock (Thursday 2026-10-15) is in week 3 of 12.
	 */
	val HubTerm = E2eFixture("3c0872e9c59fdb202d301f275f205d15", HUB_FILES)

	/**
	 * The four attempts of that term. Thursday is covered by blocks 1 to 8 without a gap (CI5311 1-2, CI5437 3-4, EG1114
	 * 5-6, CI4325 7-8), so a class is in progress at 12:00Z in any zone from UTC-4 to UTC+3. CI5311 and CI5437 overlap
	 * on Monday (block 2) and the university reports a clash on CI5437.
	 */
	val HubAttemptCi5311 = E2eFixture("bcf78551a4690db6408d7929979d1710", HUB_FILES)
	val HubAttemptCi5437 = E2eFixture("fce0ba865d683cab65dffe3272cd14bf", HUB_FILES)
	val HubAttemptEg1114 = E2eFixture("dc405fba9704f392f4af205c7b95b185", HUB_FILES)
	val HubAttemptCi4325 = E2eFixture("2ce4c3bc5329a36fc59a24800847e5ee", HUB_FILES)

	/**
	 * The evaluations of that term, each at 12:00Z of its day so every zone reads the same calendar day. Week 1: a graded
	 * quiz of CI5311. Week 2: an ungraded test of CI4325 (Tuesday 6, the oldest). Week 3: an ungraded test of CI5311 on
	 * Monday 12 (overdue, class day), a test of CI5437 today (class blocks 3-4) and a test of EG1114 on Friday 16 (no
	 * class that day).
	 */
	val HubEvaluationGraded = E2eFixture("631e2d6ae9659ac881ca68723feb6956", listOf(EVALUATIONS_SUMMARY_HUB))
	val HubEvaluationOldest = E2eFixture("4cb72a9aeac527f60f7143d7d7044d20", listOf(EVALUATIONS_SUMMARY_HUB))
	val HubEvaluationOverdue = E2eFixture("f07ad77e4259171580e339784b1687de", listOf(EVALUATIONS_SUMMARY_HUB))
	val HubEvaluationToday = E2eFixture("29ba9acf1f4750da615caa4dfdf81cb7", listOf(EVALUATIONS_SUMMARY_HUB))
	val HubEvaluationNoClass = E2eFixture("8f596f65f45d2c1829cd131689a0f9f9", listOf(EVALUATIONS_SUMMARY_HUB))

	/**
	 * The newest and the second newest closed terms of the hub record (`StatisticsHubFixturesTest` checks the order). The
	 * finished term of the hub shares the first, and the share sheet opens on it; the second is its next chip.
	 */
	val HubNewestClosedTermKey = E2eFixture("2026-JAN_MAR", listOf(SYNC_SUMMARY_HUB))
	val HubPreviousClosedTermKey = E2eFixture("2024-SEP_DEC", listOf(SYNC_SUMMARY_HUB))

	/** The background the app ships (and the catalog serves too): what the sheet keeps without the catalog. */
	val ShareBackgroundIncluded = E2eFixture("laberinto-cromovegetal", listOf(SHARE_BACKGROUNDS_CATALOG))

	/** The catalog's second background, the first one the strip shows after the included one, in view without a swipe. */
	val ShareBackgroundSecond = E2eFixture("fuente-hidrocinetica", listOf(SHARE_BACKGROUNDS_CATALOG))

	val SyntheticDegreeProjectTerm = E2eFixture("synthetic-2026-jul-aug-degree-project", listOf(SYNC_SUCCESS))
	val SyntheticEp1308Attempt = E2eFixture("synthetic-2026-jul-aug-ep1308", listOf(SYNC_SUCCESS))

	val GradedEvaluation = E2eFixture(
		value = "741ee22ed3a76b1bbd7deb77e9490435",
		declaredIn = listOf("mocks/config/evaluations-base-state.json")
	)

	/** Subjects of the hub term; the hub tags its rows and its points of attention by code. */
	val SubjectCi5311 = E2eFixture("CI5311", listOf(SYNC_SUMMARY_HUB))
	val SubjectCi5437 = E2eFixture("CI5437", listOf(SYNC_SUMMARY_HUB))

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
	val SubjectCi4325 = E2eFixture("CI4325", listOf(SEARCH_CATALOG))

	/** Served by the stats mock that answers 404 (`subjects-detail-unavailable`). */
	val SubjectQa = E2eFixture("QA", listOf("mocks/__files/subjects/get-subject-stats-qa-unavailable.json"))

	/** Its stats fail once and then succeed (`subjects-detail-failed-retry`). */
	val SubjectQb = E2eFixture("QB", listOf("mocks/mappings/subjects/get-subject-stats-qb-success.json"))

	/** The search result `DebugSubjectsApiDataSource` returns after one transient failure for the query `rx`. */
	val SubjectRx = E2eFixture.derived("RX", "debug search result the app builds itself for the query rx")

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

	/** Search queries and the subjects the search mocks return for them (the record search scenarios type these). */
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

	/**
	 * The subject-search queries the subjects scenarios type and the subject each lists: the catalog mock serves `ci`,
	 * the debug source of the subjects module the rest (`SubjectSearchFixturesTest` checks both).
	 */
	val SubjectSearchCi = SearchExpectation("ci", listOf(SubjectCi2511.value))
	val SubjectSearchQa = SearchExpectation("qa", listOf(SubjectQa.value))
	val SubjectSearchQb = SearchExpectation("qb", listOf(SubjectQb.value))
	val SubjectSearchRx = SearchExpectation("rx", listOf(SubjectRx.value))

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
		HubTerm,
		HubAttemptCi5311,
		HubAttemptCi5437,
		HubAttemptEg1114,
		HubAttemptCi4325,
		HubEvaluationGraded,
		HubEvaluationOldest,
		HubEvaluationOverdue,
		HubEvaluationToday,
		HubEvaluationNoClass,
		HubNewestClosedTermKey,
		HubPreviousClosedTermKey,
		ShareBackgroundIncluded,
		ShareBackgroundSecond,
		SubjectCi5311,
		SubjectCi5437,
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
		SubjectCi4325,
		SubjectQa,
		SubjectQb,
		SubjectRx,
		PensumNodeMath1,
		PensumNodeMath2,
		PensumNodeLanguage1,
		PensumNodeLongInternshipMath1
	)
}
