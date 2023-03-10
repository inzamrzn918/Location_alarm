package com.rbofficial.inzam.locationalarm;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.telephony.CarrierConfigManager;
import android.location.GpsStatus;

import androidx.annotation.NonNull;

public interface LListener extends LocationListener, GpsStatus.Listener {
    @Override
    void onLocationChanged(@NonNull Location location);
    void onProviderDisabled(String provider);
    void onProviderEnabled(String provider);
    void onStatusChanged(String provider, int status, Bundle extras);
    void onGPSStatusChanged(int event);
}
