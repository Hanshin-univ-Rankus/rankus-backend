package org.univ.rankus.adapter.in.web.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;    // DTO 리스트
    private long totalElements; // 전체 아이템 수
    private int totalPages;     // 전체 페이지 수
    private int page;           // 현재 페이지 (0-based)
    private int size;           // 페이지 크기

    public static <T> PageResponse<T> from(List<T> content, long totalElements, int totalPages, int page, int size) {
        return PageResponse.<T>builder()
                .content(content)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .page(page)
                .size(size)
                .build();
    }
}