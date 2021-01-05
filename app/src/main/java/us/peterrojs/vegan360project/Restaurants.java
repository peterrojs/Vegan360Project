package us.peterrojs.vegan360project;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;

import java.io.InputStream;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class Restaurants extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        DrawerLayout.DrawerListener, NavigationView.OnNavigationItemSelectedListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    GoogleApiClient mGoogleApiClient;
    SmoothActionBarDrawerToggle toggle;
    DrawerLayout drawer;
    String displayName;
    NavigationView navView;
    String profilePictureURL;
    private static final String LOG_TAG = Restaurants.class.getSimpleName();

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurants);

        // Set The Toolbar up
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the drawer and the toggle
        drawer = (DrawerLayout) findViewById(R.id.main_content);
        toggle = new SmoothActionBarDrawerToggle (this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navView = (NavigationView) findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //SnackBar

        /*GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Restaurants.this.getResources().getString(R.string.default_web_client_id)).requestEmail().build();

        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build(); */

        try {
            Object[] params = {this, getApplicationContext()};
            Object[] response = (Object[]) new AuthenticationStuff().execute(params).get();

            mGoogleApiClient = (GoogleApiClient) response[1];
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        catch (NullPointerException e) {
                Log.e(LOG_TAG, "NullPointerException setDisplayHomeAsUpEnabled");
            }

            displayName = getIntent().getStringExtra("name");
            profilePictureURL = getIntent().getStringExtra("profilePictureURL");
            setTheHeaderUp(displayName, profilePictureURL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_restaurants, menu);
        return true;
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.d(LOG_TAG, "onOptionsItemSelected");

        switch (id) {
            case R.id.action_sign_out:
                Log.d(LOG_TAG, "onOptionsItemSelected Switch");
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void signOut() {
        Log.d(LOG_TAG, "signOutMethod");
        if (AccessToken.getCurrentAccessToken() != null) {
            // Facebook
            Log.d(LOG_TAG, "IfFacebook");
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    LoginManager.getInstance().logOut();
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                }
            });
        } else {
            // Google
            Log.d(LOG_TAG, "IfGoogle");
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    FirebaseAuth.getInstance().signOut();
                    Log.d(LOG_TAG, "ResultCallback");
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                }
            });
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_better_vegan) {
            toggle.runWhenIdle(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), BetterVeganActivity.class));
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                }
            });
        }
        else if (id == R.id.nav_restaurants) { }
        else if (id == R.id.nav_recipes) {
            //startActivity(new Intent(getApplicationContext(), Recipes.class));
        }
        else if (id == R.id.nav_news) {

            toggle.runWhenIdle(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), NewsActivity.class));
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                }
            });
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onDrawerSlide(View view, float v) {

    }

    @Override
    public void onDrawerOpened(View view) {

    }

    @Override
    public void onDrawerClosed(View view) {

    }

    @Override
    public void onDrawerStateChanged(int i) {

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    RestaurantsNearby restaurantNearby = new RestaurantsNearby();
                    return restaurantNearby;
                case 1:
                    RestaurantsMap restaurantMap = new RestaurantsMap();
                    return restaurantMap;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "NEARBY";
                case 1:
                    return "MAP";
            }
            return null;
        }
    }

    public void setTheHeaderUp(String displayName, String profileUrl) {
        Log.d(LOG_TAG, "setTheHeaderUp Started");
        navView = (NavigationView) findViewById(R.id.nav_view);
        View header = navView.getHeaderView(0);
        TextView name = (TextView) header.findViewById(R.id.name_text);
        CircleImageView imageView = (CircleImageView) header.findViewById(R.id.profile_picture);
        Drawable drawable = LoadImageFromWeb(profileUrl);
        name.setText(displayName);
        if (drawable != null) {
            Log.d(LOG_TAG, "setTheHeaderUp drawable ain't null");
            imageView.setImageDrawable(drawable);
        }
        Log.d(LOG_TAG, "setTheHeaderUp drawable is null");
        Log.d(LOG_TAG, "setTheHeaderUp Ended");
    }

    public static Drawable LoadImageFromWeb(String url) {
        try {
            InputStream inputStream = (InputStream) new URL(url).getContent();
            Drawable drawable = Drawable.createFromStream(inputStream, "srcName");
            return drawable;
        } catch (Exception e) {
            return null;
        }
    }
}
