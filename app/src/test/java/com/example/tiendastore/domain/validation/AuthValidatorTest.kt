package com.example.tiendastore.domain.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {
    @Test
    fun `login fails with short password and invalid email`() {
        val errors = AuthValidator.validateLogin("a", "123")
        assertFalse(errors.isEmpty())
        assertTrue(errors.containsKey("email"))
        assertTrue(errors.containsKey("password"))
    }

    @Test
    fun `register passes with valid data`() {
        val errors = AuthValidator.validateRegister("Carlos", "carlos@mail.com", "123456", "123456")
        assertTrue(errors.isEmpty())
    }
}
