package com.example

import com.example.data.model.PaymentChannel
import com.example.data.model.formatRupiah
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testRupiahFormatting() {
        val formatted = formatRupiah(1250000L)
        assertTrue(formatted.contains("1.250.000") || formatted.contains("1,250,000") || formatted.contains("1250000"))
    }

    @Test
    fun testPaymentChannelsConfig() {
        val qris = PaymentChannel.QRIS
        assertEquals("qris", qris.code)
        assertEquals(1500L, qris.fee)

        val vaBca = PaymentChannel.VA_BCA
        assertEquals("va_bca", vaBca.code)
        assertEquals(3500L, vaBca.fee)
    }

    @Test
    fun testPricingCalculations() {
        val unitPrice = 500000L
        val quantity = 2
        val subtotal = unitPrice * quantity
        val taxFee = (subtotal * 11) / 100
        val adminFee = PaymentChannel.QRIS.fee
        val total = subtotal + taxFee + adminFee

        assertEquals(1000000L, subtotal)
        assertEquals(110000L, taxFee)
        assertEquals(1111500L, total)
    }
}
