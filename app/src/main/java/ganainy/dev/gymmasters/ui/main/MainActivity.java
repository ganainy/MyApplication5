package ganainy.dev.gymmasters.ui.main;

import static ganainy.dev.gymmasters.ui.findUser.FindUserFragment.ALL;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.ActivityMainBinding;
import ganainy.dev.gymmasters.databinding.AppBarMainBinding;
import ganainy.dev.gymmasters.models.app_models.Exercise;
import ganainy.dev.gymmasters.models.app_models.Post;
import ganainy.dev.gymmasters.models.app_models.User;
import ganainy.dev.gymmasters.models.app_models.Workout;
import ganainy.dev.gymmasters.ui.createExercise.CreateExerciseFragment;
import ganainy.dev.gymmasters.ui.createWorkout.CreateWorkoutFragment;
import ganainy.dev.gymmasters.ui.muscle.MuscleFragment;
import ganainy.dev.gymmasters.ui.findUser.FindUserFragment;
import ganainy.dev.gymmasters.ui.profile.ProfileFragment;
import ganainy.dev.gymmasters.ui.map.MapsActivity;
import ganainy.dev.gymmasters.ui.main.posts.PostsFragment;
import ganainy.dev.gymmasters.ui.main.posts.postComments.PostCommentsFragment;
import ganainy.dev.gymmasters.ui.userExercises.UserExercisesFragment;
import ganainy.dev.gymmasters.ui.main.workoutsList.UserWorkoutsFragment;
import ganainy.dev.gymmasters.ui.specificExercise.ExerciseFragment;
import ganainy.dev.gymmasters.ui.specificExercise.youtubeFragment.YoutubeFragment;
import ganainy.dev.gymmasters.ui.login.LoginActivity;
import ganainy.dev.gymmasters.ui.userInfo.UserFragment;
import ganainy.dev.gymmasters.ui.workout.WorkoutFragment;
import ganainy.dev.gymmasters.utils.AuthUtils;
import ganainy.dev.gymmasters.utils.NetworkChangeReceiver;
import ganainy.dev.gymmasters.utils.NetworkUtil;
import ganainy.dev.gymmasters.utils.SharedPrefUtils;

public class MainActivity extends AppCompatActivity implements ActivityCallback {

    public static final String SOURCE = "source";
    public static final String FOLLOWERS = "followers";
    public static final String FIND = "find";
    public static final String FOLLOWING = "following";
    public NetworkChangeReceiver networkChangeReceiver;
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private AppBarMainBinding appBarMainBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        // Access the included layout
        appBarMainBinding = AppBarMainBinding.bind(binding.getRoot());

        setContentView(binding.getRoot());

