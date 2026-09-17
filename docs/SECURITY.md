# VaultSync Security Architecture & Threat Model

> **Core Tenet**: Zero Plaintext Storage. All files entering the vault are encrypted at rest using hardware-protected keys before bytes touch storage media.

---

## 🛡 Cryptographic Primitives

| Component | Standard / Algorithm | Parameters |
| :--- | :--- | :--- |
| **Symmetric Encryption** | AES-GCM (Galois/Counter Mode) | 256-bit key, 96-bit random IV/nonce, 128-bit authentication tag |
| **Integrity Checksum** | SHA-256 (Secure Hash Algorithm 2) | 256-bit digest |
| **Key Generation** | Secure Random PRNG | Hardware-backed entropy source |
| **Key Management (iOS)** | Apple Keychain Services | `kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly` |
| **Key Management (Android)** | Android Keystore Provider | `PURPOSE_ENCRYPT \| PURPOSE_DECRYPT`, `BLOCK_MODE_GCM`, `ENCRYPTION_PADDING_NONE` |

---

## 🔒 Threat Model & Mitigations

### 1. Physical Device Theft / Forensic Disk Extraction
* **Threat**: An adversary copies the flash memory or inspects unencrypted SQLite files directly.
* **Mitigation**:
  - Encrypted file payloads are stored in the app's sandboxed directory as raw ciphertext with prepended 12-byte initialization vectors and 16-byte authentication tags.
  - The SQLite database contains only the document metadata and SHA-256 hash.
  - The AES-256 keys never leave the hardware security module (Secure Enclave on iOS, StrongBox/TEE on Android) and are non-exportable.

### 2. File Tampering / Bit-Rot / Man-in-the-Middle Sync
* **Threat**: An altered or truncated file is injected into the sync pipeline.
* **Mitigation**:
  - AES-GCM provides authenticated encryption: any modification to the ciphertext triggers an `AEADBadTagException` (Android) or `AuthenticationFailure` (iOS) upon decryption.
  - Pre- and post-transfer SHA-256 digests ensure byte-level integrity verification against the recorded metadata.

### 3. Key Compromise via Application Memory Dump
* **Threat**: Adversary inspects memory dumps from a rooted or jailbroken device.
* **Mitigation**:
  - Android Keystore keys are generated inside the Trusted Execution Environment (TEE) or StrongBox Keymaster. The actual key bytes are never loaded into the application heap.
  - Cryptographic operations (`Cipher.init()`) occur within the Keystore boundary where supported.

---

## 🔄 Encryption & Decryption Pipeline

```text
IMPORT FILE
   │
   ▼
[Read Plaintext Bytes into Memory]
   │
   ▼
[Query Hardware Keystore / Keychain for Vault Key Alias]
   │
   ▼
[Generate Fresh 96-bit Cryptographic Nonce / IV]
   │
   ▼
[Execute AES-GCM 256-bit Encryption]
   ├── Generates Ciphertext + 128-bit Auth Tag
   │
   ▼
[Assemble Binary Payload: Nonce (12B) + Ciphertext + Tag (16B)]
   │
   ▼
[Calculate SHA-256 on Encrypted File Bytes]
   │
   ▼
[Atomic Write to Sandboxed Disk Path]
   │
   ▼
[Persist Document Metadata in Room / SwiftData]
```
