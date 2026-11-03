package sa.factory.chamberslegalconsu.model

import java.io.Serializable

data class ChambersLegalItem(
    val id: String,
    val title: String,
    val description: String,
    val priceSar: Double,
    val badge: String = "معتمد",
    val category: String = "عام",
    val status: String = "AVAILABLE"
) : Serializable

data class ChambersLegalOrder(
    val orderId: String,
    val items: List<ChambersLegalItem>,
    val subtotalSar: Double,
    val vatSar: Double,
    val totalSar: Double,
    val paymentMethod: String = "Mada Contactless EMV",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "CONFIRMED"
) : Serializable

data class ChambersLegalTelemetry(
    val peripheralName: String,
    val status: String,
    val liveReading: String,
    val batteryPct: Int = 98,
    val signalDbm: Int = -58,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}

data class LegalContractMetadata(
    val contractId: String,
    val lawyerLicenseNo: String,
    val clientNationalId: String,
    val contractCategory: String,
    val stipulations: List<String>,
    val feeSar: Double,
    val timestampEpochMs: Long = System.currentTimeMillis()
) : Serializable

data class HsmDigitalSignature(
    val keyId: String,
    val algorithm: String = "SHA256withECDSA",
    val canonicalDigestHex: String,
    val signatureDerHex: String,
    val publicKeyDerHex: String
) : Serializable

data class NotarizedContractSeal(
    val contractId: String,
    val sealUuid: String,
    val digitalSignature: HsmDigitalSignature,
    val isVerified: Boolean,
    val notarizedAtEpochMs: Long = System.currentTimeMillis()
) : Serializable
