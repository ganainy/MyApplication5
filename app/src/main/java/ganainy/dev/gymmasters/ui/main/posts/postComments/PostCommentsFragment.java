package ganainy.dev.gymmasters.ui.main.posts.postComments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.WorkoutPostCommentsFragmentBinding;
import ganainy.dev.gymmasters.models.app_models.Exercise;
import ganainy.dev.gymmasters.models.app_models.Post;
import ganainy.dev.gymmasters.models.app_models.User;
import ganainy.dev.gymmasters.models.app_models.Workout;
import ganainy.dev.gymmasters.ui.main.ActivityCallback;
import ganainy.dev.gymmasters.ui.main.posts.PostCallback;
import ganainy.dev.gymmasters.utils.AuthUtils;

public class PostCommentsFragment extends Fragment {

    public static final String TAG = "PostCommentsFragment";
    public static final String POST = "post";

    private PostCommentsAdapter postCommentsAdapter;
    private PostCommentsViewModel mViewModel;
    private WorkoutPostCommentsFragmentBinding binding;

    public static PostCommentsFragment newInstance(Post post) {
        PostCommentsFragment fragment = new PostCommentsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(POST, post);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = WorkoutPostCommentsFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(PostCommentsViewModel.class);
        initRecycler();
        initListeners();

        if (savedInstanceState == null) {
            Post post = getArguments().getParcelable(POST);
            mViewModel.setPost(post);
            mViewModel.getPostComments();
        }

        mViewModel.getPostCommentListLiveData().observe(getViewLifecycleOwner(), postCommentList -> {
            postCommentsAdapter.setData(postCommentList);
            postCommentsAdapter.notifyDataSetChanged();
        });

        mViewModel.getUpdatedPostCommentsLiveData().observe(getViewLifecycleOwner(), updatedPostCommentList -> {
            postCommentsAdapter.setData(updatedPostCommentList);
            postCommentsAdapter.notifyItemInserted(binding.recyclerView.getAdapter().getItemCount());
            binding.recyclerView.smoothScrollToPosition(binding.recyclerView.getAdapter().getItemCount());
        });

        mViewModel.getUpdatedPostLikesLiveData().observe(getViewLifecycleOwner(), updatedPostLikesList -> {
            postCommentsAdapter.setData(updatedPostLikesList);
            postCommentsAdapter.notifyItemChanged(0);
        });

        mViewModel.getLoadingPostCreatorProfileLiveData().observe(getViewLifecycleOwner(), isProfileLoading -> {
            if (isProfileLoading) binding.loadingProfileLayout.getRoot().setVisibility(View.VISIBLE);
            else binding.loadingProfileLayout.getRoot().setVisibility(View.GONE);
        });
    }

    private void initRecycler() {
        postCommentsAdapter = new PostCommentsAdapter(getActivity().getApplication(), new PostCommentCallback() {
            @Override
            public void onExerciseClicked(Exercise exercise) {
                ((ActivityCallback) requireActivity()).openExerciseFragment(exercise);
            }

            @Override
            public void onWorkoutClicked(Workout workout) {
                ((ActivityCallback) requireActivity()).onOpenWorkoutFragment(workout);
            }

            @Override
            public void onUserClicked(String postCreatorId) {
                mViewModel.getUserById(postCreatorId).observe(getViewLifecycleOwner(), postCreatorEvent -> {
                    User postCreator = postCreatorEvent.getContentIfNotHandled();
                    if (postCreator != null) {
                        ((ActivityCallback) requireActivity()).onOpenUserFragment(postCreator);
                    }
                });
            }

            @Override
            public void onPostLike(Post post) {
                mViewModel.likePost();
            }

            @Override
            public void onPostComment(Post post) {
                binding.commentEditText.requestFocus();
            }
        });

        binding.recyclerView.setItemAnimator(null);
        binding.recyclerView.setAdapter(postCommentsAdapter);
    }

    private void initListeners() {
        // Soft keyboard send action on keyboard does the same job as send button
        binding.commentEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                mViewModel.saveComment(binding.commentEditText.getText().toString());
                binding.commentEditText.setText("");
            }
            return false;
        });

        binding.sendImageView.setOnClickListener(v -> {
            if (binding.commentEditText.getText().toString().trim().isEmpty()) {
                binding.commentEditText.startAnimation(shakeError());
            } else {
                mViewModel.saveComment(binding.commentEditText.getText().toString());
                binding.commentEditText.setText("");
            }
        });
    }

    public TranslateAnimation shakeError() {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(500);
        shake.setInterpolator(new CycleInterpolator(7));
        return shake;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;  // Clean up the binding
    }
}