package ru.lessons.my.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PageResult<T> {
    private int page;
    private int size;
    private long totalPages;
    private long totalElements;
    private List<T> content;
}
