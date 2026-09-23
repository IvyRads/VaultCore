# VaultCore API Mapping

The backend runs at `http://localhost:8080`; browser requests require `Authorization: Bearer <token>` after login.

| Frontend page/action | Endpoint | Method | Tables |
|---|---|---:|---|
| `login.html` login | `/api/auth/login` | POST | users |
| `Sign_up.html` registration | `/api/auth/register` | POST | users, user_profiles, customers |
| Customer details | `/api/customers/{id}` | GET | customers, users, user_profiles, branches, accounts |
| Account transactions | `/api/accounts/{id}/transactions` | GET | accounts, transactions |
| Customer transactions | `/api/customers/{id}/transactions` | GET | customers, accounts, transactions |
| Deposit | `/api/transactions/deposit` | POST | accounts, transactions, transaction_logs |
| Withdrawal | `/api/transactions/withdraw` | POST | accounts, transactions, transaction_logs |
| Transfer | `/api/transfers` | POST | accounts, transfers, transactions, transaction_logs |
| Account transfers | `/api/accounts/{id}/transfers` | GET | transfers |
| My cards | `/api/cards` | GET | cards, accounts |
| Block card | `/api/cards/{id}/block` | POST | cards, account_requests |
| Unblock card | `/api/cards/{id}/unblock` | POST | cards, account_requests |
| My accounts | `/api/accounts` | GET | accounts |
| Freeze account | `/api/accounts/{id}/freeze` | POST | accounts, account_requests |
| Unfreeze account | `/api/accounts/{id}/unfreeze` | POST | accounts, account_requests |
| My fraud reports | `/api/fraud-reports` | GET | fraud_reports |
| Submit fraud report | `/api/fraud-reports` | POST | fraud_reports |

Registration intentionally sets `is_verified=true`; email verification is not implemented for this project.

## Request examples

```json
POST /api/auth/login
{"email":"customer@example.com","password":"your-password"}
```

```json
POST /api/transactions/deposit
{"accountId":1,"amount":5000,"description":"Cash deposit"}
```

```json
POST /api/transfers
{"senderAccountId":1,"receiverAccountId":2,"amount":3000}
```

```json
POST /api/cards/{id}/block
{"reason":"Lost Card","remarks":"Reported immediately from customer dashboard"}
```

```json
POST /api/accounts/{id}/freeze
{"reason":"Suspicious Activity","remarks":"Temporary freeze requested by customer"}
```

```json
POST /api/fraud-reports
{"accountId":1,"transactionId":null,"fraudType":"Phishing","description":"Suspicious link received by email","incidentDate":"2026-09-17","contactPreference":"Email"}
```
