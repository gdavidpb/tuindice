package com.gdavidpb.tuindice.scenarios.shared

import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.enterText
import com.gdavidpb.tuindice.scenariokit.dsl.submitTextEntry
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags

/** From the pensum: opens the subject search from its top bar and waits for the search screen. */
fun StepBuilder.openSubjectSearch() {
	tap(BaseUiTags.topBarActionButton(TopBarAction.SearchPensumAction))
	waitVisible(SubjectsUiTags.SearchScreen, Within.Action)
}

/** Types [query] in the search field and sends its search action: the keyboard goes, the results are not behind it. */
fun StepBuilder.searchSubjectsFor(query: String) {
	tap(SubjectsUiTags.SearchTextField)
	enterText(SubjectsUiTags.SearchTextField, query)
	submitTextEntry()
}
