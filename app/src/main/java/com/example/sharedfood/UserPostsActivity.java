package com.example.sharedfood;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class UserPostsActivity extends AppCompatActivity
        implements MyPostsAdapter.PostDeleteListener, MyPostsAdapter.PostEditListener {

    private static final String TAG = "UserPostsActivity"; // הגדרת תגית ללוגים של פעילות זו
    private RecyclerView postRecyclerView; // משתנה שמייצג את רשימת הפוסטים
    private MyPostsAdapter postAdapter; // אדפטר לניהול הפוסטים בתצוגה
    private FirebaseFirestore db; // חיבור למסד הנתונים Firestore
    private String userId; // ✅ הוספנו את המשתנה שחסר לך, מזהה המשתמש
    private List<Post> postList; // רשימה לאחסון הפוסטים של המשתמש

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_posts); // קביעת התצוגה שתהיה על המסך (XML המתאים)

        db = FirebaseFirestore.getInstance(); // אתחול חיבור למסד הנתונים Firestore
        postRecyclerView = findViewById(R.id.postRecyclerView); // מציאת רכיב ה-RecyclerView מתוך ה-XML
        postRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // הגדרת תצוגה ליניארית של הפוסטים ברשימה

        postList = new ArrayList<>(); // יצירת רשימה ריקה לאחסון הפוסטים

        // בדיקה אם המסך הנוכחי מציג פוסטים של אדמין או לא
        boolean isAdminView = getIntent().getBooleanExtra("isAdminView", false);
        postAdapter = new MyPostsAdapter(postList, this, this, isAdminView); // יצירת אדפטר עבור הפוסטים
        postRecyclerView.setAdapter(postAdapter); // חיבור האדפטר ל-RecyclerView

        // קבלת מזהה המשתמש מה-Firebase
        FirebaseAuth auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid(); // קבלת מזהה המשתמש הנוכחי מ-FirebaseAuth
        Log.d(TAG, "📥 Received userId: " + userId); // ✅ הדפסת מה שהתקבל מתוך ה-Intent

        // אם מזהה המשתמש ריק או לא הועבר, הצגת הודעת שגיאה ויציאה מהפעילות
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "שגיאה: מזהה משתמש חסר", Toast.LENGTH_SHORT).show();
            finish(); // סיום הפעילות אם לא קיבלנו מזהה תקין
            return; // יציאה מהמתודה (לא טוענים את הפוסטים)
        }

        Log.d(TAG, "📩 Received userId: " + userId); // הדפסת המזהה של המשתמש שהתקבל

        loadUserPosts(); // קריאה לפונקציה שתטען את הפוסטים של המשתמש
    }

    private void loadUserPosts() {
        // הדפסת מידע בלוג לפני ביצוע השאילתא
        Log.d(TAG, "🔍 Checking userId before querying Firestore: " + userId);

        // ביצוע שאילתא במסד הנתונים לפי מזהה המשתמש
        db.collection("posts")
                .whereEqualTo("userId", userId.trim()) // ✅ מחפש את הפוסטים של המשתמש על פי מזהה המשתמש
                .get()
                .addOnCompleteListener(task -> { // מאזין לתוצאה של השאילתא
                    if (task.isSuccessful()) { // אם השאילתא הצליחה
                        postList.clear(); // ניקוי הרשימה הקודמת לפני טעינת הפוסטים החדשים

                        if (task.getResult().isEmpty()) { // אם לא נמצאו פוסטים
                            Toast.makeText(this, "למשתמש זה אין פוסטים", Toast.LENGTH_SHORT).show(); // הצגת הודעה למשתמש
                        } else {
                            // אם יש פוסטים, נוסיף אותם לרשימה
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Post post = document.toObject(Post.class); // המרת המסמך לאובייקט מסוג Post
                                post.setId(document.getId()); // הגדרת ה-ID של הפוסט
                                postList.add(post); // הוספת הפוסט לרשימה
                            }
                        }

                        postAdapter.notifyDataSetChanged(); // עדכון האדפטר שהפוסטים השתנו
                    } else {
                        Toast.makeText(this, "שגיאה בטעינת הפוסטים", Toast.LENGTH_SHORT).show(); // הצגת הודעת שגיאה אם לא הצלחנו לשלוף את הפוסטים
                    }
                });
    }

    @Override
    public void onDeleteClick(Post post) {
        // מחיקת פוסט ממסד הנתונים
        db.collection("posts").document(post.getId()).delete()
                .addOnSuccessListener(aVoid -> loadUserPosts()) // אם המחיקה הצליחה, טוענים שוב את הפוסטים
                .addOnFailureListener(e -> Toast.makeText(this, "שגיאה במחיקת הפוסט", Toast.LENGTH_SHORT).show()); // הצגת הודעת שגיאה אם לא הצלחנו למחוק את הפוסט
    }

    @Override
    public void onEditClick(Post post) {
        // יצירת Intent למסך עריכת הפוסט
        Intent intent = new Intent(this, EditPostActivity.class);
        intent.putExtra("postId", post.getId()); // העברת מזהה הפוסט למטרה של עריכה
        startActivity(intent); // התחלת מסך העריכה
    }
}
