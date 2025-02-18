package com.example.sharedfood;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.sharedfood.User;

public class UserListActivity extends AppCompatActivity {

    private static final String TAG = "UserListActivity"; // תג לזיהוי הודעות לוג
    private RecyclerView userRecyclerView; // רכיב להצגת רשימת המשתמשים
    private UserAdapter userAdapter; // אדפטר להצגת הנתונים ברשימה
    private FirebaseFirestore db; // חיבור למסד הנתונים Firestore
    private FirebaseAuth mAuth; // ניהול אימות המשתמשים
    private final Handler handler = new Handler();
    private final Runnable removeExpiredBansTask = new Runnable() {
        @Override
        public void run() {
            checkAndRemoveExpiredTempBans();
            handler.postDelayed(this, 5 * 60 * 1000); // פועל כל 5 דקות
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(removeExpiredBansTask); // התחלת המשימה המתוזמנת
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(removeExpiredBansTask); // הפסקת המשימה
    }

    // ✅ פונקציה לפתיחת רשימת הפוסטים של המשתמש שנבחר
    public void viewUserPosts(User user) {
        Intent intent = new Intent(UserListActivity.this, UserPostsActivity.class);
        intent.putExtra("userEmail", user.getEmail());
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list); // קביעת ממשק המשתמש מתוך קובץ ה-XML של הפעילות

        // אתחול Firebase Firestore לטיפול במסד הנתונים בענן
        db = FirebaseFirestore.getInstance();

        // אתחול Firebase Authentication לטיפול באימות המשתמשים
        mAuth = FirebaseAuth.getInstance();

        // אתחול ה-RecyclerView להצגת רשימת המשתמשים
        userRecyclerView = findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ✅ מאזין ללחיצה על משתמש, מוביל לרשימת הפוסטים שלו
        userAdapter = new UserAdapter(new ArrayList<>(), this::performActionOnUser, this::viewUserPosts);
        userRecyclerView.setAdapter(userAdapter);



        // טעינת המשתמשים מתוך מסד הנתונים והצגתם ברשימה
        loadUsers();
    }






    private void loadUsers() {
        // אתחול חיבור ל-Firebase Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // בדיקה והסרת חסימות זמניות שפג תוקפן
        checkAndRemoveExpiredTempBans();

        // שליפת כל המנהלים מתוך מסד הנתונים
        db.collection("admins").get().addOnCompleteListener(adminsTask -> {
            if (adminsTask.isSuccessful()) {
                List<String> adminEmails = new ArrayList<>(); // רשימה לשמירת כתובות האימייל של המנהלים
                adminsTask.getResult().forEach(admin -> adminEmails.add(admin.getId())); // הוספת כל מנהל לרשימה

                // שליפת כל המשתמשים ממסד הנתונים
                db.collection("users").get().addOnCompleteListener(usersTask -> {
                    if (usersTask.isSuccessful()) {
                        List<User> userList = new ArrayList<>(); // יצירת רשימה לשמירת פרטי המשתמשים
                        usersTask.getResult().forEach(document -> {
                            String email = document.getId(); // שליפת כתובת האימייל של המשתמש

                            // בדיקה האם המשתמש אינו מנהל - אם הוא לא נמצא ברשימת המנהלים
                            if (!adminEmails.contains(email)) {
                                // בדיקה האם המשתמש חסום
                                boolean isBanned = document.getBoolean("is_banned") != null && document.getBoolean("is_banned");

                                // בדיקה האם יש למשתמש חסימה זמנית ושמירת זמן סיום החסימה
                                Long tempBanTime = document.contains("temp_ban_time") ? document.getLong("temp_ban_time") : null;

                                // יצירת אובייקט משתמש והוספתו לרשימה
                                userList.add(new User(email, isBanned, tempBanTime));
                            }
                        });

                        // עדכון הנתונים באדפטר כדי להציג את המשתמשים ברשימה
                        userAdapter.updateUsers(userList);
                    } else {
                        // הדפסת שגיאה ללוג במקרה של כישלון בשליפת המשתמשים
                        Log.e(TAG, "Failed to load users", usersTask.getException());
                    }
                });
            } else {
                // הדפסת שגיאה ללוג במקרה של כישלון בשליפת המנהלים
                Log.e(TAG, "Failed to load admins", adminsTask.getException());
            }
        });
    }


    private void performActionOnUser(User user, String action) {
        // ניתוב הפעולה המתבקשת
        switch (action) {
            case "ban":
                banUser(user);
                break;
            case "temp_ban":
                showTempBanDialog(user); // הצגת דיאלוג לבחירת משך החסימה
                break;
            case "promote":
                promoteToAdmin(user);
                break;
        }
    }

