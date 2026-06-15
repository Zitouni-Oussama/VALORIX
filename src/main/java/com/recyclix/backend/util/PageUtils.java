package com.recyclix.backend.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@UtilityClass
public class PageUtils {

    public static <E, D> PageResponse<D> toPageResponse(Page<E> page, Function<E, D> mapper) {
        List<D> content = page.getContent().stream().map(mapper).toList();

        return PageResponse.<D>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    public static <T> PageResponse<T> toPageResponse(Page<T> page) {
        return toPageResponse(page, Function.identity());
    }
}