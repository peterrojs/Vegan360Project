package us.peterrojs.vegan360project;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class SmoothActionBarDrawerToggle extends ActionBarDrawerToggle {
    private Runnable runnable;

    public SmoothActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout,
                                       Toolbar toolbar, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
        super(activity,drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);
    }

    @Override
    public void onDrawerClosed(View view) {
        super.onDrawerClosed(view);
    }

    @Override
    public void onDrawerStateChanged(int newState) {

        /** When users taps the option in NavDrawer onDrawerStateChanged is called
         * Then it waits until the DrawerLayout.STATE_IDLE is true and runs the runnable*/

        super.onDrawerStateChanged(newState);
        if (runnable != null && newState == DrawerLayout.STATE_IDLE) {
            runnable.run();
            runnable = null;
        }
    }

    /** Sets the runnable */
    public void runWhenIdle(Runnable runnable) {
        this.runnable = runnable;
    }
}
