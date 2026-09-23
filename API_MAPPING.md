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
