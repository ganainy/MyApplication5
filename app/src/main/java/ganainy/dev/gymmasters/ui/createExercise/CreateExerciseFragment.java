package ganainy.dev.gymmasters.ui.createExercise;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.CircleProgress;
import com.shashank.sony.fancytoastlib.FancyToast;


import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.CreateExerciseFragmentBinding;
import ganainy.dev.gymmasters.utils.ApplicationViewModelFactory;

import static ganainy.dev.gymmasters.ui.main.exercisesCategories.ExercisesCategoriesFragment.ABS;
import static ganainy.dev.gymmasters.ui.main.exercisesCategories.ExercisesCategoriesFragment.BACK;
import static ganainy.dev.gymmasters.ui.main.exercisesCategories.ExercisesCategoriesFragment.BICEPS;
import static ganainy.dev.gymmasters.ui.main.exercisesCategories.ExercisesCategoriesFragment.CARDIO;
import static ganainy.dev.gymmasters.ui.main.exercisesCategories.ExercisesCategoriesFragment.CHEST;
import static ganainy.dev.gymmasters.ui.main.exercisesCategories.ExercisesCategoriesFragment.LOWERLEG;
import static ganainy.dev.gymmasters.ui.main.exercisesCategories.ExercisesCategoriesFragment.SHOULDER;
import static ganainy.dev.gymmasters.ui.main.exercisesCategories.ExercisesCategoriesFragment.SHOWALL;
import static ganainy.dev.gymmasters.ui.main.exercisesCategories.ExercisesCategoriesFragment.TRICEPS;

public class CreateExerciseFragment extends Fragment {

    private CreateExerciseFragmentBinding binding;
    private static final int PICK_IMAGE = 101;
    private static final int PICK_IMAGE2 = 102;
    public static final String SELECT_PICTURE = "Select Picture";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CreateExerciseFragmentBinding.inflate(inflater, container, false);
         return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

         bodyPartSpinner= binding.bodyPartSpinner;
         mechanicSpinner= binding.mechanicSpinner;
         nameEditText= binding.nameEditText;
         executionEditText= binding.executionEditText;
         additionalNotesEditText= binding.additionalNotesEditText;
         firstExercisePhoto= binding.workoutImageView;
         secondExercisePhoto= binding.addExercisePhoto2;
         parentScroll= binding.parentScroll;
         loadingLayout= binding.loadingLayout.getRoot();
         circleProgress= binding.loadingLayout.circleProgress;
         exerciseNameDot= binding.exerciseNameDot;
         executionDot= binding.executionDot;
         mechanicDot= binding.mechanicDot;
         targetedMuscleDot= binding.targetedMuscleDot;
         exerciseImagesDot= binding.exerciseImagesDot;

        // Set up the click listener for the ImageView
        binding.backArrowImageView.setOnClickListener(v -> onBackArrowClicked());

        binding.saveButton.setOnClickListener(v ->    mViewModel.saveExercise(nameEditText.getText().toString(),
                executionEditText.getText().toString(), additionalNotesEditText.getText().toString()));


        binding.workoutImageView.setOnClickListener(v ->  getPhotoFromGallery());

        binding.addExercisePhoto2.setOnClickListener(v ->  getPhoto2FromGallery());

        muscleSpinnerCode();
        mechanicSpinnerCode();


        initViewModel();

