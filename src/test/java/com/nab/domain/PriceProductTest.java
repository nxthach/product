package com.nab.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import com.nab.web.rest.TestUtil;

public class PriceProductTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(PriceProduct.class);
        PriceProduct priceProduct1 = new PriceProduct();
        priceProduct1.setId(1L);
        PriceProduct priceProduct2 = new PriceProduct();
        priceProduct2.setId(priceProduct1.getId());
        assertThat(priceProduct1).isEqualTo(priceProduct2);
        priceProduct2.setId(2L);
        assertThat(priceProduct1).isNotEqualTo(priceProduct2);
        priceProduct1.setId(null);
        assertThat(priceProduct1).isNotEqualTo(priceProduct2);
    }
}
