package ganainy.dev.gymmasters.ui.specificExercise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.ExerciseFragmentBinding;
import ganainy.dev.gymmasters.models.app_models.Exercise;
import ganainy.dev.gymmasters.ui.main.ActivityCallback;

public class ExerciseFragment extends Fragment {

    public static final String ISOLATED = "isolated";
    public static final String COMPOUND = "compound";
    public static final String EXERCISE = "exercise";

    private ExerciseViewModel mViewModel;
    private ExerciseFragmentBinding binding;

    public static ExerciseFragment newInstance(Exercise exercise) {
        ExerciseFragment exerciseFragment = new ExerciseFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXERCISE, exercise);
        exerciseFragment.setArguments(bundle);
        return exerciseFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ExerciseFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setupClickListeners();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViewModel();

        if (getArguments().getParcelable(EXERCISE) != null) {
            Exercise exercise = getArguments().getParcelable(EXERCISE);
            showExerciseInUi(exercise);
            mViewModel.setExercise(exercise);
            mViewModel.loadExercisePhotos();
            mViewModel.isLoggedUserExercise();
        }

        /*only show delete button if this exercise is owned by logged in user*/
        mViewModel.getIsLoggedUserExerciseLiveData().observe(getViewLifecycleOwner(), isLoggedUserExercise -> {
            if (isLoggedUserExercise) binding.deleteImageView.setVisibility(View.VISIBLE);
            else binding.deleteImageView.setVisibility(View.GONE);
        });

        /*show/hide photo/youtube player based on user choice*/
        mViewModel.getExerciseSelectedImageLiveData().observe(getViewLifecycleOwner(), exerciseViewType -> {
            switch (exerciseViewType) {
                case IMAGE_ONE:
                    showFirstPhoto();
                    break;
                case IMAGE_TWO:
                    showSecondPhoto();
                    break;
            }
        });

        mViewModel.getIsExerciseDeletedSuccessfullyLiveData().observe(getViewLifecycleOwner(), isExerciseDeletedSuccessfully -> {
            if (isExerciseDeletedSuccessfully) {
                Toast.makeText(requireActivity(), R.string.exercise_deleted_successfully, Toast.LENGTH_LONG).show();
                requireActivity().onBackPressed();
            } else {
                Toast.makeText(requireActivity(), R.string.error_deleting_exercise, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupClickListeners() {
        binding.backArrowImageView.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.showVideoButton.setOnClickListener(v -> openYoutubeFragment());
        binding.mechanicQuestionMark.setOnClickListener(v -> showMechanicInfoDialog(binding.mechanicTextView.getText().toString().toLowerCase()));
        binding.deleteImageView.setOnClickListener(v -> showConfirmDeleteDialog());
    }

    private void openYoutubeFragment() {
        ActivityCallback activityCallback = (ActivityCallback) requireActivity();
        activityCallback.openYoutubeFragment(mViewModel.getExercise().getName());
    }

    private void showConfirmDeleteDialog() {
        new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.delete_exercise_q)
                .setMessage(R.string.confirm_delete_exercise)
                .setIcon(R.drawable.ic_delete_black_24dp)
                .setPositiveButton(R.string.delete, (dialog, which) -> mViewModel.deleteExercise())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showSecondPhoto() {
        binding.progressBar.setVisibility(View.GONE);
        binding.numOneEditText.setVisibility(View.GONE);
        binding.numTwoEditText.setVisibility(View.VISIBLE);
        binding.workoutImageView.setImageDrawable(mViewModel.getSecondDrawable());
    }

    private void initViewModel() {
        mViewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);
    }

    private void showExerciseInUi(Exercise exercise) {
        binding.nameTextView.setText(exercise.getName());
        binding.titleTextView.setText(exercise.getName());
        binding.executionTextView.setText(exercise.getExecution());
        binding.mechanicTextView.setText(exercise.getMechanism());
        binding.targetedMuscleTextView.setText(exercise.getBodyPart());
        if (exercise.getAdditional_notes() == null) binding.additionalNotesTextView.setText(R.string.none);
        else binding.additionalNotesTextView.setText(exercise.getAdditional_notes());
    }

    private void showFirstPhoto() {
        binding.progressBar.setVisibility(View.GONE);
        binding.numOneEditText.setVisibility(View.VISIBLE);
        binding.numTwoEditText.setVisibility(View.GONE);
        binding.workoutImageView.setImageDrawable(mViewModel.getFirstDrawable());
    }

    private void showMechanicInfoDialog(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setCancelable(false)
                .setPositiveButton(R.string.got_it, (dialogInterface, i) -> {
                    // Do nothing
                });

        // Change message depending on type of mechanism
        switch (s) {
            case ISOLATED:
                builder.setMessage(getString(R.string.isolated_definition));
                break;
            case COMPOUND:
                builder.setMessage(R.string.compound_definition);
                break;
        }
        builder.create().show();
    }
}