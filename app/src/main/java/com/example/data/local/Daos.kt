package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY id ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    fun getEventById(id: Long): Flow<EventEntity?>

    @Query("SELECT COUNT(*) FROM events")
    suspend fun getEventCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    fun getOrderById(id: Long): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE invoiceNumber = :invoice LIMIT 1")
    fun getOrderByInvoice(invoice: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE invoiceNumber = :invoice LIMIT 1")
    suspend fun getOrderByInvoiceSync(invoice: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("UPDATE orders SET status = :status, settledAt = :settledAt WHERE invoiceNumber = :invoiceNumber")
    suspend fun updateOrderStatus(invoiceNumber: String, status: String, settledAt: Long?)
}

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY id DESC")
    fun getAllTickets(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE orderInvoice = :invoice")
    fun getTicketsByInvoice(invoice: String): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE ticketCode = :code LIMIT 1")
    suspend fun getTicketByCode(code: String): TicketEntity?

    @Query("SELECT * FROM tickets WHERE id = :id LIMIT 1")
    fun getTicketById(id: Long): Flow<TicketEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<TicketEntity>)

    @Query("UPDATE tickets SET isCheckedIn = :isCheckedIn, checkInTimestamp = :timestamp WHERE ticketCode = :code")
    suspend fun updateCheckIn(code: String, isCheckedIn: Boolean, timestamp: Long)
}
