package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.QuickoGreen
import com.example.ui.theme.QuickoSurface
import com.example.viewmodel.QuickoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                QuickoApp()
            }
        }
    }
}

@Composable
fun QuickoApp(viewModel: QuickoViewModel = viewModel()) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedRestaurant by viewModel.selectedRestaurant.collectAsState()
    val cart by viewModel.cart.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_root"),
        bottomBar = {
            QuickoBottomNavigation(
                activeTab = selectedTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main views based on active navigation tab
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    QuickoTab.HOME -> HomeScreen(viewModel = viewModel)
                    QuickoTab.SEARCH -> SearchScreen(viewModel = viewModel)
                    QuickoTab.ORDERS -> OrdersScreen(viewModel = viewModel)
                    QuickoTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                }
            }

            // Quick Floating Basket Bar for Menu Selections
            if (cart.isNotEmpty() && selectedRestaurant != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    CheckoutFloatingBar(
                        cartItems = cart,
                        restaurant = selectedRestaurant!!,
                        onCheckoutClicked = {
                            viewModel.placeOrder(selectedRestaurant!!.name)
                        }
                    )
                }
            }

            // High Fidelity Animated Bottom Menu Sheet
            AnimatedVisibility(
                visible = selectedRestaurant != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.fillMaxSize()
            ) {
                selectedRestaurant?.let { restaurant ->
                    RestaurantMenuDrawer(
                        restaurant = restaurant,
                        cart = cart,
                        onClose = { viewModel.selectRestaurant(null) },
                        onAdd = { viewModel.addToCart(it) },
                        onRemove = { viewModel.removeFromCart(it) }
                    )
                }
            }
        }
    }
}

// ==========================================
// HOME SCREEN VIEW
// ==========================================
@Composable
fun HomeScreen(viewModel: QuickoViewModel) {
    val selectedAddress by viewModel.selectedAddress.collectAsState()
    val userCoins by viewModel.userCoins.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val restaurants by viewModel.filteredRestaurants.collectAsState()

    // Filter chip states
    val near by viewModel.nearestFilter.collectAsState()
    val offers by viewModel.offersFilter.collectAsState()
    val highlyRated by viewModel.highlyRatedFilter.collectAsState()
    val pureVeg by viewModel.pureVegFilter.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Top Premium Location Bar Widget
        item {
            LocationHeader(
                address = selectedAddress,
                coins = userCoins,
                onAddressClick = { viewModel.cycleAddress() }
            )
        }

        // 2. Search Section Trigger
        item {
            HomeSearchBar(
                query = query,
                onSearchTextChange = {
                    viewModel.setSearchQuery(it)
                    viewModel.selectTab(QuickoTab.SEARCH) // switch tab to show results
                },
                onSearchClicked = {
                    viewModel.selectTab(QuickoTab.SEARCH)
                }
            )
        }

        // 3. Huge Food Offer Banner Card (50% OFF)
        item {
            PromoOfferBanner(
                onPromoClick = {
                    viewModel.toggleOffersFilter()
                }
            )
        }

        // 4. Burger/Pizza/Drinks Categories Carousel & Icons
        item {
            CategoriesSection(
                categories = viewModel.categories,
                selectedCategory = selectedCategory,
                onCategoryClick = { viewModel.selectCategory(it) }
            )
        }

        // 5. Scrollable Sub-Filter Chips Row
        item {
            FilterChipsRow(
                near = near,
                offers = offers,
                highlyRated = highlyRated,
                pureVeg = pureVeg,
                hasActiveFilters = (near || offers || highlyRated || pureVeg || selectedCategory != null),
                onNearToggle = { viewModel.toggleNearestFilter() },
                onOffersToggle = { viewModel.toggleOffersFilter() },
                onHighlyRatedToggle = { viewModel.toggleHighlyRatedFilter() },
                onPureVegToggle = { viewModel.togglePureVegFilter() },
                onClearAll = { viewModel.resetAllFilters() }
            )
        }

        // Section Title: Premium Dining Hub
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Featured Premium Kitchens",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E293B)
                    )
                )
                Text(
                    text = "${restaurants.size} Stores",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = QuickoGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        // Empty Search Results State
        if (restaurants.isEmpty()) {
            item {
                EmptySearchState(
                    title = "No Matches Found",
                    subtitle = "We couldn't find matching kitchens or cuisines. Try resetting filters!",
                    onReset = { viewModel.resetAllFilters() }
                )
            }
        }

        // 6. Premium Restaurant Cards Scroll List
        items(restaurants, key = { it.id }) { restaurant ->
            RestaurantCard(
                restaurant = restaurant,
                onRestaurantClick = { viewModel.selectRestaurant(restaurant) }
            )
        }
    }
}

