package com.rbofficial.inzam.locationalarm;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;

import static com.rbofficial.inzam.locationalarm.Utils.LOCATION_ACCESS_CODE;
import static com.rbofficial.inzam.locationalarm.Utils.LOCATION_REFRESH_DISTANCE;
import static com.rbofficial.inzam.locationalarm.Utils.LOCATION_REFRESH_TIME;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.Geoposition;
import com.microsoft.maps.MapAnimationKind;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapImage;
import com.microsoft.maps.MapScene;
import com.microsoft.maps.MapTappedEventArgs;
import com.microsoft.maps.MapUserInterfaceOptions;
import com.microsoft.maps.MapUserLocationButtonTappedEventArgs;
import com.microsoft.maps.OnMapTappedListener;
import com.microsoft.maps.OnMapUserLocationButtonTappedListener;
import com.rbofficial.inzam.locationalarm.databinding.FragmentFirstBinding;
import com.microsoft.maps.MapRenderMode;
import com.microsoft.maps.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class FirstFragment extends Fragment implements LListener {


    private FragmentFirstBinding binding;
    private MapView mMapView;
    private MapElementLayer mPinLayer;
    private Location currLocation ;
    private String pushpinTitle = "";
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private MapUserInterfaceOptions uiOptions;
    private Geoposition geoposition;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        Utils.getPermissions(requireContext(), Utils.LOCATION_ACCESS_CODE, Utils.permissions);
        preferences = requireContext().getSharedPreferences(BuildConfig.APPLICATION_ID,
                Context.MODE_PRIVATE);
        editor = preferences.edit();
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }


    public double getDistance(Location org, Location dest){
        String URL = "";
        String API_KEY = "AlPGvIKjbZMk-0Pu3W7vSDyaOG4vUxhYm79aXGPcaG2vSsBV5vdPpS5wtF0EGAYh";
        String TRAVEL_MODE = "1";
        String ORIGIN = org.getLatitude()+","+org.getLongitude();
        String DESTINATION = dest.getLatitude()+","+dest.getLongitude();
        String TIME_UNITE = "minute";

        URL = Utils.BASE_MAP_URL+"origins="+ORIGIN+"&destinations="+DESTINATION+"&travelMode="+TRAVEL_MODE+"&timeUnit="+TIME_UNITE+"&key="+API_KEY;
        Log.d(Utils.TAG, "getDistance: "+URL);
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        StringRequest request = new StringRequest(URL, response -> {
            try {
                JSONObject object = new JSONObject(response);
                JSONArray array = object.getJSONArray("resourceSets");
                JSONObject obj = array.getJSONObject(0);
                JSONArray array1 = obj.getJSONArray("resources");
                JSONObject obj2 = array1.getJSONObject(0);
                JSONArray arr3 = obj2.getJSONArray("results");
                JSONObject obj3 = arr3.getJSONObject(0);
                String travelDistance = obj3.getString("travelDistance");
                Toast.makeText(requireContext(), travelDistance, Toast.LENGTH_LONG).show();
                Log.d(Utils.TAG, travelDistance);
                double distance = Double.parseDouble(travelDistance);
                alertUser(distance);
                editor.putString("distance", travelDistance);
                editor.commit();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Toast.makeText(requireContext(), error.toString(), Toast.LENGTH_SHORT).show();
        });

        queue.add(request);

        return 0.0;
    }

    private void alertUser(double distance) {
        Vibrator v = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (distance<2 && distance>1){
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }else if(distance<=1 && distance>0.5){
            long[] timing = {500,500,500};
            v.vibrate(VibrationEffect.createWaveform(timing, 2));
        }else if(distance<=0.5){
            long[] timing = {500,500,500};
            v.vibrate(VibrationEffect.createWaveform(timing, 5));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode==LOCATION_ACCESS_CODE){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                updateLocation();

            }else {
                Utils.getPermissions(requireContext(), LOCATION_ACCESS_CODE, permissions);
            }
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currLocation = new Location(LocationManager.GPS_PROVIDER);
        currLocation.setLongitude(preferences.getFloat("clon",0));
        currLocation.setLatitude(preferences.getFloat("clat",0));

        updateLocation();

        mMapView = new MapView(requireContext(), MapRenderMode.RASTER);
        mMapView.setCredentialsKey("AlPGvIKjbZMk-" +
                "0Pu3W7vSDyaOG4vUxhYm79aXGPcaG2vSsBV5vdPpS5wtF0EGAYh");
        binding.mapView.addView(mMapView);
        mPinLayer = new MapElementLayer();
        mMapView.getLayers().add(mPinLayer);
        uiOptions = mMapView.getUserInterfaceOptions();
        uiOptions.setUserLocationButtonVisible(true);
        uiOptions.setTiltButtonVisible(false);
        uiOptions.setDirectionsButtonVisible(true);
        uiOptions.setZoomGestureEnabled(true);
        mMapView.setBusinessLandmarksVisible(true);



        uiOptions.addOnUserLocationButtonTappedListener(eventArgs -> {
            updateLocation();
            return true;
        });


        mMapView.addOnMapTappedListener(mapTappedEventArgs -> {
            geoposition = mapTappedEventArgs.location.getPosition();
            editor.putFloat("dlat", (float) geoposition.getLatitude());
            editor.putFloat("dlon", (float) geoposition.getLongitude());
            editor.putString("to", mapTappedEventArgs.location.getPosition().getLatitude()
                    +","+mapTappedEventArgs.location.getPosition().getLongitude());
            editor.commit();
            mPinLayer.getElements().clear();
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireContext());
            alertDialog.setTitle("Add a note");

            final EditText input = new EditText(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            alertDialog.setView(input);
            MapIcon pushpin = new MapIcon();

            pushpin.setLocation(mapTappedEventArgs.location);
            alertDialog.setPositiveButton("SET ALARM", (dialogInterface, i) -> {
                pushpinTitle = input.getText().toString().trim();
                pushpin.setTitle(pushpinTitle);
            });
            alertDialog.setNegativeButton("CANCEL", null);
            alertDialog.show();

//                pushpin.setImage(new MapImage(Utils.getBitmap(R.drawable.ic_location_red)));

            mPinLayer.getElements().add(pushpin);

            if (currLocation!=null){
                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(geoposition.getLatitude());
                location.setLongitude(geoposition.getLongitude());

                getDistance(currLocation, location);

            }
            return true;
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }




    @SuppressLint("MissingPermission")
    private void updateLocation() {
        Toast.makeText(requireContext(), "Updating", Toast.LENGTH_SHORT).show();
        LocationManager mLocationManager = (LocationManager) requireContext()
                .getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, this);
        currLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        currLocation = mLocationManager.
        Location dest = new Location(LocationManager.GPS_PROVIDER);
        if (geoposition==null){
            Toast.makeText(requireContext(), "Please select destination", Toast.LENGTH_SHORT).show();
        }
//        if(geoposition!=null){
//            dest.setLatitude(preferences.getFloat("dlat", 0.0F));
//            dest.setLongitude(preferences.getFloat("dlon",0.0F));
//
//        }

        getDistance(currLocation, dest);

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mMapView.setScene(MapScene.createFromLocationAndRadius(new Geopoint(location.getLatitude(),
                location.getLongitude()
        ), 1500), MapAnimationKind.LINEAR);
        Toast.makeText(requireContext(), location.toString(), Toast.LENGTH_SHORT).show();
        currLocation = location;
        editor.putString("from", location.getLatitude()+","+location.getLongitude());
        MapIcon icon = new MapIcon();
        Geopoint geopoint = new Geopoint(location.getLatitude(), location.getLongitude());
        icon.setLocation(geopoint);
        mPinLayer.getElements().add(icon);
        mMapView.getLayers().add(mPinLayer);
        icon.setLocation(geopoint);
        updateLocation();
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (Objects.equals(provider, LocationManager.GPS_PROVIDER)){
            Toast.makeText(requireContext(), "Please enable GPS", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(requireContext(), "GPS Enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onGPSStatusChanged(int event) {

    }

    @Override
    public void onGpsStatusChanged(int i) {

    }
}
