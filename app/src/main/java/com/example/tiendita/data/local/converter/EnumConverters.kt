package com.example.tiendita.data.local.converter

import androidx.room.TypeConverter

enum class AccountRole(val code: String) {
    ADMIN("ADMIN"),
    ALMACEN("ALMACEN"),
    CONSULTA("CONSULTA");

    companion object {
        fun fromCode(code: String): AccountRole {
            return entries.find { it.code == code } 
                ?: throw IllegalArgumentException("Unknown AccountRole code: $code")
        }
    }
}

enum class MovementType(val code: String) {
    IN("IN"),
    OUT("OUT"),
    ADJ_IN("ADJ_IN"),
    ADJ_OUT("ADJ_OUT"),
    TRANSFER("TRANSFER");

    companion object {
        fun fromCode(code: String): MovementType {
            return entries.find { it.code == code }
                ?: throw IllegalArgumentException("Unknown MovementType code: $code")
        }
    }
}

enum class CalendarEventStatus(val code: String) {
    PENDING("PENDING"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED");

    companion object {
        fun fromCode(code: String): CalendarEventStatus {
            return entries.find { it.code == code }
                ?: throw IllegalArgumentException("Unknown CalendarEventStatus code: $code")
        }
    }
}

enum class ProductUnit(val code: String) {
    PIECE("PIECE");

    companion object {
        fun fromCode(code: String): ProductUnit {
            return entries.find { it.code == code }
                ?: throw IllegalArgumentException("Unknown ProductUnit code: $code")
        }
    }
}

class EnumConverters {
    @TypeConverter
    fun toAccountRole(code: String): AccountRole = AccountRole.fromCode(code)

    @TypeConverter
    fun fromAccountRole(role: AccountRole): String = role.code

    @TypeConverter
    fun toMovementType(code: String): MovementType = MovementType.fromCode(code)

    @TypeConverter
    fun fromMovementType(type: MovementType): String = type.code

    @TypeConverter
    fun toCalendarEventStatus(code: String): CalendarEventStatus = CalendarEventStatus.fromCode(code)

    @TypeConverter
    fun fromCalendarEventStatus(status: CalendarEventStatus): String = status.code

    @TypeConverter
    fun toProductUnit(code: String): ProductUnit = ProductUnit.fromCode(code)

    @TypeConverter
    fun fromProductUnit(unit: ProductUnit): String = unit.code
}
