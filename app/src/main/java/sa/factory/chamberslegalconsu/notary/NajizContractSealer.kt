package sa.factory.chamberslegalconsu.notary

import sa.factory.chamberslegalconsu.hardware.HsmDigitalSignatureEngine
import sa.factory.chamberslegalconsu.model.LegalContractMetadata
import sa.factory.chamberslegalconsu.model.NotarizedContractSeal
import java.nio.charset.StandardCharsets
import java.security.PrivateKey
import java.security.PublicKey
import java.util.Locale
import java.util.UUID

/**
 * Najiz Judicial & Commercial Digital Contract Notarization Sealer.
 * Generates tamper-evident cryptographic seals compliant with Saudi Ministry of Justice digital standards.
 * Zero-Emoji Invariant strictly maintained.
 */
object NajizContractSealer {

    /**
     * Serializes legal contract metadata into a deterministic canonical string for digital hashing.
     */
    fun canonicalizeContract(contract: LegalContractMetadata): ByteArray {
        val stipulationsMerged = contract.stipulations.joinToString(";")
        val canonical = String.format(
            Locale.US,
            "CONTRACT_ID:%s|LAWYER_LIC:%s|CLIENT_ID:%s|CATEGORY:%s|FEE_SAR:%.2f|TIMESTAMP:%d|STIPULATIONS:%s",
            contract.contractId,
            contract.lawyerLicenseNo,
            contract.clientNationalId,
            contract.contractCategory,
            contract.feeSar,
            contract.timestampEpochMs,
            stipulationsMerged
        )
        return canonical.toByteArray(StandardCharsets.UTF_8)
    }

    /**
     * Seals and notarizes a contract using the HSM private key.
     */
    fun notarizeContract(
        contract: LegalContractMetadata,
        hsmPrivateKey: PrivateKey,
        lawyerPublicKey: PublicKey,
        keyId: String = "NAJIZ-HSM-KEY-MOJ"
    ): NotarizedContractSeal {
        val canonicalBytes = canonicalizeContract(contract)
        val digitalSig = HsmDigitalSignatureEngine.signPayload(
            data = canonicalBytes,
            privateKey = hsmPrivateKey,
            publicKey = lawyerPublicKey,
            keyId = keyId
        )

        val sealUuid = UUID.randomUUID().toString()
        val isVerified = HsmDigitalSignatureEngine.verifyDigitalSignature(canonicalBytes, digitalSig)

        return NotarizedContractSeal(
            contractId = contract.contractId,
            sealUuid = sealUuid,
            digitalSignature = digitalSig,
            isVerified = isVerified,
            notarizedAtEpochMs = System.currentTimeMillis()
        )
    }

    /**
     * Verifies the integrity and authenticity of a notarized contract seal against current contract data.
     */
    fun verifyContractSeal(
        contract: LegalContractMetadata,
        seal: NotarizedContractSeal
    ): Boolean {
        if (contract.contractId != seal.contractId) {
            return false
        }
        val canonicalBytes = canonicalizeContract(contract)
        return HsmDigitalSignatureEngine.verifyDigitalSignature(canonicalBytes, seal.digitalSignature)
    }
}
