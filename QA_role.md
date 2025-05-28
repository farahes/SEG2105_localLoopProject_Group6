# ❓ Q&A: Why Store the `role` Field if We Already Have a Child Class?

---

## 🧩 The Core Issue

Firebase only stores **field values** (like `role`, `email`, etc.) — not the **Java class type** (`Organizer`, `Participant`).

When you save:

```java
mDatabaseUsersRef.child(userId).setValue(newUserProfile);
```

Firebase serializes the entire object to JSON.  
There’s **no way to add extra metadata** outside the object.  
So, if `role` isn’t a field inside the class, it **won’t be saved**. ❌

---

## 🤔 Why Not Just Use Class Type?

Java knows the object’s class at runtime, but **Firebase doesn’t**.  
Once data is retrieved, you lose the original type:

```java
User user = snapshot.getValue(User.class); // Just a generic User
```

So you need a string marker (like role) to help decide how to reconstruct:

```java
if ("Organizer".equals(role)) {
    user = snapshot.getValue(Organizer.class);
}
```

## 🚫 Why Not Skip Subclasses?

You could stuff everything into a single `User` class, but that leads to:

- 🧹 **Messy, hard-to-maintain code**
- 🚫 **No clear logic separation**
- 🧨 **Problems as the app grows**

Subclasses like `Organizer` make the architecture **cleaner** and **easier to extend**.

---

## ✅ Summary

We store the `role` inside the object:

- ✅ Because **Firebase only saves what’s in the object**
- ✅ Because it helps us **rebuild the correct type later**
- ✅ Because it avoids **`instanceof` hacks** or **casting failures**

Not ideal **Object-Oriented Programming** — but **necessary and effective** for Firebase-based apps.

