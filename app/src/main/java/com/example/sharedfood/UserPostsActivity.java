package com.example.sharedfood;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class UserPostsActivity extends AppCompatActivity implements MyPostsAdapter.PostDeleteListener {

    private static final String TAG = "UserPostsActivity";
    private RecyclerView postRecyclerView;
    private MyPostsAdapter postAdapter;
    private FirebaseFirestore db;
    private String userEmail;
    private List<Post> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_posts);

        db = FirebaseFirestore.getInstance();
        postRecyclerView = findViewById(R.id.postRecyclerView);
        postRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        postAdapter = new MyPostsAdapter(postList, this, null, true);
        postRecyclerView.setAdapter(postAdapter);

        userEmail = getIntent().getStringExtra("userEmail");
        loadUserPosts();
    }

    private void loadUserPosts() {
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "שגיאה: אימייל משתמש חסר", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("posts").whereEqualTo("userId", userEmail).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Post post = document.toObject(Post.class);
                            post.setId(document.getId());
                            postList.add(post);
                        }
                        postAdapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "Failed to load posts", task.getException());
                        Toast.makeText(this, "שגיאה בטעינת הפוסטים", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDeleteClick(Post post) {
        db.collection("posts").document(post.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "הפוסט נמחק בהצלחה", Toast.LENGTH_SHORT).show();
                    loadUserPosts(); // רענון הרשימה
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting post", e);
                    Toast.makeText(this, "שגיאה במחיקת הפוסט", Toast.LENGTH_SHORT).show();
                });
    }
}
