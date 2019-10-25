package com.example.myapplication.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.myapplication.R;
import com.example.myapplication.model.User;
import com.example.myapplication.utils.MyConstant;
import com.example.myapplication.utils.NetworkChangeReceiver;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancytoastlib.FancyToast;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 101;
    public NetworkChangeReceiver receiver;
    Boolean bl = true;
    private FirebaseAuth mAuth;
    private TextInputEditText emailEditText, passwordEditText;
    private Button login;
    TextView signup;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = findViewById(R.id.loginButton);
        signup = findViewById(R.id.signupTextView);
        passwordEditText = findViewById(R.id.passwordEditText);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        checkInternet();

        //open signup activity
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Hide keyboard and login
                login();
            }
        });


        /**after pressing done on keyboard after writing password it will login*/
        loginWithDone();


        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                //.requestIdToken("224559312783-78jrnd8t87e8vovvmveshiaad9p4bb54.apps.googleusercontent.com")
                .build();


        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_ICON_ONLY);
    }

    private void login() {
        /**hide keyboard*/
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        checkEmailAndPassword();
    }

    private void loginWithDone() {
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    /* Write your logic here that will be executed when user taps next button */
                    login();

                    handled = true;
                }
                return handled;
            }
        });

    }


    private void checkEmailAndPassword() {

        emailEditText = findViewById(R.id.emailEditText);
        String fillhere = getResources().getString((R.string.fillhere_signuplogin_error));
        if (emailEditText.getText().toString().trim().isEmpty() || emailEditText.getText().toString().trim().equals(" "))
            emailEditText.setError(fillhere);
        else if (passwordEditText.getText().toString().trim().isEmpty() || passwordEditText.getText().toString().trim().equals(" "))
            passwordEditText.setError(fillhere);
        else
            loginToFirebase();
    }

    private void loginToFirebase() {
        //show fake progressbar to simulate loading
        final ConstraintLayout constraintLayout = findViewById(R.id.constraint);
        constraintLayout.setVisibility(View.VISIBLE);

        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(emailEditText.getText().toString().trim(), passwordEditText.getText().toString().trim())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            constraintLayout.setVisibility(View.GONE);
                            //set loggedInUserId value which will be used later
                            MyConstant.loggedInUserId = mAuth.getUid();
                            FancyToast.makeText(LoginActivity.this, "Login successful.", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            constraintLayout.setVisibility(View.GONE);
                            FancyToast.makeText(LoginActivity.this, task.getException() + "", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();

                        }

                    }
                });


    }


    public void checkInternet() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver(this);
        registerReceiver(receiver, filter);
        bl = receiver.is_connected();
        Log.d("Boolean ", bl.toString());
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {

        }
    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    private void updateUI(GoogleSignInAccount account) {
        if (account == null) {
            //ask user to login
            Log.i(TAG, "updateUI: account is null");
            FancyToast.makeText(LoginActivity.this, "Error signing in with google account ,\n please login with Gym master account instead.", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
        } else {
            //user already added his google account
            /**live data to observe and return when user data is saved */
            addGoogleUserToDB(account).observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if (aBoolean) {
                        /**executes if account was added to db or not added if already existed*/
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        FancyToast.makeText(LoginActivity.this, "Login Successful.", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();

                        startActivity(intent);
                        finish();
                    } else {
                        /**onCancelled called when adding or checking if google account is in db*/
                        FancyToast.makeText(LoginActivity.this, "Error signing in with google account ,\n please login with Gym master account instead.", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                    }
                }
            });



        }
    }


    private LiveData<Boolean> addGoogleUserToDB(final GoogleSignInAccount account) {
        final MutableLiveData<Boolean> load = new MutableLiveData<>();
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("users");
        final User newUser;

        newUser = new User(account.getId(), account.getDisplayName(), account.getEmail(),
                account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null);

        Log.i(TAG, "addGoogleUserToDB: " + account.getEmail());

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(account.getId())) {
                    myRef.child(account.getId()).setValue(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            load.setValue(true);
                        }
                    });
                } else {
                    load.setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                load.setValue(false);
            }
        });

        return load;
    }


    @Override
    public void onClick(View view) {
        /**google sign in clicked*/
        if (view.getId() == R.id.sign_in_button) {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);

        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }
}
