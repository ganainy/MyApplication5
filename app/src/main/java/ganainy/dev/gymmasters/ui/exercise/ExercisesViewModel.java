package ganainy.dev.gymmasters.ui.exercise;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ganainy.dev.gymmasters.models.app_models.Exercise;
import ganainy.dev.gymmasters.utils.FirebaseUtils;
import ganainy.dev.gymmasters.utils.NetworkState;

import static ganainy.dev.gymmasters.ui.main.exercisesCategories.ExercisesCategoriesFragment.SHOWALL;

public class ExercisesViewModel extends ViewModel {
    private static final String TAG = "ExerciseActivityViewMod";
    public static final String EXERCISES = "exercises";

    private List<Exercise> selectedMuscleExerciseArrayList = new ArrayList<>();
    private MutableLiveData<List<Exercise>> exerciseListLiveData =new MutableLiveData<>();
    private MutableLiveData<NetworkState> networkStateLiveData =new MutableLiveData<>();

    public LiveData<NetworkState> getNetworkStateLiveData() {
        return networkStateLiveData;
    }
    public LiveData<List<Exercise>> getExerciseListLiveData() {
        return exerciseListLiveData;
    }

    /**load all/certain muscle exercises from firebase database*/
    public void getSelectedMuscleExercises(String muscle) {
        networkStateLiveData.setValue(NetworkState.LOADING);

        final DatabaseReference exercisesRef = FirebaseDatabase.getInstance().getReference(EXERCISES);

        if (!muscle.equals(SHOWALL)) {
            DatabaseReference selectedMuscleRef =  exercisesRef.child(muscle);
            selectedMuscleExerciseArrayList.clear();
            selectedMuscleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds:dataSnapshot.getChildren()) {
                        Exercise exerciseFromSnapshot = FirebaseUtils.getExerciseFromSnapshot(ds);
                        selectedMuscleExerciseArrayList.add(exerciseFromSnapshot);
                    }
                    if (selectedMuscleExerciseArrayList.size()==0)
                        networkStateLiveData.setValue(NetworkState.EMPTY);
                    else {
                        exerciseListLiveData.setValue(selectedMuscleExerciseArrayList);
                        networkStateLiveData.setValue(NetworkState.SUCCESS);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    networkStateLiveData.setValue(NetworkState.ERROR);
                }
            });
        }else {
            loadAllExercises();
        }
    }

    /**extract exercise model from snapshot*/


    private void loadAllExercises() {
        networkStateLiveData.setValue(NetworkState.LOADING);
        final DatabaseReference exercisesNode = FirebaseDatabase.getInstance().getReference(EXERCISES);

        exercisesNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                selectedMuscleExerciseArrayList.clear();
                for (DataSnapshot dsBig : dataSnapshot.getChildren()) {
                    for (DataSnapshot ds : dsBig.getChildren()) {
                        Exercise exerciseFromSnapshot = FirebaseUtils.getExerciseFromSnapshot(ds);
                        selectedMuscleExerciseArrayList.add(exerciseFromSnapshot);
                        }
                }
                if (selectedMuscleExerciseArrayList.size()==0)
                    networkStateLiveData.setValue(NetworkState.EMPTY);
                else {
                    exerciseListLiveData.setValue(selectedMuscleExerciseArrayList);
                    networkStateLiveData.setValue(NetworkState.SUCCESS);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                networkStateLiveData.setValue(NetworkState.ERROR);
            }
        });

    }






}