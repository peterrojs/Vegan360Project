package us.peterrojs.vegan360project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookSdk;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;


public class SplashActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    GoogleSignInResult result;
    GoogleApiClient mGoogleApiClient;
    GoogleSignInOptions gso;
    OptionalPendingResult<GoogleSignInResult> optionalPendingResult;

    // Facebook
    AccessTokenTracker accessTokenTracker;

    Bitmap profileBitmap;

    // Splash screen timeout
    private static final long SPLASH_TIME_OUT = 1000;
    private static final String LOG_TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate Started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FacebookSdk.sdkInitialize(this);
        FirebaseApp.initializeApp(this);

        // Facebook
        /*accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                checkLogin(currentAccessToken);
            }
        };

        profileBitmap = null;

        // Google
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(SplashActivity.this.getResources().getString(R.string.default_web_client_id)).requestEmail().build();

        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
        optionalPendingResult = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient); */


        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null) {
            startActivity(new Intent(this, SignInActivity.class));
        }
        else {
            startActivity(new Intent(this, NewsActivity.class));
        }
        Log.d(LOG_TAG, "onCreate Ended");
    }

    /*private void checkLogin(final AccessToken currentToken) {
        Log.d(LOG_TAG, "checkLogin Started");
            if (currentToken != null) {
                new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //String[] data = { Profile.getCurrentProfile().getId(), null};
                    /*AsyncTask asyncTask =  new getProfilePicture().execute(data);
                    try {
                        profileBitmap = (Bitmap) asyncTask.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    if (profileBitmap != null) {
                        intent.putExtra("Bitmap", profileBitmap);
                    }
                    Log.d(LOG_TAG, "checkLogin Facebook");
                    Intent intent = new Intent(SplashActivity.this, Restaurants.class);
                    startActivity(intent);

                    finish();
                }
            }, SPLASH_TIME_OUT);
        }

            else if (optionalPendingResult.isDone()) {
                Log.d(LOG_TAG, "checkLogin Google");
                    if (mGoogleApiClient != null) {
                        // If the user cached credentials are valid the optionalPendingResult will be "done"
                        // and the GoogleSignInResult'll be ready
                        Log.d(LOG_TAG, "Got cached sign-in, or optionPendingResult.isDone() = true");
                        result = optionalPendingResult.get();
                        handleGoogleResult(result);
                    }
                    else {
                        handleGoogleResult(result);
                    }

                }
                else {
                    // Attempt to sign in user silently
                    optionalPendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                        @Override
                        public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                            Log.d(LOG_TAG, "Sign in user silently, handleGoogleResult");
                            handleGoogleResult(result);
                        }
                    });
                }
        Log.d(LOG_TAG, "checkLogin Ended");
    }

        /*private Bitmap getProfilePicture(String id, AccessToken token) {
            /* make the API call
            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/" + id + "/picture",
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {

                        }
                    }
            ).executeAsync();
        } */

        private void resultIsNull() {
            Log.d(LOG_TAG, "resultIsNull");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(LOG_TAG, "elseToken");
                    Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
                    startActivity(intent);

                    finish();
                }
            }, SPLASH_TIME_OUT);
        }



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "onConnectionFailed");
        Toast.makeText(getApplicationContext(), "Something Went Wrong", Toast.LENGTH_LONG).show();
    }

    public void handleGoogleResult(GoogleSignInResult result) {
        Log.d(LOG_TAG, "handleGoogleResult");
        try {
            if (result.isSuccess()) {
                GoogleSignInAccount googleAccount = result.getSignInAccount();
                String name = googleAccount.getDisplayName();
                String profilePictureURL = googleAccount.getPhotoUrl().toString();
                Intent intent = new Intent(getApplicationContext(), Restaurants.class);
                intent.putExtra("name", name);
                intent.putExtra("profilePictureURL", profilePictureURL);
                startActivity(intent);
            }
        }
        catch (NullPointerException e) {
            Log.d(LOG_TAG, "NullPointerException");
            resultIsNull();
        }
    }
    
}
