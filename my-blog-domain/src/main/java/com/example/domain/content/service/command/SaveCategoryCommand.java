package com.example.domain.content.service.command;

public record SaveCategoryCommand(String name, String description, Integer sortOrder) {}
