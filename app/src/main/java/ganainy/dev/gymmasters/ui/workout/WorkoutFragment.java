package ganainy.dev.gymmasters.ui.workout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.WorkoutFragmentBinding;
import ganainy.dev.gymmasters.models.app_models.Exercise;
import ganainy.dev.gymmasters.models.app_models.Workout;
import ganainy.dev.gymmasters.ui.main.ActivityCallback;
import ganainy.dev.gymmasters.utils.AuthUtils;
import ganainy.dev.gymmasters.utils.SharedPrefUtils;

import static ganainy.dev.gymmasters.utils.SharedPrefUtils.IS_FIRST_SHOWING_OF_WORKOUT;

public class WorkoutFragment extends Fragment {
    private static final String TAG = "SpecificWorkoutActivity";
    public static final String WORKOUT = "workout";
    private Workout workout;
    private WorkoutViewModel workoutViewModel;
    private SpecificWorkoutAdapter specificWorkoutAdapter;
    private WorkoutFragmentBinding binding;

    public static WorkoutFragment newInstance(Workout workout) {
        WorkoutFragment workoutFragment = new WorkoutFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(WORKOUT, workout);
        workoutFragment.setArguments(bundle);
        return workoutFragment;
    }

    public WorkoutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment using View Binding
        binding = WorkoutFragmentBinding.inflate(inflater, container, false);
        setupRecycler();

        binding.workoutHintLayout.closeHintImageView.setOnClickListener(v ->
                binding.workoutHintLayout.getRoot().setVisibility(View.INVISIBLE));

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        workoutViewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);

        if (getArguments() != null && getArguments().getParcelable(WORKOUT) != null) {
            workout = getArguments().getParcelable(WORKOUT);

            if (workout != null) {
                specificWorkoutAdapter.setData(workout.getWorkoutExerciseList());
                specificWorkoutAdapter.notifyDataSetChanged();
                showHintIfFirstViewedWorkout();

                binding.titleTextView.setText(workout.getName());
                if (workout.getCreatorId().equals(AuthUtils.getLoggedUserId(requireContext()))) {
                    binding.deleteImageView.setVisibility(View.VISIBLE);
                } else {
                    binding.deleteImageView.setVisibility(View.GONE);
                }
            }
        }
    }

    private void setupRecycler() {
        specificWorkoutAdapter = new SpecificWorkoutAdapter(requireActivity().getApplication(),
                new ExerciseInsideWorkoutCallback() {
                    @Override
                    public void onTimeExerciseClicked(Exercise exercise, Integer adapterPosition) {
                        openSelectedExerciseFragment(exercise);
                    }

                    @Override
                    public void onRepsExerciseClicked(Exercise exercise, Integer adapterPosition) {
                        openSelectedExerciseFragment(exercise);
                    }
                });

        binding.workoutRecycler.setAdapter(specificWorkoutAdapter);
    }

    private void openSelectedExerciseFragment(Exercise exercise) {
        ((ActivityCallback) requireActivity()).openExerciseFragment(exercise);
    }

    private void deleteWorkout() {
        // Remove exercise data from db
        final String workoutId = workout.getId();
        final String photoLink = workout.getPhotoLink();

        FirebaseDatabase.getInstance().getReference(WORKOUT).child(workoutId).setValue(null); // Delete workout data
        FirebaseStorage.getInstance().getReference().child(photoLink).delete(); // Delete workout photo
        Toast.makeText(requireActivity(), R.string.deleted_successfully, Toast.LENGTH_SHORT).show();
        requireActivity().onBackPressed();
    }

    /** Check if this is the first time user viewing workout to show hint */
    private void showHintIfFirstViewedWorkout() {
        Boolean isFirstShowingForWorkout = SharedPrefUtils.getBoolean(requireContext(),
                IS_FIRST_SHOWING_OF_WORKOUT);

        if (isFirstShowingForWorkout) return;

        binding.workoutHintLayout.getRoot().setVisibility(View.VISIBLE);
        SharedPrefUtils.putBoolean(requireContext(), false, IS_FIRST_SHOWING_OF_WORKOUT);
    }
}