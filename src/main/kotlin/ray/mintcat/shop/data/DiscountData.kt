package ray.mintcat.shop.data

data class DiscountData(
    val group: String,
    val permission: String,
    val shop: List<String>,
    val data: Map<String, Double>,
)
