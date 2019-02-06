package cz.mendelu.busitweek2019;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.cardemulation.CardEmulation;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.List;

import cz.mendelu.busItWeek.library.BeaconTask;
import cz.mendelu.busItWeek.library.ChoicePuzzle;
import cz.mendelu.busItWeek.library.CodeTask;
import cz.mendelu.busItWeek.library.GPSTask;
import cz.mendelu.busItWeek.library.ImageSelectPuzzle;
import cz.mendelu.busItWeek.library.Puzzle;
import cz.mendelu.busItWeek.library.SimplePuzzle;
import cz.mendelu.busItWeek.library.StoryLine;
import cz.mendelu.busItWeek.library.Task;
import cz.mendelu.busItWeek.library.beacons.BeaconDefinition;
import cz.mendelu.busItWeek.library.beacons.BeaconUtil;
import cz.mendelu.busItWeek.library.map.MapUtil;
import cz.mendelu.busItWeek.library.qrcode.QRCodeUtil;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, LocationEngineListener {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationComponent locationComponent;

    private StoryLine storyLine;
    private Task currentTask;

    private Marker currentMarker;

    private BeaconUtil beaconUtil;

    private ImageButton qrCodeButton;
    private CardView beaconScanningCard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1Ijoic2hldnNoaW5rbyIsImEiOiJjanJzeTdhMWkwZzM4NDRuOG52cmxxczRmIn0.kElFbuIW3mnSbcnsTVNF4w");
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mapView = findViewById(R.id.map_view);
        qrCodeButton = findViewById(R.id.qr_button);
        beaconScanningCard = findViewById(R.id.beacon_scanning);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        beaconUtil = new BeaconUtil(this);

        storyLine = StoryLine.open(this, BusITWeekDatabaseHelper.class);
        currentTask = storyLine.currentTask();

        qrCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QRCodeUtil.startQRScan(MapActivity.this);
            }
        });


    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        mapboxMap.getUiSettings().setAllGesturesEnabled(true);
        initializeListeners();
        updateMarkers();
    }

    private void initializeListeners() {
        if (currentTask != null) {
            if (currentTask instanceof GPSTask) {
                //todo scan for location
                initializeLocationCompoment();
                initializeLocationEngine();
                qrCodeButton.setVisibility(View.GONE);
                beaconScanningCard.setVisibility(View.GONE);
            }

            if (currentTask instanceof CodeTask) {
                qrCodeButton.setVisibility(View.VISIBLE);
                beaconScanningCard.setVisibility(View.GONE);
            }

            if (currentTask instanceof BeaconTask) {
                BeaconDefinition definition = new BeaconDefinition((BeaconTask) currentTask) {
                    @Override
                    public void execute() {
                        runPuzzleActivity(currentTask.getPuzzle());
                    }
                };
                beaconUtil.addBeacon(definition);
                beaconUtil.startRanging();
                qrCodeButton.setVisibility(View.GONE);
                beaconScanningCard.setVisibility(View.VISIBLE);
            }
        }

    }

    private void removeListener() {
        qrCodeButton.setVisibility(View.GONE);

        if (beaconUtil.isRanging()) {
            beaconUtil.stopRanging();
            beaconUtil.clearBeacons();
        }

       if (mapboxMap != null && locationEngine != null){
            locationEngine.removeLocationUpdates();
        }


    }

    private void updateMarkers() {
        if (currentTask != null && mapboxMap != null) {
            if (currentMarker != null) {
                mapboxMap.removeMarker(currentMarker);
            }
            currentMarker = mapboxMap.addMarker(createTaskMarker(this, currentTask));
        }
    }

    private void runPuzzleActivity(Puzzle puzzle) {
        if (puzzle instanceof SimplePuzzle) {
            startActivity(new Intent(this, SimplePuzzleActivity.class));
        }
        if (puzzle instanceof ChoicePuzzle) {
            startActivity(new Intent(this, ChoicePuzzleActivity.class));
        }
        if (puzzle instanceof ImageSelectPuzzle) {
            startActivity(new Intent(this, ImagePuzzleActivity.class));
        }

    }

    private MarkerOptions createTaskMarker(Context context, Task task) {
        int color = R.color.colorGPS;

        if (task instanceof BeaconTask) {
            color = R.color.colorBeacon;
        }

        if (task instanceof CodeTask) {
            color = R.color.colorQR;
        }

        return new MarkerOptions()
                .position(new LatLng(task.getLatitude(), task.getLongitude()))
                .icon(MapUtil.createColoredCircleMarker(context, task.getName(), color));

    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        currentTask = storyLine.currentTask();
        if (currentTask == null) {
            //no more tasks
            startActivity(new Intent(this, FInishActivity.class));
            finish();
        } else {
            initializeListeners();
            updateMarkers();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        removeListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String qrCode = QRCodeUtil.onScanResult(this, requestCode, resultCode, data);
        if (qrCode != null) {
            if (qrCode.equals(((CodeTask) currentTask).getQR())) {
                runPuzzleActivity(currentTask.getPuzzle());
            }
        }
    }

    private void initializeLocationCompoment() {
        if (permissionsManager.areLocationPermissionsGranted(this)) {
            if (mapboxMap != null) {
                locationComponent = mapboxMap.getLocationComponent();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationComponent.activateLocationComponent(this);
                locationComponent.setLocationComponentEnabled(true);
            }
        }else{
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressLint("MissingPermission")
    private void initializeLocationEngine(){
        if(mapboxMap != null && PermissionsManager.areLocationPermissionsGranted(this)){
            locationEngine = new LocationEngineProvider(this )
                    .obtainBestLocationEngineAvailable();
            locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
            locationEngine.setInterval(1000);
            locationEngine.requestLocationUpdates();
            locationEngine.addLocationEngineListener(this);
            locationEngine.activate();

        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {

    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (currentTask != null && currentTask instanceof GPSTask){
            double radius = ((GPSTask) currentTask).getRadius();
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng taskLocation = new LatLng(currentTask.getLatitude(), currentTask.getLongitude());
            if (userLocation.distanceTo(taskLocation)<radius){
                runPuzzleActivity(currentTask.getPuzzle());
            }
        }

    }
}
