package ganainy.dev.gymmasters.ui.main.posts.postComments;

import android.view.View;

import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.CommentItemBinding;
import ganainy.dev.gymmasters.databinding.EmptyCommentsItemBinding;
import ganainy.dev.gymmasters.models.app_models.Comment;
import ganainy.dev.gymmasters.models.app_models.User;

public class EmptyCommentsViewHolder extends RecyclerView.ViewHolder {
    private final EmptyCommentsItemBinding binding;

    public EmptyCommentsViewHolder(View view) {
        super(view);
        binding = EmptyCommentsItemBinding.bind(view);
    }

}