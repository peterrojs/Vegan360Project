package us.peterrojs.vegan360project;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;

public class RestaurantsNearby extends Fragment {

    Double lon;
    Double lat;
    TextView loadingText;
    private static final String TAG = "RestaurantsNearby";
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadingText = (TextView) getActivity().findViewById(R.id.text_loading);
        XmlNearby();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_nearby_restaurants, container, false);
        return rootView;

    }


    /**
     * Determines whether location2 is better than location1
     */
    protected boolean isBetterLocation (Location location1, Location location2) {
        if (location2 == null) {
            // New location1 is always better than no location1
            return true;
        }

        // Check whether the new location1 fix is newer or older
        long timeDelta = location1.getTime() - location2.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location1, use the new
        // because the user has likely moved
        if(isSignificantlyNewer) {
            return true;
        }

        // If the new location1 is more than two minutes older it must be worse
        else if (isSignificantlyOlder){
            return false;
        }

        // Check whether the new location1 fix is more or less accurate
        int accuracyDelta = (int) (location1.getAccuracy() - location2.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location1 are from the same provider
        boolean isFromTheSameProvider = isSameProvider(location1.getProvider(), location2.getProvider());

        // Determine location1 quality using combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        }
        else if (isNewer && !isLessAccurate) {
            return true;
        }
        else if (isNewer && !isSignificantlyLessAccurate && isFromTheSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public void XmlNearby() {
        Location theRightLocation;
        // Sets the text to getting data from the server
        loadingText.setText(R.string.workingMagic);

        LocationManager locManager = (LocationManager) (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        String locationProviderNetwork = locManager.NETWORK_PROVIDER;
        String locationProviderGPS = locManager.GPS_PROVIDER;

        // gets two locations and checks which is better
        try {
            /** One-time location request */
            Location locationNetwork = locManager.getLastKnownLocation(locationProviderNetwork);
            Location locationGPS = locManager.getLastKnownLocation(locationProviderGPS);
            isBetterLocation(locationNetwork, locationGPS);

            // If yes it'll use GPS
            // if not it'll use network
            if (isBetterLocation(locationNetwork, locationGPS)) {
                theRightLocation = locationGPS;
                lon = theRightLocation.getLongitude();
                lat = theRightLocation.getLatitude();
                callXMLRPC("vg.search.byCoords");
            }
            else if (!isBetterLocation(locationNetwork, locationGPS)) {
                theRightLocation = locationNetwork;
                lon = theRightLocation.getLongitude();
                lat = theRightLocation.getLatitude();
                callXMLRPC("vg.search.byCoords");
            }
            // if they are both null it displays an alert
            else if (locationNetwork == null && locationGPS == null) {
                AlertDialog.Builder alertBuild = new AlertDialog.Builder(getActivity().getBaseContext());
                alertBuild.setCancelable(false);
                alertBuild.setTitle("Error");
                alertBuild.setMessage("Couldn't get your location");
                alertBuild.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {  }
                });
                AlertDialog alert = alertBuild.create();
                alert.show();
                return;
            }
        } catch (SecurityException e) {

        }
        catch (Exception e) {
           /* AlertDialog.Builder alertBuild = new AlertDialog.Builder(getActivity().getBaseContext());
            alertBuild.setCancelable(false);
            alertBuild.setTitle("Enable Location");
            alertBuild.setMessage("Please enable location services for Vegan360 \n in order to use this feature");
            alertBuild.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {  }
            });
            AlertDialog alert = alertBuild.create();
            alert.show(); */
        }
    }

    public void callXMLRPC(String methodName) {
        // Creates a class and runs doInBackground()
        String[] methods = {methodName};
        new XmlRpc().doInBackground(methods);
    }
    public void onFinish() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        RestaurantsNearby fragment = new RestaurantsNearby();
        fragmentTransaction.replace(R.id.frag_container, fragment);
        fragmentTransaction.commit();
    }

    class XmlRpc extends AsyncTask<String, Void, String> {
        Object o;

        @Override
        protected String doInBackground(String... params) {
            try {
                XMLRPCClient client = new XMLRPCClient(new URL("http://veganguide.org/api"));
                Map<String, Object> struct = new HashMap<>();
                struct.put("apikey", "orpgblykglkw");
                struct.put("lan", "en");

                String methodName = params[0];
                if (methodName == "vg.search.byCoords") {
                    // Set exception
                    sendCoordsMethod(struct, lon, lat);
                    o = client.call(methodName, struct);
                }
                String response = o.toString();
                Log.d(TAG, response);

                if(!response.contains("status=ok")) {
                    // nothing
                }
                else {

                }
            }
            catch (MalformedURLException e) {
                Log.e(TAG, "URL Exception");
            }
            catch (XMLRPCException e) {
                Log.e(TAG, "XML-RPC Exception");
            }
            catch (NullPointerException e) {
                Log.e(TAG, "NullPointer Exception");
            }
            return null;
        }
        public Object sendCoordsMethod(Map<String, Object> struct, Double lon, Double lat) {
            Map<String, Object> structInfo = new HashMap<>();
            structInfo.put("lon", lon);
            structInfo.put("lat", lat);
            structInfo.put("radius", 250.0);

            struct.put("query", structInfo);

            return struct;
        }
    }

}
