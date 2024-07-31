package ganainy.dev.gymmasters.ui.muscle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.LoadingLayoutShimmerBinding;
import ganainy.dev.gymmasters.databinding.MuscleFragmentBinding;
import ganainy.dev.gymmasters.models.app_models.Exercise;
import ganainy.dev.gymmasters.shared_adapters.ExercisesAdapter;
import ganainy.dev.gymmasters.ui.main.ActivityCallback;
import ganainy.dev.gymmasters.utils.MiscellaneousUtils;
import ganainy.dev.gymmasters.utils.NetworkState;

import static ganainy.dev.gymmasters.ui.main.exercisesCategories.ExercisesCategoriesFragment.SELECTED_MUSCLE;

public class MuscleFragment extends Fragment {
    private static final String TAG = "ExercisesActivity";
    public static final String EXERCISE = "exercise";
    MuscleViewModel mViewModel;

    private MuscleFragmentBinding binding;
    private LoadingLayoutShimmerBinding loadingLayoutShimmerBinding;
    private ExercisesAdapter exercisesAdapter;

    public static MuscleFragment newInstance(String selectedMuscle) {
        MuscleFragment muscleFragment = new MuscleFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SELECTED_MUSCLE, selectedMuscle);
        muscleFragment.setArguments(bundle);
        return muscleFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = MuscleFragmentBinding.inflate(inflater, container, false);
        loadingLayoutShimmerBinding=LoadingLayoutShimmerBinding.bind(binding.loadingLayoutShimmer.getRoot());
        View view = binding.getRoot();

        setupRecycler();
        setupSearchView();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(MuscleViewModel.class);

        if (getArguments().getString(SELECTED_MUSCLE) != null) {
            String selectedMuscle = getArguments().getString(SELECTED_MUSCLE);
            setTabHeaderImage(selectedMuscle);
            mViewModel.getSelectedMuscleExercises(selectedMuscle);
        }

        mViewModel.getExerciseListLiveData().observe(getViewLifecycleOwner(), exercises -> {
            exercisesAdapter.setData(exercises);
            exercisesAdapter.notifyDataSetChanged();
        });

        mViewModel.getNetworkStateLiveData().observe(getViewLifecycleOwner(), this::handleNetworkStateUi);
    }

    private void handleNetworkStateUi(NetworkState networkState) {
        switch (networkState) {
            case SUCCESS:
                binding.errorLayout.getRoot().setVisibility(View.GONE);
                binding.emptyLayout.getRoot().setVisibility(View.GONE);
                binding.exercisesRecyclerView.setVisibility(View.VISIBLE);
                loadingLayoutShimmerBinding.getRoot().setVisibility(View.GONE);
                break;
            case ERROR:
                binding.errorLayout.getRoot().setVisibility(View.VISIBLE);
                binding.emptyLayout.getRoot().setVisibility(View.GONE);
                binding.exercisesRecyclerView.setVisibility(View.GONE);
                loadingLayoutShimmerBinding.getRoot().setVisibility(View.GONE);
                break;
            case LOADING:
                binding.errorLayout.getRoot().setVisibility(View.GONE);
                binding.emptyLayout.getRoot().setVisibility(View.GONE);
                binding.exercisesRecyclerView.setVisibility(View.GONE);
                loadingLayoutShimmerBinding.getRoot().setVisibility(View.VISIBLE);
                break;
            case EMPTY:
                binding.errorLayout.getRoot().setVisibility(View.GONE);
                binding.emptyLayout.getRoot().setVisibility(View.VISIBLE);
                binding.exercisesRecyclerView.setVisibility(View.GONE);
                loadingLayoutShimmerBinding.getRoot().setVisibility(View.GONE);
                break;
        }
    }

    /**
     * set header image based on selected muscle from previous fragment
     */
    private void setTabHeaderImage(String selectedMuscle) {
        binding.htabHeader.setImageResource(MiscellaneousUtils.getImageId(requireActivity(), selectedMuscle));
    }

    private void setupRecycler() {
        //handle click of certain exercise
        exercisesAdapter = new ExercisesAdapter(requireActivity(), clickedExercise -> {
            ((ActivityCallback) requireActivity()).openExerciseFragment(clickedExercise);
        });
        binding.exercisesRecyclerView.setAdapter(exercisesAdapter);
    }

    private void setupSearchView() {
        //do filtering when I type in search or click search
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryString) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String queryString) {
                ArrayList<Exercise> filteredExercises = new ArrayList<>();
                for (Exercise exercise : mViewModel.getSelectedMuscleExerciseList()) {
                    if (exercise.getName().toLowerCase().contains(queryString)) {
                        filteredExercises.add(exercise);
                    }
                }
                exercisesAdapter.setData(filteredExercises);
                exercisesAdapter.notifyDataSetChanged();
                return false;
            }
        });
    }
}