package com.example.linkup.EventOperation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.linkup.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class FindRoute extends BottomSheetDialogFragment {
    // Layout objects
    ImageView btnSearch;
    EditText source;

    // Gain destination
    String startingPoint, destination;

    // Location client
    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.form_find_route, container, false);

        // [Gain destination]
        if (getArguments() != null && getArguments().containsKey("destination")) {
            destination = (String) getArguments().getSerializable("destination");
        }

        // [START gain layout objects]
        btnSearch = view.findViewById(R.id.btnSearch);
        source = view.findViewById(R.id.source);
        // [END gain]

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Fetch current location
        getCurrentLocation();

        // [START layout component function]
        // Search the Route
        btnSearch.setOnClickListener(v -> {
            startingPoint = source.getText().toString();
            if (TextUtils.isEmpty(startingPoint)) {
                Toast.makeText(getContext(), "Starting point is required", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Searching...", Toast.LENGTH_SHORT).show();
                try {
                    String start = URLEncoder.encode(startingPoint, "UTF-8");
                    String end = URLEncoder.encode(destination, "UTF-8");
                    Uri uri = Uri.parse("https://www.google.com.hk/maps/dir/" + start + "/" + end);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setPackage("com.google.android.apps.maps");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent); // Use context to start the activity
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Failed to encode location.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // [END layout component function]

        return view;
    }

    // Method to fetch the current location
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request location permissions
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        // Fetch last known location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        // Get latitude and longitude
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Set the starting point as "latitude,longitude"
                        startingPoint = latitude + "," + longitude;
                        source.setText(startingPoint); // Auto-fill the source EditText
                    } else {
                        Toast.makeText(getContext(), "Unable to retrieve current location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, fetch location
                getCurrentLocation();
            } else {
                // Permission denied
                Toast.makeText(getContext(), "Location permission is required to fetch your current location.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}