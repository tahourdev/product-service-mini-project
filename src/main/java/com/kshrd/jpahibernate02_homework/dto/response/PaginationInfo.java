package com.kshrd.jpahibernate02_homework.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaginationInfo {
    private Integer totalElements;
    private Integer currentPage;
    private Integer pageSize;
    private Integer totalPages;

    public PaginationInfo(Integer page, Integer size, Integer totalCount) {
        this.totalElements = totalCount;
        this.currentPage = page;
        this.pageSize = size;
        this.totalPages = (totalCount / size) + (totalCount % size > 0 ? 1 : 0);
    }
}
