package ganainy.dev.gymmasters.ui.workout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.InsideWorkoutItemRepsBinding;
import ganainy.dev.gymmasters.databinding.InsideWorkoutItemDurationBinding;
import ganainy.dev.gymmasters.databinding.DotItemBinding;
import ganainy.dev.gymmasters.models.app_models.Exercise;

public class SpecificWorkoutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_REPS = 1;
    private static final int TYPE_TIME = 2;
    private static final int TYPE_DOT = 3;

    private final Context context;
    private List<Exercise> workoutExerciseList;
    private final ExerciseInsideWorkoutCallback exerciseInsideWorkoutCallback;

    public SpecificWorkoutAdapter(Context context, ExerciseInsideWorkoutCallback exerciseInsideWorkoutCallback) {
        this.context = context;
        this.exerciseInsideWorkoutCallback = exerciseInsideWorkoutCallback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_REPS) {
            InsideWorkoutItemRepsBinding binding = InsideWorkoutItemRepsBinding.inflate(inflater, viewGroup, false);
            return new RepsExerciseViewHolder(binding);
        } else if (viewType == TYPE_TIME) {
            InsideWorkoutItemDurationBinding binding = InsideWorkoutItemDurationBinding.inflate(inflater, viewGroup, false);
            return new TimedExerciseViewHolder(binding);
        } else if (viewType == TYPE_DOT) {
            DotItemBinding binding = DotItemBinding.inflate(inflater, viewGroup, false);
            return new DotViewHolder(binding);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof RepsExerciseViewHolder) {
            ((RepsExerciseViewHolder) viewHolder).setDetails(workoutExerciseList.get(position));
        } else if (viewHolder instanceof TimedExerciseViewHolder) {
            ((TimedExerciseViewHolder) viewHolder).setDetails(workoutExerciseList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return workoutExerciseList == null ? 0 : workoutExerciseList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Exercise currentExercise = workoutExerciseList.get(position);

        if (currentExercise == null) {
            return TYPE_DOT;
        } else if (currentExercise.getDuration() != null) {
            return TYPE_TIME;
        } else if (currentExercise.getSets() != null && currentExercise.getReps() != null) {
            return TYPE_REPS;
        }

        return -1;
    }

    public void setData(List<Exercise> workoutExerciseList) {
        if (workoutExerciseList.get(0) != null) {
            workoutExerciseList.add(0, null);
        }
        if (workoutExerciseList.get(workoutExerciseList.size() - 1) != null) {
            workoutExerciseList.add(null);
        }
        this.workoutExerciseList = workoutExerciseList;
    }

    class RepsExerciseViewHolder extends RecyclerView.ViewHolder {
        private final InsideWorkoutItemRepsBinding binding;

        RepsExerciseViewHolder(InsideWorkoutItemRepsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.setOnClickListener(view ->
                    exerciseInsideWorkoutCallback.onRepsExerciseClicked(workoutExerciseList.get(getAdapterPosition()), getAdapterPosition()));
        }

        public void setDetails(Exercise workoutExercise) {
            binding.textViewExName.setText(workoutExercise.getName());
            binding.textViewSets.setText(workoutExercise.getSets() + " Sets");
            binding.textViewReps.setText(workoutExercise.getReps() + " Reps");
            binding.textViewTargetMuscle.setText(workoutExercise.getBodyPart());

            Glide.with(context)
                    .load(workoutExercise.getPreviewPhotoOneUrl())
                    .apply(new RequestOptions().placeholder(R.drawable.loading_animation).error(R.drawable.ic_dumbell_grey))
                    .circleCrop()
                    .into(binding.exerciseImageView);
        }
    }

    class TimedExerciseViewHolder extends RecyclerView.ViewHolder {
        private final InsideWorkoutItemDurationBinding binding;

        TimedExerciseViewHolder(InsideWorkoutItemDurationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.setOnClickListener(view ->
                    exerciseInsideWorkoutCallback.onTimeExerciseClicked(workoutExerciseList.get(getAdapterPosition()), getAdapterPosition()));
        }

        public void setDetails(Exercise workoutExercise) {
            binding.textViewExName.setText(workoutExercise.getName());
            binding.textViewSets.setText(workoutExercise.getSets() + " Sets");
            binding.textViewTime.setText(workoutExercise.getDuration() + " Secs");
            binding.textViewTargetMuscle.setText(workoutExercise.getBodyPart());
        }
    }

    class DotViewHolder extends RecyclerView.ViewHolder {
        DotViewHolder(DotItemBinding binding) {
            super(binding.getRoot());
        }
    }
}