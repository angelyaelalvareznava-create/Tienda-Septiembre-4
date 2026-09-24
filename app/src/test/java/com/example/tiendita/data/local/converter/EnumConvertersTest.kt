package com.example.tiendita.data.local.converter

import org.junit.Assert.assertEquals
import org.junit.Test

class EnumConvertersTest {

    private val converters = EnumConverters()

    @Test
    fun `AccountRole converts back and forth`() {
        val original = AccountRole.ALMACEN
        val code = converters.fromAccountRole(original)
        val decoded = converters.toAccountRole(code)
        assertEquals(original, decoded)
        assertEquals("ALMACEN", code)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `AccountRole throws on unknown code`() {
        converters.toAccountRole("UNKNOWN_ROLE")
    }

    @Test
    fun `MovementType converts back and forth`() {
        val original = MovementType.TRANSFER
        val code = converters.fromMovementType(original)
        val decoded = converters.toMovementType(code)
        assertEquals(original, decoded)
        assertEquals("TRANSFER", code)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `MovementType throws on unknown code`() {
        converters.toMovementType("MAGIC_MOVE")
    }

    @Test
    fun `CalendarEventStatus converts back and forth`() {
        val original = CalendarEventStatus.COMPLETED
        val code = converters.fromCalendarEventStatus(original)
        val decoded = converters.toCalendarEventStatus(code)
        assertEquals(original, decoded)
        assertEquals("COMPLETED", code)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `CalendarEventStatus throws on unknown code`() {
        converters.toCalendarEventStatus("DELETED")
    }

    @Test
    fun `ProductUnit converts back and forth`() {
        val original = ProductUnit.PIECE
        val code = converters.fromProductUnit(original)
        val decoded = converters.toProductUnit(code)
        assertEquals(original, decoded)
        assertEquals("PIECE", code)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `ProductUnit throws on unknown code`() {
        converters.toProductUnit("KG")
    }
}
