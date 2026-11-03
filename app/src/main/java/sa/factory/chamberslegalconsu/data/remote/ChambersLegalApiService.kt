package sa.factory.chamberslegalconsu.data.remote

import sa.factory.chamberslegalconsu.model.ChambersLegalItem
import sa.factory.chamberslegalconsu.model.ChambersLegalOrder

interface ChambersLegalApiService {
    suspend fun fetchCatalog(): List<ChambersLegalItem>
    suspend fun submitOrder(order: ChambersLegalOrder): Boolean
}
