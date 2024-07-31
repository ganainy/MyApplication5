package ganainy.dev.gymmasters.ui.findUser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.UserItemBinding;
import ganainy.dev.gymmasters.models.app_models.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private final Context context;
    private final UserCallback userCallback;

    public UserAdapter(Context context, UserCallback userCallback) {
        this.context = context;
        this.userCallback = userCallback;
    }

    public void setData(List<User> userList) {
        this.userList = userList;
        notifyDataSetChanged(); // Notify adapter of data changes
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UserItemBinding binding = UserItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User currentUser = userList.get(position);

        holder.binding.nameEditText.setText(currentUser.getName());

        if (currentUser.getPhoto() != null) {
            Glide.with(context).load(currentUser.getPhoto()).into(holder.binding.userImageView);
        }

        if (currentUser.getFollowers() != null) {
            holder.binding.followerCountShimmer.setText(context.getString(R.string.followers_count, currentUser.getFollowers()));
        }

        if (currentUser.getRating() != null) {
            holder.binding.ratingBar.setRating(currentUser.getRating());
        }

        holder.itemView.setOnClickListener(view -> {
            userCallback.onUserClicked(currentUser, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return userList == null ? 0 : userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private final UserItemBinding binding;

        public UserViewHolder(@NonNull UserItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}