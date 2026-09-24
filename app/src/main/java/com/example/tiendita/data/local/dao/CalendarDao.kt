package com.example.tiendita.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tiendita.data.local.entity.CalendarEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {
    @Insert
    suspend fun insertEvent(event: CalendarEventEntity): Long

    @Update
    suspend fun updateEvent(event: CalendarEventEntity)

    @Query("SELECT * FROM calendar_events WHERE id = :id")
    suspend fun getEventById(id: Long): CalendarEventEntity?

    @Query("SELECT * FROM calendar_events WHERE start_at >= :start AND start_at <= :end")
    fun getEventsInDateRangeFlow(start: Long, end: Long): Flow<List<CalendarEventEntity>>
}
