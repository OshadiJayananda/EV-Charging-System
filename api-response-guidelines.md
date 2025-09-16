# 📌 API Response Handling (Backend Guidelines)

To align with the frontend error handling utility (`handleError` in `api.ts`),  
the backend should **always return a JSON object with a `message` field** in error responses.  
This ensures clear toast notifications in the web app.

---

## ✅ Success Responses
**200 OK / 201 Created**
```json
{
  "id": 3,
  "name": "Charlie Brown",
  "email": "charlie@example.com",
  "role": "user"
}
```

---

## ❌ Error Responses (with `message` field)

**400 Bad Request**
```json
{
  "message": "Invalid email format"
}
```

**401 Unauthorized**
```json
{
  "message": "Authentication required"
}
```

**403 Forbidden**
```json
{
  "message": "You do not have permission to perform this action"
}
```

**404 Not Found**
```json
{
  "message": "User not found"
}
```

**409 Conflict**
```json
{
  "message": "Email already in use"
}
```

**500 Internal Server Error**
```json
{
  "message": "Unexpected error occurred. Please try again later."
}
```

---

## 🔑 Key Points
- Always include a `message` field in error responses.  
- Success responses should return structured objects (DTOs).  
- Errors should **not return raw stack traces** — only user-friendly `message`.  
- Apply consistently across all endpoints (User, Station, Booking, Notification, etc.).
