package ganainy.dev.gymmasters.ui.specificWorkout;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.models.app_models.Exercise;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SpecificWorkoutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "SpecificWorkoutAdapter";
    private static final int TYPE_REPS = 1;
    private static final int TYPE_TIME = 2;
    private final Context context;
    private List<Exercise> workoutExerciseList;
    private ExerciseInsideWorkoutCallback exerciseInsideWorkoutCallback;

    public SpecificWorkoutAdapter(Context context, ExerciseInsideWorkoutCallback exerciseInsideWorkoutCallback) {
        this.context = context;
        this.exerciseInsideWorkoutCallback=exerciseInsideWorkoutCallback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;
        if (viewType == TYPE_REPS) { // for TYPE_REPS layout

            view = LayoutInflater.from(context).inflate(R.layout.inside_workout_item_reps, viewGroup, false);
            return new RepsExerciseViewHolder(view);

        } else { // for TYPE_TIME layout
            view = LayoutInflater.from(context).inflate(R.layout.inside_workout_item_duration, viewGroup, false);
            return new TimedExerciseViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == TYPE_REPS) {
            ((RepsExerciseViewHolder) viewHolder).setDetails(workoutExerciseList.get(position));
        } else {
            ((TimedExerciseViewHolder) viewHolder).setDetails(workoutExerciseList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return workoutExerciseList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (workoutExerciseList.get(position).getDuration() == null) {
            return TYPE_REPS;

        } else {
            return TYPE_TIME;
        }
    }

    public void setDataSource(List<Exercise> workoutExerciseList) {
        this.workoutExerciseList = workoutExerciseList;

    }


    //
    class RepsExerciseViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.textViewExName)
        TextView textViewExName;

        @BindView(R.id.textViewSets)
        TextView textViewSets;

        @BindView(R.id.textViewReps)
        TextView textViewReps;

        @BindView(R.id.textViewTargetMuscle)
        TextView textViewTargetMuscle;


        RepsExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(view ->
                    exerciseInsideWorkoutCallback.onRepsExerciseClicked(workoutExerciseList.get(getAdapterPosition()),getAdapterPosition()));
        }

        public void setDetails(Exercise workoutExercise) {
            textViewExName.setText(workoutExercise.getName());
            textViewSets.setText(workoutExercise.getSets() + " Sets");
            textViewReps.setText(workoutExercise.getReps() + " Reps");
            textViewTargetMuscle.setText(workoutExercise.getBodyPart());
        }
    }

    //
    class TimedExerciseViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textViewExName)
        TextView textViewExName;

        @BindView(R.id.textViewSets)
        TextView textViewSets;

        @BindView(R.id.textViewTime)
        TextView textViewTime;

        @BindView(R.id.textViewTargetMuscle)
        TextView textViewTargetMuscle;

        TimedExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(view ->
                    exerciseInsideWorkoutCallback.onTimeExerciseClicked(workoutExerciseList.get(getAdapterPosition()),getAdapterPosition()));
        }


        public void setDetails(Exercise workoutExercise) {
            textViewExName.setText(workoutExercise.getName());
            textViewSets.setText(workoutExercise.getSets() + " Sets");
            textViewTime.setText(workoutExercise.getDuration() + " Secs");
            textViewTargetMuscle.setText(workoutExercise.getBodyPart());
        }
    }


}
