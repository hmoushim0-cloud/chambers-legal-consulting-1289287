package sa.factory.chamberslegalconsu.domain.usecase

import sa.factory.chamberslegalconsu.domain.repository.ChambersLegalRepository
import sa.factory.chamberslegalconsu.model.ChambersLegalItem
import sa.factory.chamberslegalconsu.model.ChambersLegalOrder
import sa.factory.chamberslegalconsu.model.ChambersLegalTelemetry
import kotlinx.coroutines.flow.Flow

class GetCatalogUseCase(private val repository: ChambersLegalRepository) {
    operator fun invoke(): Flow<List<ChambersLegalItem>> = repository.catalogFlow
}

class ManageCartUseCase(private val repository: ChambersLegalRepository) {
    val cart = repository.activeCart
    fun add(item: ChambersLegalItem) = repository.addToCart(item)
    fun remove(item: ChambersLegalItem) = repository.removeFromCart(item)
    fun clear() = repository.clearCart()
}

class ProcessCheckoutUseCase(private val repository: ChambersLegalRepository) {
    suspend operator fun invoke(paymentMethod: String = "Mada Contactless EMV"): ChambersLegalOrder =
        repository.checkout(paymentMethod)
}

class ObserveHardwareTelemetryUseCase(private val repository: ChambersLegalRepository) {
    operator fun invoke(): Flow<ChambersLegalTelemetry> = repository.telemetryFlow
}
