package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.EventDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MyTicketsScreen
import com.example.ui.screens.OrganizerDashboardScreen
import com.example.ui.screens.OrganizerScannerScreen
import com.example.ui.screens.PaymentGatewayScreen
import com.example.ui.screens.PaymentSuccessScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryIndigoLight
import com.example.ui.viewmodel.EventViewModel

enum class Screen {
    HOME,
    EVENT_DETAIL,
    CHECKOUT,
    PAYMENT_GATEWAY,
    PAYMENT_SUCCESS,
    MY_TICKETS,
    ORGANIZER_SCANNER,
    ORGANIZER_DASHBOARD
}

enum class NavTab(val title: String, val icon: ImageVector, val screen: Screen) {
    EXPLORE("Event", Icons.Default.Event, Screen.HOME),
    MY_TICKETS("Tiket Saya", Icons.Default.ConfirmationNumber, Screen.MY_TICKETS),
    SCANNER("Gate Scan", Icons.Default.QrCodeScanner, Screen.ORGANIZER_SCANNER),
    ANALYTICS("Organizer", Icons.Default.BarChart, Screen.ORGANIZER_DASHBOARD)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EventPaymentApp()
            }
        }
    }
}

@Composable
fun EventPaymentApp() {
    val viewModel: EventViewModel = viewModel()

    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var activeInvoice by remember { mutableStateOf<String?>(null) }
    var currentTab by remember { mutableStateOf(NavTab.EXPLORE) }

    // Navigation stack for back button
    var screenStack by remember { mutableStateOf(listOf(Screen.HOME)) }

    fun navigateTo(screen: Screen) {
        currentScreen = screen
        screenStack = screenStack + screen
        // Keep bottom nav in sync
        when (screen) {
            Screen.HOME -> currentTab = NavTab.EXPLORE
            Screen.MY_TICKETS -> currentTab = NavTab.MY_TICKETS
            Screen.ORGANIZER_SCANNER -> currentTab = NavTab.SCANNER
            Screen.ORGANIZER_DASHBOARD -> currentTab = NavTab.ANALYTICS
            else -> {}
        }
    }

    fun popBack() {
        if (screenStack.size > 1) {
            val newStack = screenStack.dropLast(1)
            screenStack = newStack
            val previous = newStack.last()
            currentScreen = previous
            when (previous) {
                Screen.HOME -> currentTab = NavTab.EXPLORE
                Screen.MY_TICKETS -> currentTab = NavTab.MY_TICKETS
                Screen.ORGANIZER_SCANNER -> currentTab = NavTab.SCANNER
                Screen.ORGANIZER_DASHBOARD -> currentTab = NavTab.ANALYTICS
                else -> {}
            }
        } else {
            currentScreen = Screen.HOME
            currentTab = NavTab.EXPLORE
        }
    }

    val showBottomBar = currentScreen in listOf(
        Screen.HOME,
        Screen.MY_TICKETS,
        Screen.ORGANIZER_SCANNER,
        Screen.ORGANIZER_DASHBOARD
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .border(1.dp, Color(0xFF1E2638)),
                    containerColor = Color(0xFF0D121D),
                    tonalElevation = 8.dp
                ) {
                    NavTab.values().forEach { tab ->
                        val selected = currentTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                currentTab = tab
                                navigateTo(tab.screen)
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (selected) Color(0xFF38BDF8) else Color(0xFF64748B)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    color = if (selected) Color.White else Color(0xFF64748B),
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onEventClick = { event ->
                            viewModel.selectEvent(event)
                            navigateTo(Screen.EVENT_DETAIL)
                        },
                        onScannerClick = {
                            navigateTo(Screen.ORGANIZER_SCANNER)
                        }
                    )
                }

                Screen.EVENT_DETAIL -> {
                    EventDetailScreen(
                        viewModel = viewModel,
                        onBack = { popBack() },
                        onProceedToCheckout = {
                            navigateTo(Screen.CHECKOUT)
                        }
                    )
                }

                Screen.CHECKOUT -> {
                    CheckoutScreen(
                        viewModel = viewModel,
                        onBack = { popBack() },
                        onOrderCreated = { invoice ->
                            activeInvoice = invoice
                            navigateTo(Screen.PAYMENT_GATEWAY)
                        }
                    )
                }

                Screen.PAYMENT_GATEWAY -> {
                    val invoice = activeInvoice ?: ""
                    PaymentGatewayScreen(
                        invoiceNumber = invoice,
                        viewModel = viewModel,
                        onBack = { popBack() },
                        onPaymentSuccess = {
                            navigateTo(Screen.PAYMENT_SUCCESS)
                        }
                    )
                }

                Screen.PAYMENT_SUCCESS -> {
                    PaymentSuccessScreen(
                        viewModel = viewModel,
                        onViewTickets = {
                            screenStack = listOf(Screen.HOME)
                            navigateTo(Screen.MY_TICKETS)
                        },
                        onGoHome = {
                            screenStack = listOf(Screen.HOME)
                            currentScreen = Screen.HOME
                            currentTab = NavTab.EXPLORE
                        }
                    )
                }

                Screen.MY_TICKETS -> {
                    BackHandler {
                        currentScreen = Screen.HOME
                        currentTab = NavTab.EXPLORE
                    }
                    MyTicketsScreen(
                        viewModel = viewModel,
                        onExploreEvents = {
                            currentScreen = Screen.HOME
                            currentTab = NavTab.EXPLORE
                        },
                        onOpenScanner = {
                            navigateTo(Screen.ORGANIZER_SCANNER)
                        }
                    )
                }

                Screen.ORGANIZER_SCANNER -> {
                    BackHandler {
                        currentScreen = Screen.HOME
                        currentTab = NavTab.EXPLORE
                    }
                    OrganizerScannerScreen(
                        viewModel = viewModel,
                        onBack = {
                            currentScreen = Screen.HOME
                            currentTab = NavTab.EXPLORE
                        }
                    )
                }

                Screen.ORGANIZER_DASHBOARD -> {
                    BackHandler {
                        currentScreen = Screen.HOME
                        currentTab = NavTab.EXPLORE
                    }
                    OrganizerDashboardScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