// ==========================================
// SEARCH SCREEN VIEW
// ==========================================
@Composable
fun SearchScreen(viewModel: QuickoViewModel) {
    val query by viewModel.searchQuery.collectAsState()
    val restaurants by viewModel.filteredRestaurants.collectAsState()
    val focusManager = LocalFocusManager.current

    val popularTags = listOf(
        Pair("Biryani", "🍛"),
        Pair("Pizza", "🍕"),
        Pair("Burger", "🍔"),
        Pair("Drinks", "🍹"),
        Pair("Healthy", "🥗"),
        Pair("Noodles", "🍜"),
        Pair("Desserts", "🍰")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("search_screen")
            .padding(top = 16.dp)
    ) {
        // Title heading
        Text(
            text = "Fine Dining Hub Search",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Real-time Text Input
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search delicious foods, biryanis, pizzas...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = QuickoGreen) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear text", tint = Color.Gray)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = QuickoGreen,
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("search_textbox")
        )

        // Popular Searches chips
        Text(
            text = "Popular Searches",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF475569)),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(popularTags) { tag ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (query.equals(tag.first, ignoreCase = true)) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, if (query.equals(tag.first, ignoreCase = true)) QuickoGreen else Color.Transparent),
                    modifier = Modifier.clickable {
                        viewModel.setSearchQuery(tag.first)
                        focusManager.clearFocus()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = tag.second, modifier = Modifier.padding(end = 4.dp))
                        Text(
                            text = tag.first,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (query.equals(tag.first, ignoreCase = true)) QuickoGreen else Color(0xFF334155)
                            )
                        )
                    }
                }
            }
        }

        Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)

        // Matching Dishes / Restaurant results list
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (restaurants.isEmpty()) {
                item {
                    EmptySearchState(
                        title = "No search results",
                        subtitle = "We couldn't match '${query}' with any local kitchens.",
                        onReset = { viewModel.setSearchQuery("") }
                    )
                }
            } else {
                item {
                    Text(
                        text = "Matching Dining Stores",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(restaurants) { restaurant ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { viewModel.selectRestaurant(restaurant) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFECFDF5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = restaurant.iconEmoji, fontSize = 28.sp)
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = restaurant.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = restaurant.cuisines.joinToString(", "),
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                                    Text(
                                        text = "${restaurant.rating} • ${restaurant.deliveryTime}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF475569),
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.selectRestaurant(restaurant) },
                                modifier = Modifier.background(Color(0xFFECFDF5), CircleShape).size(36.dp)
                            ) {
                                Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = "View Menu", tint = QuickoGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// ORDERS SCREEN VIEW (LIVE TRACKER)
// ==========================================
@Composable
fun OrdersScreen(viewModel: QuickoViewModel) {
    val orders by viewModel.orders.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("orders_screen")
            .padding(16.dp)
    ) {
        Text(
            text = "Your Orders Hub",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(orders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Header: Shop details
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = order.restaurantName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                )
                                Text(
                                    text = "Order ID: ${order.id} • ${order.date}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            // Dynamic Status Badge
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = when (order.deliveryStep) {
                                    4 -> Color(0xFFDCFCE7)
                                    1 -> Color(0xFFEFF6FF)
                                    else -> Color(0xFFFEF3C7)
                                }
                            ) {
                                Text(
                                    text = order.status,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = when (order.deliveryStep) {
                                            4 -> Color(0xFF15803D)
                                            1 -> Color(0xFF1D4ED8)
                                            else -> Color(0xFFB45309)
                                        }
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))

                        // Meals and pricing Info
                        order.items.forEach { cartItem ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${cartItem.menuItem.emoji} ${cartItem.menuItem.name} x ${cartItem.quantity}",
                                    fontSize = 14.sp,
                                    color = Color(0xFF334155)
                                )
                                Text(
                                    text = "₹${cartItem.menuItem.price * cartItem.quantity}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Convenience Fee Included", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = "Paid ₹${order.totalPrice}",
                                fontWeight = FontWeight.Bold,
                                color = QuickoGreen,
                                fontSize = 15.sp
                            )
                        }

                        // Real-time Delivery Stepper Tracking System (If active)
                        if (order.deliveryStep in 1..3) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Live Order Progress Tracker",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Linear representation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(4) { stepIndex ->
                                    val stepNum = stepIndex + 1
                                    val isPassedOrCurrent = order.deliveryStep >= stepNum
                                    val isCurrent = order.deliveryStep == stepNum

                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isCurrent) QuickoGreen
                                                else if (isPassedOrCurrent) Color(0xFFD1FAE5)
                                                else Color(0xFFF1F5F9)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isPassedOrCurrent && !isCurrent) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF059669),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "$stepNum",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (isCurrent) Color.White else Color(0xFF64748B)
                                            )
                                        }
                                    }

                                    // Interstitial Line
                                    if (stepIndex < 3) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(4.dp)
                                                .background(
                                                    if (order.deliveryStep > stepNum) QuickoGreen
                                                    else Color(0xFFE2E8F0)
                                                )
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Placed", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (order.deliveryStep >= 1) QuickoGreen else Color.Gray)
                                Text("Cooking", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (order.deliveryStep >= 2) QuickoGreen else Color.Gray)
                                Text("On Way", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (order.deliveryStep >= 3) QuickoGreen else Color.Gray)
                                Text("Arrived", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (order.deliveryStep >= 4) QuickoGreen else Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// PROFILE SCREEN VIEW
// ==========================================
@Composable
fun ProfileScreen(viewModel: QuickoViewModel) {
    val coins by viewModel.userCoins.collectAsState()
    val address by viewModel.selectedAddress.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_screen")
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // User primary card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Avatar with glowing ring
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(QuickoGreen.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👤", fontSize = 42.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Welcome to QUICKO!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                )
                Text(
                    text = "noormuhammad28446@gmail.com",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Loyalty Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFECFDF5),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🪙", fontSize = 28.sp, modifier = Modifier.padding(end = 8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Quicko Club Loyalty Wallet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF064E3B)
                            )
                            Text(
                                text = "$coins QC coins active (Silver Tier)",
                                fontSize = 12.sp,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                }
            }
        }

        // Profile Menu Links
        Text(
            text = "Your Preferences",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        ProfileMenuRow(icon = Icons.Outlined.Place, title = "Current Delivery Spot", subtitle = address, clickable = true, onClick = { viewModel.cycleAddress() })
        ProfileMenuRow(icon = Icons.Outlined.Receipt, title = "Coin Redemption Center", subtitle = "Redeem $coins coins for gourmet discounts", clickable = false)
        ProfileMenuRow(icon = Icons.Outlined.Star, title = "Quicko Elite Plus membership", subtitle = "Active with Unlimited Free Delivery privileges", clickable = false)
        ProfileMenuRow(icon = Icons.Outlined.Info, title = "App Version info", subtitle = "v1.4.1 (Figma design export ready)", clickable = false)
    }
}

