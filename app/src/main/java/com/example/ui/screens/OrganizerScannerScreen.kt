package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.CheckInResult
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryIndigoLight
import com.example.ui.theme.RoseError
import com.example.ui.viewmodel.EventViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrganizerScannerScreen(
    viewModel: EventViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val tickets by viewModel.allTickets.collectAsState()
    val checkInResult by viewModel.checkInResult.collectAsState()
    var manualInputCode by remember { mutableStateOf("") }

    val totalCheckedIn = remember(tickets) { tickets.count { it.isCheckedIn } }
    val totalRemaining = remember(tickets) { tickets.count { !it.isCheckedIn } }

    // Laser sweep animation for the scanner
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("organizer_scanner_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Top Bar
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
                    IconButton(onClick = onBack, modifier = Modifier.testTag("scanner_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Gate Scanner Petugas",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Validasi QR & Barcode Tiket Masuk",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Stats Counter
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF131A2A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243047))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Total Check-In", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(text = "$totalCheckedIn", color = EmeraldSuccess, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFF334155)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Belum Masuk", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(text = "$totalRemaining", color = Color(0xFF38BDF8), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFF334155)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Total Kuota", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(text = "${tickets.size}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Viewfinder Scanner Box
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF090D15))
                        .border(2.dp, PrimaryIndigoLight, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Animated laser line
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val currentY = size.height * laserY
                        drawLine(
                            color = Color(0xFF06B6D4),
                            start = Offset(0f, currentY),
                            end = Offset(size.width, currentY),
                            strokeWidth = 3.dp.toPx()
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Arahkan Barcode / QR ke Kotak Ini",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Verification Outcome Banner
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                when (val res = checkInResult) {
                    is CheckInResult.Success -> {
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("scan_result_success"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldSuccess)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "TIKET VALID - CHECK-IN BERHASIL", color = Color(0xFF6EE7B7), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Pengunjung: ${res.ticket.attendeeName}", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Event: ${res.ticket.eventTitle}", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                                Text(text = "Akses: ${res.ticket.tierName} • ${res.ticket.gateNumber}", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "Kode: ${res.ticket.ticketCode}", color = Color(0xFF94A3B8), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                    is CheckInResult.AlreadyUsed -> {
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("scan_result_warning"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF451A03)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "PERINGATAN: TIKET SUDAH DIGUNAKAN", color = Color(0xFFFCD34D), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Pengunjung: ${res.ticket.attendeeName}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Telah check-in pada: ${res.usedAtFormatted}", color = Color(0xFFFDE68A), fontSize = 12.sp)
                                Text(text = "Akses: ${res.ticket.gateNumber}", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                            }
                        }
                    }
                    is CheckInResult.NotFound -> {
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("scan_result_error"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF4C0519)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, RoseError)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = RoseError, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = "TIKET TIDAK DITEMUKAN", color = Color(0xFFFDA4AF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Kode tiket tidak terdaftar dalam database event.", color = Color(0xFFFECDD3), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    null -> {
                        // Empty / Ready placeholder
                    }
                }
            }
        }

        // Manual Input or Quick Test Chips
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Tes Validasi / Masukkan Kode Manual",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = manualInputCode,
                        onValueChange = { manualInputCode = it },
                        placeholder = { Text("Contoh: EP-SW26-VIP-8819", color = Color(0xFF64748B), fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_ticket_code"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF131A2A),
                            unfocusedContainerColor = Color(0xFF131A2A),
                            focusedBorderColor = PrimaryIndigoLight,
                            unfocusedBorderColor = Color(0xFF26324D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (manualInputCode.isNotBlank()) {
                                viewModel.checkInTicket(manualInputCode)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        modifier = Modifier.testTag("submit_checkin_button")
                    ) {
                        Text(text = "Validasi", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Pilih Cepat Tiket dari Database:",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tickets.take(6).forEach { tkt ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (tkt.isCheckedIn) Color(0xFF334155) else Color(0xFF1E293B))
                                .border(1.dp, if (tkt.isCheckedIn) Color(0xFF475569) else Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                                .clickable {
                                    manualInputCode = tkt.ticketCode
                                    viewModel.checkInTicket(tkt.ticketCode)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("quick_code_${tkt.ticketCode}")
                        ) {
                            Text(
                                text = "${tkt.ticketCode} (${if (tkt.isCheckedIn) "Used" else "Active"})",
                                color = if (tkt.isCheckedIn) Color(0xFF94A3B8) else Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
