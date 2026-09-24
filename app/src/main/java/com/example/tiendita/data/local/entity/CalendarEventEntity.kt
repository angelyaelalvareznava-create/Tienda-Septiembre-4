package com.example.tiendita.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tiendita.data.local.converter.CalendarEventStatus

@Entity(
    tableName = "calendar_events",
    foreignKeys = [
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["id"],
            childColumns = ["employee_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = UserAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["created_by"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["employee_id"]),
        Index(value = ["created_by"]),
        Index(value = ["start_at"])
    ]
)
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val title: String,
    
    val description: String?,
    
    @ColumnInfo(name = "start_at")
    val startAt: Long,
    
    @ColumnInfo(name = "end_at")
    val endAt: Long?,
    
    val status: CalendarEventStatus,
    
    @ColumnInfo(name = "employee_id")
    val employeeId: Long?,
    
    @ColumnInfo(name = "created_by")
    val createdBy: Long,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
