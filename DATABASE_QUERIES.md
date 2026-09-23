# Database Queries and Concepts

The application uses JPA repositories and parameterized JPQL. These SQL shapes describe the relational operations used by the backend and are useful for the DBMS viva.

```sql
SELECT c.customer_id, c.customer_number, u.email, p.first_name, p.last_name
FROM customers c JOIN users u ON u.user_id = c.user_id
LEFT JOIN user_profiles p ON p.user_id = u.user_id;
```

```sql
SELECT a.account_id, a.account_number, a.balance, a.status
FROM accounts a WHERE a.customer_id = :customerId;
```

```sql
SELECT t.* FROM transactions t JOIN accounts a ON a.account_id = t.account_id
WHERE a.customer_id = :customerId ORDER BY t.transaction_date DESC;
```

```sql
SELECT b.branch_id, b.branch_name, COUNT(c.customer_id) AS customers
FROM branches b LEFT JOIN customers c ON c.branch_id = b.branch_id
GROUP BY b.branch_id, b.branch_name;
```

Balance operations are one database transaction: account update, history insert, and audit log insert commit together or roll back together. Pessimistic row locks prevent concurrent withdrawals from using the same stale balance.
