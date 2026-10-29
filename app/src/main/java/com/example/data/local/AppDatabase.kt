package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [EventEntity::class, OrderEntity::class, TicketEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun orderDao(): OrderDao
    abstract fun ticketDao(): TicketDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "event_payment_gateway.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val eventDao = database.eventDao()
            val orderDao = database.orderDao()
            val ticketDao = database.ticketDao()

            if (eventDao.getEventCount() == 0) {
                val seedEvents = listOf(
                    EventEntity(
                        title = "Jakarta Soundwave Music Festival 2026",
                        category = "Musik & Konser",
                        date = "14 - 15 Nov 2026",
                        time = "15:00 - 23:30 WIB",
                        location = "Jakarta Pusat",
                        venue = "Stadion Madya Gelora Bung Karno (GBK)",
                        organizer = "Soundwave Live Production",
                        startingPrice = 450000L,
                        description = "Festival musik akbar tahunan paling dinanti dengan 3 panggung spektakuler, audio visual bertaraf internasional, festival kuliner, dan deretan musisi papan atas nasional dan internasional.",
                        lineup = "Sheila on 7, Hindia, NIKI, Rich Brian, Isyana Sarasvati, Maliq & D'Essentials, Pamungkas, Barasuara",
                        tiersJson = """[
                            {"id":"sb_eb","name":"Early Bird Festival","price":450000,"description":"Akses 2 hari festival, standing area panggung utama","quota":500,"remaining":42},
                            {"id":"sb_reg","name":"Presale 1 Festival Pass","price":650000,"description":"Akses 2 hari area festival reguler + merchandise wristband","quota":1500,"remaining":320},
                            {"id":"sb_vip","name":"VIP Lounge & Tribune","price":1250000,"description":"Akses viewing deck khusus, VIP Bar & Toilet, fast track entry","quota":400,"remaining":65},
                            {"id":"sb_vvip","name":"VVIP Backstage Experience","price":2500000,"description":"Meet & Greet pass, free flow canapes & lounge, official merchandise kit","quota":100,"remaining":14}
                        ]""",
                        gradientStartHex = 0xFF4F46E5,
                        gradientEndHex = 0xFF06B6D4
                    ),
                    EventEntity(
                        title = "Indonesia Tech & AI Summit 2026",
                        category = "Tech & AI",
                        date = "28 Nov 2026",
                        time = "08:30 - 17:30 WIB",
                        location = "Jakarta Pusat",
                        venue = "JIExpo Convention Center Kemayoran",
                        organizer = "Indonesia AI Society & TechMedia",
                        startingPrice = 175000L,
                        description = "Konferensi teknologi & kecerdasan buatan terbesar di Asia Tenggara. Menghadirkan keynote speaker dunia, pameran startup terdepan, demo AI praktis, dan networking expo.",
                        lineup = "Google AI Research Lead, VP Engineering GoTo, Tech Evangelist AWS, Founder Bukalapak, CTO unicorn fintek",
                        tiersJson = """[
                            {"id":"tech_std","name":"Student Pass (KTM)","price":175000,"description":"Akses seluruh sesi seminar stage 2, e-certificate resmi","quota":300,"remaining":88},
                            {"id":"tech_gen","name":"General Attendee Pass","price":380000,"description":"Akses keynote all-stages, workshop tracks, lunch box, expo hall","quota":800,"remaining":210},
                            {"id":"tech_vip","name":"VIP Executive & Networking","price":950000,"description":"Akses VIP lounge, reserved front row seats, exclusive networking dinner","quota":150,"remaining":27}
                        ]""",
                        gradientStartHex = 0xFF2563EB,
                        gradientEndHex = 0xFF7C3AED
                    ),
                    EventEntity(
                        title = "Java Jazz & Art Showcase 2026",
                        category = "Musik & Seni",
                        date = "05 Des 2026",
                        time = "16:00 - 23:00 WIB",
                        location = "Yogyakarta",
                        venue = "Jogja Expo Center (JEC) Indoor Arena",
                        organizer = "Swara Nuswantara Arts",
                        startingPrice = 280000L,
                        description = "Malam syahdu perpaduan musik jazz nusantara kontemporer dengan galeri seni instalasi cahaya interaktif dan pameran kriya UMKM lokal terkurasi.",
                        lineup = "Tulus, Tompi, Indra Lesmana Quartet, Eva Celia, Ardhito Pramono, Nonaria",
                        tiersJson = """[
                            {"id":"jazz_reg","name":"Regular Festival Seated","price":280000,"description":"Free seating tribun, akses galeri instalasi seni","quota":600,"remaining":150},
                            {"id":"jazz_vip","name":"VIP Floor Table","price":650000,"description":"Numbered table seating, complimentary welcome drink, art catalog","quota":200,"remaining":45},
                            {"id":"jazz_royal","name":"Royal Patron Box","price":1200000,"description":"Private balcony sofa, private butler, signed memorabilia vinyl","quota":40,"remaining":8}
                        ]""",
                        gradientStartHex = 0xFFD97706,
                        gradientEndHex = 0xFFDC2626
                    ),
                    EventEntity(
                        title = "Jakarta Eco Run & Marathon 2026",
                        category = "Olahraga",
                        date = "12 Des 2026",
                        time = "05:00 - 10:30 WIB",
                        location = "Jakarta Pusat",
                        venue = "Monumen Nasional (Monas) Silang Merdeka",
                        organizer = "Komunitas Lari Hijau Nusantara",
                        startingPrice = 220000L,
                        description = "Lari maraton ramah lingkungan terbesar di ibu kota. Setiap pelari berkontribusi pada penanaman 1 pohon mangrove di pesisir utara Jakarta.",
                        lineup = "Running jersey dryfit jersey, BIB nomor dada chip RFID, medali logam finisher, recovery booth",
                        tiersJson = """[
                            {"id":"run_5k","name":"5K Fun Walk & Run","price":220000,"description":"Kategori keluarga & pemula, medali finisher, goodie bag","quota":1000,"remaining":340},
                            {"id":"run_10k","name":"10K Timed Challenge","price":350000,"description":"Timing chip resmi, isotonic hydrations, finisher medal & e-cert","quota":800,"remaining":120},
                            {"id":"run_21k","name":"21K Half Marathon","price":480000,"description":"Half marathon berstandar PASI, timing chip, finisher tee exclusive","quota":500,"remaining":75}
                        ]""",
                        gradientStartHex = 0xFF059669,
                        gradientEndHex = 0xFF0284C7
                    ),
                    EventEntity(
                        title = "Masterclass UI/UX & Fintech Product Design",
                        category = "Workshop",
                        date = "19 Des 2026",
                        time = "09:00 - 16:30 WIB",
                        location = "Surabaya",
                        venue = "Grand City Convention Hall & Live Interactive",
                        organizer = "Design Guild Indonesia",
                        startingPrice = 150000L,
                        description = "Bedah tuntas arsitektur UI/UX payment gateway, conversion rate optimization, micro-interactions, dan studi kasus sistem pembayaran real-world.",
                        lineup = "Head of Product Design Fintek Unicorn, Staff UX Researcher, Principal Design System Lead",
                        tiersJson = """[
                            {"id":"ws_online","name":"Online Live Streaming Pass","price":150000,"description":"Akses rekaman HD 30 hari, materi Figma kit, e-certificate","quota":500,"remaining":230},
                            {"id":"ws_offline","name":"Offline Hands-on Lab Seat","price":490000,"description":"Tatap muka langsung, review portofolio 1-on-1, networking lunch","quota":80,"remaining":18}
                        ]""",
                        gradientStartHex = 0xFF7C3AED,
                        gradientEndHex = 0xFFDB2777
                    )
                )
                eventDao.insertEvents(seedEvents)

                // Seed one active sample order & ticket so the user can test the scanner & pass immediately!
                val sampleInvoice = "INV-202611-77892"
                val sampleOrder = OrderEntity(
                    invoiceNumber = sampleInvoice,
                    eventId = 1L,
                    eventTitle = "Jakarta Soundwave Music Festival 2026",
                    eventDate = "14 - 15 Nov 2026",
                    eventVenue = "Stadion Madya Gelora Bung Karno (GBK)",
                    tierId = "sb_vip",
                    tierName = "VIP Lounge & Tribune",
                    ticketQuantity = 1,
                    unitPrice = 1250000L,
                    subtotal = 1250000L,
                    adminFee = 1500L,
                    taxFee = 137500L,
                    totalAmount = 1389000L,
                    paymentChannelCode = "qris",
                    status = "SETTLED",
                    createdAt = System.currentTimeMillis() - 7200000L,
                    expiresAt = System.currentTimeMillis() - 6300000L,
                    settledAt = System.currentTimeMillis() - 6900000L,
                    customerName = "Budi Pratama",
                    customerEmail = "budi.pratama@email.com",
                    customerPhone = "081289382100",
                    customerIdNumber = "3171052901940003",
                    vaNumber = "8808990182938120",
                    qrisPayload = "00020101021226670016ID.CO.QRIS.WWW01189360091800000000015204581453033605802ID5920EVENTPAY INDONESIA6007JAKARTA62190115INV202611778926304A1B2",
                    paymentCode = "EP-PAY-77892"
                )
                orderDao.insertOrder(sampleOrder)

                val sampleTicket = TicketEntity(
                    ticketCode = "EP-SW26-VIP-8819",
                    orderInvoice = sampleInvoice,
                    eventId = 1L,
                    eventTitle = "Jakarta Soundwave Music Festival 2026",
                    eventDate = "14 - 15 Nov 2026",
                    eventTime = "15:00 - 23:30 WIB",
                    eventVenue = "Stadion Madya Gelora Bung Karno (GBK)",
                    tierName = "VIP Lounge & Tribune",
                    attendeeName = "Budi Pratama",
                    attendeeEmail = "budi.pratama@email.com",
                    attendeePhone = "081289382100",
                    isCheckedIn = false,
                    checkInTimestamp = null,
                    gateNumber = "VIP GATE 1",
                    seatInfo = "VIP Lounge Platform B"
                )
                ticketDao.insertTickets(listOf(sampleTicket))
            }
        }
    }
}
