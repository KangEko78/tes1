package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.EventEntity
import com.example.data.local.OrderEntity
import com.example.data.local.TicketEntity
import com.example.data.model.PaymentChannel
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

sealed class CheckInResult {
    data class Success(val ticket: TicketEntity) : CheckInResult()
    data class AlreadyUsed(val ticket: TicketEntity, val usedAtFormatted: String) : CheckInResult()
    object NotFound : CheckInResult()
}

class EventRepository(private val database: AppDatabase) {
    private val eventDao = database.eventDao()
    private val orderDao = database.orderDao()
    private val ticketDao = database.ticketDao()

    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val allTickets: Flow<List<TicketEntity>> = ticketDao.getAllTickets()

    suspend fun ensureInitialized() {
        AppDatabase.populateInitialData(database)
    }

    fun getEventById(id: Long): Flow<EventEntity?> = eventDao.getEventById(id)
    fun getOrderByInvoice(invoice: String): Flow<OrderEntity?> = orderDao.getOrderByInvoice(invoice)
    fun getTicketsByInvoice(invoice: String): Flow<List<TicketEntity>> = ticketDao.getTicketsByInvoice(invoice)
    fun getTicketById(id: Long): Flow<TicketEntity?> = ticketDao.getTicketById(id)

    suspend fun createOrder(
        event: EventEntity,
        tierId: String,
        tierName: String,
        unitPrice: Long,
        quantity: Int,
        channel: PaymentChannel,
        customerName: String,
        customerEmail: String,
        customerPhone: String,
        customerIdNumber: String
    ): String {
        val invoiceNumber = "INV-" + SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date()) + "-" + Random.nextInt(10000, 99999)
        val subtotal = unitPrice * quantity
        val taxFee = (subtotal * 11) / 100 // PPN 11%
        val adminFee = channel.fee
        val totalAmount = subtotal + taxFee + adminFee

        val vaPrefix = when (channel) {
            PaymentChannel.VA_BCA -> "88089"
            PaymentChannel.VA_MANDIRI -> "89312"
            PaymentChannel.VA_BNI -> "98821"
            PaymentChannel.VA_BRI -> "12845"
            else -> "88000"
        }
        val vaNumber = vaPrefix + Random.nextLong(1000000000L, 9999999999L)
        val qrisPayload = "00020101021226670016ID.CO.QRIS.WWW011893600918" +
                Random.nextLong(1000000000L, 9999999999L) +
                "5204581453033605802ID5920EVENTPAY INDONESIA6007JAKARTA62190115" +
                invoiceNumber.replace("-", "") + "6304" + Random.nextInt(1000, 9999).toString(16).uppercase()
        val paymentCode = "PAY-" + Random.nextInt(100000, 999999)

        val now = System.currentTimeMillis()
        val expiresAt = now + (15 * 60 * 1000L) // 15 minutes payment gateway window

        val order = OrderEntity(
            invoiceNumber = invoiceNumber,
            eventId = event.id,
            eventTitle = event.title,
            eventDate = event.date,
            eventVenue = event.venue,
            tierId = tierId,
            tierName = tierName,
            ticketQuantity = quantity,
            unitPrice = unitPrice,
            subtotal = subtotal,
            adminFee = adminFee,
            taxFee = taxFee,
            totalAmount = totalAmount,
            paymentChannelCode = channel.code,
            status = "PENDING",
            createdAt = now,
            expiresAt = expiresAt,
            settledAt = null,
            customerName = customerName,
            customerEmail = customerEmail,
            customerPhone = customerPhone,
            customerIdNumber = customerIdNumber,
            vaNumber = vaNumber,
            qrisPayload = qrisPayload,
            paymentCode = paymentCode
        )

        orderDao.insertOrder(order)
        return invoiceNumber
    }

    suspend fun simulatePaymentSuccess(invoiceNumber: String): Boolean {
        val order = orderDao.getOrderByInvoiceSync(invoiceNumber) ?: return false
        if (order.status == "SETTLED") return true

        val now = System.currentTimeMillis()
        orderDao.updateOrderStatus(invoiceNumber, "SETTLED", now)

        // Generate digital tickets for this settled order
        val tickets = mutableListOf<TicketEntity>()
        val gateOptions = listOf("GATE A - UTAMA", "GATE B - BARAT", "GATE C - TIMUR")
        val gate = gateOptions[Random.nextInt(gateOptions.size)]

        for (i in 1..order.ticketQuantity) {
            val shortCode = "EP-" + SimpleDateFormat("yy", Locale.getDefault()).format(Date()) +
                    "-" + Random.nextInt(1000, 9999) + "-" + Random.nextInt(1000, 9999)
            val seat = if (order.tierName.contains("VIP", ignoreCase = true)) {
                "VIP Row " + ('A'..'F').random() + "-" + Random.nextInt(1, 30)
            } else {
                "Festival General Standing"
            }

            tickets.add(
                TicketEntity(
                    ticketCode = shortCode,
                    orderInvoice = order.invoiceNumber,
                    eventId = order.eventId,
                    eventTitle = order.eventTitle,
                    eventDate = order.eventDate,
                    eventTime = "18:00 WIB",
                    eventVenue = order.eventVenue,
                    tierName = order.tierName,
                    attendeeName = if (order.ticketQuantity == 1) order.customerName else "${order.customerName} (Tiket #$i)",
                    attendeeEmail = order.customerEmail,
                    attendeePhone = order.customerPhone,
                    isCheckedIn = false,
                    checkInTimestamp = null,
                    gateNumber = gate,
                    seatInfo = seat
                )
            )
        }

        ticketDao.insertTickets(tickets)
        return true
    }

    suspend fun checkInTicket(code: String): CheckInResult {
        val cleanCode = code.trim().uppercase()
        val ticket = ticketDao.getTicketByCode(cleanCode) ?: return CheckInResult.NotFound

        if (ticket.isCheckedIn) {
            val format = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("in", "ID"))
            val usedAt = ticket.checkInTimestamp?.let { format.format(Date(it)) } ?: "Sebelumnya"
            return CheckInResult.AlreadyUsed(ticket, usedAt)
        }

        val now = System.currentTimeMillis()
        ticketDao.updateCheckIn(ticket.ticketCode, true, now)
        return CheckInResult.Success(ticket.copy(isCheckedIn = true, checkInTimestamp = now))
    }
}
