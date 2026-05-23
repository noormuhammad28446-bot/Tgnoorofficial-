package com.example.model

data class Category(
    val id: String,
    val name: String,
    val emoji: String,
    val backgroundColorHex: Long
)

data class MenuItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val rating: Double,
    val isVeg: Boolean,
    val emoji: String
)

data class Restaurant(
    val id: String,
    val name: String,
    val rating: Double,
    val reviewCount: String,
    val deliveryTime: String,
    val distance: String,
    val costForTwo: String,
    val cuisines: List<String>,
    val discountTag: String,
    val discountDetail: String,
    val isVeg: Boolean = false,
    val isFeatured: Boolean = false,
    val iconEmoji: String,
    val menuItems: List<MenuItem> = emptyList()
)

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int
)

enum class QuickoTab {
    HOME, SEARCH, ORDERS, PROFILE
}

data class Order(
    val id: String,
    val restaurantName: String,
    val date: String,
    val items: List<CartItem>,
    val totalPrice: Double,
    val status: String, // "Preparing", "Out for Delivery", "Delivered"
    val deliveryStep: Int // 1 to 4 for real-time progress steps
)
