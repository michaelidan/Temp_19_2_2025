package com.example.sharedfood;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> userList;

    private final UserActionListener actionListener;
    private final OnUserClickListener onUserClickListener;


    public interface UserActionListener {
        void onAction(User user, String action);
    }

    public interface OnUserClickListener {
        void onUserClick(User user);
    }


    //    public UserAdapter(List<User> userList, UserActionListener listener) {
//        this.userList = userList;
//        this.listener = listener;
//    }
    public UserAdapter(List<User> userList, UserActionListener actionListener, OnUserClickListener onUserClickListener) {
        this.userList = userList;
        this.actionListener = actionListener;
        this.onUserClickListener = onUserClickListener;
    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.emailTextView.setText(user.getEmail());

        // זיהוי אם אנחנו ב- UserPostListActivity
        boolean isPostListActivity = onUserClickListener != null;

        if (isPostListActivity) {
            holder.viewPostsButton.setVisibility(View.VISIBLE);
            holder.viewPostsButton.setOnClickListener(v -> onUserClickListener.onUserClick(user));
            holder.promoteButton.setVisibility(View.GONE); // מסתיר את כפתור המנהל
        } else {
            holder.viewPostsButton.setVisibility(View.GONE);
            holder.promoteButton.setVisibility(View.VISIBLE);
            holder.promoteButton.setOnClickListener(v -> actionListener.onAction(user, "promote"));
        }

        holder.banButton.setText(user.isBanned() ? "בטל חסימה" : "חסום");
        holder.banButton.setOnClickListener(v -> actionListener.onAction(user, "ban"));

        holder.tempBanButton.setOnClickListener(v -> actionListener.onAction(user, "temp_ban"));
    }



    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateUsers(List<User> updatedList) {
        userList.clear();
        userList.addAll(updatedList);
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView;
        Button banButton, tempBanButton, promoteButton, viewPostsButton; // הוספנו את viewPostsButton

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.userEmailTextView);
            banButton = itemView.findViewById(R.id.banUserButton);
            tempBanButton = itemView.findViewById(R.id.tempBanUserButton);
            promoteButton = itemView.findViewById(R.id.promoteUserButton);
            viewPostsButton = itemView.findViewById(R.id.viewPostsButton); // אתחול כפתור "פוסטים"
        }
    }

}
