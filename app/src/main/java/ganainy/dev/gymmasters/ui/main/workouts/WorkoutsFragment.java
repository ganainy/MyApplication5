package ganainy.dev.gymmasters.ui.main.workouts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.FragmentWorkoutsBinding;
import ganainy.dev.gymmasters.shared_adapters.WorkoutAdapter;
import ganainy.dev.gymmasters.ui.main.ActivityCallback;
import ganainy.dev.gymmasters.utils.NetworkState;

/**
 * A simple {@link Fragment} subclass.
 */
public class WorkoutsFragment extends Fragment {

    private static final String TAG = "MainFragmentWorkouts";
    public static final String WORKOUT = "workout";
    private WorkoutAdapter workoutAdapter;
    private WorkoutsViewModel workoutsViewModel;
    private FragmentWorkoutsBinding binding;

    public WorkoutsFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance() {
        return new WorkoutsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentWorkoutsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        workoutsViewModel = new ViewModelProvider(this).get(WorkoutsViewModel.class);

        workoutsViewModel.getWorkoutListLiveData().observe(getViewLifecycleOwner(), workouts -> {
            workoutAdapter.setDataSource(workouts);
            workoutAdapter.notifyDataSetChanged();
        });

        workoutsViewModel.getNetworkStateLiveData().observe(getViewLifecycleOwner(), this::handleNetworkStateUi);
    }

    private void handleNetworkStateUi(NetworkState networkState) {
        // Ensure visibility changes are done on the root view of the binding
        View root = binding.getRoot();

        switch (networkState) {
            case SUCCESS:
                binding.errorLayout.getRoot().setVisibility(View.GONE);
                binding.emptyLayout.getRoot().setVisibility(View.GONE);
                binding.workoutRecyclerView.setVisibility(View.VISIBLE);
                binding.loadingLayoutShimmer.getRoot().setVisibility(View.GONE);
                break;
            case ERROR:
                binding.errorLayout.getRoot().setVisibility(View.VISIBLE);
                binding.emptyLayout.getRoot().setVisibility(View.GONE);
                binding.workoutRecyclerView.setVisibility(View.GONE);
                binding.loadingLayoutShimmer.getRoot().setVisibility(View.GONE);
                break;
            case LOADING:
                binding.errorLayout.getRoot().setVisibility(View.GONE);
                binding.emptyLayout.getRoot().setVisibility(View.GONE);
                binding.workoutRecyclerView.setVisibility(View.GONE);
                binding.loadingLayoutShimmer.getRoot().setVisibility(View.VISIBLE);
                break;
            case EMPTY:
                binding.errorLayout.getRoot().setVisibility(View.GONE);
                binding.emptyLayout.getRoot().setVisibility(View.VISIBLE);
                binding.workoutRecyclerView.setVisibility(View.GONE);
                binding.loadingLayoutShimmer.getRoot().setVisibility(View.GONE);
                break;
        }
    }

    private void setupRecycler() {
        workoutAdapter = new WorkoutAdapter(requireContext().getApplicationContext(), clickedWorkout -> {
            ((ActivityCallback) requireActivity()).onOpenWorkoutFragment(clickedWorkout);
        });
        binding.workoutRecyclerView.setAdapter(workoutAdapter);
    }
}