    private void showTempBanDialog(User user) {
        // בדיקה אם המשתמש כבר חסום זמנית
        db.collection("temp_banned_users").document(user.getEmail()).get().addOnSuccessListener(document -> {
            if (document.exists()) {
                // המשתמש כבר חסום זמנית - מבטלים את החסימה באופן מיידי
                cancelTempBan(user);
            } else {
                // המשתמש אינו חסום זמנית - הצגת דיאלוג לחסימה
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("בחר את משך החסימה")
                        .setItems(new CharSequence[]{"3 שעות", "24 שעות", "3 ימים", "שבוע", "חודש", "חצי דקה (לבדיקות בלבד)"}, (dialog, which) -> {
                            long durationInHours = 0;

                            // חישוב משך החסימה לפי הבחירה
                            switch (which) {
                                case 0: durationInHours = 3; break; // 3 שעות
                                case 1: durationInHours = 24; break; // יום אחד
                                case 2: durationInHours = 72; break; // 3 ימים
                                case 3: durationInHours = 168; break; // שבוע
                                case 4: durationInHours = 720; break; // חודש
                                case 5: durationInHours = 1; break; // חצי דקה (לבדיקות בלבד)
                            }

                            ////////////////////////////////////////////
                            // ניהול זמן החסימה - חישוב מילישניות
                            // קטע קוד זה נועד לבדוק חסימה זמנית קצרה (חצי דקה בלבד)
                            long durationInMillis;
                            if (durationInHours == 1) {
                                durationInMillis = 30 * 1000; // חצי דקה
                            } else {
                                durationInMillis = durationInHours * 60 * 60 * 1000; // חישוב בשעות
                            }
                            // הערה: חובה להסיר את אפשרות "חצי דקה" לאחר הבדיקות
                            ////////////////////////////////////////////

                            tempBanUser(user, durationInMillis); // קריאה לפונקציה שמחילה את החסימה הזמנית
                        })
                        .setNegativeButton("ביטול", null)
                        .show();
            }
        });
    }


    private void banUser(User user) {
        boolean isBanned = user.isBanned(); // בדיקה אם המשתמש כרגע חסום
        db.collection("users").document(user.getEmail())
                .update("is_banned", !isBanned) // שינוי סטטוס החסימה
                .addOnSuccessListener(aVoid -> {
                    if (isBanned) {
                        // הסרת המשתמש מאוסף החסומים
                        db.collection("banned_users").document(user.getEmail())
                                .delete()
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(this, "החסימה בוטלה בהצלחה", Toast.LENGTH_SHORT).show();
                                    loadUsers();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error removing user from banned_users", e);
                                    Toast.makeText(this, "שגיאה בהסרת המשתמש", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // הוספת המשתמש לאוסף החסומים
                        Map<String, Object> bannedData = new HashMap<>();
                        bannedData.put("email", user.getEmail());
                        bannedData.put("banned_at", System.currentTimeMillis());

                        db.collection("banned_users").document(user.getEmail())
                                .set(bannedData)
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(this, "המשתמש נחסם בהצלחה", Toast.LENGTH_SHORT).show();
                                    loadUsers();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error adding user to banned_users", e);
                                    Toast.makeText(this, "שגיאה בהוספת המשתמש לחסומים", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user ban status", e);
                    Toast.makeText(this, "שגיאה בעדכון סטטוס המשתמש", Toast.LENGTH_SHORT).show();
                });
    }

    private void tempBanUser(User user, long durationInHours) {
        long currentTimeMillis = System.currentTimeMillis();
        long banEndTimeMillis = currentTimeMillis + (durationInHours * 60 * 60 * 1000); // חישוב זמן פקיעת החסימה
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // עדכון המשתמש באוסף temp_banned_users
        Map<String, Object> tempBanData = new HashMap<>();
        tempBanData.put("email", user.getEmail());
        tempBanData.put("ban_end_time", banEndTimeMillis);

        db.collection("temp_banned_users")
                .document(user.getEmail())
                .set(tempBanData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "משתמש נחסם זמנית", Toast.LENGTH_SHORT).show();
                    loadUsers(); // עדכון הרשימה
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error applying temporary ban", e);
                    Toast.makeText(this, "שגיאה בחסימה זמנית", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkAndRemoveExpiredTempBans() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        long currentTimeMillis = System.currentTimeMillis();

        db.collection("temp_banned_users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    querySnapshot.forEach(document -> {
                        Long banEndTime = document.getLong("ban_end_time");
                        if (banEndTime != null && banEndTime < currentTimeMillis) {
                            // הסרת המשתמש מאוסף temp_banned_users
                            db.collection("temp_banned_users").document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "חסימה זמנית הסתיימה עבור: " + document.getId()))
                                    .addOnFailureListener(e -> Log.e(TAG, "שגיאה בהסרת חסימה זמנית עבור: " + document.getId(), e));
                        }
                    });
                    // לאחר הסרת חסימות שפגו, עדכון רשימת המשתמשים
                    loadUsers();
                })
                .addOnFailureListener(e -> Log.e(TAG, "שגיאה בגישה לאוסף temp_banned_users", e));
    }
    private void cancelTempBan(User user) {
        db.collection("temp_banned_users").document(user.getEmail()).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "החסימה הזמנית בוטלה", Toast.LENGTH_SHORT).show();
                    loadUsers(); // עדכון הרשימה
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error canceling temporary ban", e);
                    Toast.makeText(this, "שגיאה בביטול החסימה הזמנית", Toast.LENGTH_SHORT).show();
                });
    }

    private void promoteToAdmin(User user) {
        Log.d(TAG, "promoteToAdmin: Trying to promote " + user.getEmail()); // לצורך בדיקה

        // הכנת נתוני המנהל
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("email", user.getEmail());
        adminData.put("isSuperAdmin", false); // התאמה לפי הלוגיקה שלך

        // הוספת המשתמש לאוסף המנהלים
        db.collection("admins").document(user.getEmail())
                .set(adminData) // סיריאליזציה נכונה של נתוני המנהל
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "המשתמש הועלה לדרגת מנהל", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "promoteToAdmin: Success for " + user.getEmail()); // לצורך בדיקה
                    loadUsers(); // טעינה מחדש של הרשימה
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "promoteToAdmin: Error for " + user.getEmail(), e); // לצורך בדיקה
                    Toast.makeText(this, "שגיאה בהפיכת המשתמש למנהל", Toast.LENGTH_SHORT).show();
                });
    }
}