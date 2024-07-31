package ganainy.dev.gymmasters.ui.main.exercisesCategories;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ganainy.dev.gymmasters.databinding.CategoryItemBinding;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder> {

    private List<Pair<String, Drawable>> categoryList;
    private final CategoryCallback categoryCallback;

    public CategoriesAdapter(CategoryCallback categoryCallback) {
        this.categoryCallback = categoryCallback;
    }

    public void setData(List<Pair<String, Drawable>> categoryList) {
        this.categoryList = categoryList;
        notifyDataSetChanged(); // Notify adapter of data changes
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CategoryItemBinding binding = CategoryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Pair<String, Drawable> currentCategory = categoryList.get(position);

        if (currentCategory.first != null) {
            holder.binding.textViewCategoryName.setText(currentCategory.first);
        }

        if (currentCategory.second != null) {
            holder.binding.categoryImageView.setImageDrawable(currentCategory.second);
        }

        holder.itemView.setOnClickListener(view -> {
            categoryCallback.onCategorySelected(currentCategory.first);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList == null ? 0 : categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final CategoryItemBinding binding;

        public CategoryViewHolder(@NonNull CategoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}