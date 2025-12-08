🪙 RupayGo – Offline CBDC Wallet (MVP)
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


A secure, device-bound, offline-capable digital cash system built for RBI Harbinger.

RupayGo enables offline person-to-person payments using secure hardware-bound tokens, QR-based exchange, and seamless online reconciliation with a backend issuer bank.

This project demonstrates how CBDC can work without internet, while preventing double spending and maintaining strong security guarantees.

🚀 Key Features
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

 1. Offline Token Transfers

Send and receive CBDC tokens without internet

QR-based handshake to transfer ownership

Sender → Receiver transfer is atomic and secure

 2. Hardware-Backed Security

Tokens stored in EncryptedSharedPreferences

Device-bound cryptographic keys (AES-GCM)

Prevents tampering and cloning

 3. Double-Spending Protection

Sender’s token removed immediately after transfer

Distributed ledger on client + reconciliation on server

 4. Online Sync & Settlement

When internet restores, app syncs pending transactions

Backend updates real bank balance securely

Uses digital signatures and server validation

 5. Multi-hop Token Transfers

A received token can be used again offline

Fully chainable just like real currency

Offline Transfer Flow (Simplified)
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

1️⃣ Sender initiates transfer

Opens wallet → chooses token → QR created

Contains token ID + signature + metadata

2️⃣ Receiver scans sender QR

Verifies authenticity

Accepts token temporarily

3️⃣ Receiver shows confirmation QR

Sender scans → token removed from sender

Ownership shifts to receiver

No internet required.


🛠 Tech Stack
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

📱 Client (Android App)

Kotlin

Jetpack Compose / UI

EncryptedSharedPreferences

QR Code (ZXing)

Coroutines & Flow

Token storage with reactive balance

🖥 Backend (Node.js Server)

Express.js

SQLite (lightweight ledger)

Crypto for signature validation

Token issuance + reconciliation

Dynamic account updates



## 📱 App Screenshots
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

<p align="center">
  <img src="https://github.com/rohits1450/RuPay.G.O/blob/master/RuPay.G.O-proof/WhatsApp%20Image%202025-12-08%20at%2022.50.29_4489186f.jpg?raw=true" width="250" />
  <img src="https://github.com/rohits1450/RuPay.G.O/blob/master/RuPay.G.O-proof/WhatsApp%20Image%202025-12-08%20at%2022.50.29_fe1f07b2.jpg?raw=true" width="250" />
 <img src="https://github.com/rohits1450/RuPay.G.O/blob/master/RuPay.G.O-proof/WhatsApp%20Image%202025-12-08%20at%2022.50.29_4879bb87.jpg?raw=true" width="250" />
</p>

<p align="center">
  <img src="https://github.com/rohits1450/RuPay.G.O/blob/master/RuPay.G.O-proof/WhatsApp%20Image%202025-12-08%20at%2022.50.31_20413732.jpg?raw=true" width="250" />
 <img src="https://github.com/rohits1450/RuPay.G.O/blob/master/RuPay.G.O-proof/WhatsApp%20Image%202025-12-08%20at%2023.02.26_dbae8216.jpg?raw=true" width="250" />
  <img src="https://github.com/rohits1450/RuPay.G.O/blob/master/RuPay.G.O-proof/WhatsApp%20Image%202025-12-08%20at%2022.50.30_2260d7d8.jpg?raw=true" width="250" /> 
</p>

<p align="center">
  <img src="https://github.com/rohits1450/RuPay.G.O/blob/master/RuPay.G.O-proof/WhatsApp%20Image%202025-12-08%20at%2022.50.30_a5859c4a.jpg?raw=true" width="250" />
<img src="https://github.com/rohits1450/RuPay.G.O/blob/master/RuPay.G.O-proof/WhatsApp%20Image%202025-12-08%20at%2022.50.30_cde7bf9f.jpg?raw=true" width="250" />
</p>



