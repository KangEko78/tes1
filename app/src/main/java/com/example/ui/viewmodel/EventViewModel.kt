package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.EventEntity
import com.example.data.local.OrderEntity
import com.example.data.local.TicketEntity
import com.example.data.model.PaymentChannel
import com.example.data.model.TicketTierItem
import com.example.data.repository.CheckInResult
import com.example.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = EventRepository(database)

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
    }

    // Filter & Search
    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val rawEvents: StateFlow<List<EventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredEvents: StateFlow<List<EventEntity>> = combine(
        rawEvents,
        _selectedCategory,
        _searchQuery
    ) { events, category, query ->
        events.filter { event ->
            val matchCategory = if (category == "Semua") true else event.category.contains(category, ignoreCase = true)
            val matchQuery = if (query.isBlank()) true else {
                event.title.contains(query, ignoreCase = true) ||
                        event.location.contains(query, ignoreCase = true) ||
                        event.lineup.contains(query, ignoreCase = true)
            }
            matchCategory && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Orders & Tickets
    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTickets: StateFlow<List<TicketEntity>> = repository.allTickets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Checkout Flow State
    private val _selectedEvent = MutableStateFlow<EventEntity?>(null)
    val selectedEvent: StateFlow<EventEntity?> = _selectedEvent.asStateFlow()

    private val _selectedTier = MutableStateFlow<TicketTierItem?>(null)
    val selectedTier: StateFlow<TicketTierItem?> = _selectedTier.asStateFlow()

    private val _ticketQuantity = MutableStateFlow(1)
    val ticketQuantity: StateFlow<Int> = _ticketQuantity.asStateFlow()

    // Attendee form fields
    val customerName = MutableStateFlow("")
    val customerEmail = MutableStateFlow("")
    val customerPhone = MutableStateFlow("")
    val customerIdNumber = MutableStateFlow("")

    // Selected payment channel
    private val _selectedPaymentChannel = MutableStateFlow(PaymentChannel.QRIS)
    val selectedPaymentChannel: StateFlow<PaymentChannel> = _selectedPaymentChannel.asStateFlow()

    // Active Order for Payment Gateway Screen
    private val _currentInvoice = MutableStateFlow<String?>(null)
    val currentInvoice: StateFlow<String?> = _currentInvoice.asStateFlow()

    private val _currentOrder = MutableStateFlow<OrderEntity?>(null)
    val currentOrder: StateFlow<OrderEntity?> = _currentOrder.asStateFlow()

    private val _isProcessingPayment = MutableStateFlow(false)
    val isProcessingPayment: StateFlow<Boolean> = _isProcessingPayment.asStateFlow()

    private val _paymentSuccessEvent = MutableStateFlow<String?>(null)
    val paymentSuccessEvent: StateFlow<String?> = _paymentSuccessEvent.asStateFlow()

    // Gate Scanner Result State
    private val _checkInResult = MutableStateFlow<CheckInResult?>(null)
    val checkInResult: StateFlow<CheckInResult?> = _checkInResult.asStateFlow()

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectEvent(event: EventEntity) {
        _selectedEvent.value = event
        // Select first tier by default
        val tiers = parseTiers(event.tiersJson)
        _selectedTier.value = tiers.firstOrNull()
        _ticketQuantity.value = 1
    }

    fun selectTier(tier: TicketTierItem) {
        _selectedTier.value = tier
    }

    fun setQuantity(qty: Int) {
        if (qty in 1..10) {
            _ticketQuantity.value = qty
        }
    }

    fun selectPaymentChannel(channel: PaymentChannel) {
        _selectedPaymentChannel.value = channel
    }

    fun setCurrentInvoice(invoice: String) {
        _currentInvoice.value = invoice
        viewModelScope.launch {
            repository.getOrderByInvoice(invoice).collect { order ->
                _currentOrder.value = order
            }
        }
    }

    fun createOrder(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val event = _selectedEvent.value
        val tier = _selectedTier.value
        if (event == null || tier == null) {
            onError("Silakan pilih event dan kategori tiket.")
            return
        }

        val name = customerName.value.trim()
        val email = customerEmail.value.trim()
        val phone = customerPhone.value.trim()
        val idNum = customerIdNumber.value.trim()

        if (name.isBlank() || email.isBlank() || phone.isBlank()) {
            onError("Lengkapi nama, email, dan nomor WhatsApp pemesan.")
            return
        }

        viewModelScope.launch {
            try {
                val invoice = repository.createOrder(
                    event = event,
                    tierId = tier.id,
                    tierName = tier.name,
                    unitPrice = tier.price,
                    quantity = _ticketQuantity.value,
                    channel = _selectedPaymentChannel.value,
                    customerName = name,
                    customerEmail = email,
                    customerPhone = phone,
                    customerIdNumber = if (idNum.isNotBlank()) idNum else "3171000000000001"
                )
                setCurrentInvoice(invoice)
                onSuccess(invoice)
            } catch (e: Exception) {
                onError(e.message ?: "Gagal memproses pesanan.")
            }
        }
    }

    fun simulateInstantPayment(invoice: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isProcessingPayment.value = true
            kotlinx.coroutines.delay(1200) // realistic gateway webhook roundtrip
            repository.simulatePaymentSuccess(invoice)
            _isProcessingPayment.value = false
            _paymentSuccessEvent.value = invoice
            onComplete()
        }
    }

    fun clearPaymentSuccessEvent() {
        _paymentSuccessEvent.value = null
    }

    fun checkInTicket(code: String) {
        viewModelScope.launch {
            val result = repository.checkInTicket(code)
            _checkInResult.value = result
        }
    }

    fun clearCheckInResult() {
        _checkInResult.value = null
    }

    fun parseTiers(json: String): List<TicketTierItem> {
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<TicketTierItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TicketTierItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        price = obj.getLong("price"),
                        description = obj.optString("description", ""),
                        quota = obj.optInt("quota", 100),
                        remaining = obj.optInt("remaining", 50)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
