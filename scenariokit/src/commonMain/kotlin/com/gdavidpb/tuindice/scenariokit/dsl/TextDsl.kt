package com.gdavidpb.tuindice.scenariokit.dsl

import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.TextEntryMode

/** Types [text] as key events; a non-secure field is re-read and must show [expect] (default: [text]). */
fun StepBuilder.enterText(tag: String, text: String, expect: String? = null, replace: Boolean = false) =
	add(Step.EnterText(Query.Tag(tag), text, expect, false, replace, TextEntryMode.Keys, site()))

/** Types into a secure field, which cannot be read back. */
fun StepBuilder.enterSecureText(tag: String, text: String, replace: Boolean = false) =
	add(Step.EnterText(Query.Tag(tag), text, null, true, replace, TextEntryMode.Keys, site()))

/** Assigns [text] atomically instead of typing it. */
fun StepBuilder.setText(tag: String, text: String, expect: String? = null) =
	add(Step.EnterText(Query.Tag(tag), text, expect, false, true, TextEntryMode.Set, site()))

fun StepBuilder.enterText(query: Query, text: String, expect: String? = null, replace: Boolean = false) =
	add(Step.EnterText(query, text, expect, false, replace, TextEntryMode.Keys, site()))

fun StepBuilder.clearText(tag: String) = add(Step.ClearText(Query.Tag(tag), site()))

fun StepBuilder.finishTextEntry() = add(Step.FinishTextEntry(site()))
