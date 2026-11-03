package sa.factory.chamberslegalconsu.domain.repository

import sa.factory.chamberslegalconsu.model.ChambersLegalItem
import sa.factory.chamberslegalconsu.model.ChambersLegalOrder
import sa.factory.chamberslegalconsu.model.ChambersLegalTelemetry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChambersLegalRepository {
    val catalogFlow: Flow<List<ChambersLegalItem>>
    val activeCart: StateFlow<List<ChambersLegalItem>>
    val telemetryFlow: Flow<ChambersLegalTelemetry>
    
    fun addToCart(item: ChambersLegalItem)
    fun removeFromCart(item: ChambersLegalItem)
    fun clearCart()
    suspend fun checkout(paymentMethod: String): ChambersLegalOrder
    suspend fun refreshCatalog()
}
