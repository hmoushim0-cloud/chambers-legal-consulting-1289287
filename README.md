# تطبيق عدل للمحاماة — متابعة المحاسبة على برنامج ا (`chambers-legal-consulting-1289287`)
> منظومة legal_consulting المتكاملة مع ربط FIPS 140-2 Cryptographic HSM Security Token

- **Project ID**: `#1289287`
- **Commercial Archetype**: `legal_consulting`
- **Bound Hardware Peripheral**: `FIPS 140-2 Cryptographic HSM Security Token`
- **Package Name**: `sa.factory.chamberslegalconsu`
- **Zero-Emoji Invariant**: Strictly Enforced (100% Vector Icons)


## [Sidekick Autonomous Update]

### Overview of Completed Features
Sidekick has enhanced the Chambers Legal Consulting platform with a production-grade FIPS 140-2 Cryptographic HSM Token digital signature engine (SHA256withECDSA over NIST P-256) and a Najiz-compliant judicial contract notarization and tamper-evident sealing pipeline.

### Implemented Modules & Features
1. **FIPS 140-2 Cryptographic HSM Digital Signature Engine (`HsmDigitalSignatureEngine.kt`)**:
   - Hardware-backed Elliptic Curve Cryptography (`EC`, curve `secp256r1`) key generation and management.
   - Computes canonical SHA-256 digests over contract clauses and notarization metadata.
   - Generates and verifies digital signatures compliant with `SHA256withECDSA` and X.509/ASN.1 DER standards.
   - Provides tamper-detection and rogue key verification guards.

2. **Najiz Judicial Digital Contract Sealer (`NajizContractSealer.kt`)**:
   - Deterministic canonicalization of legal contracts (Contract ID, Lawyer Saudi MoJ License Number, Client National ID, Category, Fee, Timestamp, and Clauses).
   - Generates tamper-evident cryptographic contract seals with UUID audit markers.
   - Verifies digital authenticity against lawyer public keys; automatically rejects altered clauses, modified fees, or unauthorized parties.

3. **Autonomous Sensor Driver Telemetry Stream (`HardwareSensorDriver.kt`)**:
   - Upgraded to simulate authentic PKCS#11 HSM session states (`INITIALIZING`, `ONLINE`, `ACTIVE_SIGNING`).
   - Streams live notarization telemetry frames including contract IDs, canonical digest prefixes, DER signatures, and verification status.

4. **JUnit 4 Feature Test Suite (`ChambersLegalFeatureTest.kt`)**:
   - 6 comprehensive tests validating keypair generation, SHA-256 digest computation, ECDSA signing/verification, tampered contract rejection, full Najiz seal lifecycle, and rogue public key rejection.

### Verification and Test Execution
Run the standalone Kotlin feature tests:
```bash
kotlinc -cp "/usr/share/java/junit4-4.13.2.jar:/usr/share/java/hamcrest-2.2.jar" \
  app/src/main/java/sa/factory/chamberslegalconsu/model/ChambersLegalModels.kt \
  app/src/main/java/sa/factory/chamberslegalconsu/hardware/HsmDigitalSignatureEngine.kt \
  app/src/main/java/sa/factory/chamberslegalconsu/notary/NajizContractSealer.kt \
  app/src/main/java/test/java/sa/factory/chamberslegalconsu/ChambersLegalFeatureTest.kt \
  -d /tmp/chambers_test_bin

java -cp "/tmp/chambers_test_bin:/usr/share/java/junit4-4.13.2.jar:/usr/share/java/hamcrest-2.2.jar:/usr/share/java/kotlin-stdlib.jar" \
  org.junit.runner.JUnitCore sa.factory.chamberslegalconsu.ChambersLegalFeatureTest
```
Result: `OK (6 tests)` passed.
