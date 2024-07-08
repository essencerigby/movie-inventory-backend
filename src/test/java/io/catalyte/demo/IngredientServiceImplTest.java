package io.catalyte.demo;

import io.catalyte.demo.ingredient.Ingredient;
import io.catalyte.demo.ingredient.IngredientRepository;
import io.catalyte.demo.ingredient.IngredientService;
import io.catalyte.demo.ingredient.IngredientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IngredientServiceImplTest {
    IngredientService ingredientService;

    @Mock
    IngredientRepository ingredientRepository;

    Ingredient testIngredient;
    Ingredient testIngredient2;

    @BeforeEach
    public void setUp() {
        List<String> sampleAllergenList = Arrays.asList(
                "Nuts",
                "Gluten"
        );

        ingredientService = new IngredientServiceImpl(ingredientRepository);
        testIngredient = new Ingredient(1,true,"Test Ingredient", BigDecimal.valueOf(15.50),BigDecimal.valueOf(10.50),"lb", sampleAllergenList);
        testIngredient2 = new Ingredient(2,true,"Test Ingredient 2", BigDecimal.valueOf(20.99), BigDecimal.valueOf(2),"oz", sampleAllergenList);
    }

    @Test
    public void createIngredient_withNoValidation_PersistIngredient() {
        when(ingredientRepository.save(testIngredient)).thenReturn(testIngredient);

        Ingredient result = ingredientService.createIngredient(testIngredient);

        assertEquals("Test Ingredient", result.getName());
        assertEquals(true, result.getActive());
        assertEquals("lb", result.getUnitOfMeasure());
        assertEquals(Arrays.asList("Nuts", "Gluten"), result.getAllergens());
    }

    @Test
    public void getIngredients_withNoIngredientsPresent_returnsEmptyArray() {
        when(ingredientRepository.findAll()).thenReturn(Arrays.asList());

        List <Ingredient> result = ingredientService.getIngredients();

        assertEquals(0, result.size());
        assertNotNull(result);
    }

    @Test
    public void getIngredients_withIngredientsPresent_returnsAllIngredients() {
        List<Ingredient> expectedIngredients = Arrays.asList(testIngredient, testIngredient2);
        when(ingredientRepository.findAll()).thenReturn(expectedIngredients);

        List <Ingredient> result = ingredientService.getIngredients();

        assertEquals(2, result.size());
        assertEquals(expectedIngredients, result);
    }

    @Test
    public void getIngredient_withValidID_returnsIngredientWithMatchingID() {
        testIngredient.setId(1);

        when(ingredientRepository.findById(testIngredient.getId())).thenReturn(Optional.of(testIngredient));
        Ingredient result = ingredientService.getIngredientById(1);
        assertEquals(testIngredient, result);
    }

    @Test
    public void getIngredient_withInvalidID_returnsErrorWithMessage() {
        testIngredient.setId(18);

        when(ingredientRepository.findById(testIngredient.getId())).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> ingredientService.getIngredientById(testIngredient.getId()), "Ingredient not found.");
    }

    @Test
    public void deleteIngredientById_withExistingId_deletesIngredient(){

        when(ingredientRepository.findById(1)).thenReturn(
                Optional.of(testIngredient));
        ingredientService.deleteIngredientById(1);
        verify(ingredientRepository).findById(1);
        verify(ingredientRepository).deleteById(1);
    }

    @Test
    public void deleteIngredientById_withInvalidID_throwsError() {
        int invalidId = 999;
        when(ingredientRepository.findById(
                invalidId)).thenReturn(
                Optional.empty());
        ResponseStatusException result = assertThrows(
                ResponseStatusException.class, () ->
                        ingredientService.deleteIngredientById(
                                invalidId)
        );
        assertEquals(HttpStatus.NOT_FOUND,
                result.getStatusCode());
        assertEquals(
                "404 NOT_FOUND \"Ingredient not found.\"",
                result.getMessage());
    }

    @Test
    public void editIngredient_whenIngredientIdIsValid_returnsEditedIngredient() {
        when(ingredientRepository.findById(1)).thenReturn(Optional.of(testIngredient));
        when(ingredientRepository.save(testIngredient2)).thenReturn(testIngredient2);
        Ingredient editedIngredient = ingredientService.editIngredient(testIngredient2, 1);
        assertEquals(testIngredient2.getName(), editedIngredient.getName(), "Ingredient names do not match.");
    }

    @Test
    public void editIngredient_whenIngredientIdIsInvalid_throwsError() {
        when(ingredientRepository.findById(2)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> ingredientService.editIngredient(testIngredient, 2), "editIngredient did not throw an error.");
    }
}
