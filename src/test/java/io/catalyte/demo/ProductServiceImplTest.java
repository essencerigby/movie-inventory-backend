package io.catalyte.demo;

import io.catalyte.demo.products.Product;
import io.catalyte.demo.products.ProductRepository;
import io.catalyte.demo.products.ProductService;
import io.catalyte.demo.products.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {
    ProductService productService;

    @Mock
    ProductRepository productRepository;

    Product testProduct;
    List<String> sampleList;

    @BeforeEach
    public void setUp() {
        productService = new ProductServiceImpl(productRepository);
        testProduct = new Product(1, true, "",
                "TestName", "", 5, sampleList,
                "", 5.0, sampleList, 5.0);
    }

    @Test
    public void createProduct_withValidProduct_returnsPersistedProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        Product result = productService.createProduct(testProduct);
        assertEquals("TestName", result.getName(), "Product was Invalid");
    }

    @Test
    public void createProduct_withInvalidProduct_returnsNothing() {
        testProduct.setName("");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            productService.createProduct(testProduct);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode(), "Product was saved.");
    }
}
