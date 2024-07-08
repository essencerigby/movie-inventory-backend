package io.catalyte.demo.products;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation & business logic layer.
 * Provides methods for CRUD operations on Product objects.
 */
@Service
public class ProductServiceImpl implements ProductService {
    ProductRepository productRepository;
    ProductValidator productValidator = new ProductValidator();

    /**
     * Constructs a new instance of ProductServiceImpl with the specified ProductRepository.
     *
     * @param productRepository The ProductRepository instance to be used by this service.
     */
    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Retrieves a list of all products.
     *
     * @return A list of all products in the system.
     */
    public List<Product> getProducts() {
        return productRepository.findAll(); // Get All Products Logic goes here
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id The ID of the product to retrieve.
     * @return The product with the specified ID.
     */
    public Product getProductById(int id) {
        try {
            return productRepository.findById(id).orElseThrow();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
        }
    }

    /**
     * Retrieves a product by its name.
     * Exact matches only.
     *
     * @param name The name of the product to retrieve.
     * @return The product(s) with the specified name.
     */
    public List<Product> getProductByName(String name) {
        if (name.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name value is empty");
        }

        List<Product> tempList = productRepository.findByNameIgnoreCase(name);

        if (!tempList.isEmpty()) {
            return productRepository.findByNameIgnoreCase(name);
        } else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
    }

    /**
     * Creates a new product in the repository
     *
     * @param productToCreate - Product Object containing unique identifier, active status, name,
     *                        imageUrl, vendorId, ingredientsList, classification, cost, allergenList,
     *                        and salePrice
     * @return the created product
     */
    public Product createProduct(Product productToCreate) {
        String errorMessage = productValidator.validateProduct(productToCreate);
        if (!errorMessage.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
        errorMessage = productValidator.isUniqueProduct(productToCreate.getName(), getProducts());
        if (!errorMessage.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, errorMessage);
        }

        Product formattedProduct = productValidator.formatProduct(productToCreate);

        productRepository.save(formattedProduct);
        return formattedProduct;
    }

    /**
     * Updates an existing product.
     *
     * @param id The ID of the product to update.
     * @param productToEdit The updated product data.
     * @return The updated product.
     */
    public Product editProduct(Product productToEdit, int id) {
        if (productRepository.findById(id).isPresent()) {
            String errorMessage = productValidator.validateProduct(productToEdit);
            if (!errorMessage.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
            }
            List<Product> products = getProducts();
            boolean nameExists = products.stream()
                .anyMatch(product -> product.getId() != id && product.getName().equalsIgnoreCase(productToEdit.getName()));
            if (nameExists) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Product with matching name already exists.");
            }
            productToEdit.setId(id);
            Product formattedProduct = productValidator.formatProduct(productToEdit);
            productRepository.save(formattedProduct);
            return formattedProduct;
        } else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The Product was not found");
    }

    /**
     * Deletes a product from the system.
     *
     * @param id The ID of the product to delete.
     * @throws ResponseStatusException NOT_FOUND when an invalid ID is provided.
     */
    public void deleteProductById(int id) {
        Optional<Product> foundProduct = productRepository.findById(id);

        if (foundProduct.isPresent()) {
            productRepository.deleteById(id);
        } else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "A product with this ID was not found and could not be deleted.");
    }
}
