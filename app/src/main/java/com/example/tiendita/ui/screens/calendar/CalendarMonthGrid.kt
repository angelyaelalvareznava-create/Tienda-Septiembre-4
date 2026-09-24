package com.example.tiendita.ui.screens.calendar

import java.util.Calendar

/**
 * Función pura para calcular la cuadrícula de un mes específico.
 * Retorna una lista de enteros nulos o con el día del mes.
 * Los nulos representan espacios vacíos antes del primer día del mes.
 * El mes debe ser base 0 (0 = Enero, 11 = Diciembre).
 */
fun getDaysInMonth(year: Int, month: Int): List<Int?> {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month)
    cal.set(Calendar.DAY_OF_MONTH, 1)

    // Calendar.DAY_OF_WEEK: 1 (Domingo) a 7 (Sábado)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val grid = mutableListOf<Int?>()
    
    // Espacios vacíos previos al día 1
    for (i in 1 until firstDayOfWeek) {
        grid.add(null)
    }
    
    // Días del mes
    for (day in 1..daysInMonth) {
        grid.add(day)
    }
    
    // Espacios vacíos al final para completar semanas completas de 7 días
    while (grid.size % 7 != 0) {
        grid.add(null)
    }

    return grid
}