@Composable
fun ProfileMenuRow(icon: ImageVector, title: String, subtitle: String, clickable: Boolean, onClick: () -> Unit = {}) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = clickable) { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = QuickoGreen, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (clickable) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
            }
        }
    }
}

// Decorative spacing wrapper

// ==========================================
// CRITICAL REUSABLE SUB-COMPONENTS
// ==========================================

@Composable
fun LocationHeader(address: String, coins: Int, onAddressClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Address section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clickable { onAddressClick() }
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(QuickoGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "location marker",
                    tint = QuickoGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Work",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A), // Slate 900
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        tint = QuickoGreen,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = address,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B), // Slate 500
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Coins balance badge + Notification on right
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFEF3C7),
                border = BorderStroke(1.dp, Color(0xFFFCD34D)),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = "🪙 $coins QC",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = Color(0xFFB45309),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF8FAFC), CircleShape) // bg-slate-50
                    .border(1.dp, Color(0xFFF1F5F9), CircleShape)
            ) {
                Box {
                    Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = Color(0xFF475569))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeSearchBar(query: String, onSearchTextChange: (String) -> Unit, onSearchClicked: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF1F5F9), // bg-slate-100 helper container
        border = null,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onSearchClicked() }
            .testTag("home_search_bar")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF94A3B8), // slate-400
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (query.isEmpty()) "Search 'Burger King'..." else query,
                color = Color(0xFF94A3B8), // slate-400
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            // Settings filter asset slider icon
            Icon(
                Icons.Default.Settings,
                contentDescription = "Filters Slider",
                tint = QuickoGreen,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PromoOfferBanner(onPromoClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onPromoClick() }
            .testTag("promo_banner"),
        shape = RoundedCornerShape(24.dp), // rounded-3xl corner radius
        colors = CardDefaults.cardColors(containerColor = QuickoGreen)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Absolute blur bubble representation (right visual background)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 24.dp, y = 24.dp)
                    .size(128.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "50% OFF",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color.White,
                        lineHeight = 32.sp
                    )
                    Text(
                        text = "Up to ₹100 on first 3 orders",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // White coupon promo tag
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "USE CODE: QUICKO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = QuickoGreen,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // Decorative food floating circle on right side
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(Color(0xFFFB923C), CircleShape) // bg-orange-400
                        .border(4.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🍔",
                        fontSize = 38.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CategoriesSection(
    categories: List<Category>,
    selectedCategory: String?,
    onCategoryClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Text(
            text = "Curated Categories",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1E293B)
            ),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category.id
                Column(
                    modifier = Modifier
                        .clickable { onCategoryClick(category.id) }
                        .testTag("category_item_${category.id}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp), // rounded-2xl
                        color = if (isSelected) Color(0xFFE5F7ED) else Color(0xFFF8FAFC), // bg-[#00B14F]/10 or slate-50
                        border = BorderStroke(
                            width = 1.2.dp,
                            color = if (isSelected) QuickoGreen else Color(0xFFF1F5F9) // border-slate-100
                        ),
                        shadowElevation = 1.dp,
                        modifier = Modifier.size(58.dp) // w-14 h-14 equivalent (approx 56-58dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = category.emoji,
                                fontSize = 26.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = category.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) QuickoGreen else Color(0xFF475569) // text-slate-600
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipsRow(
    near: Boolean,
    offers: Boolean,
    highlyRated: Boolean,
    pureVeg: Boolean,
    hasActiveFilters: Boolean,
    onNearToggle: () -> Unit,
    onOffersToggle: () -> Unit,
    onHighlyRatedToggle: () -> Unit,
    onPureVegToggle: () -> Unit,
    onClearAll: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasActiveFilters) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFEE2E2),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.clickable { onClearAll() }
                ) {
                    Text(
                        text = "Reset All X",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        item {
            FilterChipWidget(title = "Nearest ⏱️", selected = near, onToggle = onNearToggle)
        }
        item {
            FilterChipWidget(title = "Great Offers 🏷️", selected = offers, onToggle = onOffersToggle)
        }
        item {
            FilterChipWidget(title = "Top Rated 4.5+ ⭐", selected = highlyRated, onToggle = onHighlyRatedToggle)
        }
        item {
            FilterChipWidget(title = "Pure Veg 🟢", selected = pureVeg, onToggle = onPureVegToggle)
        }
    }
}

@Composable
fun FilterChipWidget(title: String, selected: Boolean, onToggle: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color(0xFFD1FAE5) else Color.White,
        border = BorderStroke(width = 1.dp, color = if (selected) QuickoGreen else Color(0xFFE2E8F0)),
        modifier = Modifier
            .clickable { onToggle() }
            .testTag("filter_$title")
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) QuickoGreen else Color(0xFF334155),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun RestaurantCard(restaurant: Restaurant, onRestaurantClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp), // rounded-24px from Sleek Design
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)), // border-slate-100
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onRestaurantClick() }
            .testTag("restaurant_card_${restaurant.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Visual decorative upper section with food banner emoji
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp) // h-32 is 128dp
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))
                        )
                    )
            ) {
                // Large emojis row representing inventory
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = restaurant.iconEmoji, fontSize = 52.sp)
                    if (restaurant.menuItems.isNotEmpty()) {
                        Text(text = restaurant.menuItems.last().emoji, fontSize = 50.sp)
                    }
                }

                // Top-right floating ratings container from HTML model: rounded-lg text-[10px] font-bold flex bg-white/90
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "ratings",
                            tint = Color(0xFFFBBF24), // yellow-400
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${restaurant.rating} (500+)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }

                // Bottom-left free delivery wrapper from Sleek HTML model
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = QuickoGreen,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Free Delivery",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Text info details section
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = restaurant.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A), // slate-900
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = restaurant.cuisines.joinToString(" • "),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF64748B), // slate-500
                                fontSize = 12.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Estimated duration panel matching right side of HTML card exactly
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = restaurant.deliveryTime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155) // slate-700
                        )
                        Text(
                            text = "ESTIMATED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8), // slate-400
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fine separator line
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom billing/distance summaries
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = "Dist", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        Text(
                            text = " ${restaurant.distance} • Cost for two: ",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = restaurant.costForTwo,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                    }
                }

                // Offer footer detailed banner description if available
                if (restaurant.discountDetail.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE5F7ED), // 10% opacity equivalent light green
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🎁", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = restaurant.discountDetail,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = QuickoGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptySearchState(title: String, subtitle: String, onReset: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🔍", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)),
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )
        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(containerColor = QuickoGreen)
        ) {
            Text("Reset Search Filters")
        }
    }
}

