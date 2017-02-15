package com.example.dina.map1;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // define the display assembly compass picture
    private ImageView image;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    // device sensor manager
    private SensorManager mSensorManager;

    TextView tvHeading;

    private int n =0;

    final ArrayList<com.firebase.client.DataSnapshot> points =  new ArrayList<com.firebase.client.DataSnapshot>();
    final ArrayList<com.firebase.client.DataSnapshot> sp =  new ArrayList<com.firebase.client.DataSnapshot>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // our compass image
        image = (ImageView) findViewById(R.id.imageViewCompass);

        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.tvHeading);

        Intent intent = getIntent();
        tvHeading.setText("Dest: " +  intent.getStringExtra("dest"));

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //   Firebase.setandroid
        Firebase.setAndroidContext(this);
        Firebase ref = new Firebase("https://map1-ab0da.firebaseio.com/points");

        ref.addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                int i = 0;
                for (com.firebase.client.DataSnapshot data: dataSnapshot.getChildren()) {
                    addToArrayList(data);

                }
                sp.add(points.get(21));
                sp.add(points.get(33));
                sp.add(points.get(2));
                if(points.size()==111){
                    com.firebase.client.DataSnapshot point= findSrc(31.20722670, 29.92458723);
                    System.out.println("Nearest point" + point.child("id").getValue());
                    System.out.println(countPoints());
                }
                else System.out.println("zeroooo");

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }



    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        //tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");
       // Intent intent = getIntent();
        //tvHeading.setText("Dest: " +  intent.getStringExtra("dest"));


        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        currentDegree = -degree;


        double distance = showPath(  29.92451850,31.20733452,0);
        String test= Double.toString(distance);
         //text.setText(test);
        Log.i("MainActivity" ,"hello"+ test) ;
        //Log.i("MainActivity" ,"size hereeeeeee plz"+ points.size()) ;


    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void addToArrayList(com.firebase.client.DataSnapshot data){
        points.add(data);
    }

    private int countPoints(){
        Log.i("MainActivity" ,"size hereeeeeee plz"+ points.size());
        return  points.size();
    }


    private com.firebase.client.DataSnapshot findSrc(double longitute, double latitute){
        double min = 0.0;
        com.firebase.client.DataSnapshot point = null;
        for (int i = 0; i < points.size(); i++){
            double x = 0.0;
            double y = 0.0;
            //System.out.println("lAt num " +i+" "+ points.get(i).child("lat").getValue());
            x = (double)points.get(i).child("long").getValue();
            y=  (double)points.get(i).child("lat").getValue();
            if(x!=0.0 && y!= 0.0){
                double dist = getDistance(longitute, latitute, x, y);

                if (dist < min || min == 0.0) {
                    min = dist;
                    point = points.get(i);
                }

            }

        }
        return point;
    }

    private double  getDegrees(double lat1,double long1, double lat2,double long2,double  headX) {
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(long2-long1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1)*Math.sin(lat2) -
                Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
        double brng = Math.toDegrees(Math.atan2(y, x));

        // fix negative degrees
        if(brng<0) {
            brng=360-Math.abs(brng);
        }

        return brng - headX;
    }

    private double showPath(double lat,double longitude,double headX){
        Log.i("MainActivity" ,"size hereeeeeee plz"+ points.size()) ;
        if(points.size()==111) {
            if (lat == (double) sp.get(n).child("lat").getValue() && longitude == (double) sp.get(n).child("long").getValue()) {
                n++;
            }
            double lat2 = (double) sp.get(n).child("lat").getValue();
            double long2 = (double) sp.get(n).child("long").getValue();
           // Log.i("MainActivity" ,"checkkkkkkk"+ lat2 + ".............."+long2) ;
            return getDegrees(lat, longitude, lat2, long2, headX);
        }
        return 0;
    }

    private double getDistance(double long1,double lat1,double long2,double lat2){
        Location locationA = new Location("point A");
        locationA.setLatitude(long1);
        locationA.setLongitude(lat1);
        Location locationB = new Location("point B");
        locationB.setLatitude(long2);
        locationB.setLongitude(lat2);
        return locationA.distanceTo(locationB) ;
    }


}