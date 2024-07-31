package ganainy.dev.gymmasters.ui.createWorkout;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.CreateExerciseFragmentBinding;
import ganainy.dev.gymmasters.databinding.CreateWorkoutFragmentBinding;
import ganainy.dev.gymmasters.models.app_models.Exercise;
import ganainy.dev.gymmasters.utils.ApplicationViewModelFactory;

import com.github.lzyzsd.circleprogress.CircleProgress;
import com.shashank.sony.fancytoastlib.FancyToast;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateWorkoutFragment extends Fragment {
    private static final String TAG = "CreateWorkoutFragment";
    public static final String SELECT_PICTURE = "Select Picture";

    private String sets = "4";
    private String reps = "10";
    EditText nameEditText;
    EditText durationEditText;
    Spinner levelSpinner;
    ImageView workoutImage;
    RecyclerView exercisesRecycler;
    EditText searchView;
    CircleProgress circleProgress;
    ConstraintLayout loadingLayout;
    private ExerciseAdapterAdvanced exerciseAdapter;
    CreateWorkoutFragmentBinding binding;
    CreateWorkoutViewModel mViewModel;



    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {


         nameEditText=binding.nameEditText;

         durationEditText=binding.durationEditText;

         levelSpinner=binding.levelSpinner;

         workoutImage=binding.workoutImageView;

         exercisesRecycler=binding.exercisesRecycler;
         searchView=binding.searchView;
         circleProgress=binding.loadingLayout.circleProgress;

         binding.backArrowImageView.setOnClickListener(v ->
            requireActivity().onBackPressed()
        );


        binding.workoutImageView.setOnClickListener(v ->
                openGalleryImageChooser()
        );

        binding.uploadButton.setOnClickListener(v ->
                onUploadClicked()
        );


         loadingLayout= binding.loadingLayout.getRoot();


        super.onViewCreated(view, savedInstanceState);
    }


    void openGalleryImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, SELECT_PICTURE), 103);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViewModel();

        mViewModel.getNetworkStateLiveData().observe(getViewLifecycleOwner(),networkState -> {
            switch (networkState){

                case SUCCESS:
                    loadingLayout.setVisibility(View.GONE);
                    FancyToast.makeText(getActivity(), getString(R.string.workout_uploaded), FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                    requireActivity().onBackPressed();
                    break;
                case ERROR:
                    loadingLayout.setVisibility(View.GONE);
                    FancyToast.makeText(getActivity(), getString(R.string.workout_upload_failed), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                    break;
                case LOADING:
                    loadingLayout.setVisibility(View.VISIBLE);
                    break;
                case EMPTY:
                    break;
            }

        });

        mViewModel.getImageUriLiveData().observe(getViewLifecycleOwner(),imageUri->{
            workoutImage.setPadding(0, 0, 0, 0);
            workoutImage.setImageURI(imageUri);
        });

        mViewModel.getUploadProgressLiveData().observe(getViewLifecycleOwner(),progress->{
             circleProgress.setProgress(progress);
        });

        mViewModel.getExerciseListLiveData().observe(getViewLifecycleOwner(),exerciseList->{
            exerciseAdapter.setDataSource(exerciseList);
            exerciseAdapter.notifyDataSetChanged();
        });

        mViewModel.getFieldIssueLiveData().observe(getViewLifecycleOwner(),fieldIssue->{
            WorkoutFieldIssue contentIfNotHandled = fieldIssue.getContentIfNotHandled();
            if (contentIfNotHandled!=null){
                switch (contentIfNotHandled){
                    case NAME:
                        FancyToast.makeText(getActivity(), getString(R.string.workout_name_atleast_6), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                        break;
                    case DURATION:
                        FancyToast.makeText(getActivity(), getString(R.string.duration_0_to_120), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                        break;
                    case IMAGE:
                        FancyToast.makeText(getActivity(), getString(R.string.select_workout_image), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                        break;
                    case LEVEL:
                        FancyToast.makeText(getActivity(), getString(R.string.select_workout_level), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                        break;
                    case EMPTY_EXERCISES:
                        FancyToast.makeText(getActivity(), getString(R.string.workout_doesnt_have_any_exercise), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                        break;
                }
            }
        });

        mViewModel.getDownloadedExerciseListLiveData().observe(getViewLifecycleOwner(),updatedExerciseList->{
            exerciseAdapter.setDataSource(updatedExerciseList);
            exerciseAdapter.notifyDataSetChanged();
        });

    }






    private void initViewModel() {
        ApplicationViewModelFactory applicationViewModelFactory=new ApplicationViewModelFactory(requireActivity().getApplication());
        mViewModel = new ViewModelProvider(this,applicationViewModelFactory).get(CreateWorkoutViewModel.class);
    }

    public CreateWorkoutFragment() {
        // Required empty public constructor
    }

    private void levelSpinnerCode() {
        final String[] level = {"", "Beginner", "Intermediate", "Professional"};
        ArrayAdapter arrayAdapter = new ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, level);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        levelSpinner.setAdapter(arrayAdapter);
        levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mViewModel.setWorkoutLevel(level[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CreateWorkoutFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    private void setupRecycler() {
        //addExerciseToWorkout
        exerciseAdapter = new ExerciseAdapterAdvanced(getActivity(),new AddExerciseCallback(){
            @Override
            public void onExercisesAdded(Exercise exercise,Integer adapterPosition) {
                openSetsAndRepsAlertDialog(exercise);
            }

            @Override
            public void onExercisesDeleted(Exercise exercise,Integer adapterPosition) {
                mViewModel.removeExerciseFromWorkout(exercise);
            }
        });
        exercisesRecycler.setAdapter(exerciseAdapter);
        addSearchFunctionality();
    }

    private void addSearchFunctionality() {
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mViewModel.filterExercises(editable);
            }
        });
    }





    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null)
            return;
        if (requestCode == 103) {
            mViewModel.setImageUri(data.getData());
        }
    }

    public void onUploadClicked() {
        /*this list contains exercises i added to the work out and each one has sets and reps*/
        if (mViewModel.validateInputs(nameEditText.getText().toString().trim(),
                durationEditText.getText().toString().trim()))
        {
            mViewModel.uploadWorkoutImage();
            mViewModel.setWorkoutName(nameEditText.getText().toString());
            mViewModel.setWorkoutDuration(durationEditText.getText().toString());
        }
    }


    private void openSetsAndRepsAlertDialog(final Exercise selectedExercise) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.sets_reps_layout, null);
        // Inflate and set the layout for the dialog
        builder.setTitle(R.string.chose_sets_reps_count);
        builder.setCancelable(false);
        builder.setIcon(R.drawable.ic_muscle);

        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.save, (dialog, id) -> {

                    selectedExercise.setSets(sets);
                    selectedExercise.setReps(reps);
                    mViewModel.addExerciseToWorkout(selectedExercise);

                    FancyToast.makeText(requireContext(), getString(R.string.added_exercise_to_workout), FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();

                }).setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    //do nothing
                });
        builder.create();
        builder.show();


        //two number pickers in alert dialog to choose sets and reps for clicked exercise
        NumberPicker setsPicker = view.findViewById(R.id.setsPicker);
        NumberPicker repsPicker = view.findViewById(R.id.repsPicker);

        setsPicker.setMinValue(4);
        setsPicker.setMaxValue(12);

        repsPicker.setMinValue(10);
        repsPicker.setMaxValue(100);


        setsPicker.setOnValueChangedListener((numberPicker, i, i1) ->
                sets = String.valueOf(numberPicker.getValue()));

        repsPicker.setOnValueChangedListener((numberPicker, i, i1) ->
                reps = String.valueOf(numberPicker.getValue()));
    }


}