        mViewModel.getMissingFieldLiveData().observe(getViewLifecycleOwner(), missingField -> {
            ExerciseFieldIssue contentIfNotHandled = missingField.getContentIfNotHandled();
            if (contentIfNotHandled==null)return;
            switch (contentIfNotHandled) {
                case NAME:
                    FancyToast.makeText(requireActivity(), getString(R.string.exercise_name_atleast_6), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                    break;
                case EXECUTION:
                    FancyToast.makeText(requireActivity(), getString(R.string.execution_atleast_30), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                    break;
                case MECHANIC:
                    FancyToast.makeText(requireActivity(), getString(R.string.select_mechanic), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                    break;
                case TARGET_MUSCLE:
                    FancyToast.makeText(requireActivity(), getString(R.string.select_target_muscle), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                    break;
                case PHOTO:
                    FancyToast.makeText(requireActivity(), getString(R.string.select_photos), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                    break;
            }
        });


        mViewModel.getUploadProgressLiveData().observe(getViewLifecycleOwner(), progress -> {
            circleProgress.setProgress(progress);
        });


        mViewModel.getNetworkStateLiveData().observe(getViewLifecycleOwner(), networkState -> {
            switch (networkState) {
                case SUCCESS:
                    loadingLayout.setVisibility(View.GONE);
                    FancyToast.makeText(requireActivity(), getString(R.string.exercises_added_successfully), FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();
                    requireActivity().onBackPressed();
                    break;
                case ERROR:
                    FancyToast.makeText(requireActivity(), getString(R.string.something_went_wrong), FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();
                    break;
                case LOADING:
                    loadingLayout.setVisibility(View.VISIBLE);
                    break;
                case EMPTY:
                    break;
                case STOP_LOADING:
                    loadingLayout.setVisibility(View.GONE);
                    break;
            }
        });


        mViewModel.getFirstImageUriLiveData().observe(getViewLifecycleOwner(), firstImageUri -> {
            firstExercisePhoto.setImageURI(firstImageUri);
        });

        mViewModel.getSecondImageUriLiveData().observe(getViewLifecycleOwner(), secondImageUri -> {
            secondExercisePhoto.setImageURI(secondImageUri);

        });

        mViewModel.getRepeatedNameTransformation().observe(getViewLifecycleOwner(), isRepeatedName -> {
            if (isRepeatedName) {
                FancyToast.makeText(requireContext(), getString(R.string.exercise_was_same_name_exist),
                        FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
            }
        });

        mViewModel.getFirstImageUploadedTransformation().observe(getViewLifecycleOwner(),isFirstImageUploaded->{
            //Do nothing, observing to activate transformation
        });

        mViewModel.getSecondImageUploadedTransformation().observe(getViewLifecycleOwner(),isExerciseUploaded->{
            //Do nothing, observing to activate transformation
        });


    }
    void getPhoto2FromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, SELECT_PICTURE), PICK_IMAGE2);
    }
    void getPhotoFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, SELECT_PICTURE), PICK_IMAGE);
    }

    Spinner bodyPartSpinner;
    Spinner mechanicSpinner;
    TextView nameEditText;
    TextView executionEditText;
    TextView additionalNotesEditText;
    ImageView firstExercisePhoto;
    ImageView secondExercisePhoto;
    ScrollView parentScroll;
    ConstraintLayout loadingLayout;
    CircleProgress circleProgress;
    ImageView exerciseNameDot;
    ImageView executionDot;
    ImageView mechanicDot;
    ImageView targetedMuscleDot;
    ImageView exerciseImagesDot;


    private void onBackArrowClicked() {
        // Handle the back button click event
        requireActivity().onBackPressed();
    }





    private CreateExerciseViewModel mViewModel;


    public static CreateExerciseFragment newInstance() {
        return new CreateExerciseFragment();
    }





    private void initViewModel() {
        ApplicationViewModelFactory applicationViewModelFactory=new ApplicationViewModelFactory(requireActivity().getApplication());
        mViewModel = new ViewModelProvider(this,applicationViewModelFactory).get(CreateExerciseViewModel.class);
    }

    private void mechanicSpinnerCode() {
        final String[] mechanic = {"", "Compound", "Isolated"};
        ArrayAdapter arrayAdapter = new ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mechanic);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        mechanicSpinner.setAdapter(arrayAdapter);
        mechanicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mViewModel.setExerciseMechanic(mechanic[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void muscleSpinnerCode() {

        final String[] muscles = {"", "Chest", "Triceps", "Shoulders", "Biceps", "Abs", "Back", "Leg", "Cardio", "Other"};
        ArrayAdapter arrayAdapter = new ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, muscles);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        bodyPartSpinner.setAdapter(arrayAdapter);
        bodyPartSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int selectedIndex, long l) {
                switch (selectedIndex) {
                    case 1:
                        mViewModel.setExerciseSelectedMuscle(CHEST);
                        break;
                    case 2:
                        mViewModel.setExerciseSelectedMuscle(TRICEPS);
                        break;
                    case 3:
                        mViewModel.setExerciseSelectedMuscle(SHOULDER);
                        break;
                    case 4:
                        mViewModel.setExerciseSelectedMuscle(BICEPS);
                        break;
                    case 5:
                        mViewModel.setExerciseSelectedMuscle(ABS);
                        break;
                    case 6:
                        mViewModel.setExerciseSelectedMuscle(BACK);
                        break;
                    case 7:
                        mViewModel.setExerciseSelectedMuscle(LOWERLEG);
                        break;
                    case 8:
                        mViewModel.setExerciseSelectedMuscle(CARDIO);
                        break;
                    case 9:
                        mViewModel.setExerciseSelectedMuscle(SHOWALL);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null)
            return;
        if (requestCode == PICK_IMAGE) {
            mViewModel.setFirstImageUri(data.getData());
        }
        if (requestCode == PICK_IMAGE2) {
            mViewModel.setSecondImageUri(data.getData());
        }
    }


}