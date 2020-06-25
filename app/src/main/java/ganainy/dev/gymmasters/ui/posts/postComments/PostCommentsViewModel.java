package ganainy.dev.gymmasters.ui.posts.postComments;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ganainy.dev.gymmasters.models.app_models.Comment;
import ganainy.dev.gymmasters.models.app_models.Exercise;
import ganainy.dev.gymmasters.models.app_models.Post;
import ganainy.dev.gymmasters.models.app_models.User;
import ganainy.dev.gymmasters.models.app_models.Workout;
import ganainy.dev.gymmasters.utils.AuthUtils;

import static ganainy.dev.gymmasters.ui.exercise.ExercisesViewModel.EXERCISES;

public class PostCommentsViewModel extends AndroidViewModel {

    public static final String USERS = "users";
    public static final String LIKER_ID_LIST = "likerIdList";
    public static final String LIKE_COUNT = "likeCount";
    public static final String WORKOUT = "workout";

    public static final String COMMENT_LIST = "commentList";
    public static final String COMMENT_COUNT = "commentCount";
    public static final String TAG = "PostCommentsViewModel";

    PostComment emptyComment = new PostComment(PostComment.PostCommentType.EMPTY_COMMENTS);
    PostComment loadingComment = new PostComment(PostComment.PostCommentType.LOADING_COMMENTS);

    private Post mPost;

    /*will hold original comments plus any new user comments by user*/
    List<PostComment> postCommentList=new ArrayList<>();
    /*one time live data to get post comments on first fragment opening*/
    private MutableLiveData<List<PostComment>> postCommentListLiveData = new MutableLiveData<>();
    /*this live data for changes in comments*/
    private MutableLiveData<List<PostComment>> updatedPostCommentsLiveData = new MutableLiveData<>();

    /*this live data for changes in likes*/
    private MutableLiveData<List<PostComment>> updatedPostLikesLiveData = new MutableLiveData<>();

    public PostCommentsViewModel(@NonNull Application application) {
        super(application);
    }

    public void saveComment(String commentText) {
        if (mPost.getWorkout()!=null)
        saveWorkoutComment(commentText);
        else if (mPost.getExercise()!=null)
            saveExerciseComment(commentText);
    }

    private void saveWorkoutComment(String commentText) {
        Comment comment = new Comment(AuthUtils.getLoggedUserId(getApplication()), System.currentTimeMillis(), commentText);
        if (mPost.getWorkout().getCommentList() == null) {
            //post has NO comments
            ArrayList<Comment> commentList = new ArrayList<>();
            commentList.add(comment);
            mPost.getWorkout().setCommentList(commentList);
        } else {
            //post has previous comments
            mPost.getWorkout().getCommentList().add(comment);
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put(COMMENT_LIST, mPost.getWorkout().getCommentList());
        updates.put(COMMENT_COUNT, mPost.getWorkout().getCommentList().size());

        FirebaseDatabase.getInstance().getReference().child(WORKOUT).child(mPost.getWorkout().getId()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "onDataChange: suc");
                }).addOnFailureListener(e -> {
            Log.d(TAG, "onDataChange: " + e.getMessage());
            //todo handle error
        });


