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
import ganainy.dev.gymmasters.utils.ApplicationViewModelFactory;

public class YoutubeFragment extends Fragment {

    private static final String NAME = "name";
    public static final String YOUTUBE_SEARCH_URL = "https://www.youtube.com/results?search_query=";

    private YoutubeViewModel mViewModel;
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViewModel();

        // Add YouTube player as fragment lifecycle observer to control release when fragment is destroyed
        getViewLifecycleOwner().getLifecycle().addObserver(binding.youtubePlayerView);

        if (getArguments() != null && getArguments().getString(NAME) != null) {
            mViewModel.getExerciseVideoId(getArguments().getString(NAME));
        }

        // Observe videoId and load video if available
        mViewModel.getVideoIdLiveData().observe(getViewLifecycleOwner(), videoId -> {
            if (videoId != null) {
                binding.youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                        youTubePlayer.loadVideo(videoId, mViewModel.getVideoCurrentSecond());
                    }

                    @Override
                    public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float second) {
                        mViewModel.setVideoCurrentSecond(second);
                        super.onCurrentSecond(youTubePlayer, second);
                    }
                });
            } else {
                showOpenInYoutubeAlertDialog();
            }
        });
    }

    private void initViewModel() {
        ApplicationViewModelFactory applicationViewModelFactory = new ApplicationViewModelFactory(requireActivity().getApplication());
        mViewModel = new ViewModelProvider(this, applicationViewModelFactory).get(YoutubeViewModel.class);
    }

    private void showOpenInYoutubeAlertDialog() {
        new AlertDialog.Builder(requireActivity())
                .setMessage(R.string.in_app_play_failed)
                .setTitle(R.string.error_playing_video)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_SEARCH_URL + mViewModel.getExerciseName()));
                    startActivity(intent);
                })
                .setNegativeButton(R.string.no, null)
                .create().show();
    }
}