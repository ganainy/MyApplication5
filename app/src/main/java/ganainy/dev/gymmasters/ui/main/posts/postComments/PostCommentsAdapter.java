package ganainy.dev.gymmasters.ui.main.posts.postComments;

import android.app.Application;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.CommentItemBinding;
import ganainy.dev.gymmasters.databinding.EmptyCommentsItemBinding;
import ganainy.dev.gymmasters.databinding.LoadingItemBinding;
import ganainy.dev.gymmasters.databinding.PostExerciseItemBinding;
import ganainy.dev.gymmasters.databinding.PostWorkoutItemBinding;
import ganainy.dev.gymmasters.models.app_models.Comment;
import ganainy.dev.gymmasters.models.app_models.Exercise;
import ganainy.dev.gymmasters.models.app_models.User;
import ganainy.dev.gymmasters.models.app_models.Workout;
import ganainy.dev.gymmasters.ui.main.posts.PostCallback;
import ganainy.dev.gymmasters.utils.AuthUtils;

/**
 * this adapter can be used to show exercises/workouts/post comments
 */
public class PostCommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int RECYCLER_TYPE_WORKOUT = 0;
    private static final int RECYCLER_TYPE_EXERCISE = 1;
    private static final int RECYCLER_TYPE_COMMENT = 2;
    private static final int RECYCLER_TYPE_NO_COMMENTS = 3;
    private static final int RECYCLER_TYPE_LOADING = 4;
    Application app;
    List<PostComment> postCommentList;
    PostCommentCallback postCommentCallback;

    public PostCommentsAdapter(Application app, PostCommentCallback postCommentCallback) {
        this.app = app;
        this.postCommentCallback = postCommentCallback;
    }

    public void setData(List<PostComment> postCommentList) {
        this.postCommentList = postCommentList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType== RECYCLER_TYPE_COMMENT){
            View view = LayoutInflater.from(app).inflate(R.layout.comment_item, viewGroup, false);
            return new PostCommentViewHolder(view);
        }else if (viewType== RECYCLER_TYPE_EXERCISE){
            View view = LayoutInflater.from(app).inflate(R.layout.post_exercise_item, viewGroup, false);
            return new PostExerciseViewHolder(view);
        }
        else if (viewType== RECYCLER_TYPE_WORKOUT){
            View view = LayoutInflater.from(app).inflate(R.layout.post_workout_item, viewGroup, false);
            return new PostWorkoutViewHolder(view);
        }else if (viewType== RECYCLER_TYPE_NO_COMMENTS){
            View view = LayoutInflater.from(app).inflate(R.layout.empty_comments_item, viewGroup, false);
            return new EmptyCommentsViewHolder(view);
        }else if (viewType== RECYCLER_TYPE_LOADING){
            View view = LayoutInflater.from(app).inflate(R.layout.loading_item, viewGroup, false);
            return new LoadingViewHolder(view);
        }
        return null;//error
    }





    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder instanceof PostExerciseViewHolder) {
            ((PostExerciseViewHolder) viewHolder).setDetails(postCommentList.get(position));
        } else if (viewHolder instanceof PostWorkoutViewHolder) {
            ((PostWorkoutViewHolder) viewHolder).setDetails(postCommentList.get(position));
        } else if (viewHolder instanceof PostCommentViewHolder) {
            ((PostCommentViewHolder) viewHolder).setDetails(postCommentList.get(position));
        }


    }

    @Override
    public int getItemCount() {
        return postCommentList == null ? 0 : postCommentList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (postCommentList.get(position).getPostCommentType().equals(PostComment.PostCommentType.POST_EXERCISE)) {
            return RECYCLER_TYPE_EXERCISE;
        } else if (postCommentList.get(position).getPostCommentType().equals(PostComment.PostCommentType.POST_WORKOUT)) {
            return RECYCLER_TYPE_WORKOUT;
        } else if (postCommentList.get(position).getPostCommentType().equals(PostComment.PostCommentType.COMMENT)) {
            return RECYCLER_TYPE_COMMENT;
        } else if (postCommentList.get(position).getPostCommentType().equals(PostComment.PostCommentType.LOADING_COMMENTS)) {
            return RECYCLER_TYPE_LOADING;
        }else if (postCommentList.get(position).getPostCommentType().equals(PostComment.PostCommentType.EMPTY_COMMENTS)) {
            return RECYCLER_TYPE_NO_COMMENTS;
        }

        return -1;

    }

    public  class LoadingViewHolder extends RecyclerView.ViewHolder {
        private final LoadingItemBinding binding;

        public LoadingViewHolder(View view) {
            super(view);
            binding = LoadingItemBinding.bind(view);
        }

    }


    public class PostCommentViewHolder extends RecyclerView.ViewHolder {
        private final CommentItemBinding binding;

        public PostCommentViewHolder(View view) {
            super(view);
            binding = CommentItemBinding.bind(view);
        }

        public void setDetails(PostComment postComment) {
            Pair<Comment, User> currentUserCommentPair = postComment.getUserCommentPair();
            Glide.with(app)
                    .load(currentUserCommentPair.second.getPhoto())
                    .apply(new RequestOptions().placeholder(R.drawable.loading_animation).error(R.drawable.anonymous_profile))
                    .circleCrop()
                    .into(binding.commenterImageView);

            binding.commenterNameTextView.setText(currentUserCommentPair.second.getName());
            binding.commentTextView.setText(currentUserCommentPair.first.getText());
            binding.commentDateTextView.setText(new PrettyTime().format(new Date(currentUserCommentPair.first.getDateCreated())));
        }
    }

    class PostExerciseViewHolder extends RecyclerView.ViewHolder {
        private final PostExerciseItemBinding binding;

        public PostExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = PostExerciseItemBinding.bind(itemView);
        }

        public void setDetails(final PostComment postComment) {
            Exercise exercise = postComment.getPost().getExercise();

            Glide.with(app)
                    .load(exercise.getCreatorImageUrl())
                    .apply(new RequestOptions().placeholder(R.drawable.loading_animation).error(R.drawable.anonymous_profile))
                    .circleCrop()
                    .into(binding.profileImageView);

            binding.userNameTextView.setText(exercise.getCreatorName());
            binding.dateTextView.setText(new PrettyTime().format(new Date((Long.parseLong(exercise.getDate())))));

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

            if (exercise.getCommentList() != null)
                binding.commentCountTextView.setText(Long.toString(exercise.getCommentList().size()));
            else
                binding.commentCountTextView.setText("0");

            if (exercise.getLikerIdList() != null)
                binding.likeCountTextView.setText(Long.toString(exercise.getLikerIdList().size()));
            else
                binding.likeCountTextView.setText("0");

            if (exercise.getLikerIdList() != null &&
                    exercise.getLikerIdList().contains(AuthUtils.getLoggedUserId(app)))
                binding.likeImage.setImageResource(R.drawable.ic_like_blue);
            else
                binding.likeImage.setImageResource(R.drawable.ic_like_grey);

            binding.likeButton.setOnClickListener(v -> postCommentCallback.onPostLike(postComment.getPost()));
            binding.commentButton.setOnClickListener(v -> postCommentCallback.onPostComment(postComment.getPost()));
            binding.profileImageView.setOnClickListener(v -> postCommentCallback.onUserClicked(exercise.getCreatorId()));
            binding.userNameTextView.setOnClickListener(v -> postCommentCallback.onUserClicked(exercise.getCreatorId()));
            binding.exerciseOneImageView.setOnClickListener(v -> postCommentCallback.onExerciseClicked(exercise));
            binding.exerciseTwoImageView.setOnClickListener(v -> postCommentCallback.onExerciseClicked(exercise));
            binding.exerciseNameTextView.setOnClickListener(v -> postCommentCallback.onExerciseClicked(exercise));
        }
    }

    class PostWorkoutViewHolder extends RecyclerView.ViewHolder {
        private final PostWorkoutItemBinding binding;

        public PostWorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = PostWorkoutItemBinding.bind(itemView);
        }

        public void setDetails(PostComment postComment) {
            Workout workout = postComment.getPost().getWorkout();

            Glide.with(app)
                    .load(workout.getCreatorImageUrl())
                    .apply(new RequestOptions().placeholder(R.drawable.loading_animation).error(R.drawable.anonymous_profile))
                    .circleCrop()
                    .into(binding.profileImageView);

            binding.userNameTextView.setText(workout.getCreatorName());
            binding.workoutNameTextView.setText(workout.getName());
            binding.dateTextView.setText(new PrettyTime().format(new Date((Long.parseLong(workout.getDate())))));

            Glide.with(app)
                    .load(workout.getPhotoLink())
                    .apply(new RequestOptions().placeholder(R.drawable.loading_animation).error(R.drawable.ic_exercise_grey))
                    .into(binding.workoutImageView);

            binding.workoutDurationTextView.setText(workout.getDuration());
            binding.workoutDifficultyTextView.setText(workout.getLevel());

            if (workout.getCommentList() != null)
                binding.commentCountTextView.setText(Long.toString(workout.getCommentList().size()));
            else
                binding.commentCountTextView.setText("0");

            if (workout.getLikerIdList() != null)
                binding.likeCountTextView.setText(Long.toString(workout.getLikerIdList().size()));
            else
                binding.likeCountTextView.setText("0");

            if (workout.getLikerIdList() != null &&
                    workout.getLikerIdList().contains(AuthUtils.getLoggedUserId(app)))
                binding.likeImage.setImageResource(R.drawable.ic_like_blue);
            else
                binding.likeImage.setImageResource(R.drawable.ic_like_grey);

            switch (workout.getLevel().toLowerCase()) {
                case "beginner":
                    binding.difficultyBackgroundImageView.setImageResource(R.drawable.ic_triangle_green);
                    binding.workoutDifficultyTextView.setText(R.string.beginner);
                    break;
                case "intermediate":
                    binding.difficultyBackgroundImageView.setImageResource(R.drawable.ic_triangle_yellow);
                    binding.workoutDifficultyTextView.setText(R.string.intermediate);
                    break;
                case "professional":
                    binding.difficultyBackgroundImageView.setImageResource(R.drawable.ic_triangle_red);
                    binding.workoutDifficultyTextView.setText(R.string.professional);
                    break;
            }

            binding.likeButton.setOnClickListener(v -> postCommentCallback.onPostLike(postComment.getPost()));
            binding.commentButton.setOnClickListener(v -> postCommentCallback.onPostComment(postComment.getPost()));
            binding.workoutNameTextView.setOnClickListener(v -> postCommentCallback.onWorkoutClicked(workout));
            binding.workoutImageView.setOnClickListener(v -> postCommentCallback.onWorkoutClicked(workout));
            binding.profileImageView.setOnClickListener(v -> postCommentCallback.onUserClicked(workout.getCreatorId()));
            binding.userNameTextView.setOnClickListener(v -> postCommentCallback.onUserClicked(workout.getCreatorId()));
        }
    }
    }
