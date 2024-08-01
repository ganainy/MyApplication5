package ganainy.dev.gymmasters.ui.signup;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.ui.login.LoginActivity;
import ganainy.dev.gymmasters.ui.main.MainActivity;
import ganainy.dev.gymmasters.utils.ApplicationViewModelFactory;

public class SignUpActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 101;
    private static final int RC_SIGN_IN = 102;
    public static final String IMAGE_URI = "imageUri";

    ImageView profileImage;
    private TextInputEditText userNameEditText, emailEditText, passwordEditText;
    private TextInputLayout passwordTextInputLayout,usernameTextInputLayout,emailTextInputLayout;
    Button signUp;
    ProgressBar progressBar;
    private GoogleSignInClient mGoogleSignInClient;
    private ImageView semiTransparentBackgroundImage;

    private SignUpViewModel signUpViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initViews();

        initViewModel();

        signUpViewModel.isFirstSignUpShowing().observe(this, isFirstSignUpShowing -> {
            if (isFirstSignUpShowing) askUserToAddPhoto();
        });

        signUpViewModel.getUsernameError().observe(this, errorHint ->
                {
                    String errorHintContentIfNotHandled = errorHint.getContentIfNotHandled();
                    if(errorHintContentIfNotHandled!=null&&errorHintContentIfNotHandled.isEmpty()){
                        usernameTextInputLayout.setError("");
                    }else if(errorHintContentIfNotHandled != null){
                        usernameTextInputLayout.setError(errorHintContentIfNotHandled);
                    }
                }
        );


        signUpViewModel.getEmailError().observe(this, errorHint ->
                {
                    String errorHintContentIfNotHandled = errorHint.getContentIfNotHandled();
                    if(errorHintContentIfNotHandled!=null&&errorHintContentIfNotHandled.isEmpty()){
                        emailTextInputLayout.setError("");
                    }else if(errorHintContentIfNotHandled != null){
                        emailTextInputLayout.setError(errorHintContentIfNotHandled);
                    }
                }
        );

        signUpViewModel.getPasswordError().observe(this, errorHint ->
                {
                    String errorHintContentIfNotHandled = errorHint.getContentIfNotHandled();
                    if(errorHintContentIfNotHandled!=null&&errorHintContentIfNotHandled.isEmpty()){
                        passwordTextInputLayout.setError("");
                    }else if(errorHintContentIfNotHandled != null){
                        passwordTextInputLayout.setError(errorHintContentIfNotHandled);
                    }
                }
               );

        signUpViewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            if (isLoading) {
                semiTransparentBackgroundImage.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
            } else {
                semiTransparentBackgroundImage.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }
        });


        signUpViewModel.isUserDataUploadedMediatorLiveData.observe(this, aBoolean -> {
            //need to observe mediator live data to be able to trigger callback in viewModel
            // constructor
        });


        signUpViewModel.getToastLiveData().observe(this, pairEvent -> {
            if (!pairEvent.getHasBeenHandled()) {
                Pair<Integer, Integer> toastPair = pairEvent.getContentIfNotHandled();
                Toast.makeText(
                        SignUpActivity.this,
                        getApplication().getString(toastPair.first),
                        toastPair.second)
                        .show();
            }
        });

        signUpViewModel.getNavigateToMainLiveData().observe(this, shouldNavigateToMainActivity -> {
            if (!shouldNavigateToMainActivity.getHasBeenHandled()) {
                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            }
        });


        signUpViewModel.getIsGoogleUserDataUploadedLiveData().observe(this, isGoogleUserDataUploaded -> {
            if (isGoogleUserDataUploaded) {
                //executes if account was added to db or not added if already existed
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            } else {
                //onCancelled called when adding or checking if google account is in db*/
                Toast.makeText(SignUpActivity.this, R.string.use_gym_master_account, Toast.LENGTH_LONG).show();
            }
        });


        signUpViewModel.getImageUriLiveData().observe(this, profileImageUri -> {
            profileImage.setImageURI(profileImageUri);
        });

    }

    private void initViews() {
        profileImage = findViewById(R.id.profile_image_shimmer);
        signUp = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        userNameEditText = findViewById(R.id.userNameEditText);
        semiTransparentBackgroundImage = findViewById(R.id.semiTransparentBackgroundImage);
        TextView signInTextView = findViewById(R.id.signUpButton);
        SignInButton googleSignInButton = findViewById(R.id.sign_in_button);
        passwordTextInputLayout=findViewById(R.id.passwordTextInputLayout);
        usernameTextInputLayout=findViewById(R.id.userNameTextInputLayout);
        emailTextInputLayout=findViewById(R.id.emailShimmer);


        googleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });


        signInTextView.setOnClickListener(v -> {
                    startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
                    finish();
                }
        );

        profileImage.setOnClickListener(v -> openImageChooser());

        signUp.setOnClickListener(v -> signUpViewModel.authenticateUser());



        userNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                signUpViewModel.validateUsername(s.toString());
            }
        });


        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                signUpViewModel.validatePassword(s.toString());
            }
        });

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                signUpViewModel.validateEmail(s.toString());
            }
        });


        //todo fix authenticateUser is called twice when GO on keyboard is pressed
        //on softkeyboard action done sign up user
        /*passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId== EditorInfo.IME_ACTION_GO){
                signUpViewModel.authenticateUser();
            }
            return false;
        });*/


        setupGoogleSignIn();

    }


    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    private void setupGoogleSignIn() {
        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestIdToken(getString(R.string.request_id_token))
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
    }

    private void initViewModel() {
        ApplicationViewModelFactory applicationViewModelFactory = new ApplicationViewModelFactory(getApplication());
        signUpViewModel = new ViewModelProvider(this, applicationViewModelFactory).get(SignUpViewModel.class);
    }

    private void askUserToAddPhoto() {
        TapTargetView.showFor(this,                 // `this` is an Activity
                TapTarget.forView(findViewById(R.id.profile_image_shimmer), "Click to add profile picture", "It's optional but will help your followers to get to know you better")
                        // All options below are optional
                        .outerCircleColor(R.color.blue)      // Specify a color for the outer circle
                        .titleTextSize(25)                  // Specify the size (in sp) of the title text
                        .descriptionTextSize(18)            // Specify the size (in sp) of the description text
                        .textColor(R.color.grey)            // Specify a color for both the title and description text
                        .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                        .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                        .drawShadow(true)                   // Whether to draw a drop shadow or not
                        .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                        .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                        .targetRadius(60),                  // Specify the target radius (in dp)
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        openImageChooser();
                    }
                });
        //update shared pref value
        signUpViewModel.updateIsFirstSignUpShowing();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && requestCode == PICK_IMAGE) {
            signUpViewModel.setImageUri(data.getData());
        }
        if (requestCode == RC_SIGN_IN) {
            // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
            signUpViewModel.handleGoogleSignIn(data);
        }

    }

}
