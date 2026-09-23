package com.example.tiendita.calendar

import com.example.tiendita.ui.screens.calendar.getDaysInMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalendarMonthGridTest {

    @Test
    fun `leap year february returns 29 days`() {
        // Febrero 2024 (bisiesto). Mes 1 = Febrero
        val days = getDaysInMonth(2024, 1)
        val validDays = days.filterNotNull()
        assertEquals(29, validDays.size)
        assertEquals(1, validDays.first())
        assertEquals(29, validDays.last())
    }

    @Test
    fun `month with 30 days`() {
        // Septiembre 2026. Mes 8 = Septiembre
        val days = getDaysInMonth(2026, 8)
        val validDays = days.filterNotNull()
        assertEquals(30, validDays.size)
    }

    @Test
    fun `month with 31 days`() {
        // Octubre 2026. Mes 9 = Octubre
        val days = getDaysInMonth(2026, 9)
        val validDays = days.filterNotNull()
        assertEquals(31, validDays.size)
    }

    @Test
    fun `first day alignment is correct`() {
        // Septiembre 2026. El día 1 cae en Martes.
        // Domingo(0), Lunes(1), Martes(2). Por lo tanto, 2 nulos al inicio.
        val days = getDaysInMonth(2026, 8)
        assertNull(days[0]) // Domingo
        assertNull(days[1]) // Lunes
        assertEquals(1, days[2]) // Martes
    }

    @Test
    fun `transition from december to january alignment`() {
        // Diciembre 2025. Día 1 = Lunes.
        val decDays = getDaysInMonth(2025, 11)
        assertNull(decDays[0])
        assertEquals(1, decDays[1])
        
        // Enero 2026. Día 1 = Jueves.
        val janDays = getDaysInMonth(2026, 0)
        assertNull(janDays[0]) // Dom
        assertNull(janDays[1]) // Lun
        assertNull(janDays[2]) // Mar
        assertNull(janDays[3]) // Mié
        assertEquals(1, janDays[4]) // Jue
    }
}
