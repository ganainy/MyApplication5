package ganainy.dev.gymmasters.ui.main.workoutsList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.LoggedUserWorkoutsFragmentBinding;
import ganainy.dev.gymmasters.shared_adapters.WorkoutAdapter;
import ganainy.dev.gymmasters.ui.main.ActivityCallback;
import ganainy.dev.gymmasters.utils.NetworkState;


public class UserWorkoutsFragment extends Fragment {

    public static final String USER_NAME = "userName";

    //todo what is this USER_ID supposed to be?
    private static final String USER_ID = "";
    private UserWorkoutsViewModel mViewModel;
    private WorkoutAdapter workoutAdapter;
    private LoggedUserWorkoutsFragmentBinding binding;

    public UserWorkoutsFragment() {
    }

    public static UserWorkoutsFragment newInstance(String userId, String userName) {
        UserWorkoutsFragment userWorkoutsFragment = new UserWorkoutsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(USER_ID, userId);
        bundle.putString(USER_NAME, userName);
        userWorkoutsFragment.setArguments(bundle);
        return userWorkoutsFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = LoggedUserWorkoutsFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(UserWorkoutsViewModel.class);
        mViewModel.downloadLoggedUserWorkouts(getArguments().getString(USER_ID));

        setupToolbarTitle(getArguments().getString(USER_NAME));

        mViewModel.getWorkoutListLiveData().observe(getViewLifecycleOwner(), workouts -> {
            workoutAdapter.setDataSource(workouts);
            workoutAdapter.notifyDataSetChanged();
        });

        mViewModel.getNetworkStateLiveData().observe(getViewLifecycleOwner(), this::handleNetworkStateUi);
    }

    private void setupToolbarTitle(String username) {
        if (username != null)
            binding.toolbarTitleTextView.setText(username + "'s workouts");
    }

    private void handleNetworkStateUi(NetworkState networkState) {
        switch (networkState) {
            case SUCCESS:
                binding.errorLayout.getRoot().setVisibility(View.INVISIBLE);
                binding.emptyLayout.getRoot().setVisibility(View.INVISIBLE);
                binding.workoutRecyclerView.setVisibility(View.VISIBLE);
                binding.loadingLayoutShimmer.getRoot().setVisibility(View.INVISIBLE);
                break;
            case ERROR:
                binding.errorLayout.getRoot().setVisibility(View.VISIBLE);
                binding.emptyLayout.getRoot().setVisibility(View.INVISIBLE);
                binding.workoutRecyclerView.setVisibility(View.INVISIBLE);
                binding.loadingLayoutShimmer.getRoot().setVisibility(View.INVISIBLE);
                break;
            case LOADING:
                binding.errorLayout.getRoot().setVisibility(View.INVISIBLE);
                binding.emptyLayout.getRoot().setVisibility(View.INVISIBLE);
                binding.workoutRecyclerView.setVisibility(View.INVISIBLE);
                binding.loadingLayoutShimmer.getRoot().setVisibility(View.VISIBLE);
                break;
            case EMPTY:
                binding.errorLayout.getRoot().setVisibility(View.INVISIBLE);
                binding.emptyLayout.getRoot().setVisibility(View.VISIBLE);
                binding.workoutRecyclerView.setVisibility(View.INVISIBLE);
                binding.loadingLayoutShimmer.getRoot().setVisibility(View.INVISIBLE);
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