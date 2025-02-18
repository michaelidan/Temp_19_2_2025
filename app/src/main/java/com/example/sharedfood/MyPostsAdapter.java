package com.example.sharedfood;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.PostViewHolder> {
    private List<Post> posts;
    // Michael START 14.01.2025 SSSSSSSSSSSSSSSSSSSSSS
    private PostDeleteListener deleteListener;
    private PostEditListener editListener; // מאזין לעריכה בנפרד
    private boolean isAdminView; // האם מוצג במסך ניהול


    // ממשקים שמגדיר אירועים לכפתורי עריכה ומחיקה
    public interface PostEditListener {
        void onEditClick(Post post); // אירוע בעת לחיצה על עריכה
    }

    public interface PostDeleteListener {
        void onDeleteClick(Post post); // אירוע בעת לחיצה על מחיקה
    }


    // בנאי המקבל רשימת פוסטים ומאזין אחד לשני האירועים
    public MyPostsAdapter(List<Post> posts, PostDeleteListener deleteListener, PostEditListener editListener) {
        this.posts = posts;
        this.deleteListener = deleteListener;
        this.editListener = editListener;  // (Michael ADD 14.01.2025)
    }
    /*
    // בנאי המקבל רשימת פוסטים ומאזין אחד לשני האירועים
    public MyPostsAdapter(List<Post> posts, PostDeleteListener deleteListener, PostEditListener editListener) {
        this.posts = posts;
        this.deleteListener = deleteListener;
        this.editListener = editListener;  // (Michael ADD 14.01.2025)
    }
*/
    public MyPostsAdapter(List<Post> posts, PostDeleteListener deleteListener, PostEditListener editListener, boolean isAdminView) {
        this.posts = posts;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
        this.isAdminView = isAdminView; // Michael ADD 30.01.2025
    }

    // Michael END 14.01.2025  EEEEEEEEEEEEEEEEEEEEEEEEEEEEEE
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView descriptionText;
        TextView locationText;
        ChipGroup filtersChipGroup;
        ImageButton editPostButton;
        ImageButton deletePostButton;

        public PostViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.postImage);
            descriptionText = view.findViewById(R.id.postDescription);
            locationText = view.findViewById(R.id.postLocation);
            filtersChipGroup = view.findViewById(R.id.filtersChipGroup);
            editPostButton = view.findViewById(R.id.editPostButton);
            deletePostButton = view.findViewById(R.id.deletePostButton);
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);

        // תיאור
        holder.descriptionText.setText(post.getDescription() != null ? post.getDescription() : "תיאור לא זמין");

        // טיפול בתמונה
        if (post.getImageBitmap() != null) {
            holder.imageView.setImageBitmap(post.getImageBitmap());
            holder.imageView.setVisibility(View.VISIBLE);
        } else {
            holder.imageView.setVisibility(View.GONE);
        }

        // מיקום
        holder.locationText.setText(post.getCity() != null ? post.getCity() : "מיקום לא זמין");

        // סינונים
        holder.filtersChipGroup.removeAllViews();
        if (post.getFilters() != null && !post.getFilters().isEmpty()) {
            for (String filter : post.getFilters()) {
                Chip chip = new Chip(holder.filtersChipGroup.getContext());
                chip.setText(filter);
                chip.setCheckable(false);
                holder.filtersChipGroup.addView(chip);
            }
        } else {
            Chip chip = new Chip(holder.filtersChipGroup.getContext());
            chip.setText("אין סינונים");
            chip.setCheckable(false);
            holder.filtersChipGroup.addView(chip);
        }


        // כפתור עריכה (רק אם יש מאזין)
        if (editListener != null) {
            holder.editPostButton.setVisibility(View.VISIBLE);
            holder.editPostButton.setOnClickListener(v -> editListener.onEditClick(post));
        } else {
            holder.editPostButton.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return posts.size();
    }
}
