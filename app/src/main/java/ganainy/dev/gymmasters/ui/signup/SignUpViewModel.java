package ganainy.dev.gymmasters.ui.signup;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.models.app_models.User;
import ganainy.dev.gymmasters.utils.Event;
import ganainy.dev.gymmasters.utils.MiscellaneousUtils;
import ganainy.dev.gymmasters.utils.SharedPrefUtils;

import static ganainy.dev.gymmasters.ui.profile.ProfileViewModel.USER_IMAGES;

public class SignUpViewModel extends ViewModel {

    private static final String TAG = "SignUpViewModel";
    private Application app;
    private String username = "";
    private String email = "";
    private String password = "";
    private Uri imageUri;

    private MutableLiveData<Boolean> isFirstShowingOfSignUpLiveData = new MutableLiveData<>();
    private MutableLiveData<Event<String>> userNameErrorLiveData = new MutableLiveData<>();
    private MutableLiveData<Event<String>> emailErrorLiveData = new MutableLiveData<>();
    private MutableLiveData<Event<String>> passwordErrorLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private MutableLiveData<Event<Boolean>> navigateToMainLiveData = new MutableLiveData<>();
    private MutableLiveData<Uri> imageUriLiveData = new MutableLiveData<>();
    private MutableLiveData<Event<Pair<Integer, Integer>>> toastLiveData = new MutableLiveData<>();
    MutableLiveData<Boolean> isUserDataUploadedLiveData = new MutableLiveData<>();

    MutableLiveData<Boolean> isGoogleUserDataUploadedLiveData = new MutableLiveData<>();
    MediatorLiveData<Boolean> isUserDataUploadedMediatorLiveData = new MediatorLiveData<>();

    public SignUpViewModel(final Application app) {
        this.app = app;

        isUserDataUploadedMediatorLiveData.addSource(isUserDataUploadedLiveData, isUserDataUploaded -> {
            if (isUserDataUploaded) {
                navigateToMainLiveData.setValue(new Event<>(true));
            } else {
                isLoadingLiveData.setValue(false);
                toastLiveData.setValue(new Event<>(new Pair<>(R.string.error_signing_up, 1)));
            }
        });


    }

    LiveData<Boolean> isFirstSignUpShowing() {
        isFirstShowingOfSignUpLiveData.setValue(SharedPrefUtils.getBoolean(app, SharedPrefUtils.IS_FIRST_SHOWING_OF_SIGN_UP));
        return isFirstShowingOfSignUpLiveData;
    }

    public void updateIsFirstSignUpShowing() {
        SharedPrefUtils.putBoolean(app, false, SharedPrefUtils.IS_FIRST_SHOWING_OF_SIGN_UP);
    }

