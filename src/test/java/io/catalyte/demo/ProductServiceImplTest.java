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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {
    ProductService productService;

    @Mock
    ProductRepository productRepository;

    Product testProduct;
    Product testProduct1;
    Product testProduct2;
    Product testProduct3;
    Product testProduct4;
    List<Product> testProducts;
    Product testProductToEdit;

    @BeforeEach
    public void setUp() {
        List<String> sampleIngredientList = Arrays.asList(
                "Ingredient 1",
                "Ingredient 2"
        );

        List<String> sampleAllergenList = Arrays.asList(
                "Dairy",
                "Soy"
        );

        productService = new ProductServiceImpl(productRepository);
        testProduct = new Product(1, true, "SampleDescription",
                "TestName", "5", sampleIngredientList,
                "Drink", "Coffee", "5.0", sampleAllergenList, "5.0", "5.0");

        testProduct1 = new Product();
        testProduct1.setName("Basketball");
        testProduct2 = new Product();
        testProduct2.setName("Football");
        testProduct3 = new Product();
        testProduct3.setName("Basketball");
        testProduct4 = new Product();
        testProductToEdit = new Product(1, false, "SampleDescriptionToEdit",
                "EditedName", "5", sampleIngredientList,
                "Drink", "Coffee", "5.0", sampleAllergenList, "5.0", "5.0");
    }

    @Test
    public void createProduct_withValidProduct_returnsPersistedProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        Product result = productService.createProduct(testProduct);
        assertEquals("TestName", result.getName(), "Product was Invalid");
    }

    @Test
    public void createProduct_withInvalidProduct_throwsError() {
        testProduct.setName("");

        assertThrows(ResponseStatusException.class, () -> productService.createProduct(testProduct), "Product was saved.");
    }

    @Test
    public void createProduct_withDuplicateProduct_throwsError() {
        List<Product> sampleProductList = Arrays.asList(
                testProduct,
                testProduct
        );
        when(productRepository.findAll()).thenReturn(sampleProductList);
        assertThrows(ResponseStatusException.class, () -> productService.createProduct(testProduct));
    }

    @Test
    public void getProduct_withValidID_returnsProductWithMatchingID() {
        testProduct.setId(1);

        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));
        Product result = productService.getProductById(1);
        assertEquals(testProduct, result);
    }

    @Test
    public void getProduct_withInvalidID_returnsErrorWithMessage() {
        testProduct.setId(18);

        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> productService.getProductById(testProduct.getId()), "Product not found.");
    }

    @Test
    public void getProductByName_whenNameExists_shouldReturnProduct() {
        testProducts = Arrays.asList(testProduct2);
        when(productRepository.findByNameIgnoreCase("Football")).thenReturn(testProducts);

        List<Product> result = productService.getProductByName("Football");

        assertEquals(testProduct2.getName(), result.get(0).getName());
    }

    @Test
    public void getProductByName_whenNameDoesntExist_shouldThrow404Exception() {
        testProducts = Arrays.asList();
        when(productRepository.findByNameIgnoreCase("Football")).thenReturn(testProducts);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                productService.getProductByName("Football"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    public void getProductByName_whenNameIsNullOrEmpty_shouldThrow400Exception() {

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                productService.getProductByName(""));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    public void getProductByName_whenMultipleNamesExist_shouldReturnAllProductsWithThatName() {
        testProducts = Arrays.asList(testProduct1, testProduct3);
        when(productRepository.findByNameIgnoreCase("Basketball")).thenReturn(testProducts);

        List<Product> result = productService.getProductByName("Basketball");

        assertTrue(result.size() > 1);
    }

    @Test
    public void editProduct_whenProductIdIsValid_shouldReturnObject() {
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(testProductToEdit)).thenReturn(testProductToEdit);
        Product editedProduct = productService.editProduct(testProductToEdit, 1);
        assertEquals(testProductToEdit.getDescription(), editedProduct.getDescription());
    }

    @Test
    public void editProduct_whenTryingToUpdateId_shouldDefaultToPathId() {
        int id = testProduct.getId();
        when(productRepository.findById(id)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(testProductToEdit)).thenReturn(testProductToEdit);
        testProductToEdit.setId(25);
        Product editedProduct = productService.editProduct(testProductToEdit, id);
        assertEquals(id, editedProduct.getId());
        assertNotEquals(99, editedProduct.getId());
    }

    @Test
    public void editProduct_whenNameIsEmpty_shouldReturn400Error() {
        testProductToEdit.setName("");
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> productService.editProduct(testProductToEdit, 1));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 BAD_REQUEST \" Name is empty.\"", exception.getMessage());
    }

    @Test
    public void editProduct_whenProductIdIsNotValid_shouldReturn404Error() {
        int invalidID = 25; // Assuming this Product ID does not exist
        when(productRepository.findById(invalidID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> productService.editProduct(testProductToEdit, invalidID));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("404 NOT_FOUND \"The Product was not found\"", exception.getMessage());
    }

    @Test
    public void editProduct_withDuplicateName_throwsConflictException() {
        List<Product> sampleProducts = Arrays.asList(testProduct, new Product(2, true, "SampleDescription2",
                "EditedName", "5", Arrays.asList("Ingredient 3"), "Drink", "Coffee", "5.0",
                Arrays.asList("Dairy"), "5.0", "5.0"));
        when(productRepository.findAll()).thenReturn(sampleProducts);
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> productService.editProduct(testProductToEdit, testProductToEdit.getId()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Product with matching name already exists.", exception.getReason());
    }

    @Test

    public void deleteProductByID_withValidID_deletesProduct() {
        int testID = testProduct.getId();

        when(productRepository.findById(testID)).thenReturn(Optional.of(testProduct));
        productService.deleteProductById(testID);

        //Verify each method was called once
        verify(productRepository).findById(testID);
        verify(productRepository).deleteById(testID);
    }

    @Test
    public void deleteProductByID_withInvalidID_throwsError() {
        int invalidID = 7;

        ResponseStatusException result = assertThrows(ResponseStatusException.class, () -> productService.deleteProductById(invalidID));

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }
}

