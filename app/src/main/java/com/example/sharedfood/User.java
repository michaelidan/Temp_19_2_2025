package com.example.sharedfood;

public class User {

    private String id; // 🔴 נשתמש ב-id כי זה מזהה המשתמש בפיירסטור
    private String email;
    private boolean isBanned;
    private Long tempBanTime;


    // Constructor with three parameters
    public User(String email, boolean isBanned, Long tempBanTime) {
        this.email = email;
        this.isBanned = isBanned;
        this.tempBanTime = tempBanTime;
    }

    // Constructor with two parameters
    public User(String email, boolean isBanned) {
        this.email = email;
        this.isBanned = isBanned;
        this.tempBanTime = null; // Default to null
    }

    // 🔹 בנאי מלא
    public User(String id, String email, boolean isBanned, Long tempBanTime) {
        this.id = id;  // 🔴 שומר את ה-ID הנכון
        this.email = email;
        this.isBanned = isBanned;
        this.tempBanTime = tempBanTime;
    }

    // 🔹 בנאי עבור Firestore (חובה)
    public User() {}


    // Getters and Setters

    //public String getId() { return userId; } // ✅ מחזיר את ה-ID הנכון


    public String getId() { return id; }


    public void setId(String id) { this.id = id; }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isBanned() {
        return isBanned;
    }

    public void setBanned(boolean banned) {
        isBanned = banned;
    }

    public Long getTempBanTime() {
        return tempBanTime;
    }

    public void setTempBanTime(Long tempBanTime) {
        this.tempBanTime = tempBanTime;
    }
}
