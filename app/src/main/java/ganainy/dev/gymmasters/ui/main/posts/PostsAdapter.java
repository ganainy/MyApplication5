package ganainy.dev.gymmasters.ui.main.posts;

import android.app.Application;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.PostExerciseItemBinding;
import ganainy.dev.gymmasters.databinding.PostWorkoutItemBinding;
import ganainy.dev.gymmasters.models.app_models.Exercise;
import ganainy.dev.gymmasters.models.app_models.Post;
import ganainy.dev.gymmasters.models.app_models.Workout;
import ganainy.dev.gymmasters.utils.AuthUtils;

public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_WORKOUT = 0;
    private static final int TYPE_EXERCISE = 1;

    private final Application app;
    private List<Post> postList;
    private final PostCallback postCallback;

    public PostsAdapter(Application app, PostCallback postCallback) {
        this.app = app;
        this.postCallback = postCallback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_EXERCISE) {
            PostExerciseItemBinding binding = PostExerciseItemBinding.inflate(LayoutInflater.from(app), viewGroup, false);
            return new PostExerciseViewHolder(binding);
        } else {
            PostWorkoutItemBinding binding = PostWorkoutItemBinding.inflate(LayoutInflater.from(app), viewGroup, false);
            return new PostWorkoutViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == TYPE_EXERCISE) {
            ((PostExerciseViewHolder) viewHolder).bind(postList.get(position));
        } else {
            ((PostWorkoutViewHolder) viewHolder).bind(postList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return postList == null ? 0 : postList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return postList.get(position).getEntityType() == 0 ? TYPE_EXERCISE : TYPE_WORKOUT;
    }

    public void setData(List<Post> postList) {
        this.postList = postList;
        notifyDataSetChanged();
    }

    class PostExerciseViewHolder extends RecyclerView.ViewHolder {

        private final PostExerciseItemBinding binding;

        public PostExerciseViewHolder(PostExerciseItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Post post) {
            Exercise exercise = post.getExercise();

            Glide.with(app)
                    .load(exercise.getCreatorImageUrl())
                    .apply(new RequestOptions().placeholder(R.drawable.loading_animation).error(R.drawable.anonymous_profile))
                    .circleCrop()
                    .into(binding.profileImageView);

            binding.userNameTextView.setText(exercise.getCreatorName());
            binding.dateTextView.setText(new PrettyTime().format(new Date(Long.parseLong(exercise.getDate()))));

            Glide.with(app)
                    .load(exercise.getPreviewPhotoOneUrl())
                    .apply(new RequestOptions().placeholder(R.drawable.loading_animation).error(R.drawable.ic_exercise_grey))
                    .into(binding.exerciseOneImageView);

            Glide.with(app)
                    .load(exercise.getPreviewPhotoTwoUrl())
                    .apply(new RequestOptions().placeholder(R.drawable.loading_animation).error(R.drawable.ic_exercise_2_grey))
                    .into(binding.exerciseTwoImageView);

            binding.exerciseNameTextView.setText(exercise.getName());
            binding.targetMuscleTextView.setText(exercise.getBodyPart());

            binding.commentCountTextView.setText(String.valueOf(exercise.getCommentList() != null ? exercise.getCommentList().size() : 0));
            binding.likeCountTextView.setText(String.valueOf(exercise.getLikerIdList() != null ? exercise.getLikerIdList().size() : 0));

            binding.likeImage.setImageResource(exercise.getLikerIdList() != null && exercise.getLikerIdList().contains(AuthUtils.getLoggedUserId(app))
                    ? R.drawable.ic_like_blue : R.drawable.ic_like_grey);

            binding.likeButton.setOnClickListener(v -> postCallback.onPostLike(post, getAdapterPosition()));
            binding.commentButton.setOnClickListener(v -> postCallback.onPostComment(post, 0));
            binding.exerciseNameTextView.setOnClickListener(v -> postCallback.onExerciseClicked(exercise, getAdapterPosition()));
            binding.exerciseOneImageView.setOnClickListener(v -> postCallback.onExerciseClicked(exercise, getAdapterPosition()));
            binding.exerciseTwoImageView.setOnClickListener(v -> postCallback.onExerciseClicked(exercise, getAdapterPosition()));
            binding.profileImageView.setOnClickListener(v -> postCallback.onUserClicked(exercise.getCreatorId()));
            binding.userNameTextView.setOnClickListener(v -> postCallback.onUserClicked(exercise.getCreatorId()));
        }
    }

    class PostWorkoutViewHolder extends RecyclerView.ViewHolder {

        private final PostWorkoutItemBinding binding;

        public PostWorkoutViewHolder(PostWorkoutItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Post post) {
            Workout workout = post.getWorkout();

            Glide.with(app)
                    .load(workout.getCreatorImageUrl())
                    .apply(new RequestOptions().placeholder(R.drawable.loading_animation).error(R.drawable.anonymous_profile))
                    .circleCrop()
                    .into(binding.profileImageView);

            binding.userNameTextView.setText(workout.getCreatorName());
            binding.workoutNameTextView.setText(workout.getName());
            binding.dateTextView.setText(new PrettyTime().format(new Date(Long.parseLong(workout.getDate()))));

            Glide.with(app)
                    .load(workout.getPhotoLink())
                    .apply(new RequestOptions().placeholder(R.drawable.loading_animation).error(R.drawable.ic_exercise_grey))
                    .into(binding.workoutImageView);

            binding.workoutDurationTextView.setText(workout.getDuration());
            binding.workoutDifficultyTextView.setText(workout.getLevel());

            binding.commentCountTextView.setText(String.valueOf(workout.getCommentList() != null ? workout.getCommentList().size() : 0));
            binding.likeCountTextView.setText(String.valueOf(workout.getLikerIdList() != null ? workout.getLikerIdList().size() : 0));

            binding.likeImage.setImageResource(workout.getLikerIdList() != null && workout.getLikerIdList().contains(AuthUtils.getLoggedUserId(app))
                    ? R.drawable.ic_like_blue : R.drawable.ic_like_grey);

            int difficultyResId;
            int difficultyBgResId;
            switch (workout.getLevel().toLowerCase()) {
                case "beginner":
                    difficultyResId = R.string.beginner;
                    difficultyBgResId = R.drawable.ic_triangle_green;
                    break;
                case "intermediate":
                    difficultyResId = R.string.intermediate;
                    difficultyBgResId = R.drawable.ic_triangle_yellow;
                    break;
                case "professional":
                    difficultyResId = R.string.professional;
                    difficultyBgResId = R.drawable.ic_triangle_red;
                    break;
                default:
                    difficultyResId = R.string.unknown; // Fallback
                    difficultyBgResId = R.drawable.ic_triangle_grey; // Fallback
                    break;
            }
            binding.workoutDifficultyTextView.setText(difficultyResId);
            binding.difficultyBackgroundImageView.setImageResource(difficultyBgResId);

            binding.likeButton.setOnClickListener(v -> postCallback.onPostLike(post, getAdapterPosition()));
            binding.commentButton.setOnClickListener(v -> postCallback.onPostComment(post, 1));
            binding.workoutNameTextView.setOnClickListener(v -> postCallback.onWorkoutClicked(workout, getAdapterPosition()));
            binding.workoutImageView.setOnClickListener(v -> postCallback.onWorkoutClicked(workout, getAdapterPosition()));
            binding.profileImageView.setOnClickListener(v -> postCallback.onUserClicked(workout.getCreatorId()));
            binding.userNameTextView.setOnClickListener(v -> postCallback.onUserClicked(workout.getCreatorId()));
        }
    }
}