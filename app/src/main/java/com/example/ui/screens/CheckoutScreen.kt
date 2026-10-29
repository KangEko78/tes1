package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentCategory
import com.example.data.model.PaymentChannel
import com.example.data.model.formatRupiah
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryIndigoLight
import com.example.ui.viewmodel.EventViewModel

@Composable
fun CheckoutScreen(
    viewModel: EventViewModel,
    onBack: () -> Unit,
    onOrderCreated: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val event = viewModel.selectedEvent.collectAsState().value ?: return
    val tier = viewModel.selectedTier.collectAsState().value ?: return
    val qty by viewModel.ticketQuantity.collectAsState()
    val selectedChannel by viewModel.selectedPaymentChannel.collectAsState()

    val name by viewModel.customerName.collectAsState()
    val email by viewModel.customerEmail.collectAsState()
    val phone by viewModel.customerPhone.collectAsState()
    val idNumber by viewModel.customerIdNumber.collectAsState()

    val subtotal = tier.price * qty
    val taxFee = (subtotal * 11) / 100
    val adminFee = selectedChannel.fee
    val totalAmount = subtotal + taxFee + adminFee

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("checkout_lazy_column"),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Header Top Bar
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF101726),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("checkout_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Checkout Pembayaran",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Payment Gateway Terenkripsi 256-bit",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Order Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131A2A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243047))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = event.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${event.date} • ${event.venue}",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        androidx.compose.material3.HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${tier.name} ($qty tiket)",
                                color = Color(0xFFCBD5E1),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatRupiah(subtotal),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Attendee Form
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Data Pemesan / Pengunjung",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "E-Tiket akan dikirimkan ke email dan nomor kontak ini.",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Name Field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.customerName.value = it },
                        label = { Text("Nama Lengkap (sesuai KTP)", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF94A3B8)) },
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_name"),
                        shape = RoundedCornerShape(12.dp),
                        colors = outlinedFieldColors(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { viewModel.customerEmail.value = it },
                        label = { Text("Alamat Email", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF94A3B8)) },
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_email"),
                        shape = RoundedCornerShape(12.dp),
                        colors = outlinedFieldColors(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Phone Field
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { viewModel.customerPhone.value = it },
                        label = { Text("Nomor WhatsApp / HP", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF94A3B8)) },
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_phone"),
                        shape = RoundedCornerShape(12.dp),
                        colors = outlinedFieldColors(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ID Card Field
                    OutlinedTextField(
                        value = idNumber,
                        onValueChange = { viewModel.customerIdNumber.value = it },
                        label = { Text("Nomor KTP / Paspor (Opsional)", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF94A3B8)) },
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_id"),
                        shape = RoundedCornerShape(12.dp),
                        colors = outlinedFieldColors(),
                        singleLine = true
                    )
                }
            }

            // Payment Channel Selection Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Metode Pembayaran",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pilih kanal payment gateway yang ingin digunakan:",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Group payment channels
            items(PaymentChannel.values()) { channel ->
                val isSelected = selectedChannel == channel
                val icon = getChannelIcon(channel.category)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.selectPaymentChannel(channel) }
                        .testTag("channel_item_${channel.code}"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color(0xFF1E1B4B) else Color(0xFF131A2A),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) PrimaryIndigoLight else Color(0xFF222C3E)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF312E81) else Color(0xFF1E293B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = channel.title,
                                    color = if (isSelected) Color.White else Color(0xFFE2E8F0),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${channel.estimatedTime} • Biaya: ${channel.feeDescription}",
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Price Breakdown Card
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1626)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Rincian Pembayaran",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Harga Tiket ($qty x ${formatRupiah(tier.price)})", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text(text = formatRupiah(subtotal), color = Color(0xFFCBD5E1), fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Pajak Hiburan & PPN 11%", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text(text = formatRupiah(taxFee), color = Color(0xFFCBD5E1), fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Biaya Layanan Gateway (${selectedChannel.title})", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text(text = formatRupiah(adminFee), color = Color(0xFFCBD5E1), fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        androidx.compose.material3.HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Total Pembayaran", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = formatRupiah(totalAmount), color = Color(0xFF38BDF8), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Bottom Proceed Button
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color(0xFF0F1523),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222C3E))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Total Tagihan", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text(text = formatRupiah(totalAmount), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        errorMessage = null
                        viewModel.createOrder(
                            onSuccess = { invoice ->
                                onOrderCreated(invoice)
                            },
                            onError = { err ->
                                errorMessage = err
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    modifier = Modifier.testTag("submit_checkout_button")
                ) {
                    Text(text = "Buka Gateway", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF131A2A),
    unfocusedContainerColor = Color(0xFF131A2A),
    focusedBorderColor = PrimaryIndigoLight,
    unfocusedBorderColor = Color(0xFF26324D),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = Color(0xFFA5B4FC),
    unfocusedLabelColor = Color(0xFF64748B)
)

private fun getChannelIcon(category: PaymentCategory): ImageVector {
    return when (category) {
        PaymentCategory.QRIS -> Icons.Default.QrCode
        PaymentCategory.VIRTUAL_ACCOUNT -> Icons.Default.AccountBalance
        PaymentCategory.E_WALLET -> Icons.Default.CreditCard
        PaymentCategory.CREDIT_CARD -> Icons.Default.CreditCard
        PaymentCategory.RETAIL -> Icons.Default.Store
    }
}