// ==========================================
// DETAILED MENU DRAWER SLIDE-UP SHEET
// ==========================================
@Composable
fun RestaurantMenuDrawer(
    restaurant: Restaurant,
    cart: List<CartItem>,
    onClose: () -> Unit,
    onAdd: (MenuItem) -> Unit,
    onRemove: (MenuItem) -> Unit
) {
    // Semi-transparent dim background layer
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.4f))
            .clickable { onClose() }
            .testTag("menu_drawer_backdrop")
    ) {
        // Elevated Bottom sheet card container itself
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxHeight(0.85f)
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { /* intercept taps on surface card */ },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top drag-bar indicator
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp, 4.dp)
                        .background(Color.LightGray, RoundedCornerShape(2.dp))
                        .align(Alignment.CenterHorizontally)
                )

                // Drawer Header Details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = restaurant.name,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
                        )
                        Text(
                            text = restaurant.cuisines.joinToString(" • "),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                            Text(
                                text = " ${restaurant.rating} (${restaurant.reviewCount} Ratings)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155),
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.background(Color(0xFFF1F5F9), CircleShape)
                    ) {
                        Icon(Icons.Rounded.Clear, contentDescription = "Close Menu", tint = Color.Gray)
                    }
                }

                Divider(color = Color(0xFFF1F5F9))

                // Scrollable Dish Items list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Explore Gourmet Menu",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B)),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(restaurant.menuItems) { dish ->
                        val cartQty = cart.find { it.menuItem.id == dish.id }?.quantity ?: 0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .testTag("menu_dish_${dish.id}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                // Veg label icon box indicator
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .border(width = 1.dp, color = if (dish.isVeg) Color(0xFF10B981) else Color(0xFFEF4444))
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (dish.isVeg) Color(0xFF10B981) else Color(0xFFEF4444))
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = dish.name,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "₹${dish.price}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = QuickoGreen)
                                )
                                Text(
                                    text = dish.description,
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            // Interactive Counter Box layout control
                            Box(
                                modifier = Modifier
                                    .width(96.dp)
                                    .height(38.dp)
                                    .border(1.dp, QuickoGreen, RoundedCornerShape(8.dp))
                                    .background(Color(0xFFECFDF5), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (cartQty > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { onRemove(dish) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("-", fontWeight = FontWeight.Black, fontSize = 16.sp, color = QuickoGreen)
                                        }

                                        Text(
                                            text = cartQty.toString(),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp,
                                            color = QuickoGreen
                                        )

                                        IconButton(
                                            onClick = { onAdd(dish) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("+", fontWeight = FontWeight.Black, fontSize = 16.sp, color = QuickoGreen)
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "ADD +",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = QuickoGreen,
                                        modifier = Modifier
                                            .clickable { onAdd(dish) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                        Divider(color = Color(0xFFF1F5F9))
                    }

                    // Spacer for checkout floating bottom panel
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutFloatingBar(
    cartItems: List<CartItem>,
    restaurant: Restaurant,
    onCheckoutClicked: () -> Unit
) {
    val totalQty = cartItems.sumOf { it.quantity }
    val totalPrice = cartItems.sumOf { it.menuItem.price * it.quantity }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = QuickoGreen,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("checkout_floating_container")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$totalQty item(s) • From ${restaurant.name}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(0.9f)
                )
                Text(
                    text = "Total: ₹$totalPrice",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier.clickable { onCheckoutClicked() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ORDER NOW ➔",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = QuickoGreen
                    )
                }
            }
        }
    }
}

// ==========================================
// BOTTOM NAVIGATION PILL BAR
// ==========================================
@Composable
fun QuickoBottomNavigation(
    activeTab: QuickoTab,
    onTabSelected: (QuickoTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.testTag("bottom_navigation_bar")
    ) {
        NavigationBarItem(
            selected = activeTab == QuickoTab.HOME,
            onClick = { onTabSelected(QuickoTab.HOME) },
            icon = { Icon(if (activeTab == QuickoTab.HOME) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = QuickoGreen,
                selectedTextColor = QuickoGreen,
                indicatorColor = Color(0xFFECFDF5),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            ),
            modifier = Modifier.testTag("nav_home")
        )

        NavigationBarItem(
            selected = activeTab == QuickoTab.SEARCH,
            onClick = { onTabSelected(QuickoTab.SEARCH) },
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("Search", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = QuickoGreen,
                selectedTextColor = QuickoGreen,
                indicatorColor = Color(0xFFECFDF5),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            ),
            modifier = Modifier.testTag("nav_search")
        )

        NavigationBarItem(
            selected = activeTab == QuickoTab.ORDERS,
            onClick = { onTabSelected(QuickoTab.ORDERS) },
            icon = { Icon(if (activeTab == QuickoTab.ORDERS) Icons.Filled.Receipt else Icons.Outlined.Receipt, contentDescription = "Orders") },
            label = { Text("Orders", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = QuickoGreen,
                selectedTextColor = QuickoGreen,
                indicatorColor = Color(0xFFECFDF5),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            ),
            modifier = Modifier.testTag("nav_orders")
        )

        NavigationBarItem(
            selected = activeTab == QuickoTab.PROFILE,
            onClick = { onTabSelected(QuickoTab.PROFILE) },
            icon = { Icon(if (activeTab == QuickoTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = QuickoGreen,
                selectedTextColor = QuickoGreen,
                indicatorColor = Color(0xFFECFDF5),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            ),
            modifier = Modifier.testTag("nav_profile")
        )
    }
}
