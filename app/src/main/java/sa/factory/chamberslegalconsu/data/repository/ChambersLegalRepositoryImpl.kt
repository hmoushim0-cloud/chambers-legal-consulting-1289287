package sa.factory.chamberslegalconsu.data.repository

import sa.factory.chamberslegalconsu.domain.repository.ChambersLegalRepository
import sa.factory.chamberslegalconsu.hardware.HardwareSensorDriver
import sa.factory.chamberslegalconsu.model.ChambersLegalItem
import sa.factory.chamberslegalconsu.model.ChambersLegalOrder
import sa.factory.chamberslegalconsu.model.ChambersLegalTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChambersLegalRepositoryImpl(
    private val sensorDriver: HardwareSensorDriver
) : ChambersLegalRepository {

    private val _catalog = MutableStateFlow<List<ChambersLegalItem>>(listOf(
        ChambersLegalItem("1", "الباقة التنفيذية المعتمدة", "تكامل مباشر مع FIPS 140-2 Cryptographic HSM Security Token", 180.0, "معتمد", "الخدمات الرئيسية"),
        ChambersLegalItem("2", "خدمة المعالجة الفورية", "أداء أصيل بسرعة 60 إطار في الثانية", 95.0, "فوري", "المراقبة الحية"),
        ChambersLegalItem("3", "باقة الصيانة والدعم السحابي", "تشفير كامل للبيانات المحلية ومزامنة آمنة", 250.0, "شامل", "التقارير الضريبية")
    ))
    override val catalogFlow: Flow<List<ChambersLegalItem>> = _catalog.asStateFlow()

    private val _cart = MutableStateFlow<List<ChambersLegalItem>>(emptyList())
    override val activeCart: StateFlow<List<ChambersLegalItem>> = _cart.asStateFlow()

    override val telemetryFlow: Flow<ChambersLegalTelemetry> = sensorDriver.observePeripheralStream()
        .map { frame ->
            ChambersLegalTelemetry(
                peripheralName = frame.peripheralName,
                status = frame.connectionState,
                liveReading = frame.livePayload,
                batteryPct = 96,
                signalDbm = -54
            )
        }

    override fun addToCart(item: ChambersLegalItem) {
        val current = _cart.value.toMutableList()
        current.add(item)
        _cart.value = current
    }

    override fun removeFromCart(item: ChambersLegalItem) {
        val current = _cart.value.toMutableList()
        current.remove(item)
        _cart.value = current
    }

    override fun clearCart() {
        _cart.value = emptyList()
    }

    override suspend fun checkout(paymentMethod: String): ChambersLegalOrder {
        val currentItems = _cart.value.toList()
        val subtotal = currentItems.sumOf { it.priceSar }
        val vat = subtotal * 0.15
        val total = subtotal + vat
        val order = ChambersLegalOrder(
            orderId = "ORD-${System.currentTimeMillis() % 100000}",
            items = currentItems,
            subtotalSar = subtotal,
            vatSar = vat,
            totalSar = total,
            paymentMethod = paymentMethod
        )
        clearCart()
        return order
    }

    override suspend fun refreshCatalog() {
        // Offline-first sync logic
    }
}
