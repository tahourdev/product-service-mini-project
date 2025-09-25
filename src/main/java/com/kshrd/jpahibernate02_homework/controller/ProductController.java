package com.kshrd.jpahibernate02_homework.controller;

import com.kshrd.jpahibernate02_homework.base.BaseController;
import com.kshrd.jpahibernate02_homework.dto.request.ProductRequest;
import com.kshrd.jpahibernate02_homework.dto.response.ApiResponse;
import com.kshrd.jpahibernate02_homework.dto.response.PagedResponseListProduct;
import com.kshrd.jpahibernate02_homework.dto.response.PaginationInfo;
import com.kshrd.jpahibernate02_homework.model.Product;
import com.kshrd.jpahibernate02_homework.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/products")
@Tag(name = "Product")
public class ProductController extends BaseController {
    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get all products (paginated)", description = "Returns a paginated list of all products. Accepts page and size as query parameters.")
    public ResponseEntity<ApiResponse<PagedResponseListProduct>> getAllProduct(
            @RequestParam(defaultValue = "1") @Positive Integer page,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        PagedResponseListProduct pagedResponseListProduct = PagedResponseListProduct.builder()
                .items(productService.getAllProduct(page, size))
                .paginationInfo(new PaginationInfo(page, size, productService.countAllProducts()))
                .build();
        return response("Fetch all products successfully", pagedResponseListProduct);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name", description = "Returns a list of products that contain the given name (case-insensitive partial match).")
    public ResponseEntity<ApiResponse<PagedResponseListProduct>> searchProduct(@RequestParam String name,
                                                                    @RequestParam(defaultValue = "1") @Positive Integer page,
                                                                    @RequestParam(defaultValue = "10") @Positive Integer size) {
        PagedResponseListProduct pagedResponseListProduct = PagedResponseListProduct.builder()
                .items(productService.searchProduct(page, size, name.trim()))
                .paginationInfo(new PaginationInfo(page, size, productService.countSearchProducts(page,size,name.trim())))
                .build();
        return response("Products matching name " + "'" + name + "'" + " fetched successfully", pagedResponseListProduct);
    }

    @PostMapping
    @Operation(summary = "Create a new product", description = "Accepts a product request payload and creates a new product. Returns the created product.")
    public ResponseEntity<ApiResponse<Product>> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        return response("Product created successfully", HttpStatus.CREATED, productService.createProduct(productRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product by ID", description = "Deletes a product by its ID. Returns HTTP 200 if the product is successfully deleted.")
    public ResponseEntity<ApiResponse<Object>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return response("Product deleted successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Fetches a product using its unique ID. Returns 404 if not found.")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Long id) {
        return response("Product with id of " + id + " fetched successfully", productService.getProductById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product by ID", description = "Updates an existing product with the given ID using the request body. Returns the updated product.")
    public ResponseEntity<ApiResponse<Product>> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest productRequest) {
        return response("Update product successfully", productService.updateProductById(id, productRequest));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock products", description = "Returns a list of products with quantity less than the specified threshold.")
    public ResponseEntity<ApiResponse<PagedResponseListProduct>> filterProductByQuantity(@RequestParam Integer quantity,
                                                                                         @RequestParam(defaultValue = "1") @Positive Integer page,
                                                                                         @RequestParam(defaultValue = "10") @Positive Integer size) {
        PagedResponseListProduct pagedResponseListProduct = PagedResponseListProduct.builder()
                .items(productService.filterProductByQuantity(page, size, quantity))
                .paginationInfo(new PaginationInfo(page, size, productService.countFilterProducts(page,size,quantity)))
                .build();
        return response("Products with quantity less than " + quantity +" fetched successfully", pagedResponseListProduct);
    }

}
