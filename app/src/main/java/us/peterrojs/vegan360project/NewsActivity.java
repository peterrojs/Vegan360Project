package us.peterrojs.vegan360project;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class NewsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
    SmoothActionBarDrawerToggle toggle;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        new AsyncTask().execute();

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_better_vegan) {
            toggle.runWhenIdle(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), BetterVeganActivity.class));
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                }
            });
        }
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
            //startActivity(new Intent(getApplicationContext(), NewsActivity.class));
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void callUIMethods() {
        navigationView.setNavigationItemSelectedListener(this);
        drawer.addDrawerListener(toggle);
    }

    private class AsyncTask extends android.os.AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            toggle = new SmoothActionBarDrawerToggle (
                    NewsActivity.this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

            toggle.syncState();
            navigationView = (NavigationView) findViewById(R.id.nav_view);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            callUIMethods();
        }
    }
}

