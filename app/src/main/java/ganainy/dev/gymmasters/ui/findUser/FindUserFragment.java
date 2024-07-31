package ganainy.dev.gymmasters.ui.findUser;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.FindUsersFragmentBinding;
import ganainy.dev.gymmasters.models.app_models.User;
import ganainy.dev.gymmasters.ui.main.ActivityCallback;

public class FindUserFragment extends Fragment {

    private static final String TAG = FindUserFragment.class.getSimpleName();
    public static final String SOURCE = "source";
    public static final String ALL = "all";
    public static final String FOLLOWERS = "followers";
    public static final String FOLLOWING = "following";

    List<User> users = new ArrayList<>();
    List<User> filteredUsers = new ArrayList<>();

    private UserAdapter userAdapter;
    private FindUserViewModel mViewModel;
    private FindUsersFragmentBinding binding;

    public static FindUserFragment newInstance(String filterType) {
        FindUserFragment findUserFragment = new FindUserFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SOURCE, filterType);
        findUserFragment.setArguments(bundle);
        return findUserFragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(FindUserViewModel.class);

        if (getArguments() != null && getArguments().getString(SOURCE) != null) {
            switch (getArguments().getString(SOURCE)) {
                case ALL:
                    binding.titleTextView.setText(R.string.users_list);
                    mViewModel.loadAllUsers();
                    break;
                case FOLLOWERS:
                    binding.titleTextView.setText(R.string.my_followers);
                    mViewModel.loadFollowersIds();
                    break;
                case FOLLOWING:
                    binding.titleTextView.setText(R.string.users_iam_following);
                    mViewModel.loadFollowingId();
                    break;
            }
        }

        mViewModel.getNoUsersLiveData().observe(getViewLifecycleOwner(), noUserType -> {
            switch (noUserType) {
                case NO_FOLLOWERS:
                    binding.noUsersLayout.getRoot().setVisibility(View.GONE);
                    binding.noFollowersLayout.getRoot().setVisibility(View.VISIBLE);
                    binding.noFollowingLayout.getRoot().setVisibility(View.GONE);
                    break;
                case NO_FOLLOWING:
                    binding.noUsersLayout.getRoot().setVisibility(View.GONE);
                    binding.noFollowersLayout.getRoot().setVisibility(View.GONE);
                    binding.noFollowingLayout.getRoot().setVisibility(View.VISIBLE);
                    break;
                case NO_USERS:
                    binding.noUsersLayout.getRoot().setVisibility(View.VISIBLE);
                    binding.noFollowersLayout.getRoot().setVisibility(View.GONE);
                    binding.noFollowingLayout.getRoot().setVisibility(View.GONE);
                    break;
            }
        });

        mViewModel.followingUserTransformation.observe(getViewLifecycleOwner(), followingUser -> {
            // Subscribe to trigger transformation
        });

        mViewModel.followerUserTransformation.observe(getViewLifecycleOwner(), followingUser -> {
            // Subscribe to trigger transformation
        });

        mViewModel.userWithRatingTransformation.observe(getViewLifecycleOwner(), userWithRating -> {
            // Subscribe to trigger transformation
        });

        mViewModel.userWithRatingAndFollowerCountTransformation.observe(getViewLifecycleOwner(), userWithRatingAndFollowerCount -> {
            Log.d(TAG, "userWithRatingAndFollowerCountTransformation: " + userWithRatingAndFollowerCount);
            users.add(userWithRatingAndFollowerCount);
            userAdapter.setData(users);
            userAdapter.notifyItemInserted(users.size() - 1);
        });

        mViewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading)
                binding.loadingProgressbar.setVisibility(View.VISIBLE);
            else
                binding.loadingProgressbar.setVisibility(View.GONE);
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FindUsersFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setupRecycler();

        // Set up click listeners
        binding.filterImageView.setOnClickListener(v -> showSearchViewLayout());
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filteredUsers.clear();
                for (User user : users) {
                    if (user.getName().contains(newText)) {
                        filteredUsers.add(user);
                    }
                }
                userAdapter.setData(filteredUsers);
                userAdapter.notifyDataSetChanged();
                return true;
            }
        });

        binding.searchView.setOnCloseListener(() -> {
            hideSearchViewLayout();
            return false;
        });

        binding.backArrowImageView.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    private void showSearchViewLayout() {
        binding.filterImageView.setVisibility(View.GONE);
        binding.backArrowImageView.setVisibility(View.GONE);
        binding.spacer.setVisibility(View.GONE);
        binding.titleTextView.setVisibility(View.GONE);
        binding.searchView.setVisibility(View.VISIBLE);
    }

    private void hideSearchViewLayout() {
        binding.filterImageView.setVisibility(View.VISIBLE);
        binding.backArrowImageView.setVisibility(View.VISIBLE);
        binding.spacer.setVisibility(View.VISIBLE);
        binding.titleTextView.setVisibility(View.VISIBLE);
        binding.searchView.setVisibility(View.GONE);
    }

    private void setupRecycler() {
        userAdapter = new UserAdapter(requireActivity(), (user, adapterPosition) -> {
            ((ActivityCallback) requireActivity()).onOpenUserFragment(user);
        });

        binding.usersRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                /* int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                if(lastVisibleItemPosition >= users.size() - 1) {
                    Log.d(TAG, "onScrolled: " + lastVisibleItemPosition);
                    mViewModel.getMoreUsers(users.get(users.size() - 1).getId());
                } */
            }
        });

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.usersRecycler.getContext(),
                DividerItemDecoration.VERTICAL);
        binding.usersRecycler.addItemDecoration(dividerItemDecoration);
        binding.usersRecycler.setAdapter(userAdapter);
    }
}