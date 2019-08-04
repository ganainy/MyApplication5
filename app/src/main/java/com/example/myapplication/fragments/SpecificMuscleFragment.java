package com.example.myapplication.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;
import com.example.myapplication.activities.ExercisesActivity;
import com.example.myapplication.adapters.ExerciseAdapter;
import com.example.myapplication.model.Exercise;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SpecificMuscleFragment extends Fragment {

    private static final String TAG = "SpecificMuscleFragment";

    public SpecificMuscleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.fragment_specific_muscle, container, false);

    //get selected muscle from  ExercisesActivity
        ExercisesActivity activity = (ExercisesActivity) getActivity();
        String myDataFromActivity = activity.getMyData();
        Log.i(TAG, "onCreateView: " + myDataFromActivity);

        getExercise(myDataFromActivity, view);


        return view;
    }


    //load exercises from firebase database
    private void getExercise(String myDataFromActivity, final View view) {
        DatabaseReference exercisesNode = FirebaseDatabase.getInstance().getReference("excercises");
        DatabaseReference myRef = null;

        //get exercises only for the selected muscle by passing it from the exercise activity to this fragment
        switch (myDataFromActivity) {
            case "triceps": {
                myRef = exercisesNode.child("triceps");
                break;
            }
            //TODO add other cases


        }

        //once we selected the right muscle group node this could will be the same for all exercises info
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Exercise> exerciseList = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Exercise exercise = new Exercise();
                    exercise.setName(ds.child("Name").getValue().toString());
                    exercise.setExcecution(ds.child("Execution").getValue().toString());
                    exercise.setPreperation(ds.child("Preparation").getValue().toString());
                    exercise.setBodyPart(ds.child("bodyPart").getValue().toString());
                    exercise.setMechanism(ds.child("mechanic").getValue().toString());
                    exercise.setPreviewPhoto1(ds.child("preview").getValue().toString());
                    exercise.setPreviewPhoto2(ds.child("preview2").getValue().toString());
                    exercise.setUtility(ds.child("utility").getValue().toString());
                    exercise.setVideoLink(ds.child("videoLink").getValue().toString());
                    exerciseList.add(exercise);
                    Log.i(TAG, "onDataChange: " + exercise.getName());
                }

                //after loading exercises show them in the recycler view
                setupRecycler(view, exerciseList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void setupRecycler(View view, List<Exercise> exerciseList) {
        RecyclerView recyclerView = view.findViewById(R.id.exerciseRecyclerView);
        ExerciseAdapter exerciseAdapter = new ExerciseAdapter(getActivity());
        exerciseAdapter.setDataSource(exerciseList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(exerciseAdapter);
        Log.i(TAG, "list size: " + exerciseList.size());
    }


}
