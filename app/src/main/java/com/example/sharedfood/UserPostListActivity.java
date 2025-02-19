package com.example.sharedfood;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class UserPostListActivity extends AppCompatActivity {

    private RecyclerView userRecyclerView; // רשימה להצגת המשתמשים
    private UserAdapter userAdapter; // אדפטר לתצוגת המשתמשים
    private FirebaseFirestore db; // חיבור למסד הנתונים Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_post_list); // קביעת קובץ ה-XML המתאים

        db = FirebaseFirestore.getInstance(); // אתחול Firestore כדי לשלוף נתונים
        userRecyclerView = findViewById(R.id.userRecyclerView); // מציאת ה-RecyclerView מתוך ה-XML
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // הגדרת תצוגה ליניארית (רשימה אנכית)

        // אתחול האדפטר עם רשימה ריקה ופעולת צפייה בפוסטים של המשתמש
        userAdapter = new UserAdapter(new ArrayList<>(), null, this::viewUserPosts);
        userRecyclerView.setAdapter(userAdapter); // חיבור האדפטר ל-RecyclerView

        loadUsers(); // קריאה לפונקציה שתביא את רשימת המשתמשים מהמסד
    }

    private void loadUsers() {
        // שליפת רשימת המשתמשים ממסד הנתונים Firestore
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) { // בדיקה אם השליפה הצליחה
                List<User> userList = new ArrayList<>(); // יצירת רשימה חדשה של משתמשים

                // מעבר על כל המסמכים שהתקבלו מהמסד
                task.getResult().forEach(document -> {
                    String userId = document.getId(); // 🔴 קבלת userId מה-Firestore
                    String email = document.getString("email"); // שליפת האימייל של המשתמש
                    boolean isBanned = document.getBoolean("isBanned") != null && document.getBoolean("isBanned"); // בדיקה אם המשתמש חסום
                    Long tempBanTime = document.getLong("tempBanTime"); // קבלת זמן חסימה זמני, אם קיים

                    // יצירת אובייקט משתמש והוספתו לרשימה
                    userList.add(new User(userId, email, isBanned, tempBanTime));
                });

                userAdapter.updateUsers(userList); // עדכון האדפטר עם הרשימה החדשה של המשתמשים
            }
        });
    }

    private void viewUserPosts(User user) {
        Log.d("UserPostListActivity", "📌 Sending userId: " + user.getId()); // הדפסת מזהה המשתמש ללוג

        Intent intent = new Intent(this, UserPostsActivity.class); // PostsOfUserActivity -> UserPostsActivity
        // יצירת מעבר למסך הצגת הפוסטים של המשתמש
        intent.putExtra("userId", user.getId()); // שליחת userId דרך Intent למסך הבא
        startActivity(intent); // התחלת המסך החדש
    }
}
