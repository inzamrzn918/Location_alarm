package com.rbofficial.inzam.locationalarm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.microsoft.maps.BuildConfig;
import com.rbofficial.inzam.locationalarm.databinding.FragmentFirstBinding;
import com.microsoft.maps.MapRenderMode;
import com.microsoft.maps.MapView;
public class FirstFragment extends Fragment {


    private FragmentFirstBinding binding;
    private MapView mMapView;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMapView = new MapView(requireContext(),MapRenderMode.VECTOR);
        mMapView.setCredentialsKey("AlPGvIKjbZMk-0Pu3W7vSDyaOG4vUxhYm79aXGPcaG2vSsBV5vdPpS5wtF0EGAYh");
        binding.mapView.addView(mMapView);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}