        postCommentList.add(new PostComment(new Pair<>(comment,AuthUtils.getLoggedUser())));
        updatedPostCommentsLiveData.setValue(postCommentList);
    }

    private void saveExerciseComment(String commentText) {
        Comment comment = new Comment(AuthUtils.getLoggedUserId(getApplication()), System.currentTimeMillis(), commentText);
        if (mPost.getExercise().getCommentList() == null) {
            //post has NO comments
            ArrayList<Comment> commentList = new ArrayList<>();
            commentList.add(comment);
            mPost.getExercise().setCommentList(commentList);
        } else {
            //post has previous comments
            mPost.getExercise().getCommentList().add(comment);
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put(COMMENT_LIST, mPost.getExercise().getCommentList());
        updates.put(COMMENT_COUNT, mPost.getExercise().getCommentList().size());

        FirebaseDatabase.getInstance().getReference().child(EXERCISES).child(mPost.getExercise().getId()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "onDataChange: suc");
                }).addOnFailureListener(e -> {
            Log.d(TAG, "onDataChange: " + e.getMessage());
            //todo handle error
        });


        postCommentList.add(new PostComment(new Pair<>(comment,AuthUtils.getLoggedUser())));
        updatedPostCommentsLiveData.setValue(postCommentList);
    }


    public Post getPost() {
        return mPost;
    }

    public void setPost(Post mPost) {
        this.mPost = mPost;
    }

    public void getPostComments() {
        postCommentList.add(new PostComment(mPost));
        postCommentList.add(loadingComment);
        postCommentListLiveData.setValue(postCommentList);
        if (mPost.getWorkout()!=null)
            getWorkoutComments();
        else if (mPost.getExercise()!=null)
           getExerciseComments();

    }

    private void getExerciseComments() {
        FirebaseDatabase.getInstance().getReference().child(EXERCISES).child(mPost.getExercise().getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot exerciseSnapshot) {
                //comments or likes changed
                Exercise exercise = exerciseSnapshot.getValue(Exercise.class);
                if (exercise.getCommentList() == null) {
                    //no comments
                    postCommentList.add(emptyComment);
                    postCommentListLiveData.setValue(postCommentList);
                } else {
                    getCommenterInformation(exercise.getCommentList());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getWorkoutComments() {
        FirebaseDatabase.getInstance().getReference().child(WORKOUT).child(mPost.getWorkout().getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot workoutSnapshot) {
                //comments or likes changed
                Workout workout = workoutSnapshot.getValue(Workout.class);
                if (workout.getCommentList() == null) {
                  //no comments
                    postCommentList.add(emptyComment);
                    postCommentListLiveData.setValue(postCommentList);
                } else {
                    getCommenterInformation(workout.getCommentList());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getCommenterInformation(List<Comment> commentList) {
        List<Pair<Comment, User>> userCommentPairList = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().child(USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    for (int i = 0; i <= commentList.size() - 1; i++) {
                        User currentUser = userSnapshot.getValue(User.class);
                        if (currentUser.getId().equals(commentList.get(i).getCommenterId())) {
                            User user = userSnapshot.getValue(User.class);
                            userCommentPairList.add(new Pair<>(commentList.get(i), user));
                        }
                    }
                }

                /*merge post and comments in one list to be show in multi view type recycler*/
                    postCommentList.remove(emptyComment);
                    postCommentList.remove(loadingComment);
                for (Pair<Comment,User> userCommentPair:userCommentPairList){
                    postCommentList.add(new PostComment(userCommentPair));
                }
                postCommentListLiveData.setValue(postCommentList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void likePost() {

        if (mPost.getWorkout()!=null)
            likeWorkout();
        else if (mPost.getExercise()!=null)
            likeExercise();


    }

    private void likeExercise() {
        if (mPost.getExercise().getLikerIdList() == null) {
            //post has NO likes
            ArrayList<String> likeIdList = new ArrayList<>();
            likeIdList.add(AuthUtils.getLoggedUserId(getApplication()));
            mPost.getExercise().setLikerIdList(likeIdList);
        } else {
            //post has previous likes
            if (mPost.getExercise().getLikerIdList().contains(AuthUtils.getLoggedUserId(getApplication()))){
                //logged user already liked post
                mPost.getExercise().getLikerIdList().remove(AuthUtils.getLoggedUserId(getApplication()));
            }else{
                //logged user didn't like this post
                mPost.getExercise().getLikerIdList().add(AuthUtils.getLoggedUserId(getApplication()));
            }
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put(LIKER_ID_LIST, mPost.getExercise().getLikerIdList());
        updates.put(LIKE_COUNT, mPost.getExercise().getLikerIdList().size());

        FirebaseDatabase.getInstance().getReference().child(EXERCISES).child(mPost.getExercise().getId()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "onDataChange: suc");
                }).addOnFailureListener(e -> {
            Log.d(TAG, "onDataChange: " + e.getMessage());
            //todo handle error
        });

        //first item is always the post, so we update the post and notify recycler
        postCommentList.set(0,new PostComment(mPost));
        updatedPostLikesLiveData.setValue(postCommentList);
    }

    private void likeWorkout() {
        if (mPost.getWorkout().getLikerIdList() == null) {
            //post has NO likes
            ArrayList<String> likeIdList = new ArrayList<>();
            likeIdList.add(AuthUtils.getLoggedUserId(getApplication()));
            mPost.getWorkout().setLikerIdList(likeIdList);
        } else {
            //post has previous likes
            if (mPost.getWorkout().getLikerIdList().contains(AuthUtils.getLoggedUserId(getApplication()))){
                //logged user already liked post
                mPost.getWorkout().getLikerIdList().remove(AuthUtils.getLoggedUserId(getApplication()));
            }else{
                //logged user didn't like this post
                mPost.getWorkout().getLikerIdList().add(AuthUtils.getLoggedUserId(getApplication()));
            }
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put(LIKER_ID_LIST, mPost.getWorkout().getLikerIdList());
        updates.put(LIKE_COUNT, mPost.getWorkout().getLikerIdList().size());

        FirebaseDatabase.getInstance().getReference().child(WORKOUT).child(mPost.getWorkout().getId()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "onDataChange: suc");
                }).addOnFailureListener(e -> {
            Log.d(TAG, "onDataChange: " + e.getMessage());
            //todo handle error
        });

        //first item is always the post, so we update the post and notify recycler
        postCommentList.set(0,new PostComment(mPost));
        updatedPostLikesLiveData.setValue(postCommentList);
    }

    //region helpers

    public LiveData<List<PostComment>> getUpdatedPostLikesLiveData() {
        return updatedPostLikesLiveData;
    }

    public LiveData<List<PostComment>> getPostCommentListLiveData() {
        return postCommentListLiveData;
    }

    public MutableLiveData<List<PostComment>> getUpdatedPostCommentsLiveData() {
        return updatedPostCommentsLiveData;
    }

    //endregion
}