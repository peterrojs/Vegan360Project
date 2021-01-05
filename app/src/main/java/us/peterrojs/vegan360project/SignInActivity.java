package us.peterrojs.vegan360project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import layout.SignInFragment;

public class SignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, SignInFragment.OnFragmentInteractionListener {

    private static final String LOG_TAG = SignInActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private String provider;

    // Facebook
    private CallbackManager mCallbackManager;


    //Google
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;

    // TODO: Finish Google Sign In

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        getFragmentManager().beginTransaction().add(R.id.frag_container, new SignInFragment()).commit();

        FirebaseApp.initializeApp(this);

        // Google Sign In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleApiClient= new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* Fragment Activity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions).build();


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(LOG_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                }
                else {
                    // User signed out
                    Log.d(LOG_TAG, "onAuthChanged:signed_out");
                }
            }
        };

        // Facebook
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton hiddenFbButton = (LoginButton) findViewById(R.id.hidden_fb_button);
        hiddenFbButton.setReadPermissions("email", "public_profile");
        hiddenFbButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(LOG_TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(LOG_TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(LOG_TAG, "facebook:onError", error);
            }
        });

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }


    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        Log.d(LOG_TAG, "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(LOG_TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        // If sign in is successful
                        if (task.isSuccessful()) {
                            String displayName = account.getDisplayName();
                            String pictureURL = account.getPhotoUrl().toString();
                            Log.d(LOG_TAG, pictureURL);
                            Intent intent = new Intent(getBaseContext(), NewsActivity.class);
                            intent.putExtra("displayName", displayName);
                            intent.putExtra("profilePictureURL", pictureURL);
                            startActivity(intent);
                        }

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        else {
                            Log.w(LOG_TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SignInActivity.this, "Something went wrong", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    public void onClickFacebook(View v) {
        Log.d(LOG_TAG, "onClickFacebook");
        LoginButton button = (LoginButton) findViewById(R.id.hidden_fb_button);
        provider = "Facebook";
        button.performClick();
    }

    public void onClickGoogle(View v) {
        provider = "Google";
        startActivityForResult(Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient), RC_SIGN_IN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult" + requestCode);
        super.onActivityResult(requestCode, resultCode, data);

        if (provider.equals("Google")) {
            handleGoogleSignInToken(resultCode, data, Auth.GoogleSignInApi.getSignInResultFromIntent(data));
        }
        else if (provider.equals("Facebook")) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
        else {
            throw new NullPointerException("Provider doesn't equal Facebook or Google");
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(LOG_TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(LOG_TAG, "signInWithCredential:onComplete;" + task.isSuccessful());

                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "Authentication Successful");
                }
                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                else {
                    Log.w(LOG_TAG, "signInWithCredential", task.getException());
                    Snackbar.make(findViewById(R.id.parent_layout), "Something went wrong",
                            Snackbar.LENGTH_SHORT).setAction("Try Again", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            findViewById(R.id.hidden_fb_button).performClick();
                        }
                    });
                }
            }
        });
    }

    /** Method called by onActivityResult for Google Sign-In*/
    private void handleGoogleSignInToken(int resultCode, Intent data, GoogleSignInResult result) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        // Successfully signed in
        if (resultCode == ResultCodes.OK && result.isSuccess()) {
            Log.d(LOG_TAG, "GoogleSignInResult:" + result.isSuccess());
            GoogleSignInAccount account = result.getSignInAccount();
            firebaseAuthWithGoogle(account);
            finish();
        } else {
            // Sign in failed
            if (response == null) {
                Log.d(LOG_TAG, "Sign in cancelled");
            }
            else if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                Snackbar.make(findViewById(R.id.parent_layout), "Sorry, no internet", Snackbar.LENGTH_SHORT);
            }
            else if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                Snackbar.make(findViewById(R.id.parent_layout), "Something went wrong", Snackbar.LENGTH_SHORT)
                        .setAction("Try Again", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LoginButton button = (LoginButton) findViewById(R.id.hidden_fb_button);
                                button.performClick();
                            }
                        });
            }
        }
    }


    @Override
    public void onFragmentInteraction(Uri uri) { }
}