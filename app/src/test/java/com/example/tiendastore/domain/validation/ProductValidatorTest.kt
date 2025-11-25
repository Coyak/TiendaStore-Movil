package com.example.tiendastore.domain.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductValidatorTest {
    @Test
    fun `invalid price and stock produce errors`() {
        val errors = ProductValidator.validate(name = "PS5", priceText = "0", stockText = "-1", category = "Consolas")
        assertFalse(errors.isEmpty())
        assertTrue(errors.containsKey("price"))
        assertTrue(errors.containsKey("stock"))
    }

    @Test
    fun `valid product passes`() {
        val errors = ProductValidator.validate(name = "Control Pro", priceText = "49990", stockText = "5", category = "Accesorios")
        assertTrue(errors.isEmpty())
    }
}
