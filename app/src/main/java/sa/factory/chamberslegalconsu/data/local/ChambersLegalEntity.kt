package sa.factory.chamberslegalconsu.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import sa.factory.chamberslegalconsu.model.ChambersLegalItem

@Entity(tableName = "legal_consulting_items_cache")
data class ChambersLegalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val priceSar: Double,
    val badge: String,
    val category: String,
    val status: String
) {
    fun toDomain(): ChambersLegalItem = ChambersLegalItem(
        id = id,
        title = title,
        description = description,
        priceSar = priceSar,
        badge = badge,
        category = category,
        status = status
    )
}
