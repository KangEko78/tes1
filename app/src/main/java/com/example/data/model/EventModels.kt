package com.example.data.model

import java.text.NumberFormat
import java.util.Locale

enum class PaymentCategory(val label: String) {
    QRIS("Instant QRIS"),
    VIRTUAL_ACCOUNT("Virtual Account"),
    E_WALLET("E-Wallet"),
    CREDIT_CARD("Kartu Kredit/Debit"),
    RETAIL("Gerai Retail")
}

enum class PaymentChannel(
    val code: String,
    val title: String,
    val category: PaymentCategory,
    val fee: Long,
    val feeDescription: String,
    val estimatedTime: String,
    val iconName: String
) {
    QRIS("qris", "QRIS (Semua Pembayaran)", PaymentCategory.QRIS, 1500L, "Rp 1.500 (MDR 0.7%)", "Instan 5 Detik", "ic_qris"),
    VA_BCA("va_bca", "BCA Virtual Account", PaymentCategory.VIRTUAL_ACCOUNT, 3500L, "Rp 3.500", "Verifikasi Otomatis", "ic_bca"),
    VA_MANDIRI("va_mandiri", "Mandiri Virtual Account", PaymentCategory.VIRTUAL_ACCOUNT, 3500L, "Rp 3.500", "Verifikasi Otomatis", "ic_mandiri"),
    VA_BNI("va_bni", "BNI Virtual Account", PaymentCategory.VIRTUAL_ACCOUNT, 3500L, "Rp 3.500", "Verifikasi Otomatis", "ic_bni"),
    VA_BRI("va_bri", "BRI Virtual Account (BRIVA)", PaymentCategory.VIRTUAL_ACCOUNT, 3500L, "Rp 3.500", "Verifikasi Otomatis", "ic_bri"),
    EWALLET_GOPAY("ewallet_gopay", "GoPay / GoPay Later", PaymentCategory.E_WALLET, 2000L, "Rp 2.000", "Instan 1-Tap", "ic_gopay"),
    EWALLET_OVO("ewallet_ovo", "OVO Cash", PaymentCategory.E_WALLET, 2000L, "Rp 2.000", "Notifikasi Push", "ic_ovo"),
    EWALLET_DANA("ewallet_dana", "DANA Wallet", PaymentCategory.E_WALLET, 2000L, "Rp 2.000", "Instan PIN", "ic_dana"),
    EWALLET_SHOPEEPAY("ewallet_shopeepay", "ShopeePay", PaymentCategory.E_WALLET, 2000L, "Rp 2.000", "Instan App", "ic_shopeepay"),
    CREDIT_CARD("credit_card", "Visa / Mastercard / JCB", PaymentCategory.CREDIT_CARD, 4500L, "2.5% + Rp 2.000", "3D Secure OTP", "ic_card"),
    RETAIL_INDOMARET("retail_indomaret", "Indomaret / Ceriamart", PaymentCategory.RETAIL, 5000L, "Rp 5.000", "Bayar di Kasir", "ic_indomaret"),
    RETAIL_ALFAMART("retail_alfamart", "Alfamart / Alfamidi", PaymentCategory.RETAIL, 5000L, "Rp 5.000", "Bayar di Kasir", "ic_alfamart")
}

enum class OrderStatus(val label: String, val badgeColorHex: Long) {
    PENDING("Menunggu Pembayaran", 0xFFF59E0B),
    SETTLED("Pembayaran Berhasil", 0xFF10B981),
    EXPIRED("Kadaluarsa", 0xFFEF4444),
    CANCELLED("Dibatalkan", 0xFF64748B)
}

data class TicketTierItem(
    val id: String,
    val name: String,
    val price: Long,
    val description: String,
    val quota: Int,
    val remaining: Int
)

fun formatRupiah(amount: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount).replace("Rp", "Rp ")
}
