package ganainy.dev.gymmasters.ui.main.posts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.PostsFragmentBinding;
import ganainy.dev.gymmasters.models.app_models.Exercise;
import ganainy.dev.gymmasters.models.app_models.Post;
import ganainy.dev.gymmasters.models.app_models.User;
import ganainy.dev.gymmasters.models.app_models.Workout;
import java.util.Collections;
import java.util.List;
import ganainy.dev.gymmasters.ui.main.ActivityCallback;
import ganainy.dev.gymmasters.utils.AuthUtils;
import static ganainy.dev.gymmasters.ui.findUser.FindUserFragment.ALL;

public class PostsFragment extends Fragment {
    private PostsViewModel mViewModel;
    private PostsAdapter postsAdapter;
    private PostsFragmentBinding binding;

    public static PostsFragment newInstance() {
        return new PostsFragment();
    }

    public PostsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PostsFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setupRecycler();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(PostsViewModel.class);

        saveLoggedUser();

        mViewModel.getFollowingUid();

        mViewModel.getNetworkStateLiveData().observe(getViewLifecycleOwner(), networkState -> {
            switch (networkState) {
                case SUCCESS:
                    binding.errorTextView.setVisibility(View.GONE);
                    binding.emptyPostsLayout.getRoot().setVisibility(View.GONE);
                    binding.loadingGroup.setVisibility(View.GONE);
                    break;
                case ERROR:
                    binding.errorTextView.setVisibility(View.VISIBLE);
                    binding.emptyPostsLayout.getRoot().setVisibility(View.GONE);
                    binding.loadingGroup.setVisibility(View.GONE);
                    break;
                case LOADING:
                    binding.errorTextView.setVisibility(View.GONE);
                    binding.emptyPostsLayout.getRoot().setVisibility(View.GONE);
                    binding.loadingGroup.setVisibility(View.VISIBLE);
                    break;
                case EMPTY:
                    binding.errorTextView.setVisibility(View.GONE);
                    binding.emptyPostsLayout.getRoot().setVisibility(View.VISIBLE);
                    binding.loadingGroup.setVisibility(View.GONE);
                    break;
                case STOP_LOADING:
                    break;
            }
        });

        mViewModel.getPostListLiveData().observe(getViewLifecycleOwner(), posts -> {
            Collections.sort(posts, (s1, s2) -> s2.getDateStamp().compareTo(s1.getDateStamp()));
            postsAdapter.setData(posts);
            postsAdapter.notifyDataSetChanged();
        });

        mViewModel.getUpdatePostLiveData().observe(getViewLifecycleOwner(), postsEvent -> {
            Pair<List<Post>, Integer> postsPositionPair = postsEvent.getContentIfNotHandled();
            if (postsPositionPair != null) {
                postsAdapter.setData(postsPositionPair.first);
                postsAdapter.notifyItemChanged(postsPositionPair.second);
            }
        });

        mViewModel.getLoadingPostCreatorProfileLiveData().observe(getViewLifecycleOwner(), isProfileLoading -> {
            if (isProfileLoading) binding.loadingProfileLayout.getRoot().setVisibility(View.VISIBLE);
            else binding.loadingProfileLayout.getRoot().setVisibility(View.GONE);
        });
    }

    private void saveLoggedUser() {
        mViewModel.getLoggedUser().observe(getViewLifecycleOwner(), loggedUser -> {
            AuthUtils.putUser(requireContext(), loggedUser);
        });
    }

    private void setupRecycler() {
        postsAdapter = new PostsAdapter(requireActivity().getApplication(), new PostCallback() {
            @Override
            public void onExerciseClicked(Exercise exercise, Integer adapterPosition) {
                ((ActivityCallback) requireActivity()).openExerciseFragment(exercise);
            }

            @Override
            public void onWorkoutClicked(Workout workout, Integer adapterPosition) {
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
            public void onPostLike(Post post, Integer adapterPosition) {
                mViewModel.likePost(post, adapterPosition);
            }

            @Override
            public void onPostComment(Post post, Integer postType) {
                openPostCommentFragment(post);
            }
        });

        postsAdapter.setHasStableIds(true);
        binding.sharedRv.setItemAnimator(null);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.sharedRv.getContext(),
                DividerItemDecoration.VERTICAL);
        binding.sharedRv.addItemDecoration(dividerItemDecoration);

        binding.sharedRv.setAdapter(postsAdapter);

         binding.emptyPostsLayout.findUsersButton.setOnClickListener(v -> {
            ActivityCallback activityCallback = (ActivityCallback) requireActivity();
            activityCallback.onOpenFindUserFragment(ALL);
        });
    }

    private void openPostCommentFragment(Post post) {
        ActivityCallback activityCallback = (ActivityCallback) requireActivity();
        activityCallback.onOpenPostCommentFragment(post);
    }

    public void refreshPosts() {
        postsAdapter.setData(null);
        mViewModel.clearFollowingIdList();
        mViewModel.getFollowingUid();
    }
}