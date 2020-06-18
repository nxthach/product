package com.nab.web.rest;

import com.nab.ProductApp;
import com.nab.domain.Product;
import com.nab.domain.PriceProduct;
import com.nab.repository.ProductRepository;
import com.nab.service.ProductService;
import com.nab.service.dto.ProductCriteria;
import com.nab.service.ProductQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link ProductResource} REST controller.
 */
@SpringBootTest(classes = ProductApp.class)

@AutoConfigureMockMvc
@WithMockUser
public class ProductResourceIT {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_BRAND = "AAAAAAAAAA";
    private static final String UPDATED_BRAND = "BBBBBBBBBB";

    private static final String DEFAULT_COLOR = "AAAAAAAAAA";
    private static final String UPDATED_COLOR = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_DEFAULT_PRICE = new BigDecimal(1);
    private static final BigDecimal UPDATED_DEFAULT_PRICE = new BigDecimal(2);
    private static final BigDecimal SMALLER_DEFAULT_PRICE = new BigDecimal(1 - 1);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductQueryService productQueryService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProductMockMvc;

    private Product product;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Product createEntity(EntityManager em) {
        Product product = new Product()
            .code(DEFAULT_CODE)
            .name(DEFAULT_NAME)
            .brand(DEFAULT_BRAND)
            .color(DEFAULT_COLOR)
            .defaultPrice(DEFAULT_DEFAULT_PRICE);
        return product;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Product createUpdatedEntity(EntityManager em) {
        Product product = new Product()
            .code(UPDATED_CODE)
            .name(UPDATED_NAME)
            .brand(UPDATED_BRAND)
            .color(UPDATED_COLOR)
            .defaultPrice(UPDATED_DEFAULT_PRICE);
        return product;
    }

    @BeforeEach
    public void initTest() {
        product = createEntity(em);
    }

    @Test
    @Transactional
    public void createProduct() throws Exception {
        int databaseSizeBeforeCreate = productRepository.findAll().size();

        // Create the Product
        restProductMockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(product)))
            .andExpect(status().isCreated());

