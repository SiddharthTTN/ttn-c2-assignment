package com.ttn.support.web.error;

import java.util.List;

public record ApiErrorResponse(String error, List<String> details) {}
