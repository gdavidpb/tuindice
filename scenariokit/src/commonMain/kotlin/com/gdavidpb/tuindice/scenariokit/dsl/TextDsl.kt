package com.gdavidpb.tuindice.scenariokit.dsl

import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Step

/** Types [text] as key events; a non-secure field is re-read and must show [expect] (default: [text]). */
fun StepBuilder.enterText(tag: String, text: String, expect: String? = null, replace: Boolean = false) =
	add(Step.EnterText(Query.Tag(tag), text, expect, false, replace, site()))

/** Types into a secure field, which cannot show its text: it is judged by its length. */
fun StepBuilder.enterSecureText(tag: String, text: String, replace: Boolean = false) =
	add(Step.EnterText(Query.Tag(tag), text, null, true, replace, site()))

fun StepBuilder.enterText(query: Query, text: String, expect: String? = null, replace: Boolean = false) =
	add(Step.EnterText(query, text, expect, false, replace, site()))

/** Sends the IME action (search, done, go) of the focused field, as its keyboard's action key does. */
fun StepBuilder.submitTextEntry() = add(Step.SubmitTextEntry(site()))
