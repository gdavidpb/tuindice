package com.gdavidpb.tuindice.domain.model.exception

class ParseException(message: String) : IllegalStateException("Unable to parse '$message'.")