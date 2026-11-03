package sa.factory.chamberslegalconsu

import sa.factory.chamberslegalconsu.hardware.HsmDigitalSignatureEngine
import sa.factory.chamberslegalconsu.model.LegalContractMetadata
import sa.factory.chamberslegalconsu.notary.NajizContractSealer
import org.junit.Assert.*
import org.junit.Test
import java.nio.charset.StandardCharsets

/**
 * Feature Test Suite for Chambers Legal Consulting HSM Token & Najiz Contract Notarization.
 * Zero-Emoji Invariant strictly maintained.
 */
class ChambersLegalFeatureTest {

    @Test
    fun testHsmKeyPairGenerationAndAlgorithm() {
        val keyPair = HsmDigitalSignatureEngine.generateHsmKeyPair()
        assertNotNull("Private key must not be null", keyPair.private)
        assertNotNull("Public key must not be null", keyPair.public)
        assertEquals("Algorithm must be EC", "EC", keyPair.public.algorithm)
    }

    @Test
    fun testSha256DigestCalculation() {
        val testPayload = "NajizContractAuditTrail_2026".toByteArray(StandardCharsets.UTF_8)
        val digest = HsmDigitalSignatureEngine.computeSha256(testPayload)
        assertEquals("SHA-256 digest must be exactly 32 bytes", 32, digest.size)

        val hex = HsmDigitalSignatureEngine.bytesToHex(digest)
        assertEquals("SHA-256 hex must be 64 characters", 64, hex.length)
    }

    @Test
    fun testEcdsaSignatureAndVerification() {
        val keyPair = HsmDigitalSignatureEngine.generateHsmKeyPair()
        val data = "Saudi Ministry of Justice Notarization Charter".toByteArray(StandardCharsets.UTF_8)

        val digitalSig = HsmDigitalSignatureEngine.signPayload(
            data = data,
            privateKey = keyPair.private,
            publicKey = keyPair.public,
            keyId = "TEST-HSM-KEY-01"
        )

        assertEquals("Algorithm must be SHA256withECDSA", "SHA256withECDSA", digitalSig.algorithm)
        assertTrue("Signature hex must not be empty", digitalSig.signatureDerHex.isNotEmpty())

        val isValid = HsmDigitalSignatureEngine.verifyDigitalSignature(data, digitalSig)
        assertTrue("Valid ECDSA signature must verify successfully", isValid)
    }

    @Test
    fun testTamperedDataSignatureRejection() {
        val keyPair = HsmDigitalSignatureEngine.generateHsmKeyPair()
        val originalData = "Legitimate Contract Stipulation Amount: 50,000 SAR".toByteArray(StandardCharsets.UTF_8)
        val tamperedData = "Legitimate Contract Stipulation Amount: 90,000 SAR".toByteArray(StandardCharsets.UTF_8)

        val digitalSig = HsmDigitalSignatureEngine.signPayload(
            data = originalData,
            privateKey = keyPair.private,
            publicKey = keyPair.public
        )

        val isValidWithTampered = HsmDigitalSignatureEngine.verifyDigitalSignature(tamperedData, digitalSig)
        assertFalse("Tampered contract data must be rejected by ECDSA verification", isValidWithTampered)
    }

    @Test
    fun testNajizContractSealingAndTamperEvidentAudit() {
        val keyPair = HsmDigitalSignatureEngine.generateHsmKeyPair()

        val contract = LegalContractMetadata(
            contractId = "MOJ-KSA-2026-9921",
            lawyerLicenseNo = "LIC-98241",
            clientNationalId = "1098234712",
            contractCategory = "COMMERCIAL_DISPUTE",
            stipulations = listOf("Najiz Arbitration Binding", "Certified Digital Audit Trail"),
            feeSar = 25000.0,
            timestampEpochMs = 1793700000000L
        )

        val seal = NajizContractSealer.notarizeContract(
            contract = contract,
            hsmPrivateKey = keyPair.private,
            lawyerPublicKey = keyPair.public
        )

        assertTrue("Initial notarization seal must be valid", seal.isVerified)
        assertTrue("Contract seal verification against metadata must pass", NajizContractSealer.verifyContractSeal(contract, seal))

        // Negative test: Alter fee
        val tamperedContract = contract.copy(feeSar = 50000.0)
        assertFalse("Altered contract fee must invalidate cryptographic seal", NajizContractSealer.verifyContractSeal(tamperedContract, seal))

        // Negative test: Alter stipulations
        val tamperedStipulations = contract.copy(stipulations = listOf("Non-binding mediation"))
        assertFalse("Altered stipulations must invalidate cryptographic seal", NajizContractSealer.verifyContractSeal(tamperedStipulations, seal))
    }

    @Test
    fun testWrongPublicKeyRejection() {
        val legitimateKeyPair = HsmDigitalSignatureEngine.generateHsmKeyPair()
        val rogueKeyPair = HsmDigitalSignatureEngine.generateHsmKeyPair()

        val data = "Najiz Power of Attorney Grant".toByteArray(StandardCharsets.UTF_8)
        val digitalSig = HsmDigitalSignatureEngine.signPayload(
            data = data,
            privateKey = legitimateKeyPair.private,
            publicKey = legitimateKeyPair.public
        )

        val sigBytes = HsmDigitalSignatureEngine.hexToBytes(digitalSig.signatureDerHex)
        val roguePubBytes = rogueKeyPair.public.encoded

        val isValidWithRogueKey = HsmDigitalSignatureEngine.verifySignature(data, sigBytes, roguePubBytes)
        assertFalse("Verification using rogue public key must be rejected", isValidWithRogueKey)
    }
}
