package com.example.myapplication.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.ExerciseAdapterAdvanced;
import com.example.myapplication.model.Exercise;
import com.example.myapplication.model.Workout;
import com.example.myapplication.ui.MainActivity;
import com.example.myapplication.utils.MyConstant;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateWorkoutFragment extends Fragment {
    private static final String TAG = "CreateWorkoutFragment";
    @BindView(R.id.nameEditText)
    EditText nameEditText;

    @BindView(R.id.durationEditText)
    EditText durationEditText;

    @BindView(R.id.levelSpinner)
    Spinner levelSpinner;

    @BindView(R.id.workoutImageView)
    ImageView workoutImage;
    @BindView(R.id.backArrowImageView)
    ImageView backArrowImageView;

   /* @BindView(R.id.workoutImageView)
    ImageView workoutImageView;*/

    @BindView(R.id.exercisesRecycler)
    RecyclerView exercisesRecycler;


    private String newWorkoutLevel;
    private Uri imageUri;
    private List<Exercise> exerciseList;
    private ExerciseAdapterAdvanced exerciseAdapter;
    private List<Exercise> exercisesOfWorkoutList;


    public CreateWorkoutFragment() {
        // Required empty public constructor
    }


    private void uploadWorkoutImage(long timeMilli) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        final StorageReference imagesRef = storageRef.child("workoutImages/" + imageUri.getLastPathSegment() + timeMilli);
        imagesRef.putFile(imageUri);
    }

    private boolean validateInputs() {
        if (nameEditText.getText().length() < 6) {
            FancyToast.makeText(getActivity(), "Name must be at least 6 chars", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
            return false;
        } else if (!checkInRange(durationEditText.getText().toString())) {
            FancyToast.makeText(getActivity(), "Duration must be from 0 to 120 minutes", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
            return false;
        } else if (imageUri == null) {
            FancyToast.makeText(getActivity(), "Select image to represent workout", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
            return false;
        } else if (newWorkoutLevel.equals("")) {
            FancyToast.makeText(getActivity(), "Select workout level", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
            return false;
        } else if (exercisesOfWorkoutList == null || exercisesOfWorkoutList.size() == 0) {
            FancyToast.makeText(getActivity(), "Workout must have at lease 1 exercise", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
            return false;
        } else {

            return true;
        }
    }

    private boolean checkInRange(String text) {
        return Integer.valueOf(text) > 0 && Integer.valueOf(text) <= 120;
    }

    private void levelSpinnerCode() {
        final String[] level = {"", "Beginner", "Intermediate", "Professional"};
        ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, level);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        levelSpinner.setAdapter(arrayAdapter);
        levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                newWorkoutLevel = level[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_workout_fragment1, container, false);
        // setHasOptionsMenu(true);
        ButterKnife.bind(this, view);

        levelSpinnerCode();
        downloadAllExercises();
        return view;
    }


    private void downloadAllExercises() {
        exerciseList = new ArrayList<>();
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Exercise exercise = new Exercise();
                    exercise.setName(ds.child("name").getValue().toString());
                    exercise.setBodyPart(ds.child("bodyPart").getValue().toString());
                    exercise.setPreviewPhoto1(ds.child("previewPhoto1").getValue().toString());
                    exerciseList.add(exercise);

                }

                //downloadExercisesImages();
                setupRecycler();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };


        FirebaseDatabase.getInstance().getReference("excercises").addChildEventListener(childEventListener);

    }

    private void setupRecycler() {

        exerciseAdapter = new ExerciseAdapterAdvanced(getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        exerciseAdapter.setDataSource(exerciseList);
        exercisesRecycler.setLayoutManager(linearLayoutManager);
        exercisesRecycler.setAdapter(exerciseAdapter);
    }

    private void uploadWorkout() {

        /**this list contains exercises i added to the work out and each one has sets and reps*/
        exercisesOfWorkoutList = exerciseAdapter.getExercisesOfWorkoutList();


        if (!validateInputs()) {
            return;
        }


        /**unique number to attach to image path */
        Date date = new Date();
        long timeMilli = date.getTime();

        /**upload workout image to storge*/
        uploadWorkoutImage(timeMilli);


        DatabaseReference workoutRef = FirebaseDatabase.getInstance().getReference("workout");


        Workout workout = new Workout();
        workout.setName(nameEditText.getText().toString());
        workout.setDuration(durationEditText.getText().toString());
        workout.setLevel(newWorkoutLevel);
        workout.setPhotoLink("workoutImages/" + imageUri.getLastPathSegment() + timeMilli);
        workout.setCreatorId(MyConstant.loggedInUserId);
        /**save user id and date with workout*/
        String id = workoutRef.push().getKey();
        workout.setId(id);
        workout.setDate(String.valueOf(System.currentTimeMillis()));

        workout.setExercisesNumber(String.valueOf(exercisesOfWorkoutList.size()));
        workout.setWorkoutExerciseList(exercisesOfWorkoutList);

        workoutRef.child(id).setValue(workout).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                startActivity(new Intent(getActivity(), MainActivity.class));
                FancyToast.makeText(getActivity(), "Workout uploaded", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FancyToast.makeText(getActivity(), "Uploading failed , check connection and retry", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();

            }
        });
    }


    @OnClick(R.id.workoutImageView)
    void getPhotoAndShow() {

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getPhotoFromGallery();

        mainActivity.setOnBundleSelected(new MainActivity.SelectedBundle() {
            @Override
            public void onBundleSelect(Bundle bundle) {
                Log.i(TAG, "onBundleSelect1: ");
                String imageString = (String) bundle.get("imageString");
                imageUri = Uri.parse(imageString);

                workoutImage.setPadding(0, 0, 0, 0);
                workoutImage.setImageURI(imageUri);
            }
        });

    }


    @OnClick(R.id.backArrowImageView)
    public void onViewClicked() {
        //startActivity(new Intent(getActivity(), MainActivity.class));
        MainActivity mainActivity = (MainActivity) getActivity();
        FragmentTransaction fragmentTransaction = mainActivity.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.remove(this).commit();
    }

    @OnClick(R.id.uploadButton)
    public void onuploadClicked() {
        uploadWorkout();

    }

    @Override
    public void onResume() {
        super.onResume();
        /**hide toolbar in this fragment*/
        (getActivity()).findViewById(R.id.toolbarLayout).setVisibility(View.GONE);


    }

    @Override
    public void onStop() {
        super.onStop();
        //todo fix this not getting called
        /**show toolbar again of main activity on fragment destruction*/
        (getActivity()).findViewById(R.id.toolbarLayout).setVisibility(View.VISIBLE);
    }



    /*    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(TAG, "onCreateOptionsMenu: ");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_exercise_menu, menu);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.i(TAG, "onQueryTextChange: " + s);
                //so app won't crash if no data in recycler
                if (exerciseAdapter != null)
                    exerciseAdapter.getFilter().filter(s);
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });
    }*/
}