        // Validate the Product in the database
        List<Product> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeCreate + 1);
        Product testProduct = productList.get(productList.size() - 1);
        assertThat(testProduct.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testProduct.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testProduct.getBrand()).isEqualTo(DEFAULT_BRAND);
        assertThat(testProduct.getColor()).isEqualTo(DEFAULT_COLOR);
        assertThat(testProduct.getDefaultPrice()).isEqualTo(DEFAULT_DEFAULT_PRICE);
    }

    @Test
    @Transactional
    public void createProductWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = productRepository.findAll().size();

        // Create the Product with an existing ID
        product.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restProductMockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(product)))
            .andExpect(status().isBadRequest());

        // Validate the Product in the database
        List<Product> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllProducts() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList
        restProductMockMvc.perform(get("/api/products?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(product.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].brand").value(hasItem(DEFAULT_BRAND)))
            .andExpect(jsonPath("$.[*].color").value(hasItem(DEFAULT_COLOR)))
            .andExpect(jsonPath("$.[*].defaultPrice").value(hasItem(DEFAULT_DEFAULT_PRICE.intValue())));
    }
    
    @Test
    @Transactional
    public void getProduct() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get the product
        restProductMockMvc.perform(get("/api/products/{id}", product.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(product.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.brand").value(DEFAULT_BRAND))
            .andExpect(jsonPath("$.color").value(DEFAULT_COLOR))
            .andExpect(jsonPath("$.defaultPrice").value(DEFAULT_DEFAULT_PRICE.intValue()));
    }


    @Test
    @Transactional
    public void getProductsByIdFiltering() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        Long id = product.getId();

        defaultProductShouldBeFound("id.equals=" + id);
        defaultProductShouldNotBeFound("id.notEquals=" + id);

        defaultProductShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultProductShouldNotBeFound("id.greaterThan=" + id);

        defaultProductShouldBeFound("id.lessThanOrEqual=" + id);
        defaultProductShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllProductsByCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where code equals to DEFAULT_CODE
        defaultProductShouldBeFound("code.equals=" + DEFAULT_CODE);

        // Get all the productList where code equals to UPDATED_CODE
        defaultProductShouldNotBeFound("code.equals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllProductsByCodeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where code not equals to DEFAULT_CODE
        defaultProductShouldNotBeFound("code.notEquals=" + DEFAULT_CODE);

        // Get all the productList where code not equals to UPDATED_CODE
        defaultProductShouldBeFound("code.notEquals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllProductsByCodeIsInShouldWork() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where code in DEFAULT_CODE or UPDATED_CODE
        defaultProductShouldBeFound("code.in=" + DEFAULT_CODE + "," + UPDATED_CODE);

        // Get all the productList where code equals to UPDATED_CODE
        defaultProductShouldNotBeFound("code.in=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllProductsByCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where code is not null
        defaultProductShouldBeFound("code.specified=true");

        // Get all the productList where code is null
        defaultProductShouldNotBeFound("code.specified=false");
    }
                @Test
    @Transactional
    public void getAllProductsByCodeContainsSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where code contains DEFAULT_CODE
        defaultProductShouldBeFound("code.contains=" + DEFAULT_CODE);

        // Get all the productList where code contains UPDATED_CODE
        defaultProductShouldNotBeFound("code.contains=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllProductsByCodeNotContainsSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where code does not contain DEFAULT_CODE
        defaultProductShouldNotBeFound("code.doesNotContain=" + DEFAULT_CODE);

        // Get all the productList where code does not contain UPDATED_CODE
        defaultProductShouldBeFound("code.doesNotContain=" + UPDATED_CODE);
    }


    @Test
    @Transactional
    public void getAllProductsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where name equals to DEFAULT_NAME
        defaultProductShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the productList where name equals to UPDATED_NAME
        defaultProductShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllProductsByNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where name not equals to DEFAULT_NAME
        defaultProductShouldNotBeFound("name.notEquals=" + DEFAULT_NAME);

        // Get all the productList where name not equals to UPDATED_NAME
        defaultProductShouldBeFound("name.notEquals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllProductsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where name in DEFAULT_NAME or UPDATED_NAME
        defaultProductShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the productList where name equals to UPDATED_NAME
        defaultProductShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllProductsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where name is not null
        defaultProductShouldBeFound("name.specified=true");

        // Get all the productList where name is null
        defaultProductShouldNotBeFound("name.specified=false");
    }
                @Test
    @Transactional
    public void getAllProductsByNameContainsSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where name contains DEFAULT_NAME
        defaultProductShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the productList where name contains UPDATED_NAME
        defaultProductShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllProductsByNameNotContainsSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where name does not contain DEFAULT_NAME
        defaultProductShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the productList where name does not contain UPDATED_NAME
        defaultProductShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }


    @Test
    @Transactional
    public void getAllProductsByBrandIsEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where brand equals to DEFAULT_BRAND
        defaultProductShouldBeFound("brand.equals=" + DEFAULT_BRAND);

        // Get all the productList where brand equals to UPDATED_BRAND
        defaultProductShouldNotBeFound("brand.equals=" + UPDATED_BRAND);
    }

    @Test
    @Transactional
    public void getAllProductsByBrandIsNotEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where brand not equals to DEFAULT_BRAND
        defaultProductShouldNotBeFound("brand.notEquals=" + DEFAULT_BRAND);

        // Get all the productList where brand not equals to UPDATED_BRAND
        defaultProductShouldBeFound("brand.notEquals=" + UPDATED_BRAND);
    }

    @Test
    @Transactional
    public void getAllProductsByBrandIsInShouldWork() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where brand in DEFAULT_BRAND or UPDATED_BRAND
        defaultProductShouldBeFound("brand.in=" + DEFAULT_BRAND + "," + UPDATED_BRAND);

        // Get all the productList where brand equals to UPDATED_BRAND
        defaultProductShouldNotBeFound("brand.in=" + UPDATED_BRAND);
    }

    @Test
    @Transactional
    public void getAllProductsByBrandIsNullOrNotNull() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where brand is not null
        defaultProductShouldBeFound("brand.specified=true");

        // Get all the productList where brand is null
        defaultProductShouldNotBeFound("brand.specified=false");
    }
                @Test
    @Transactional
    public void getAllProductsByBrandContainsSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where brand contains DEFAULT_BRAND
        defaultProductShouldBeFound("brand.contains=" + DEFAULT_BRAND);

        // Get all the productList where brand contains UPDATED_BRAND
        defaultProductShouldNotBeFound("brand.contains=" + UPDATED_BRAND);
    }

    @Test
    @Transactional
    public void getAllProductsByBrandNotContainsSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where brand does not contain DEFAULT_BRAND
        defaultProductShouldNotBeFound("brand.doesNotContain=" + DEFAULT_BRAND);

        // Get all the productList where brand does not contain UPDATED_BRAND
        defaultProductShouldBeFound("brand.doesNotContain=" + UPDATED_BRAND);
    }


    @Test
    @Transactional
    public void getAllProductsByColorIsEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where color equals to DEFAULT_COLOR
        defaultProductShouldBeFound("color.equals=" + DEFAULT_COLOR);

        // Get all the productList where color equals to UPDATED_COLOR
        defaultProductShouldNotBeFound("color.equals=" + UPDATED_COLOR);
    }

    @Test
    @Transactional
    public void getAllProductsByColorIsNotEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where color not equals to DEFAULT_COLOR
        defaultProductShouldNotBeFound("color.notEquals=" + DEFAULT_COLOR);

        // Get all the productList where color not equals to UPDATED_COLOR
        defaultProductShouldBeFound("color.notEquals=" + UPDATED_COLOR);
    }

    @Test
    @Transactional
    public void getAllProductsByColorIsInShouldWork() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where color in DEFAULT_COLOR or UPDATED_COLOR
        defaultProductShouldBeFound("color.in=" + DEFAULT_COLOR + "," + UPDATED_COLOR);

        // Get all the productList where color equals to UPDATED_COLOR
        defaultProductShouldNotBeFound("color.in=" + UPDATED_COLOR);
    }

    @Test
    @Transactional
    public void getAllProductsByColorIsNullOrNotNull() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where color is not null
        defaultProductShouldBeFound("color.specified=true");

        // Get all the productList where color is null
        defaultProductShouldNotBeFound("color.specified=false");
    }
                @Test
    @Transactional
    public void getAllProductsByColorContainsSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where color contains DEFAULT_COLOR
        defaultProductShouldBeFound("color.contains=" + DEFAULT_COLOR);

        // Get all the productList where color contains UPDATED_COLOR
        defaultProductShouldNotBeFound("color.contains=" + UPDATED_COLOR);
    }

    @Test
    @Transactional
    public void getAllProductsByColorNotContainsSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where color does not contain DEFAULT_COLOR
        defaultProductShouldNotBeFound("color.doesNotContain=" + DEFAULT_COLOR);

        // Get all the productList where color does not contain UPDATED_COLOR
        defaultProductShouldBeFound("color.doesNotContain=" + UPDATED_COLOR);
    }


    @Test
    @Transactional
    public void getAllProductsByDefaultPriceIsEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where defaultPrice equals to DEFAULT_DEFAULT_PRICE
        defaultProductShouldBeFound("defaultPrice.equals=" + DEFAULT_DEFAULT_PRICE);

        // Get all the productList where defaultPrice equals to UPDATED_DEFAULT_PRICE
        defaultProductShouldNotBeFound("defaultPrice.equals=" + UPDATED_DEFAULT_PRICE);
    }

    @Test
    @Transactional
    public void getAllProductsByDefaultPriceIsNotEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where defaultPrice not equals to DEFAULT_DEFAULT_PRICE
        defaultProductShouldNotBeFound("defaultPrice.notEquals=" + DEFAULT_DEFAULT_PRICE);

        // Get all the productList where defaultPrice not equals to UPDATED_DEFAULT_PRICE
        defaultProductShouldBeFound("defaultPrice.notEquals=" + UPDATED_DEFAULT_PRICE);
    }

    @Test
    @Transactional
    public void getAllProductsByDefaultPriceIsInShouldWork() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where defaultPrice in DEFAULT_DEFAULT_PRICE or UPDATED_DEFAULT_PRICE
        defaultProductShouldBeFound("defaultPrice.in=" + DEFAULT_DEFAULT_PRICE + "," + UPDATED_DEFAULT_PRICE);

        // Get all the productList where defaultPrice equals to UPDATED_DEFAULT_PRICE
        defaultProductShouldNotBeFound("defaultPrice.in=" + UPDATED_DEFAULT_PRICE);
    }

    @Test
    @Transactional
    public void getAllProductsByDefaultPriceIsNullOrNotNull() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where defaultPrice is not null
        defaultProductShouldBeFound("defaultPrice.specified=true");

        // Get all the productList where defaultPrice is null
        defaultProductShouldNotBeFound("defaultPrice.specified=false");
    }

    @Test
    @Transactional
    public void getAllProductsByDefaultPriceIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where defaultPrice is greater than or equal to DEFAULT_DEFAULT_PRICE
        defaultProductShouldBeFound("defaultPrice.greaterThanOrEqual=" + DEFAULT_DEFAULT_PRICE);

        // Get all the productList where defaultPrice is greater than or equal to UPDATED_DEFAULT_PRICE
        defaultProductShouldNotBeFound("defaultPrice.greaterThanOrEqual=" + UPDATED_DEFAULT_PRICE);
    }

    @Test
    @Transactional
    public void getAllProductsByDefaultPriceIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where defaultPrice is less than or equal to DEFAULT_DEFAULT_PRICE
        defaultProductShouldBeFound("defaultPrice.lessThanOrEqual=" + DEFAULT_DEFAULT_PRICE);

        // Get all the productList where defaultPrice is less than or equal to SMALLER_DEFAULT_PRICE
        defaultProductShouldNotBeFound("defaultPrice.lessThanOrEqual=" + SMALLER_DEFAULT_PRICE);
    }

    @Test
    @Transactional
    public void getAllProductsByDefaultPriceIsLessThanSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where defaultPrice is less than DEFAULT_DEFAULT_PRICE
        defaultProductShouldNotBeFound("defaultPrice.lessThan=" + DEFAULT_DEFAULT_PRICE);

        // Get all the productList where defaultPrice is less than UPDATED_DEFAULT_PRICE
        defaultProductShouldBeFound("defaultPrice.lessThan=" + UPDATED_DEFAULT_PRICE);
    }

    @Test
    @Transactional
    public void getAllProductsByDefaultPriceIsGreaterThanSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);

        // Get all the productList where defaultPrice is greater than DEFAULT_DEFAULT_PRICE
        defaultProductShouldNotBeFound("defaultPrice.greaterThan=" + DEFAULT_DEFAULT_PRICE);

        // Get all the productList where defaultPrice is greater than SMALLER_DEFAULT_PRICE
        defaultProductShouldBeFound("defaultPrice.greaterThan=" + SMALLER_DEFAULT_PRICE);
    }


    @Test
    @Transactional
    public void getAllProductsByPriceProductIsEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(product);
        PriceProduct priceProduct = PriceProductResourceIT.createEntity(em);
        em.persist(priceProduct);
        em.flush();
        product.addPriceProduct(priceProduct);
        productRepository.saveAndFlush(product);
        Long priceProductId = priceProduct.getId();

        // Get all the productList where priceProduct equals to priceProductId
        defaultProductShouldBeFound("priceProductId.equals=" + priceProductId);

        // Get all the productList where priceProduct equals to priceProductId + 1
        defaultProductShouldNotBeFound("priceProductId.equals=" + (priceProductId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultProductShouldBeFound(String filter) throws Exception {
        restProductMockMvc.perform(get("/api/products?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(product.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].brand").value(hasItem(DEFAULT_BRAND)))
            .andExpect(jsonPath("$.[*].color").value(hasItem(DEFAULT_COLOR)))
            .andExpect(jsonPath("$.[*].defaultPrice").value(hasItem(DEFAULT_DEFAULT_PRICE.intValue())));

        // Check, that the count call also returns 1
        restProductMockMvc.perform(get("/api/products/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultProductShouldNotBeFound(String filter) throws Exception {
        restProductMockMvc.perform(get("/api/products?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restProductMockMvc.perform(get("/api/products/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingProduct() throws Exception {
        // Get the product
        restProductMockMvc.perform(get("/api/products/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateProduct() throws Exception {
        // Initialize the database
        productService.save(product);

        int databaseSizeBeforeUpdate = productRepository.findAll().size();

        // Update the product
        Product updatedProduct = productRepository.findById(product.getId()).get();
        // Disconnect from session so that the updates on updatedProduct are not directly saved in db
        em.detach(updatedProduct);
        updatedProduct
            .code(UPDATED_CODE)
            .name(UPDATED_NAME)
            .brand(UPDATED_BRAND)
            .color(UPDATED_COLOR)
            .defaultPrice(UPDATED_DEFAULT_PRICE);

        restProductMockMvc.perform(put("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedProduct)))
            .andExpect(status().isOk());

        // Validate the Product in the database
        List<Product> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeUpdate);
        Product testProduct = productList.get(productList.size() - 1);
        assertThat(testProduct.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testProduct.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testProduct.getBrand()).isEqualTo(UPDATED_BRAND);
        assertThat(testProduct.getColor()).isEqualTo(UPDATED_COLOR);
        assertThat(testProduct.getDefaultPrice()).isEqualTo(UPDATED_DEFAULT_PRICE);
    }

    @Test
    @Transactional
    public void updateNonExistingProduct() throws Exception {
        int databaseSizeBeforeUpdate = productRepository.findAll().size();

        // Create the Product

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductMockMvc.perform(put("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(product)))
            .andExpect(status().isBadRequest());

        // Validate the Product in the database
        List<Product> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteProduct() throws Exception {
        // Initialize the database
        productService.save(product);

        int databaseSizeBeforeDelete = productRepository.findAll().size();

        // Delete the product
        restProductMockMvc.perform(delete("/api/products/{id}", product.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Product> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
