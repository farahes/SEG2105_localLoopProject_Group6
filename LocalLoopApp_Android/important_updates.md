# 📄 important_updates.md  
**✅ Standardizing User Roles & Field Names Across the Project**

---

## 🚀 Summary of Recent Changes

✅ **User role is now handled via an enum:**  
We’ve replaced raw string-based roles (like `"admin"`, `"participant"`) with a `UserRole` enum for type safety and consistency.

✅ **Field keys like `firstName` and `lastName` are now centralized:**  
They're defined in the `Constants.java` class so we never hardcode them again.

---

## 📦 Where Are These Located?

| Feature        | Location                                        |
|----------------|--------------------------------------------------|
| `UserRole` enum | `models/UserRole.java`                          |
| Shared keys     | `utils/Constants.java`                          |

---

## 🧩 Usage Rules

### ✅ DO:

- ✅ Use the `UserRole` enum for **all role comparisons** and assignments:

``` java
  UserRole role = UserRole.fromString(user.getRole());  
  if (role == UserRole.ADMIN) { ... }
```

- ✅ When passing or retrieving `firstName` or `lastName` from `Intent`, Firebase, etc., use:

  intent.getStringExtra(Constants.FIRST_NAME_KEY);

---

### ❌ DON’T:

- ❌ Don’t hardcode roles like `"admin"` or call `.toLowerCase()` for role comparison.
- ❌ Don’t use `"firstName"` or `"lastName"` as raw strings.

---

## 🔁 Converting from String to Enum

If you already have the role as a string (e.g., from Firebase):

``` java
  UserRole role = UserRole.fromString(roleString);
```

If `roleString` is invalid or null, this will return `null`.

---

## 🧠 Why This Matters

Using enums and shared constants:

- Prevents typos and case sensitivity bugs.
- Makes refactoring easier.
- Improves IDE autocomplete and type-checking.
- Keeps code DRY and consistent across the app.

---

## ✅ Quick Example

### ❌ Old:

``` java
  if (user.getRole().equalsIgnoreCase("participant")) {
```

### ✅ New:

``` java
  if (UserRole.fromString(user.getRole()) == UserRole.PARTICIPANT) {
```

---
