package com.example.uu;

import androidx.annotation.ContentView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DrawingMapActivity extends AppCompatActivity  implements OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener{
    private static final String API_KEY="AIzaSyCtR1gj33Jv0oDKpb7PyHVYlXXJsFRp_KQ";
    private Uri mapUri;
    public static String TAG="draw_map";
    static boolean isDrawing=false;
    private GoogleMap mMap;
    private Polyline route_shape;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final List<LatLng> checkpoint=new ArrayList<>();
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);
    private GeoApiContext mGeoApiContext=null;
    private Context context=this;

    private int duration=0;

    private boolean hasMap=false;
    /*Test*/
    private TextView test;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing_map);
        test=findViewById(R.id.seeResult);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.make_route);
        mapFragment.getMapAsync(this);
        if(mGeoApiContext==null){
            mGeoApiContext=new GeoApiContext.Builder().apiKey(API_KEY).build();
            //mGeoApiContext=new GeoApiContext.Builder().apiKey(getString(R.string.GMP_KEY)).build();
        }
        //mBitmapCreatedListener=(OnBitmapCreatedListener) this;

        ImageButton undo=(ImageButton)findViewById(R.id.undo);
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkpoint.remove(checkpoint.size()-1);
                route_shape.setPoints(checkpoint);
            }
        });

        ImageButton delete_map=(ImageButton)findViewById(R.id.del_map);
        delete_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkpoint.clear();
                route_shape.setPoints(checkpoint);
            }
        });

        ImageButton draw=(ImageButton) findViewById(R.id.draw);
        draw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDrawing= !isDrawing;
                if(isDrawing){
                    undo.setVisibility(View.VISIBLE);
                    delete_map.setVisibility(View.VISIBLE);
                }
                else{
                    undo.setVisibility(View.INVISIBLE);
                    delete_map.setVisibility(View.INVISIBLE);
                }
            }
        });

        ImageButton savemap=(ImageButton) findViewById(R.id.savemap);
        savemap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getURLOfMap();
                hasMap=true;
            }
        });

        ImageButton exit=(ImageButton) findViewById(R.id.saveAndexit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkpoint.clear();
                if(hasMap) {
                    setResult(Activity.RESULT_OK, new Intent().putExtra("mapUri", mapUri));
                }
                finish();
            }
        });


    }
    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(result.routes[0].overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(context, R.color.blue));
                    polyline.setClickable(true);

                }
            }
        });
    }
    private void calculateDirections(LatLng markerPosition){
        Log.d(TAG, "calculateDirections: calculating directions.");

        test.setText("calculateDirections: calculating directions.");
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                markerPosition.latitude,markerPosition.longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);
        directions.alternatives(true);
        directions.mode(TravelMode.TRANSIT);
        directions.origin(
                new com.google.maps.model.LatLng(37.5779805, 126.977364)
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].steps[0].duration);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                addPolylinesToMap(result);
            }
            @Override
            public void onFailure(Throwable e) {
                Log.e("TAG", "calculateDirections: Failed to get directions: " + e.getMessage() );
            }
        });
    }
    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {
        // Flip from solid stroke to dotted stroke pattern.
        if ((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        } else {
            // The default pattern is a solid stroke.
            polyline.setPattern(null);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap=googleMap;
        PolylineOptions route_info=new PolylineOptions().clickable(true);
        route_shape=mMap.addPolyline(route_info);
        LatLng Gyeongbokgung = new LatLng(37.5779805, 126.977364);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Gyeongbokgung, 15));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                if(!isDrawing){
                    //when map clicked
                    MarkerOptions markerOptions=new MarkerOptions();
                    //set marker position
                    markerOptions.position(latLng);
                    //set marker title
                    markerOptions.title("position : "+latLng.latitude + " , "+latLng.longitude);

                    //animation for zoom
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                    mMap.addMarker(markerOptions);
                    test.setText("onMapClick");
                    calculateDirections(latLng);
                }
                else{
                    checkpoint.add(latLng);
                    route_shape.setPoints(checkpoint);
                }
            }
        });
    }

    public void getURLOfMap() {
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            @RequiresApi(api = Build.VERSION_CODES.S)
            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                try{
                   saveImage(snapshot);

                } catch (Exception e) {
                    Log.d("snap","snapfail");
                    e.printStackTrace();
                }
            }

        };
        mMap.snapshot(callback);
    }
    private void saveImage(Bitmap bmp){
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bao); // bmp is bitmap from user image file
        String path=MediaStore.Images.Media.insertImage(this.getContentResolver(),bmp,"Title",null);
        mapUri = Uri.parse(path);
    }
}