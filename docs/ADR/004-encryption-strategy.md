# ADR 004: Zero-Plaintext Hardware-Backed Encryption Strategy

## Status
Accepted

## Context
Documents stored in the vault may contain sensitive personal or proprietary information. Merely restricting file permissions at the OS sandbox level is insufficient against physical extraction or privileged malware.

## Decision
1. **Authenticated Encryption**: Use **AES-256-GCM** with unique 96-bit initialization vectors (nonces) generated via cryptographically secure random number generators for every file.
2. **Key Isolation**: Master symmetric keys are generated within the hardware-backed keystore:
   - Android: `AndroidKeyStore` (StrongBox Keymaster / ARM TrustZone TEE).
   - iOS: Apple Keychain Services protected by the Secure Enclave.
3. **Integrity Validation**: Store SHA-256 digests of files upon import to enable rapid integrity validation and detect bit-rot or unauthorized modification.

## Consequences
- **Positive**: Ciphertext cannot be decrypted even if raw flash storage is read out.
- **Positive**: AES-GCM guarantees both confidentiality and authenticity (tampering triggers tag verification failure).
- **Negative**: Adds cryptographic CPU overhead during file import and export (mitigated by hardware AES instructions).
