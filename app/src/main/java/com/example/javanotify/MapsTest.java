package com.example.javanotify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsTest extends AppCompatActivity {
    //Initialize Variable
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;
    double currentLat, currentLong = 0;
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);

        //Assign Variable
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);

        //Initialize fused location
        client = LocationServices.getFusedLocationProviderClient(this);

        //checkpermission
        if (ActivityCompat.checkSelfPermission(MapsTest.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //call method
            getCurrentLocation();
            nearbyPolice();

        }else{
            //permission denied
            ActivityCompat.requestPermissions(MapsTest.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
        }


    }

    public void nearbyPolice() {

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" + //url
        "?location" + currentLat + "," + currentLong + //lat and long
        "&radius=5000" + //nearby radius
        "&types=" + "bakery" + //police search
        "&sensors=true" + //sensor
        "&key=" + getResources().getString(R.string.google_maps_key);//gmaps key

        new PlaceTask().execute(url);


    }

    private void getCurrentLocation() {
        //Initialize task location

        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //If successful
                if (location != null) {
                    //sync map
                    currentLat = location.getLatitude();
                    currentLong = location.getLongitude();
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            //initialize lat lng
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            //create marker options
                            MarkerOptions options = new MarkerOptions().position(latLng).title("You are here");
                            //Zoom map
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                            //add marker on map
                            googleMap.addMarker(options);


                        }
                    });
                }
            }
        });
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 44){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
                nearbyPolice();
            }

        }
    }

    private class PlaceTask extends AsyncTask<String,Integer,String> {
        @Override
        protected String doInBackground(String... strings) {
       String data = null;
            try {
                //initialize data
                 data = downloadUrl(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            //execute parser task
            new ParserTask().execute(s);
        }
    }

    private String downloadUrl(String string) throws IOException {
        //Initialize url
        URL url = new URL(string);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //connect connection
        connection.connect();
        //Inpupt stream
        InputStream stream = connection.getInputStream();
        //buffer reader
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        //string builder
        StringBuilder builder = new StringBuilder();
        //string variable
        String line = "";
        //while loop
        while ((line = reader.readLine())!= null){
            //Append line
            builder.append(line);
        }
        //get append data
        String data = builder.toString();
        //close reader
        reader.close();
        //return data
        return data;

    }

    private class ParserTask extends AsyncTask<String,Integer, List<HashMap<String,String>>> {
        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            //create json parser
            JsonParser jsonParser = new JsonParser();
            //initialize hash map list
            List<HashMap<String,String>> mapList = null;
           JSONObject object = null;
            try {
                //Initialize json object
                object = new JSONObject(strings[0]);
                //parse json object
                mapList = jsonParser.parseResult(object);
                Log.d("debug", "maplist="+String.valueOf(mapList));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return mapList;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {


            //use for loop
            for (int i=0; i<hashMaps.size(); i++){
                //initialize hash map
                HashMap<String,String> hashMapList = hashMaps.get(i);
                //get latitude
                double lat = Double.parseDouble(hashMapList.get("lat"));
                //get longitude
                double lng = Double.parseDouble(hashMapList.get("lng"));
                //Get name
                String name = hashMapList.get("name");
                //concat lat and longitude
                LatLng latlng = new LatLng(lat,lng);
                //initialize marker options
                MarkerOptions options = new MarkerOptions();
                //set position
                options.position(latlng);
                //set title
                options.title(name);
                //add marker on map
                map.addMarker(options);
                Log.d("Debug", "Hashmaplist="+String.valueOf(hashMapList));
            }

        }
    }

    public class JsonParser {
        private HashMap<String,String> parseJsonObject(JSONObject object){
            //Initialize hash map
            HashMap<String,String> datalist = new HashMap<>();

            try {
                //get name from objecdt
                String name = object.getString("name");
                //get lat from object
                String latitude = object.getJSONObject("geometry").getJSONObject("location").getString("lat");
                //get lat from object
                String longitude = object.getJSONObject("geometry").getJSONObject("location").getString("lng");
                //put all value in hash map
                datalist.put("name",name);
                datalist.put("lat",latitude);
                datalist.put("lng",longitude);
                Log.d("Debug", "datslist="+String.valueOf(datalist));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //return hashmap
            return datalist;
        }
        private List<HashMap<String,String>> parseJsonArray(JSONArray jsonArray){
            //initialize hash map list
            List<HashMap<String,String>> datalist = new ArrayList<>();
            for (int i=0; i<jsonArray.length(); i++){

                try {
                    //initialize hash map
                    HashMap<String,String> data = parseJsonObject((JSONObject) jsonArray.get(i));
                    datalist.add(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //return hashmap list
            return datalist;
        }

        public List<HashMap<String,String>> parseResult(JSONObject object){
            //initialize json array
            JSONArray jsonArray = null;
            //get result array
            try {
                jsonArray = object.getJSONArray("results");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //return array
            return parseJsonArray(jsonArray);
        }
    }

}
