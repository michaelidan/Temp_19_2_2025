package com.example.sharedfood;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> userList; // רשימת המשתמשים שיוצגו ברשימה

    private final UserActionListener actionListener; // ממשק לפעולות (חסימה, קידום)
    private final OnUserClickListener onUserClickListener; // ממשק לצפייה בפוסטים של המשתמשים

    // ממשק לפעולות אדמין (למשל חסימה, קידום)
    public interface UserActionListener {
        void onAction(User user, String action);
    }

    // ממשק ללחיצה על משתמש (למשל, צפייה בפוסטים שלו)
    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    // בנאי של האדפטר שמקבל רשימת משתמשים ושני מאזינים לפעולות
    public UserAdapter(List<User> userList, UserActionListener actionListener, OnUserClickListener onUserClickListener) {
        this.userList = userList;
        this.actionListener = actionListener;
        this.onUserClickListener = onUserClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // יצירת תצוגה עבור כל פריט ברשימה (item_user.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position); // קבלת המשתמש הנוכחי
        holder.emailTextView.setText(user.getEmail()); // הצגת האימייל של המשתמש

        // בדיקה האם המסך הנוכחי הוא UserPostListActivity
        boolean isPostListActivity = onUserClickListener != null;

        if (isPostListActivity) {
            Log.d("UserAdapter", "📌 מצב רשימת פוסטים - הצגת כפתור פוסטים למשתמש: " + user.getEmail());
            holder.viewPostsButton.setVisibility(View.VISIBLE);
            holder.viewPostsButton.setOnClickListener(v -> {
                Log.d("UserAdapter", "👆 כפתור פוסטים נלחץ עבור: " + user.getEmail());
                onUserClickListener.onUserClick(user);
            });
            holder.promoteButton.setVisibility(View.GONE); // מסתיר את כפתור המנהל
        } else {
            Log.d("UserAdapter", "📌 מצב רגיל - הצגת כפתור 'הפוך למנהל' למשתמש: " + user.getEmail());
            holder.viewPostsButton.setVisibility(View.GONE);
            holder.promoteButton.setVisibility(View.VISIBLE);
            holder.promoteButton.setOnClickListener(v -> actionListener.onAction(user, "promote"));
        }


        // קביעת טקסט לכפתור החסימה בהתאם למצב המשתמש
        holder.banButton.setText(user.isBanned() ? "בטל חסימה" : "חסום");
        holder.banButton.setOnClickListener(v -> actionListener.onAction(user, "ban")); // הפעלת אירוע חסימה

        // כפתור חסימה זמנית
        holder.tempBanButton.setOnClickListener(v -> actionListener.onAction(user, "temp_ban"));
    }

    @Override
    public int getItemCount() {
        return userList.size(); // מחזיר את מספר המשתמשים ברשימה
    }

    // פונקציה לעדכון רשימת המשתמשים ולהתראה לאדפטר שיש להציג נתונים חדשים
    public void updateUsers(List<User> updatedList) {
        userList.clear(); // ניקוי הרשימה הקיימת
        userList.addAll(updatedList); // הוספת הרשימה החדשה
        notifyDataSetChanged(); // עדכון האדפטר שהנתונים השתנו
    }

    // מחלקה פנימית לניהול התצוגה של כל פריט ברשימה
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView; // תצוגת טקסט לאימייל המשתמש
        Button banButton, tempBanButton, promoteButton, viewPostsButton; // כפתורים לפעולות שונות

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.userEmailTextView); // חיפוש מזהה אימייל
            banButton = itemView.findViewById(R.id.banUserButton); // כפתור חסימה
            tempBanButton = itemView.findViewById(R.id.tempBanUserButton); // כפתור חסימה זמנית
            promoteButton = itemView.findViewById(R.id.promoteUserButton); // כפתור קידום למנהל
            viewPostsButton = itemView.findViewById(R.id.viewPostsButton); // כפתור צפייה בפוסטים
        }
    }
}
