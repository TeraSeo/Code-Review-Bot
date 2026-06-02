package com.codebot.review.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChangedFile(
        String filename,
        String patch
) {}
