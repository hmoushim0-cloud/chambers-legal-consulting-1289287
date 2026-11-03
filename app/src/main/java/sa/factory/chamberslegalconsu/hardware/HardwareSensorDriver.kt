package sa.factory.chamberslegalconsu.hardware

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import sa.factory.chamberslegalconsu.model.LegalContractMetadata
import sa.factory.chamberslegalconsu.notary.NajizContractSealer
import java.util.Locale

data class HardwareTelemetryFrame(
    val peripheralName: String,
    val connectionState: String,
    val livePayload: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HardwareSensorDriver {
    private val peripheral = "FIPS 140-2 Cryptographic HSM Security Token"

    fun observePeripheralStream(): Flow<HardwareTelemetryFrame> = flow {
        emit(
            HardwareTelemetryFrame(
                peripheralName = peripheral,
                connectionState = "INITIALIZING",
                livePayload = "PKCS#11 Slot 0: FIPS 140-2 Level 3 Cryptographic HSM Token detected"
            )
        )
        delay(300L)

        // Generate Hardware KeyPair session
        val hsmKeyPair = HsmDigitalSignatureEngine.generateHsmKeyPair()
        emit(
            HardwareTelemetryFrame(
                peripheralName = peripheral,
                connectionState = "ONLINE",
                livePayload = "HSM Session Open: Algorithm=SHA256withECDSA | Curve=secp256r1 | Token Status=OPERATIONAL"
            )
        )

        var contractCounter = 1001
        while (true) {
            delay(1500L)

            val sampleContract = LegalContractMetadata(
                contractId = "MOJ-KSA-2026-$contractCounter",
                lawyerLicenseNo = "MOJ-LIC-48912",
                clientNationalId = "1098234712",
                contractCategory = "COMMERCIAL_DISPUTE_REPRESENTATION",
                stipulations = listOf("Najiz Arbitration Binding", "Certified Digital Audit Trail"),
                feeSar = 15000.0,
                timestampEpochMs = System.currentTimeMillis()
            )

            val seal = NajizContractSealer.notarizeContract(
                contract = sampleContract,
                hsmPrivateKey = hsmKeyPair.private,
                lawyerPublicKey = hsmKeyPair.public
            )

            val formattedTelemetry = String.format(
                Locale.US,
                "HSM Sign: [%s] | Digest: %s... | SigDER: %s... | Seal Verified: %b",
                sampleContract.contractId,
                seal.digitalSignature.canonicalDigestHex.substring(0, 8),
                seal.digitalSignature.signatureDerHex.substring(0, 12),
                seal.isVerified
            )

            emit(
                HardwareTelemetryFrame(
                    peripheralName = peripheral,
                    connectionState = "ACTIVE_SIGNING",
                    livePayload = formattedTelemetry
                )
            )
            contractCounter++
        }
    }
}
