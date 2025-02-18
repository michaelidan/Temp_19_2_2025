package com.example.sharedfood;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class PostsOfUserActivity extends AppCompatActivity {

    private static final String TAG = "PostsOfUserActivity"; // תגית עבור לוגים לניטור הדפסות
    private RecyclerView postRecyclerView; // רכיב להצגת רשימת הפוסטים של המשתמש
    private MyPostsAdapter postAdapter; // אדפטר לטיפול בפוסטים שיוצגו ברשימה
    private FirebaseFirestore db; // חיבור למסד הנתונים Firestore
    private String userId; // מזהה המשתמש שממנו נטען הפוסטים
    private List<Post> postList; // רשימה לאחסון הפוסטים של המשתמש

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_of_user); // קביעת תצוגת ה-XML המתאימה למסך

        db = FirebaseFirestore.getInstance(); // אתחול חיבור למסד הנתונים Firestore
        postRecyclerView = findViewById(R.id.postRecyclerView); // איתור ה-RecyclerView מתוך ה-XML
        postRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // הגדרת תצוגת הרשימה כ-LinearLayout (רשימה אנכית)

        postList = new ArrayList<>(); // יצירת רשימה ריקה לאחסון הפוסטים
        postAdapter = new MyPostsAdapter(postList, null, null, false); // אתחול האדפטר עם הרשימה (ללא אפשרות עריכה)
        postRecyclerView.setAdapter(postAdapter); // חיבור האדפטר ל-RecyclerView

        // מקבלים את userId שהועבר מהאקטיביטי הקודם דרך Intent
        userId = getIntent().getStringExtra("userId");

        Log.d(TAG, "📥 Received userId: " + userId); // הדפסת המזהה שהתקבל לצורך דיבוג

        // אם מזהה המשתמש לא נמצא או ריק, יש להציג הודעת שגיאה ולסיים את האקטיביטי
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "שגיאה: מזהה משתמש חסר", Toast.LENGTH_SHORT).show();
            finish(); // סגירת המסך הנוכחי
            return; // יציאה מהמתודה
        }

        loadUserPosts(); // קריאה לפונקציה שמביאה את הפוסטים של המשתמש מהמסד
    }

    private void loadUserPosts() {
        Log.d(TAG, "🔍 Fetching posts for userId: " + userId); // הדפסת הודעה ללוגים על תחילת תהליך השליפה

        // ביצוע שאילתא ל-Firestore כדי להביא את כל הפוסטים של המשתמש לפי userId
        db.collection("posts")
                .whereEqualTo("userId", userId) // מבצע חיפוש לפי userId
                .orderBy("timestamp", Query.Direction.DESCENDING) // מיון הפוסטים לפי תאריך (מהחדש לישן)
                .get()
                .addOnCompleteListener(task -> { // מאזין לתוצאה של השאילתא
                    if (task.isSuccessful()) { // בדיקה אם השליפה מהמסד הצליחה
                        postList.clear(); // ניקוי הרשימה לפני טעינת הפוסטים החדשים

                        // אם אין פוסטים עבור המשתמש
                        if (task.getResult().isEmpty()) {
                            Log.d(TAG, "⚠️ No posts found for userId: " + userId); // הדפסת הודעה ללוג
                            Toast.makeText(this, "למשתמש זה אין פוסטים", Toast.LENGTH_SHORT).show(); // הודעה למשתמש
                        } else {
                            // מעבר על כל המסמכים שהתקבלו מהמסד והוספתם לרשימה
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Post post = document.toObject(Post.class); // המרת המסמך לאובייקט מסוג Post
                                post.setId(document.getId()); // שמירת ה-ID של הפוסט מתוך מסד הנתונים
                                postList.add(post); // הוספת הפוסט לרשימה
                                Log.d(TAG, "✅ Loaded post ID: " + post.getId() + ", Description: " + post.getDescription()); // הדפסת לוג עם מזהה ותיאור הפוסט
                            }
                        }

                        postAdapter.notifyDataSetChanged(); // עדכון האדפטר עם הנתונים החדשים
                        Log.d(TAG, "🔄 Adapter updated with " + postList.size() + " posts."); // הדפסת כמות הפוסטים שהועלו
                    } else {
                        Log.e(TAG, "❌ Failed to load posts", task.getException()); // במקרה של שגיאה, הדפסת השגיאה ללוג
                        Toast.makeText(this, "שגיאה בטעינת הפוסטים", Toast.LENGTH_SHORT).show(); // הצגת הודעת שגיאה למשתמש
                    }
                });
    }
}
