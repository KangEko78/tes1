package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val date: String,
    val time: String,
    val location: String,
    val venue: String,
    val organizer: String,
    val startingPrice: Long,
    val description: String,
    val lineup: String,
    val tiersJson: String,
    val gradientStartHex: Long,
    val gradientEndHex: Long
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val eventId: Long,
    val eventTitle: String,
    val eventDate: String,
    val eventVenue: String,
    val tierId: String,
    val tierName: String,
    val ticketQuantity: Int,
    val unitPrice: Long,
    val subtotal: Long,
    val adminFee: Long,
    val taxFee: Long,
    val totalAmount: Long,
    val paymentChannelCode: String,
    val status: String, // PENDING, SETTLED, EXPIRED, CANCELLED
    val createdAt: Long,
    val expiresAt: Long,
    val settledAt: Long? = null,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val customerIdNumber: String,
    val vaNumber: String,
    val qrisPayload: String,
    val paymentCode: String
)

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ticketCode: String,
    val orderInvoice: String,
    val eventId: Long,
    val eventTitle: String,
    val eventDate: String,
    val eventTime: String,
    val eventVenue: String,
    val tierName: String,
    val attendeeName: String,
    val attendeeEmail: String,
    val attendeePhone: String,
    val isCheckedIn: Boolean = false,
    val checkInTimestamp: Long? = null,
    val gateNumber: String = "GATE A",
    val seatInfo: String = "Festival Standing"
)
