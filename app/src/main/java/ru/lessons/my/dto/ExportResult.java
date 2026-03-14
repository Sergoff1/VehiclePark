package ru.lessons.my.dto;

public record ExportResult(byte[] data, String filename, String contentType) {}
