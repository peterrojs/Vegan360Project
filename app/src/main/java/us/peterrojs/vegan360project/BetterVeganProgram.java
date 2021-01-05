package us.peterrojs.vegan360project;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class BetterVeganProgram extends AppCompatActivity implements NavigationView
        .OnNavigationItemSelectedListener {

    private static final String LOG_TAG = BetterVeganProgram.class.getSimpleName();
    SmoothActionBarDrawerToggle toggle;
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_better_vegan_program);

        // Set The Toolbar up
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar == null) {
            Log.d(LOG_TAG, "ToolbarNull");
        }

        // Set the drawer and the toggle
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer == null) {
            Log.d(LOG_TAG, "DrawerLayoutNull");
        }
        toggle = new SmoothActionBarDrawerToggle (this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_restaurants, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sign_out) {
            (new Restaurants()).signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_better_vegan) { }
        else if (id == R.id.nav_restaurants) {
            toggle.runWhenIdle(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), Restaurants.class));
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                }
            });
        }
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

}
