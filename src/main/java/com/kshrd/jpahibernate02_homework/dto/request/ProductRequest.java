package com.kshrd.jpahibernate02_homework.dto.request;

import com.kshrd.jpahibernate02_homework.model.Product;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ProductRequest {

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotNull(message = "Price must not be null")
    @PositiveOrZero(message = "Price must be positive")
    @Digits(integer = 6, fraction = 2, message = "Invalid price")
    private BigDecimal price;

    @NotNull(message = "Quantity must not be null")
    @PositiveOrZero(message = "Quantity must be positive")
    @Digits(integer = 5, fraction = 0, message = "Only 5 digits allowed")
    private Integer quantity;

    public Product toEntity() {
        return Product.builder()
                .name(this.getName())
                .quantity(this.getQuantity())
                .price(this.getPrice())
                .build();
    }

    public Product toEntity(Long id) {
        return Product.builder()
                .id(id)
                .name(this.getName())
                .quantity(this.getQuantity())
                .price(this.getPrice())
                .build();
    }
}
