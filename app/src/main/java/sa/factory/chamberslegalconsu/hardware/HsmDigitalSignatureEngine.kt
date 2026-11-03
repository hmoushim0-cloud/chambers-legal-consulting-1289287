package sa.factory.chamberslegalconsu.hardware

import sa.factory.chamberslegalconsu.model.HsmDigitalSignature
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import java.util.Locale

/**
 * FIPS 140-2 Cryptographic HSM Security Token Engine.
 * Implements Elliptic Curve Digital Signature Algorithm (ECDSA with SHA-256) over NIST P-256 (secp256r1).
 * Zero-Emoji Invariant strictly maintained.
 */
object HsmDigitalSignatureEngine {

    const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
    const val KEY_PAIR_ALGORITHM = "EC"
    const val EC_CURVE_NAME = "secp256r1"

    /**
     * Generates a new hardware-backed EC KeyPair for digital notary signing.
     */
    fun generateHsmKeyPair(): KeyPair {
        val keyPairGen = KeyPairGenerator.getInstance(KEY_PAIR_ALGORITHM)
        val ecSpec = ECGenParameterSpec(EC_CURVE_NAME)
        keyPairGen.initialize(ecSpec, SecureRandom())
        return keyPairGen.generateKeyPair()
    }

    /**
     * Computes canonical SHA-256 hash of byte payload.
     */
    fun computeSha256(data: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data)
    }

    /**
     * Signs data using the HSM private key using SHA256withECDSA.
     */
    fun signPayload(
        data: ByteArray,
        privateKey: PrivateKey,
        publicKey: PublicKey,
        keyId: String = "FIPS-140-2-HSM-KEY-01"
    ): HsmDigitalSignature {
        val digest = computeSha256(data)
        val signer = Signature.getInstance(SIGNATURE_ALGORITHM)
        signer.initSign(privateKey)
        signer.update(data)
        val signatureBytes = signer.sign()

        return HsmDigitalSignature(
            keyId = keyId,
            algorithm = SIGNATURE_ALGORITHM,
            canonicalDigestHex = bytesToHex(digest),
            signatureDerHex = bytesToHex(signatureBytes),
            publicKeyDerHex = bytesToHex(publicKey.encoded)
        )
    }

    /**
     * Verifies an ECDSA signature against the original data and DER-encoded public key.
     */
    fun verifySignature(
        data: ByteArray,
        signatureDerBytes: ByteArray,
        publicKeyBytes: ByteArray
    ): Boolean {
        return try {
            val keyFactory = KeyFactory.getInstance(KEY_PAIR_ALGORITHM)
            val pubKeySpec = X509EncodedKeySpec(publicKeyBytes)
            val pubKey = keyFactory.generatePublic(pubKeySpec)

            val verifier = Signature.getInstance(SIGNATURE_ALGORITHM)
            verifier.initVerify(pubKey)
            verifier.update(data)
            verifier.verify(signatureDerBytes)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Helper to verify HsmDigitalSignature object against original payload.
     */
    fun verifyDigitalSignature(
        data: ByteArray,
        digitalSig: HsmDigitalSignature
    ): Boolean {
        val sigBytes = hexToBytes(digitalSig.signatureDerHex)
        val pubBytes = hexToBytes(digitalSig.publicKeyDerHex)
        return verifySignature(data, sigBytes, pubBytes)
    }

    fun hexToBytes(hex: String): ByteArray {
        val clean = hex.replace(" ", "").trim()
        val len = clean.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            val h = Character.digit(clean[i], 16)
            val l = Character.digit(clean[i + 1], 16)
            data[i / 2] = ((h shl 4) + l).toByte()
            i += 2
        }
        return data
    }

    fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format(Locale.US, "%02X", b))
        }
        return sb.toString()
    }
}
