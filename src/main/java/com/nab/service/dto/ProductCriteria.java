package com.nab.service.dto;

import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.service.filter.BigDecimalFilter;

/**
 * Criteria class for the {@link com.nab.domain.Product} entity. This class is used
 * in {@link com.nab.web.rest.ProductResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /products?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class ProductCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter code;

    private StringFilter name;

    private StringFilter brand;

    private StringFilter color;

    private BigDecimalFilter defaultPrice;

    private LongFilter priceProductId;

    public ProductCriteria() {
    }

    public ProductCriteria(ProductCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.code = other.code == null ? null : other.code.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.brand = other.brand == null ? null : other.brand.copy();
        this.color = other.color == null ? null : other.color.copy();
        this.defaultPrice = other.defaultPrice == null ? null : other.defaultPrice.copy();
        this.priceProductId = other.priceProductId == null ? null : other.priceProductId.copy();
    }

    @Override
    public ProductCriteria copy() {
        return new ProductCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getCode() {
        return code;
    }

    public void setCode(StringFilter code) {
        this.code = code;
    }

    public StringFilter getName() {
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public StringFilter getBrand() {
        return brand;
    }

    public void setBrand(StringFilter brand) {
        this.brand = brand;
    }

    public StringFilter getColor() {
        return color;
    }

    public void setColor(StringFilter color) {
        this.color = color;
    }

    public BigDecimalFilter getDefaultPrice() {
        return defaultPrice;
    }

    public void setDefaultPrice(BigDecimalFilter defaultPrice) {
        this.defaultPrice = defaultPrice;
    }

    public LongFilter getPriceProductId() {
        return priceProductId;
    }

    public void setPriceProductId(LongFilter priceProductId) {
        this.priceProductId = priceProductId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ProductCriteria that = (ProductCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(code, that.code) &&
            Objects.equals(name, that.name) &&
            Objects.equals(brand, that.brand) &&
            Objects.equals(color, that.color) &&
            Objects.equals(defaultPrice, that.defaultPrice) &&
            Objects.equals(priceProductId, that.priceProductId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        code,
        name,
        brand,
        color,
        defaultPrice,
        priceProductId
        );
    }

    @Override
    public String toString() {
        return "ProductCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (code != null ? "code=" + code + ", " : "") +
                (name != null ? "name=" + name + ", " : "") +
                (brand != null ? "brand=" + brand + ", " : "") +
                (color != null ? "color=" + color + ", " : "") +
                (defaultPrice != null ? "defaultPrice=" + defaultPrice + ", " : "") +
                (priceProductId != null ? "priceProductId=" + priceProductId + ", " : "") +
            "}";
    }

}