    public void authenticateUser() {
        if (username.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
            validateUsername(username);
            validateEmail(email);
            validatePassword(password);
            return;
        }
        isLoadingLiveData.setValue(true);
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if (mAuth.getCurrentUser() != null) {
                        // createUser success, save user data in db and update ui
                        String userId = mAuth.getCurrentUser().getUid();
                        saveUserInfoInRealtimeDb(userId);
                    } else {
                        //  sign in fails, display a message to the user.
                        toastLiveData.setValue(new Event<>(new Pair<>(R.string.signup_failed, 0)));
                    }
                    isLoadingLiveData.setValue(false);

                })
                .addOnFailureListener(e -> {
                    //  sign in fails, display a message to the user.
                    isLoadingLiveData.setValue(false);
                    toastLiveData.setValue(new Event<>(new Pair<>(R.string.signup_failed, 0)));
                });

    }


    private void saveUserInfoInRealtimeDb(final String uid) {

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("users");

        if (imageUri != null) {
            //uploadImage to firebase storage then upload data to realtime db
            final User newUser = new User(uid, username, email);

            final StorageReference imagesRef = FirebaseStorage.getInstance().getReference()
                    .child(USER_IMAGES + MiscellaneousUtils.formatUriAsTimeStampedString(imageUri));
            imagesRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    {

                    imagesRef.getDownloadUrl().addOnSuccessListener(url->{
                        Log.d(TAG, "getDownloadUrl: "+url);
                        newUser.setPhoto(url.toString());
                        myRef.child(uid).setValue(newUser).addOnSuccessListener(aVoid -> {
                            isUserDataUploadedLiveData.setValue(true);
                        }).addOnFailureListener(e -> {
                            isUserDataUploadedLiveData.setValue(false);
                        });
                    });
                    //todo we are only saving user if we can upload image and get its download url,
                        //maybe should also add user if upload failed

                    }

           ).addOnFailureListener(e -> isUserDataUploadedLiveData.setValue(false));


        } else {
            //user didn't select image only upload data to realtime db
            User newUser = new User(uid, username, email, null);
            myRef.child(uid).setValue(newUser).addOnSuccessListener(aVoid -> {
                Log.i(TAG, "onSuccess: ");
                isUserDataUploadedLiveData.setValue(true);
            }).addOnFailureListener(e -> {
                Log.i(TAG, "onFailure: " + e.getMessage());
                isUserDataUploadedLiveData.setValue(false);
            });
        }

    }

    void addGoogleUserToDB(final GoogleSignInAccount account) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("users");

        final User newUser = new User(account.getId(), account.getDisplayName(), account.getEmail(),
                account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null);

        Log.i(TAG, "addGoogleUserToDB: " + account.getEmail());

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (account.getId() != null && !dataSnapshot.hasChild(account.getId())) {
                    myRef.child(account.getId()).setValue(newUser).addOnSuccessListener(aVoid -> isGoogleUserDataUploadedLiveData.setValue(true));
                } else {
                    isGoogleUserDataUploadedLiveData.setValue(true);
                    isLoadingLiveData.setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                isGoogleUserDataUploadedLiveData.setValue(false);
                isLoadingLiveData.setValue(false);
            }
        });

    }


    public void handleGoogleSignIn(Intent data) {
        isLoadingLiveData.setValue(true);
        Task<GoogleSignInAccount> completedTask = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully
            addGoogleUserToDB(account);
        } catch (ApiException e) {
            //login with google failed, ask user to login with email and password
            toastLiveData.setValue(new Event<>(new Pair<>(R.string.use_gym_master_account, Toast.LENGTH_LONG)));
            isLoadingLiveData.setValue(false);
        }
    }


    public void validateUsername(String username) {
        this.username = username;
        if (username.trim().isEmpty())
            userNameErrorLiveData.setValue(new Event<>(app.getString(R.string.username_hint_error)));
        else
            userNameErrorLiveData.setValue(new Event<>(""));
    }

    public void validatePassword(String password) {
        this.password = password;
        if (password.trim().length() < 6)
            passwordErrorLiveData.setValue(new Event<>(app.getString(R.string.password_hint_error)));
        else
            passwordErrorLiveData.setValue(new Event<>(""));
    }

    public void validateEmail(String email) {
        this.email = email;
        if (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches())
            emailErrorLiveData.setValue(new Event<>(""));
        else
            emailErrorLiveData.setValue(new Event<>(app.getString(R.string.email_hint_error)));
    }


    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
        imageUriLiveData.setValue(imageUri);
    }


    public LiveData<Boolean> getIsGoogleUserDataUploadedLiveData() {
        return isGoogleUserDataUploadedLiveData;
    }


    LiveData<Uri> getImageUriLiveData() {
        return imageUriLiveData;
    }

    LiveData<Event<String>> getUsernameError() {
        return userNameErrorLiveData;
    }

    LiveData<Event<String>> getPasswordError() {
        return passwordErrorLiveData;
    }

    LiveData<Event<String>> getEmailError() {
        return emailErrorLiveData;
    }

    LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    LiveData<Event<Boolean>> getNavigateToMainLiveData() {
        return navigateToMainLiveData;
    }

    LiveData<Event<Pair<Integer, Integer>>> getToastLiveData() {
        return toastLiveData;
    }

}
