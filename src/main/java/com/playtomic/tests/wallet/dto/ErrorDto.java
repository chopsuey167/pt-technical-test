package com.playtomic.tests.wallet.dto;

import org.springframework.http.HttpStatus;

public record ErrorDto(HttpStatus status, String message) {}
