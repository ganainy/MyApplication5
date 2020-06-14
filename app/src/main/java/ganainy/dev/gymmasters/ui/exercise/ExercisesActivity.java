package ganainy.dev.gymmasters.ui.exercise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.shared_adapters.ExercisesAdapter;
import ganainy.dev.gymmasters.ui.specificExercise.SpecificExerciseActivity;
import ganainy.dev.gymmasters.utils.MiscellaneousUtils;
import ganainy.dev.gymmasters.utils.NetworkState;

import static ganainy.dev.gymmasters.ui.main.exercisesCategories.ExercisesCategoriesFragment.SELECTED_MUSCLE;
import static ganainy.dev.gymmasters.ui.main.home.HomeFragment.LOGGED_USER_ID;

public class ExercisesActivity extends AppCompatActivity {
    private static final String TAG = "ExercisesActivity";
    public static final String EXERCISE = "exercise";
    ExercisesViewModel exercisesViewModel;

    @BindView(R.id.collapse_toolbar)
    CollapsingToolbarLayout htab_collapse_toolbar;
    @BindView(R.id.htab_header)
    ImageView collapsingToolbarImage;
    @BindView(R.id.search_view)
    SearchView searchView;
    @BindView(R.id.loading_layout_shimmer)
    ShimmerFrameLayout shimmerLoadingLayout;
    @BindView(R.id.empty_layout)
    ConstraintLayout emptyLayout;
    @BindView(R.id.error_layout)
    ConstraintLayout errorLayout;
    @BindView(R.id.exercisesRecyclerView)
    RecyclerView exercisesRecyclerView;


    private ExercisesAdapter exercisesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);
        ButterKnife.bind(this);
        setupRecycler();
        setupSearchView();

        exercisesViewModel = new ViewModelProvider(this).get(ExercisesViewModel.class);

       if (getIntent().hasExtra(SELECTED_MUSCLE)) {
            String selectedMuscle = getIntent().getStringExtra(SELECTED_MUSCLE);
            setTabHeaderImage(selectedMuscle);
            exercisesViewModel.getSelectedMuscleExercises(selectedMuscle);
        }

        exercisesViewModel.getExerciseListLiveData().observe(this, exercises -> {
            exercisesAdapter.setData(exercises);
            exercisesAdapter.notifyDataSetChanged();
        });

        exercisesViewModel.getNetworkStateLiveData().observe(this, this::handleNetworkStateUi);
    }

    private void handleNetworkStateUi(NetworkState networkState) {
        switch (networkState){
            case SUCCESS:
                errorLayout.setVisibility(View.GONE);
                emptyLayout.setVisibility(View.GONE);
                exercisesRecyclerView.setVisibility(View.VISIBLE);
                shimmerLoadingLayout.setVisibility(View.GONE);
                break;
            case ERROR:
                errorLayout.setVisibility(View.VISIBLE);
                emptyLayout.setVisibility(View.GONE);
                exercisesRecyclerView.setVisibility(View.GONE);
                shimmerLoadingLayout.setVisibility(View.GONE);
                break;
            case LOADING:
                errorLayout.setVisibility(View.GONE);
                emptyLayout.setVisibility(View.GONE);
                exercisesRecyclerView.setVisibility(View.GONE);
                shimmerLoadingLayout.setVisibility(View.VISIBLE);
                break;
            case EMPTY:
                errorLayout.setVisibility(View.GONE);
                 emptyLayout.setVisibility(View.VISIBLE);
                exercisesRecyclerView.setVisibility(View.GONE);
                shimmerLoadingLayout.setVisibility(View.GONE);
                break;
        }
    }





    /**
     * set header image based on selected muscle from previous fragment
     */
    private void setTabHeaderImage(String selectedMuscle) {
        collapsingToolbarImage.setImageResource(MiscellaneousUtils.getImageId(this, selectedMuscle));
    }


    private void setupRecycler() {

        exercisesAdapter = new ExercisesAdapter(this, exercise -> {
            //handle click of certain exercise
            Intent intent = new Intent(ExercisesActivity.this, SpecificExerciseActivity.class);
            intent.putExtra(EXERCISE, exercise);
            startActivity(intent);
        });
        exercisesRecyclerView.setAdapter(exercisesAdapter);
    }

    private void setupSearchView() {
        //do filtering when i type in search or click search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryString) {
                exercisesAdapter.getFilter().filter(queryString);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String queryString) {
                exercisesAdapter.getFilter().filter(queryString);
                return false;
            }
        });
    }

}