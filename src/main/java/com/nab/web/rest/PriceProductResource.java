package com.nab.web.rest;

import com.nab.domain.PriceProduct;
import com.nab.repository.PriceProductRepository;
import com.nab.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link com.nab.domain.PriceProduct}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class PriceProductResource {

    private final Logger log = LoggerFactory.getLogger(PriceProductResource.class);

    private static final String ENTITY_NAME = "productPriceProduct";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PriceProductRepository priceProductRepository;

    public PriceProductResource(PriceProductRepository priceProductRepository) {
        this.priceProductRepository = priceProductRepository;
    }

    /**
     * {@code POST  /price-products} : Create a new priceProduct.
     *
     * @param priceProduct the priceProduct to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new priceProduct, or with status {@code 400 (Bad Request)} if the priceProduct has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/price-products")
    public ResponseEntity<PriceProduct> createPriceProduct(@RequestBody PriceProduct priceProduct) throws URISyntaxException {
        log.debug("REST request to save PriceProduct : {}", priceProduct);
        if (priceProduct.getId() != null) {
            throw new BadRequestAlertException("A new priceProduct cannot already have an ID", ENTITY_NAME, "idexists");
        }
        PriceProduct result = priceProductRepository.save(priceProduct);
        return ResponseEntity.created(new URI("/api/price-products/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /price-products} : Updates an existing priceProduct.
     *
     * @param priceProduct the priceProduct to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated priceProduct,
     * or with status {@code 400 (Bad Request)} if the priceProduct is not valid,
     * or with status {@code 500 (Internal Server Error)} if the priceProduct couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/price-products")
    public ResponseEntity<PriceProduct> updatePriceProduct(@RequestBody PriceProduct priceProduct) throws URISyntaxException {
        log.debug("REST request to update PriceProduct : {}", priceProduct);
        if (priceProduct.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        PriceProduct result = priceProductRepository.save(priceProduct);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, priceProduct.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /price-products} : get all the priceProducts.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of priceProducts in body.
     */
    @GetMapping("/price-products")
    public List<PriceProduct> getAllPriceProducts() {
        log.debug("REST request to get all PriceProducts");
        return priceProductRepository.findAll();
    }

    /**
     * {@code GET  /price-products/:id} : get the "id" priceProduct.
     *
     * @param id the id of the priceProduct to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the priceProduct, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/price-products/{id}")
    public ResponseEntity<PriceProduct> getPriceProduct(@PathVariable Long id) {
        log.debug("REST request to get PriceProduct : {}", id);
        Optional<PriceProduct> priceProduct = priceProductRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(priceProduct);
    }

    /**
     * {@code DELETE  /price-products/:id} : delete the "id" priceProduct.
     *
     * @param id the id of the priceProduct to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/price-products/{id}")
    public ResponseEntity<Void> deletePriceProduct(@PathVariable Long id) {
        log.debug("REST request to delete PriceProduct : {}", id);
        priceProductRepository.deleteById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
