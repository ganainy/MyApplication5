package com.example.myapplication.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.myapplication.MyConstant;
import com.example.myapplication.R;
import com.example.myapplication.activities.CreateNewExerciseActivity;
import com.example.myapplication.activities.MainActivity;
import com.example.myapplication.adapters.ExerciseAdapter;
import com.example.myapplication.adapters.WorkoutAdapter;
import com.example.myapplication.model.Exercise;
import com.example.myapplication.model.Workout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainFragmentHome extends Fragment {
    private static final String TAG = "MainFragmentHome";
    ExerciseAdapter exerciseAdapter;
    @BindView(R.id.viewMyExercisesButton)
    Button viewMyExercisesButton;
    @BindView(R.id.viewMyWorkoutsButton)
    Button viewMyWorkoutsButton;
    private View view;
    private ArrayList<Exercise> myCustomExercisesList = new ArrayList<>();

    @OnClick(R.id.createWorkout)
    void createWorkout() {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setupViewPagerForCreateWorkout();

    }

    private List<Workout> myCustomWorkoutList = new ArrayList<>();
    private WorkoutAdapter workoutAdapter;

    @OnClick(R.id.createExercise)
    void createExercise() {
        startActivity(new Intent(getActivity(), CreateNewExerciseActivity.class));
    }

    @OnClick(R.id.viewMyExercisesButton)
    void viewExercises() {

        if (haveNetworkConnection())
            downloadMyExercises();
        else
            FancyToast.makeText(getActivity(), "Check network connection and try again.", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
    }

    @OnClick(R.id.viewMyWorkoutsButton)
    void viewWorkouts() {

        if (haveNetworkConnection())
            downloadMyWorkout();
        else
            FancyToast.makeText(getActivity(), "Check network connection and try again.", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();


    }



    public MainFragmentHome() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main_fragment_home, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void downloadMyExercises() {
        final DatabaseReference exerciseNode = FirebaseDatabase.getInstance().getReference("excercises");
        exerciseNode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myCustomExercisesList.clear();
                for (DataSnapshot dsBig : dataSnapshot.getChildren()) {
                    for (DataSnapshot ds : dsBig.getChildren()) {
                        Exercise exercise = new Exercise();
                        Log.i(TAG, "onChildAdded: " + MyConstant.loggedInUserId);
                        if (ds.child("creatorId").getValue().equals(MyConstant.loggedInUserId)) {
                            exercise.setName(ds.child("name").getValue().toString());
                            exercise.setExecution(ds.child("execution").getValue().toString());
                            exercise.setPreparation(ds.child("preparation").getValue().toString());
                            exercise.setMechanism(ds.child("mechanism").getValue().toString());
                            exercise.setPreviewPhoto1(ds.child("previewPhoto1").getValue().toString());
                            exercise.setPreviewPhoto2(ds.child("previewPhoto2").getValue().toString());
                            exercise.setUtility(ds.child("utility").getValue().toString());
                            exercise.setVideoLink(ds.child("videoLink").getValue().toString());
                            myCustomExercisesList.add(exercise);
                        }
                    }
                }
                setupExercisesRecycler();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupExercisesRecycler() {
        if (myCustomExercisesList.size() == 0) {
            FancyToast.makeText(getActivity(), "You didn't create any custom exercises yet.", FancyToast.LENGTH_LONG, FancyToast.INFO, false).show();
        } else {
            Log.i(TAG, "setupExercisesRecycler: " + myCustomExercisesList.size());
            RecyclerView recyclerView = view.findViewById(R.id.customExerciseRecycler);
            exerciseAdapter = new ExerciseAdapter(getActivity(), myCustomExercisesList, "home");
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(exerciseAdapter);
        }

    }

    public void downloadMyWorkout() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("workout").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myCustomWorkoutList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child("creatorId").getValue().equals(MyConstant.loggedInUserId)) {
                        Workout workout = new Workout();
                        workout.setName(ds.child("name").getValue().toString());
                        workout.setDuration(ds.child("duration").getValue().toString() + " mins");
                        workout.setExercisesNumber(ds.child("exercisesNumber").getValue().toString());
                        workout.setPhotoLink(ds.child("photoLink").getValue().toString());
                        workout.setId(ds.child("id").getValue().toString());

                        myCustomWorkoutList.add(workout);
                    }
                }
                setupWorkoutRecycler();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupWorkoutRecycler() {
        if (myCustomWorkoutList.size() == 0) {
            FancyToast.makeText(getActivity(), "You didn't create any custom workouts yet.", FancyToast.LENGTH_LONG, FancyToast.INFO, false).show();
        } else {
            RecyclerView recyclerView = view.findViewById(R.id.customWorkoutRecycler);
            workoutAdapter = new WorkoutAdapter(getActivity(), "fragmentHome");
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(linearLayoutManager);
            workoutAdapter.setDataSource(myCustomWorkoutList);
            recyclerView.setAdapter(workoutAdapter);
        }

    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

}
