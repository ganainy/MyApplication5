package ganainy.dev.gymmasters.ui.specificExercise.youtubeFragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.databinding.YoutubeFragmentBinding;
import ganainy.dev.gymmasters.ui.specificExercise.ExerciseViewModel;
import ganainy.dev.gymmasters.utils.ApplicationViewModelFactory;

public class YoutubeFragment extends Fragment {


    private static final String NAME = "name";
    public static final String YOUTUBE_SEARCH_URL = "https://www.youtube.com/results?search_query=";

    private YoutubeFragmentBinding binding;

    public static YoutubeFragment newInstance(String exerciseName) {
        YoutubeFragment youtubeFragment = new YoutubeFragment();
        Bundle bundle = new Bundle();
        bundle.putString(NAME, exerciseName);
        youtubeFragment.setArguments(bundle);
        return youtubeFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = YoutubeFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve the exercise name from arguments and set it in the ViewModel
        if (getArguments() != null) {
            String exerciseName = getArguments().getString(NAME);
            openExerciseInYoutube(exerciseName);
        }


    }

    private void openExerciseInYoutube(String exerciseName) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_SEARCH_URL +exerciseName));
        startActivity(intent);
    }
}