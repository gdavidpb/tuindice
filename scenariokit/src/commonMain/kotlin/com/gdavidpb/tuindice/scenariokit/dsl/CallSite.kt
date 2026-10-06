package com.gdavidpb.tuindice.scenariokit.dsl

import com.gdavidpb.tuindice.scenariokit.model.Site

/**
 * Source position of the scenario line that called the DSL, or null where it cannot be
 * known. Only the JVM can see it; iOS only ever runs steps decoded from the catalog.
 */
internal expect fun captureCallSite(): Site?
