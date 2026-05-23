package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuickoViewModel : ViewModel() {

    // Lists of categories and restaurants
    val categories = listOf(
        Category("biryani", "Biryani", "🍛", 0xFFFFF1F2),
        Category("pizza", "Pizza", "🍕", 0xFFFEF3C7),
        Category("burger", "Burger", "🍔", 0xFFECFDF5),
        Category("drinks", "Drinks", "🍹", 0xFFEFF6FF),
        Category("healthy", "Healthy", "🥗", 0xFFF0FDF4),
        Category("chinese", "Chinese", "🍜", 0xFFFFFBEB),
        Category("desserts", "Desserts", "🍰", 0xFFFDF2F8)
    )

    private val allRestaurants = listOf(
        Restaurant(
            id = "1",
            name = "The Imperial Biryani",
            rating = 4.8,
            reviewCount = "1.5k+",
            deliveryTime = "20-25 mins",
            distance = "1.2 km",
            costForTwo = "₹300 for two",
            cuisines = listOf("Hyderabadi", "Biryani", "Mughlai"),
            discountTag = "50% OFF",
            discountDetail = "Up to ₹100 | Code QUICKO50",
            isVeg = false,
            isFeatured = true,
            iconEmoji = "🍛",
            menuItems = listOf(
                MenuItem("b1", "Chicken Dum Biryani", "Fragrant basmati rice layered with spiced marinated chicken and herbs.", 280.0, 4.9, false, "🍛"),
                MenuItem("b2", "Paneer Tikka Biryani", "Slow-cooked saffron rice with chargrilled spiced cottage cheese cubes.", 240.0, 4.7, true, "🍛"),
                MenuItem("b3", "Veg Dum Hyderabadi Biryani", "Aromatic rice layered with fresh seasonal farm vegetables.", 210.0, 4.6, true, "🍛"),
                MenuItem("b4", "Garlic Butter Raita", "Thick yogurt whisked with roasted garlic, cumin, and fresh mint.", 49.0, 4.8, true, "🥣")
            )
        ),
        Restaurant(
            id = "2",
            name = "Pizzeria Romana",
            rating = 4.5,
            reviewCount = "800+",
            deliveryTime = "25-30 mins",
            distance = "2.5 km",
            costForTwo = "₹450 for two",
            cuisines = listOf("Woodfired Pizza", "Italian", "Pastas"),
            discountTag = "40% OFF",
            discountDetail = "Up to ₹120 | Code PIZZA40",
            isVeg = false,
            isFeatured = true,
            iconEmoji = "🍕",
            menuItems = listOf(
                MenuItem("p1", "Margherita Supreme", "San Marzano tomatoes, fresh buffalo mozzarella, and aromatic sweet basil.", 299.0, 4.8, true, "🍕"),
                MenuItem("p2", "Spicy Pepperoni & Jalapeño", "Cured Italian pepperoni, fiery jalapeños, and extra mozzarella.", 379.0, 4.7, false, "🍕"),
                MenuItem("p3", "Veg Garden Farmhouse Deluxe", "Crisp bell peppers, red onions, button mushrooms, and sweet corn.", 349.0, 4.5, true, "🍕"),
                MenuItem("p4", "Baked Premium Garlic Bread", "Freshly baked dough brushed with rich garlic herb butter and cheese.", 129.0, 4.6, true, "🥖"),
                MenuItem("p5", "Classic Espresso Tiramisu", "Layers of coffee-soaked ladyfingers and smooth rich mascarpone.", 189.0, 4.9, true, "🍰")
            )
        ),
        Restaurant(
            id = "3",
            name = "Burger Bistro",
            rating = 4.3,
            reviewCount = "2.1k+",
            deliveryTime = "15-20 mins",
            distance = "0.9 km",
            costForTwo = "₹200 for two",
            cuisines = listOf("Gourmet Burgers", "American", "Sides"),
            discountTag = "50% OFF",
            discountDetail = "Up to ₹80 | Code BISTRO50",
            isVeg = false,
            isFeatured = false,
            iconEmoji = "🍔",
            menuItems = listOf(
                MenuItem("bu1", "Classic Crisp Veg Burger", "Crispy spiced potato & peas patty with lettuce, tomato, and secret herb mayo.", 99.0, 4.4, true, "🍔"),
                MenuItem("bu2", "Smoked BBQ Grilled Chicken Burger", "Flame-grilled chicken breast glazed with hickory BBQ sauce and melt cheese.", 159.0, 4.6, false, "🍔"),
                MenuItem("bu3", "Overloaded Cheese Fries", "Golden crispy skin-on fries drenched in warm cheddar cheese sauce.", 119.0, 4.5, true, "🍟"),
                MenuItem("bu4", "Double Oreo Blast Shake", "Creamy vanilla soft serve blended with original Oreo cookies.", 109.0, 4.7, true, "🥤")
            )
        ),
        Restaurant(
            id = "4",
            name = "The Green Bowl Cafe",
            rating = 4.7,
            reviewCount = "350+",
            deliveryTime = "30-35 mins",
            distance = "3.2 km",
            costForTwo = "₹350 for two",
            cuisines = listOf("Healthy Salads", "Keto Bowls", "Vegan"),
            discountTag = "FREE DELIVERY",
            discountDetail = "On orders above ₹199 | Code FREEDEL",
            isVeg = true,
            isFeatured = true,
            iconEmoji = "🥗",
            menuItems = listOf(
                MenuItem("h1", "Avocado Quinoa Green Salad", "Creamy Hass avocado cuts, dynamic red quinoa, organic spinach, and lemon zest.", 249.0, 4.8, true, "🥗"),
                MenuItem("h2", "Vegan Protein Super Bowl", "Tofu nuggets, steamed broccoli florets, brown rice, and peanut-sesame dressing.", 229.0, 4.6, true, "🥗"),
                MenuItem("h3", "Antioxidant Berry Smoothie", "Blend of farm wild blueberries, strawberries, bananas, and coconut milk.", 149.0, 4.7, true, "🍹"),
                MenuItem("h4", "Organic Ceremonal Matcha Latte", "Ground stone ceremonial Japanese green tea with steamed almond milk.", 159.0, 4.5, true, "🍵")
            )
        ),
        Restaurant(
            id = "5",
            name = "Dimsum Express",
            rating = 4.4,
            reviewCount = "450+",
            deliveryTime = "25-30 mins",
            distance = "2.1 km",
            costForTwo = "₹250 for two",
            cuisines = listOf("Pan-Asian", "Momos", "Noodles"),
            discountTag = "30% OFF",
            discountDetail = "Up to ₹75 | Code MOMO30",
            isVeg = false,
            isFeatured = false,
            iconEmoji = "🍜",
            menuItems = listOf(
                MenuItem("d1", "Siri Veg Fried Dimsums", "Crisp-fried momos filled with shredded garlic vegetables.", 119.0, 4.5, true, "🥟"),
                MenuItem("d2", "Spicy Schezwan Garlic Noodles", "Stir-fried noodles with crisp veggies and sizzling fiery Schezwan oil.", 169.0, 4.6, true, "🍜"),
                MenuItem("d3", "Crystal Steamed Chicken Dumplings", "Translucent fresh wrappers stuffed with juicy minced chicken.", 139.0, 4.4, false, "🥟"),
                MenuItem("d4", "Fresh Brewed Peach Iced Tea", "Classic chilled Darjeeling tea infused with dynamic sweet summer peach syrup.", 89.0, 4.5, true, "🍹")
            )
        ),
        Restaurant(
            id = "6",
            name = "Cocoa Crave Desserts",
            rating = 4.6,
            reviewCount = "1.2k+",
            deliveryTime = "18-22 mins",
            distance = "1.1 km",
            costForTwo = "₹180 for two",
            cuisines = listOf("Pastries", "Ice Cream", "Waffles"),
            discountTag = "20% OFF",
            discountDetail = "No minimum order | Code SWEET20",
            isVeg = true,
            isFeatured = false,
            iconEmoji = "🍰",
            menuItems = listOf(
                MenuItem("de1", "Belgian Double Chocolate Waffle", "Fresh warm golden waffle loaded with molten dark Belgian fudge.", 149.0, 4.8, true, "🧇"),
                MenuItem("de2", "Red Velvet Cream Pastry", "Soft scarlet sponge layers whipped with vanilla bean cream cheese.", 99.0, 4.7, true, "🍰"),
                MenuItem("de3", "Alphonso Mango Double Scoop", "Creamy chilled summer mango ice cream studded with real fruit chunks.", 119.0, 4.9, true, "🍨"),
                MenuItem("de4", "Warm Dark Choco Lava Cake", "Hot individual mud cake with an oozing warm chocolate liquid core.", 89.0, 4.8, true, "🧁")
            )
        )
    )

    // Current navigation tab
    private val _selectedTab = MutableStateFlow(QuickoTab.HOME)
    val selectedTab: StateFlow<QuickoTab> = _selectedTab.asStateFlow()

    // Query for text searches
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Quicko Coins loyalty points
    private val _userCoins = MutableStateFlow(450)
    val userCoins: StateFlow<Int> = _userCoins.asStateFlow()

    // Location Address
    private val addresses = listOf(
        "Home: Flat 402, Green Meadows, Sector 15",
        "Work: Tech Hub Tower B, 7th Floor, City IT Park",
        "Other: Iron Fitness Gym, Block C, Outer Ring Rd"
    )
    private val _selectedAddress = MutableStateFlow(addresses[0])
    val selectedAddress: StateFlow<String> = _selectedAddress.asStateFlow()

    fun cycleAddress() {
        val currentIndex = addresses.indexOf(_selectedAddress.value)
        val nextIndex = (currentIndex + 1) % addresses.size
        _selectedAddress.value = addresses[nextIndex]
    }

    // Active Category Filter
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // Sub-Filter Chips selected
    private val _nearestFilter = MutableStateFlow(false)
    val nearestFilter: StateFlow<Boolean> = _nearestFilter.asStateFlow()

    private val _offersFilter = MutableStateFlow(false)
    val offersFilter: StateFlow<Boolean> = _offersFilter.asStateFlow()

    private val _highlyRatedFilter = MutableStateFlow(false)
    val highlyRatedFilter: StateFlow<Boolean> = _highlyRatedFilter.asStateFlow()

    private val _pureVegFilter = MutableStateFlow(false)
    val pureVegFilter: StateFlow<Boolean> = _pureVegFilter.asStateFlow()

    // Observable Restaurants based on filters & categories & search queries
    private val _filteredRestaurants = MutableStateFlow(allRestaurants)
    val filteredRestaurants: StateFlow<List<Restaurant>> = _filteredRestaurants.asStateFlow()

    init {
        updateFilteredRestaurants()
    }

    fun selectTab(tab: QuickoTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        updateFilteredRestaurants()
    }

    fun selectCategory(categoryId: String?) {
        if (_selectedCategory.value == categoryId) {
            _selectedCategory.value = null // toggle off
        } else {
            _selectedCategory.value = categoryId
        }
        updateFilteredRestaurants()
    }

    fun toggleNearestFilter() {
        _nearestFilter.value = !_nearestFilter.value
        updateFilteredRestaurants()
    }

    fun toggleOffersFilter() {
        _offersFilter.value = !_offersFilter.value
        updateFilteredRestaurants()
    }

    fun toggleHighlyRatedFilter() {
        _highlyRatedFilter.value = !_highlyRatedFilter.value
        updateFilteredRestaurants()
    }

    fun togglePureVegFilter() {
        _pureVegFilter.value = !_pureVegFilter.value
        updateFilteredRestaurants()
    }

    fun resetAllFilters() {
        _selectedCategory.value = null
        _nearestFilter.value = false
        _offersFilter.value = false
        _highlyRatedFilter.value = false
        _pureVegFilter.value = false
        _searchQuery.value = ""
        updateFilteredRestaurants()
    }

    private fun updateFilteredRestaurants() {
        var list = allRestaurants

        // Category filter
        _selectedCategory.value?.let { catId ->
            list = list.filter { r ->
                r.cuisines.any { it.equals(catId, ignoreCase = true) } ||
                        r.iconEmoji == categories.find { it.id == catId }?.emoji ||
                        r.menuItems.any { it.emoji == categories.find { it.id == catId }?.emoji } ||
                        r.name.contains(catId, ignoreCase = true)
            }
        }

        // Sub queries text search
        val q = _searchQuery.value.trim()
        if (q.isNotEmpty()) {
            list = list.filter { r ->
                r.name.contains(q, ignoreCase = true) ||
                        r.cuisines.any { it.contains(q, ignoreCase = true) } ||
                        r.menuItems.any { it.name.contains(q, ignoreCase = true) || it.description.contains(q, ignoreCase = true) }
            }
        }

        // Pure Veg filter
        if (_pureVegFilter.value) {
            list = list.filter { it.isVeg }
        }

        // Highly rated filter (>= 4.5)
        if (_highlyRatedFilter.value) {
            list = list.filter { it.rating >= 4.5 }
        }

        // Offers filter (discountTag is nonempty or free delivery)
        if (_offersFilter.value) {
            list = list.filter { it.discountTag.isNotEmpty() }
        }

        // Nearest filter (distance < 1.5 km is closest)
        if (_nearestFilter.value) {
            list = list.sortedBy {
                it.distance.split(" ")[0].toDoubleOrNull() ?: 10.0
            }
        }

        _filteredRestaurants.value = list
    }

    // Selected restaurant drawer details
    private val _selectedRestaurant = MutableStateFlow<Restaurant?>(null)
    val selectedRestaurant: StateFlow<Restaurant?> = _selectedRestaurant.asStateFlow()

    fun selectRestaurant(restaurant: Restaurant?) {
        _selectedRestaurant.value = restaurant
    }

    // CART STATE
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    fun addToCart(item: MenuItem) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.menuItem.id == item.id }
        if (index >= 0) {
            val oldItem = currentList[index]
            currentList[index] = oldItem.copy(quantity = oldItem.quantity + 1)
        } else {
            currentList.add(CartItem(item, 1))
        }
        _cart.value = currentList
    }

    fun removeFromCart(item: MenuItem) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.menuItem.id == item.id }
        if (index >= 0) {
            val oldItem = currentList[index]
            if (oldItem.quantity <= 1) {
                currentList.removeAt(index)
            } else {
                currentList[index] = oldItem.copy(quantity = oldItem.quantity - 1)
            }
        }
        _cart.value = currentList
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun getCartTotal(): Double {
        return _cart.value.sumOf { it.menuItem.price * it.quantity }
    }

    // ACTIVE ORDERS
    private val _orders = MutableStateFlow<List<Order>>(
        listOf(
            Order(
                id = "QK-8842",
                restaurantName = "The Imperial Biryani",
                date = "Yesterday",
                items = listOf(CartItem(MenuItem("b1", "Chicken Dum Biryani", "", 280.0, 4.9, false, "🍛"), 1)),
                totalPrice = 329.0,
                status = "Delivered",
                deliveryStep = 4
            )
        )
    )
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    // Checkout / Place Order with Live Tracking timer!
    fun placeOrder(restaurantName: String) {
        if (_cart.value.isEmpty()) return

        val orderId = "QK-${(1000..9999).random()}"
        val totalAmount = getCartTotal() + 29.0 + 10.0 // inclusive of packing & delivery fees
        val newOrder = Order(
            id = orderId,
            restaurantName = restaurantName,
            date = "Today",
            items = _cart.value,
            totalPrice = totalAmount,
            status = "Order Placed",
            deliveryStep = 1
        )

        // Append to orders list
        val currentOrders = _orders.value.toMutableList()
        currentOrders.add(0, newOrder) // pre-pend to show newest on top
        _orders.value = currentOrders

        // Clear cart and close sheet
        clearCart()
        _selectedRestaurant.value = null

        // Add loyalty reward coins
        _userCoins.value += 30

        // Change navigation tab to ORDERS to trace active checkout in real-time
        _selectedTab.value = QuickoTab.ORDERS

        // Fire a live update coroutine that simulates delivery steps progressively
        viewModelScope.launch {
            // Step 1: Placed -> Step 2: Preparing (after 6 seconds)
            delay(6000)
            updateOrderStep(orderId, 2, "Kitchen preparing fresh food")
            // Step 2 -> Step 3: Out for Delivery (after 8 seconds)
            delay(8000)
            updateOrderStep(orderId, 3, "Express runner out for delivery")
            // Step 3 -> Step 4: Arrived (after 8 seconds)
            delay(8000)
            updateOrderStep(orderId, 4, "Delivered with joy")
        }
    }

    private fun updateOrderStep(orderId: String, step: Int, status: String) {
        val updatedList = _orders.value.map { order ->
            if (order.id == orderId) {
                order.copy(deliveryStep = step, status = status)
            } else {
                order
            }
        }
        _orders.value = updatedList
    }
}
