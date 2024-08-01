package ganainy.dev.gymmasters.ui.userExercises;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.UserExercisesFragmentBinding;
import ganainy.dev.gymmasters.shared_adapters.ExercisesAdapter;
import ganainy.dev.gymmasters.ui.main.ActivityCallback;
import ganainy.dev.gymmasters.utils.NetworkState;


public class UserExercisesFragment extends Fragment {

    public static final String USER_NAME = "userName";

    //todo what is this user id supposed to be>?
    private static final String USER_ID = "";

    private UserExercisesFragmentBinding binding;
    private ExercisesAdapter exercisesAdapter;
    private UserExercisesViewModel mViewModel;

    public static UserExercisesFragment newInstance(String userId, String userName) {
        UserExercisesFragment userExercisesFragment = new UserExercisesFragment();
        Bundle bundle = new Bundle();
        bundle.putString(USER_ID, userId);
        bundle.putString(USER_NAME, userName);
        userExercisesFragment.setArguments(bundle);
        return userExercisesFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = UserExercisesFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(UserExercisesViewModel.class);

        // Arguments can't be null since they're passed with fragment instantiation
        mViewModel.downloadLoggedUserExercises(getArguments().getString(USER_ID));

        setToolbarTitle(getArguments().getString(USER_NAME));

        mViewModel.getExerciseListLiveData().observe(getViewLifecycleOwner(), exercises -> {
            exercisesAdapter.setData(exercises);
            exercisesAdapter.notifyDataSetChanged();
        });

        mViewModel.getNetworkStateLiveData().observe(getViewLifecycleOwner(), this::handleNetworkStateUi);

        // Set up the RecyclerView
        setupRecycler();

        // Set up the back arrow click listener
        binding.backArrowImageView.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void setToolbarTitle(String userName) {
        if (userName != null) {
            binding.toolbarTitleTextView.setText(userName + "'s exercises");
        }
    }

    private void setupRecycler() {
        exercisesAdapter = new ExercisesAdapter(requireActivity().getApplicationContext(), exercise -> {
            // Handle click of certain exercise
            ActivityCallback activityCallback = (ActivityCallback) requireActivity();
            activityCallback.openExerciseFragment(exercise);
        });
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.exercisesRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        binding.exercisesRecyclerView.addItemDecoration(dividerItemDecoration);
        binding.exercisesRecyclerView.setAdapter(exercisesAdapter);
    }

    private void handleNetworkStateUi(NetworkState networkState) {
        switch (networkState) {
            case SUCCESS:
                binding.errorLayout.getRoot().setVisibility(View.INVISIBLE);
                binding.emptyLayout.getRoot().setVisibility(View.INVISIBLE);
                binding.exercisesRecyclerView.setVisibility(View.VISIBLE);
                binding.loadingLayoutShimmer.getRoot().setVisibility(View.INVISIBLE);
                break;
            case ERROR:
                binding.errorLayout.getRoot().setVisibility(View.VISIBLE);
                binding.emptyLayout.getRoot().setVisibility(View.INVISIBLE);
                binding.exercisesRecyclerView.setVisibility(View.INVISIBLE);
                binding.loadingLayoutShimmer.getRoot().setVisibility(View.INVISIBLE);
                break;
            case LOADING:
                binding.errorLayout.getRoot().setVisibility(View.INVISIBLE);
                binding.emptyLayout.getRoot().setVisibility(View.INVISIBLE);
                binding.exercisesRecyclerView.setVisibility(View.INVISIBLE);
                binding.loadingLayoutShimmer.getRoot().setVisibility(View.VISIBLE);
                break;
            case EMPTY:
                binding.errorLayout.getRoot().setVisibility(View.INVISIBLE);
                binding.emptyLayout.getRoot().setVisibility(View.VISIBLE);
                binding.exercisesRecyclerView.setVisibility(View.INVISIBLE);
                binding.loadingLayoutShimmer.getRoot().setVisibility(View.INVISIBLE);
                break;
        }
    }
}