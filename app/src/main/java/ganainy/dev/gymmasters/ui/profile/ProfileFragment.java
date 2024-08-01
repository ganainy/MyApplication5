package ganainy.dev.gymmasters.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;
import com.facebook.shimmer.ShimmerFrameLayout;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.FragmentProfileBinding;
import ganainy.dev.gymmasters.models.app_models.User;
import ganainy.dev.gymmasters.ui.main.ActivityCallback;
import ganainy.dev.gymmasters.utils.AuthUtils;

import static ganainy.dev.gymmasters.ui.main.MainActivity.FOLLOWERS;
import static ganainy.dev.gymmasters.ui.main.MainActivity.FOLLOWING;
import static ganainy.dev.gymmasters.ui.main.MainActivity.SOURCE;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE = 101;

    private Handler mHandler = new Handler();
    private FragmentProfileBinding binding;
    private ProfileViewModel mViewModel;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        mViewModel.getUserData(AuthUtils.getLoggedUserId(requireContext()));
        mViewModel.getFollowersCount(AuthUtils.getLoggedUserId(requireContext()));
        mViewModel.getFollowingCount(AuthUtils.getLoggedUserId(requireContext()));
        mViewModel.getRatingsAvg(AuthUtils.getLoggedUserId(requireContext()));

        mViewModel.getUserLiveData().observe(getViewLifecycleOwner(), this::setupUi);

        mViewModel.getFollowersCountLiveData().observe(getViewLifecycleOwner(), followerCount -> {
            binding.followersCountTextView.setText(followerCount);
        });

        mViewModel.getFollowingCountLiveData().observe(getViewLifecycleOwner(), followingCount -> {
            binding.followingCountTextView.setText(followingCount);
        });

        mViewModel.getRatingAverageLiveData().observe(getViewLifecycleOwner(), averageRating -> {
            binding.ratingAverageTextView.setText(getString(R.string.rating_formula, averageRating));
        });

        mViewModel.getUpdateAboutMe().observe(getViewLifecycleOwner(), aboutMePair -> {
            if (aboutMePair.first) {
                // Update was successful
                binding.aboutUserContentTextView.setText(aboutMePair.second);
            } else {
                // Update failed, keep old about me
            }
        });

        mViewModel.getImageUriLiveData().observe(getViewLifecycleOwner(), imageUri -> {
            mHandler.postDelayed(() -> {
                binding.profileImage.setImageURI(imageUri);
            }, 100);
        });

        mViewModel.getUploadingStateLiveData().observe(getViewLifecycleOwner(), isUploading -> {
            binding.progressBarUploadImage.setVisibility(isUploading ? View.VISIBLE : View.GONE);
        });

        mViewModel.getImageUploadSuccessLiveData().observe(getViewLifecycleOwner(), isUploadProfileImageSuccessfulEvent -> {
            Boolean isImageUploadSuccessful = isUploadProfileImageSuccessfulEvent.getContentIfNotHandled();
            if (isImageUploadSuccessful != null && isImageUploadSuccessful) {
                Toast.makeText(requireActivity(), getString(R.string.profile_image_updated_successfully), Toast.LENGTH_LONG).show();
            } else if (isImageUploadSuccessful != null) {
                Toast.makeText(requireActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
            }
        });

        setupButtonClicks();
    }

    private void setupButtonClicks() {
        binding.createWorkout.setOnClickListener(v -> {
            ActivityCallback activityCallback = (ActivityCallback) requireActivity();
            activityCallback.openCreateWorkoutFragment();
        });

        binding.createExercise.setOnClickListener(v -> {
            ActivityCallback activityCallback = (ActivityCallback) requireActivity();
            activityCallback.openCreateExerciseFragment();
        });

        binding.viewLoggedUserExercises.setOnClickListener(v -> {
            ActivityCallback activityCallback = (ActivityCallback) requireActivity();
            activityCallback.openUserExercisesFragment(AuthUtils.getLoggedUserId(requireContext()), null);
        });

        binding.viewLoggedUserWorkouts.setOnClickListener(v -> {
            ActivityCallback activityCallback = (ActivityCallback) requireActivity();
            activityCallback.openUserWorkoutsFragment(AuthUtils.getLoggedUserId(requireContext()), null);
        });

        binding.viewMyFollowersButton.setOnClickListener(v -> {
            ActivityCallback activityCallback = (ActivityCallback) requireActivity();
            activityCallback.showLoggedUserFollowers(SOURCE, FOLLOWERS);
        });

        binding.viewUsersIamFollowingButton.setOnClickListener(v -> {
            ActivityCallback activityCallback = (ActivityCallback) requireActivity();
            activityCallback.showUsersFollowedByLoggedUser(SOURCE, FOLLOWING);
        });

        binding.imageButtonChangePicture.setOnClickListener(v -> openImageChooser());

        binding.edit.setOnClickListener(v -> showAlertDialog());
    }

    private void setupUi(User user) {
        binding.textViewName.setText(user.getName());
        binding.emailTextInputLayout.setText(user.getEmail());
        loadPhotoWithShimmerEffect(user);
        setAboutMeText(user);

        mHandler.postDelayed(() -> {
            requireActivity().runOnUiThread(() -> {
                hideLoadingShimmerViews();
                showDataView();
            });
        }, 1500);
    }

    private void setAboutMeText(User user) {
        if (user.getAbout_me() == null) {
            binding.aboutUserContentTextView.setText(R.string.not_added_yet);
        } else {
            binding.aboutUserContentTextView.setText(user.getAbout_me());
        }
    }

    private void showDataView() {
        binding.textViewName.setVisibility(View.VISIBLE);
        binding.emailTextInputLayout.setVisibility(View.VISIBLE);
        binding.followersCountTextView.setVisibility(View.VISIBLE);
        binding.followingCountTextView.setVisibility(View.VISIBLE);
        binding.ratingAverageTextView.setVisibility(View.VISIBLE);
        binding.aboutUserContentTextView.setVisibility(View.VISIBLE);
    }

    private void hideLoadingShimmerViews() {
        binding.nameShimmer.setVisibility(View.INVISIBLE);
        binding.emailShimmer.setVisibility(View.INVISIBLE);
        binding.followerCountShimmer.setVisibility(View.INVISIBLE);
        binding.followingCountShimmer.setVisibility(View.INVISIBLE);
        binding.ratingAverageShimmer.setVisibility(View.INVISIBLE);
        binding.aboutUserContentShimmer.setVisibility(View.INVISIBLE);
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    private void showAlertDialog() {
        final View alertDialogView = getLayoutInflater().inflate(R.layout.edit_view, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setView(alertDialogView)
                .show();

        Button saveAlertDialog = alertDialogView.findViewById(R.id.save_action);
        Button cancelAlertDialog = alertDialogView.findViewById(R.id.cancel_action);

        saveAlertDialog.setOnClickListener(view -> {
            EditText editText = alertDialogView.findViewById(R.id.editText);
            mViewModel.updateAboutMe(editText.getText().toString(), AuthUtils.getLoggedUserId(requireContext()));
            alertDialog.dismiss();
        });
        cancelAlertDialog.setOnClickListener(view -> alertDialog.dismiss());
    }

    public void loadPhotoWithShimmerEffect(User user) {
        Shimmer shimmer = new Shimmer.AlphaHighlightBuilder()
                .setDuration(1800)
                .setBaseAlpha(0.7f)
                .setHighlightAlpha(0.6f)
                .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                .setAutoStart(true)
                .build();

        ShimmerDrawable shimmerDrawable = new ShimmerDrawable();
        shimmerDrawable.setShimmer(shimmer);

        Glide.with(this)
                .load(user.getPhoto())
                .apply(new RequestOptions().placeholder(shimmerDrawable).error(R.drawable.anonymous_profile))
                .circleCrop()
                .into(binding.profileImage);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && requestCode == PICK_IMAGE) {
            mViewModel.setImageUri(data.getData());
        }
    }
}