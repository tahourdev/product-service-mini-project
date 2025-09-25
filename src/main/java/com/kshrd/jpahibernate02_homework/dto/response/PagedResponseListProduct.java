package com.kshrd.jpahibernate02_homework.dto.response;

import com.kshrd.jpahibernate02_homework.model.Product;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
public class PagedResponseListProduct {
    private List<Product> items;
    private PaginationInfo paginationInfo;
}
