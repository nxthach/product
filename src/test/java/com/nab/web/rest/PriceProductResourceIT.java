package com.nab.web.rest;

import com.nab.ProductApp;
import com.nab.domain.PriceProduct;
import com.nab.repository.PriceProductRepository;

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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link PriceProductResource} REST controller.
 */
@SpringBootTest(classes = ProductApp.class)

@AutoConfigureMockMvc
@WithMockUser
public class PriceProductResourceIT {

    private static final LocalDate DEFAULT_START_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_START_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_END_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_END_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal(1);
    private static final BigDecimal UPDATED_PRICE = new BigDecimal(2);

    @Autowired
    private PriceProductRepository priceProductRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPriceProductMockMvc;

    private PriceProduct priceProduct;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PriceProduct createEntity(EntityManager em) {
        PriceProduct priceProduct = new PriceProduct()
            .startDate(DEFAULT_START_DATE)
            .endDate(DEFAULT_END_DATE)
            .price(DEFAULT_PRICE);
        return priceProduct;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PriceProduct createUpdatedEntity(EntityManager em) {
        PriceProduct priceProduct = new PriceProduct()
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .price(UPDATED_PRICE);
        return priceProduct;
    }

    @BeforeEach
    public void initTest() {
        priceProduct = createEntity(em);
    }

    @Test
    @Transactional
    public void createPriceProduct() throws Exception {
        int databaseSizeBeforeCreate = priceProductRepository.findAll().size();

        // Create the PriceProduct
        restPriceProductMockMvc.perform(post("/api/price-products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(priceProduct)))
            .andExpect(status().isCreated());

        // Validate the PriceProduct in the database
        List<PriceProduct> priceProductList = priceProductRepository.findAll();
        assertThat(priceProductList).hasSize(databaseSizeBeforeCreate + 1);
        PriceProduct testPriceProduct = priceProductList.get(priceProductList.size() - 1);
        assertThat(testPriceProduct.getStartDate()).isEqualTo(DEFAULT_START_DATE);
        assertThat(testPriceProduct.getEndDate()).isEqualTo(DEFAULT_END_DATE);
        assertThat(testPriceProduct.getPrice()).isEqualTo(DEFAULT_PRICE);
    }

    @Test
    @Transactional
    public void createPriceProductWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = priceProductRepository.findAll().size();

        // Create the PriceProduct with an existing ID
        priceProduct.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPriceProductMockMvc.perform(post("/api/price-products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(priceProduct)))
            .andExpect(status().isBadRequest());

        // Validate the PriceProduct in the database
        List<PriceProduct> priceProductList = priceProductRepository.findAll();
        assertThat(priceProductList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllPriceProducts() throws Exception {
        // Initialize the database
        priceProductRepository.saveAndFlush(priceProduct);

        // Get all the priceProductList
        restPriceProductMockMvc.perform(get("/api/price-products?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(priceProduct.getId().intValue())))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())))
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.intValue())));
    }
    
    @Test
    @Transactional
    public void getPriceProduct() throws Exception {
        // Initialize the database
        priceProductRepository.saveAndFlush(priceProduct);

        // Get the priceProduct
        restPriceProductMockMvc.perform(get("/api/price-products/{id}", priceProduct.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(priceProduct.getId().intValue()))
            .andExpect(jsonPath("$.startDate").value(DEFAULT_START_DATE.toString()))
            .andExpect(jsonPath("$.endDate").value(DEFAULT_END_DATE.toString()))
            .andExpect(jsonPath("$.price").value(DEFAULT_PRICE.intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingPriceProduct() throws Exception {
        // Get the priceProduct
        restPriceProductMockMvc.perform(get("/api/price-products/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePriceProduct() throws Exception {
        // Initialize the database
        priceProductRepository.saveAndFlush(priceProduct);

        int databaseSizeBeforeUpdate = priceProductRepository.findAll().size();

        // Update the priceProduct
        PriceProduct updatedPriceProduct = priceProductRepository.findById(priceProduct.getId()).get();
        // Disconnect from session so that the updates on updatedPriceProduct are not directly saved in db
        em.detach(updatedPriceProduct);
        updatedPriceProduct
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .price(UPDATED_PRICE);

        restPriceProductMockMvc.perform(put("/api/price-products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedPriceProduct)))
            .andExpect(status().isOk());

        // Validate the PriceProduct in the database
        List<PriceProduct> priceProductList = priceProductRepository.findAll();
        assertThat(priceProductList).hasSize(databaseSizeBeforeUpdate);
        PriceProduct testPriceProduct = priceProductList.get(priceProductList.size() - 1);
        assertThat(testPriceProduct.getStartDate()).isEqualTo(UPDATED_START_DATE);
        assertThat(testPriceProduct.getEndDate()).isEqualTo(UPDATED_END_DATE);
        assertThat(testPriceProduct.getPrice()).isEqualTo(UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void updateNonExistingPriceProduct() throws Exception {
        int databaseSizeBeforeUpdate = priceProductRepository.findAll().size();

        // Create the PriceProduct

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPriceProductMockMvc.perform(put("/api/price-products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(priceProduct)))
            .andExpect(status().isBadRequest());

        // Validate the PriceProduct in the database
        List<PriceProduct> priceProductList = priceProductRepository.findAll();
        assertThat(priceProductList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deletePriceProduct() throws Exception {
        // Initialize the database
        priceProductRepository.saveAndFlush(priceProduct);

        int databaseSizeBeforeDelete = priceProductRepository.findAll().size();

        // Delete the priceProduct
        restPriceProductMockMvc.perform(delete("/api/price-products/{id}", priceProduct.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<PriceProduct> priceProductList = priceProductRepository.findAll();
        assertThat(priceProductList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