        initViews();
    }

    private void initViews() {
        setupViewPager();
        setupDrawerLayout();

        // Handle click on navigation view items
        binding.navView.setNavigationItemSelectedListener(menuItem -> {

            int itemId = menuItem.getItemId();
            if (itemId == R.id.nav_discover) {
                handleDiscoverClick();
            } else if (itemId == R.id.nav_profile) {
                handleProfileClick();
            } else if (itemId == R.id.nav_map) {
                handleMapClick();
            } else if (itemId == R.id.nav_sign_out) {
                handleSignOutClick();
            }
            return false;
        });
    }

    private void setupDrawerLayout() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, appBarMainBinding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(com.mancj.materialsearchbar.R.color.white));
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void handleMapClick() {
        binding.drawerLayout.closeDrawers();
        startActivity(new Intent(this, MapsActivity.class));
    }

    private void handleSignOutClick() {
        binding.drawerLayout.closeDrawers();
        // TODO move to ViewModel
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User logged in with email/password
            mAuth.signOut();
            AuthUtils.setLoggedUserId(null);
            openLoginActivityAndClearTask();
        } else {
            // User logged in with Google account
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    openLoginActivityAndClearTask();
                    AuthUtils.setLoggedUserId(null);
                } else {
                    Toast.makeText(
                                    MainActivity.this,
                                    getString(R.string.error_logging_out) + task.getException(),
                                    Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }
    }

    private void handleDiscoverClick() {
        binding.drawerLayout.closeDrawers();
        openFindUserFragment(ALL);
    }

    private void handleProfileClick() {
        binding.drawerLayout.closeDrawers();
        ProfileFragment profileFragment = ProfileFragment.newInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container, profileFragment).addToBackStack("profileFragment").commit();
    }

    private void setupViewPager() {
        // Setup ViewPager and connect it with TabLayout
        appBarMainBinding.viewPagerMain.setAdapter(new ViewPagerAdapterMainActivity(getSupportFragmentManager()));
        appBarMainBinding.tabLayout.setupWithViewPager(appBarMainBinding.viewPagerMain, true);
        setupTabLayoutIconsAndLabels();
    }

    private void setupTabLayoutIconsAndLabels() {
        appBarMainBinding.tabLayout.getTabAt(0).setIcon(R.drawable.ic_blog_black);
        appBarMainBinding.tabLayout.getTabAt(0).setText(getString(R.string.feed));
        appBarMainBinding.tabLayout.getTabAt(1).setIcon(R.drawable.ic_dumbell_grey);
        appBarMainBinding.tabLayout.getTabAt(2).setIcon(R.drawable.ic_workout_grey);

        appBarMainBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        tab.setText(getString(R.string.feed));
                        tab.setIcon(R.drawable.ic_blog_black);
                        break;
                    case 1:
                        tab.setText(getString(R.string.exercises));
                        tab.setIcon(R.drawable.ic_dumbell_black);
                        break;
                    case 2:
                        tab.setText(getString(R.string.workouts));
                        tab.setIcon(R.drawable.ic_workout_black);
                        break;
                }
                tab.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_LABELED);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_UNLABELED);
                switch (tab.getPosition()) {
                    case 0:
                        tab.setIcon(R.drawable.ic_blog_grey);
                        break;
                    case 1:
                        tab.setIcon(R.drawable.ic_dumbell_grey);
                        break;
                    case 2:
                        tab.setIcon(R.drawable.ic_workout_grey);
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // No-op
            }
        });
    }

    private void openLoginActivityAndClearTask() {
        // Finish this activity on log out so users won't be able to log in on back press in login
        Intent openLoginInNewTaskIntent = new Intent(getApplicationContext(), LoginActivity.class);
        openLoginInNewTaskIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(openLoginInNewTaskIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // When back pressed on MainActivity, close drawer if open, else show dialog to confirm closing app
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            // TODO find cleaner way to show update in comments count when going back from PostCommentsFragment to PostsFragment
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("postCommentsFragment");
            if (fragment != null) {
                // We clicked back button on PostCommentsFragment, refresh posts so if logged user added comment it would show on comment count
                PostsFragment postsFragment = (PostsFragment) getSupportFragmentManager()
                        .findFragmentByTag("android:switcher:" + appBarMainBinding.viewPagerMain.getId() + ":" + 0);

                if (postsFragment != null && SharedPrefUtils.getBoolean(this, SharedPrefUtils.SHOULD_UPDATE_POSTS)) {
                    postsFragment.refreshPosts();
                }
            }
            super.onBackPressed();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.are_you_sure_exit)
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, (arg0, arg1) -> {
                        // Finish this activity as well as all activities immediately below it
                        finishAffinity();
                    })
                    .create().show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        NetworkUtil.unregisterNetworkReceiver(this, networkChangeReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        networkChangeReceiver = NetworkUtil.registerNetworkReceiver(this);
    }

    /** Methods called by child fragments to talk to parent activity */
    @Override
    public void openUserWorkoutsFragment(String userId, String userName) {
        UserWorkoutsFragment userWorkoutsFragment = UserWorkoutsFragment.newInstance(userId, userName);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right,
                        R.anim.exit_to_left,
                        R.anim.enter_from_left,
                        R.anim.exit_to_right);
        fragmentTransaction.add(R.id.container, userWorkoutsFragment).addToBackStack("userWorkoutsFragment").commit();
    }

    @Override
    public void openUserExercisesFragment(String userId, String userName) {
        UserExercisesFragment userExercisesFragment = UserExercisesFragment.newInstance(userId, userName);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right,
                        R.anim.exit_to_left,
                        R.anim.enter_from_left,
                        R.anim.exit_to_right);
        fragmentTransaction.add(R.id.container, userExercisesFragment).addToBackStack("userExercisesFragment").commit();
    }

    @Override
    public void openCreateWorkoutFragment() {
        CreateWorkoutFragment createWorkoutFragment = new CreateWorkoutFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right,
                        R.anim.exit_to_left,
                        R.anim.enter_from_left,
                        R.anim.exit_to_right);
        fragmentTransaction.add(R.id.container, createWorkoutFragment).addToBackStack("createWorkoutFragment").commit();
    }

    @Override
    public void openCreateExerciseFragment() {
        CreateExerciseFragment createExerciseFragment = new CreateExerciseFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right,
                        R.anim.exit_to_left,
                        R.anim.enter_from_left,
                        R.anim.exit_to_right);
        fragmentTransaction.add(R.id.container, createExerciseFragment).addToBackStack("createExerciseFragment").commit();
    }

    @Override
    public void openExerciseFragment(Exercise exercise) {
        ExerciseFragment exerciseFragment = ExerciseFragment.newInstance(exercise);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right,
                        R.anim.exit_to_left,
                        R.anim.enter_from_left,
                        R.anim.exit_to_right);
        fragmentTransaction.add(R.id.container, exerciseFragment).addToBackStack("exerciseFragment").commit();
    }

    @Override
    public void showLoggedUserFollowers(String key, String value) {
        openFindUserFragment(FOLLOWERS);
    }

    @Override
    public void showUsersFollowedByLoggedUser(String key, String value) {
        openFindUserFragment(FOLLOWING);
    }

    @Override
    public void openYoutubeFragment(String exerciseName) {
        YoutubeFragment youtubeFragment = YoutubeFragment.newInstance(exerciseName);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right,
                        R.anim.exit_to_left,
                        R.anim.enter_from_left,
                        R.anim.exit_to_right);
        fragmentTransaction.add(R.id.container, youtubeFragment).addToBackStack("youtubeFragment").commit();
    }

    @Override
    public void onOpenFindUserFragment(String filterType) {
        openFindUserFragment(filterType);
    }

    private void openFindUserFragment(String value) {
        FindUserFragment findUserFragment = FindUserFragment.newInstance(value);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right,
                        R.anim.exit_to_left,
                        R.anim.enter_from_left,
                        R.anim.exit_to_right);
        fragmentTransaction.add(R.id.container, findUserFragment).addToBackStack("findUserFragment").commit();
    }

    @Override
    public void onOpenPostCommentFragment(Post post) {
        PostCommentsFragment postCommentsFragment = PostCommentsFragment.newInstance(post);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right,
                        R.anim.exit_to_left,
                        R.anim.enter_from_left,
                        R.anim.exit_to_right);
        fragmentTransaction.add(R.id.container, postCommentsFragment, "postCommentsFragment").addToBackStack("postCommentsFragment").commit();
    }

    @Override
    public void onOpenWorkoutFragment(Workout workout) {
        WorkoutFragment workoutFragment = WorkoutFragment.newInstance(workout);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right,
                        R.anim.exit_to_left,
                        R.anim.enter_from_left,
                        R.anim.exit_to_right);
        fragmentTransaction.add(R.id.container, workoutFragment).addToBackStack("workoutFragment").commit();
    }

    @Override
    public void onOpenMuscleFragment(String muscleName) {
        MuscleFragment muscleFragment = MuscleFragment.newInstance(muscleName);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right,
                        R.anim.exit_to_left,
                        R.anim.enter_from_left,
                        R.anim.exit_to_right);
        fragmentTransaction.add(R.id.container, muscleFragment).addToBackStack("muscleFragment").commit();
    }

    @Override
    public void onOpenUserFragment(User user) {
        UserFragment userFragment = UserFragment.newInstance(user);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right,
                        R.anim.exit_to_left,
                        R.anim.enter_from_left,
                        R.anim.exit_to_right);
        fragmentTransaction.add(R.id.container, userFragment).addToBackStack("userFragment").commit();
    }
}