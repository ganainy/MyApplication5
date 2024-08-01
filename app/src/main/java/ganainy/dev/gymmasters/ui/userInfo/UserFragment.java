package ganainy.dev.gymmasters.ui.userInfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.UserFragmentBinding;
import ganainy.dev.gymmasters.models.app_models.Exercise;
import ganainy.dev.gymmasters.models.app_models.User;
import ganainy.dev.gymmasters.models.app_models.Workout;
import ganainy.dev.gymmasters.ui.main.ActivityCallback;

public class UserFragment extends Fragment {

    public static final String USER = "user";
    private UserViewModel mViewModel;
    private static final String TAG = "UserFragment";

    private UserFragmentBinding binding;

    public static UserFragment newInstance(User user) {
        UserFragment userFragment = new UserFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(USER, user);
        userFragment.setArguments(bundle);
        return userFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = UserFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null && getArguments().getParcelable(USER) != null) {
            User mUser = getArguments().getParcelable(USER);
            setToolbarTitle(mUser.getName());
            initViewModel(mUser);

            // Observe changes in the user profile model
            mViewModel.getUserProfileModelLiveData().observe(getViewLifecycleOwner(), userProfileModel -> {
                showDataInView(userProfileModel.getProfileOwner());
                updateProfileExercisesView(userProfileModel.getExercisesList(), userProfileModel.getProfileOwner().getName());
                updateProfileWorkoutView(userProfileModel.getWorkoutList(), userProfileModel.getProfileOwner().getName());
                updateFollowState(userProfileModel.getFollowState());

                if (userProfileModel.getFollowersCount() != null) {
                    binding.followerCountShimmer.setText(userProfileModel.getFollowersCount().toString());
                }
                if (userProfileModel.getRatingAverage() != null) {
                    binding.ratingAverageShimmer.setText(userProfileModel.getRatingAverage() + "/5");
                }
                if (userProfileModel.getFollowingCount() != null) {
                    binding.followingCountShimmer.setText(userProfileModel.getFollowingCount().toString());
                }
                if (userProfileModel.getLoggedUserRating() != null) {
                    setMyRate(userProfileModel.getLoggedUserRating());
                }
                if (userProfileModel.getProfileOwner().getName() != null) {
                    setAboutCardTitle(userProfileModel.getProfileOwner().getName());
                }
            });

            // Download user profile photo
            Glide.with(requireActivity())
                    .load(mUser.getPhoto())
                    .apply(new RequestOptions().placeholder(R.drawable.loading_animation).error(R.drawable.anonymous_profile))
                    .into(binding.profileImageShimmer);

            // Set up click listeners
            binding.followButton.setOnClickListener(v -> mViewModel.followUnfollow());
            binding.rateButton.setOnClickListener(v -> rate());
            binding.backArrowImageView.setOnClickListener(v -> requireActivity().onBackPressed());
            binding.showExercisesLayout.setOnClickListener(v -> {
                ActivityCallback activityCallback = (ActivityCallback) requireActivity();
                activityCallback.openUserExercisesFragment(mViewModel.getUserProfile().getProfileOwner().getId(),
                        mViewModel.getUserProfile().getProfileOwner().getName());
            });
            binding.showWorkoutsLayout.setOnClickListener(v -> {
                ActivityCallback activityCallback = (ActivityCallback) requireActivity();
                activityCallback.openUserWorkoutsFragment(mViewModel.getUserProfile().getProfileOwner().getId(),
                        mViewModel.getUserProfile().getProfileOwner().getName());
            });
        }
    }

    private void setToolbarTitle(String name) {
        if (name != null) {
            binding.toolbarTitleTextView.setText(name + " profile");
        }
    }

    private void setAboutCardTitle(String profileOwnerName) {
        binding.aboutUserTitleTextView.setText("About " + profileOwnerName + " :");
    }

    private void updateFollowState(FollowState followState) {
        switch (followState) {
            case FOLLOWING:
            case NOT_FOLLOWING:
                changeFollowButtonColors(followState);
                break;
            case ERROR:
                // Handle error state if necessary
                break;
        }
    }

    private void initViewModel(User mUser) {
        UserViewModelFactory userViewModelFactory = new UserViewModelFactory(requireActivity().getApplication(), mUser);
        mViewModel = new ViewModelProvider(this, userViewModelFactory).get(UserViewModel.class);
    }

    private void setMyRate(@Nullable Long aLong) {
        binding.rateButton.setBackground(getResources().getDrawable(R.drawable.circular_green_bordersolid)); // Green
        binding.rateButton.setImageResource(R.drawable.ic_star_yellow);
        // binding.rateButton.setText("i rated: " + aLong + "/5");
    }

    private void changeFollowButtonColors(FollowState followState) {
        if (followState == FollowState.FOLLOWING) {
            binding.followButton.setBackgroundResource(R.drawable.btn_add_green); // Green
            binding.followButton.setText(getString(R.string.following));
            binding.followButton.setTextColor(ContextCompat.getColor(requireContext(), com.mancj.materialsearchbar.R.color.white));
        } else if (followState == FollowState.NOT_FOLLOWING) {
            binding.followButton.setBackgroundResource(R.drawable.circular_light_grey_bordersolid);
            binding.followButton.setText(getString(R.string.follow));
            binding.followButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_grey));
        }
    }

    private void updateProfileExercisesView(List<Exercise> exerciseList, String profileOwnerName) {
        if (exerciseList == null || exerciseList.isEmpty()) {
            binding.exerciseCountFullTextView.setText(profileOwnerName + " has no custom exercises yet");
            binding.clickToViewExercises.setVisibility(View.GONE);
            binding.exercisesCountTextView.setText("0");
        } else {
            binding.clickToViewExercises.setVisibility(View.VISIBLE);
            binding.exerciseCountFullTextView.setText(profileOwnerName + " created " + exerciseList.size() + " custom exercises");
            binding.exercisesCountTextView.setText(Integer.toString(exerciseList.size()));
        }
    }

    private void updateProfileWorkoutView(List<Workout> workoutList, String profileOwnerName) {
        if (workoutList == null || workoutList.isEmpty()) {
            binding.workoutCountFullTextView.setText(profileOwnerName + " has no custom workouts yet");
            binding.clickToViewWorkouts.setVisibility(View.GONE);
            binding.workoutCountTextView.setText("0");
        } else {
            binding.clickToViewWorkouts.setVisibility(View.VISIBLE);
            binding.workoutCountFullTextView.setText(profileOwnerName + " created " + workoutList.size() + " custom workouts");
            binding.workoutCountTextView.setText(Integer.toString(workoutList.size()));
        }
    }

    private void showDataInView(User profileOwner) {
        binding.nameShimmer.setText(profileOwner.getName());
        binding.emailShimmer.setText(profileOwner.getEmail());
        if (profileOwner.getAbout_me() != null && !profileOwner.getAbout_me().equals("")) {
            binding.aboutUserContentTextView.setText(profileOwner.getAbout_me());
        } else {
            binding.aboutUserContentTextView.setText(profileOwner.getName() + " didn't add this information yet");
        }
    }

    private void rate() {
        final View rateView = getLayoutInflater().inflate(R.layout.rate_view, null);
        final RatingBar ratingBar = rateView.findViewById(R.id.ratingBar);

        new AlertDialog.Builder(requireActivity())
                .setTitle("Rate user")
                .setView(rateView)
                .setPositiveButton("Rate", (dialog, which) -> {
                    int rating = ratingBar.getProgress();
                    mViewModel.setRate((long) rating);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}