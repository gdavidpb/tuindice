package com.gdavidpb.tuindice.summary.ui.view

// The tags of the parts that a section header adds beside its title. They are kept apart from SummaryUiTags, which
// holds the constants and the templates of the hub and the statistics.
object SummarySectionUiTags {
	// The mark that explains a section, named after the section's own tag as its title is.
	fun info(sectionTag: String): String = "summary_section_info_$sectionTag"
}
