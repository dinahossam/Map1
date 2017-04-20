package com.example.dina.map1;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.*;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.client.core.SyncPoint;
import com.firebase.client.snapshot.DoubleNode;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    IALocationManager mLocationManager;

    // define the display assembly compass picture
    private ImageView image;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    // device sensor manager
    private SensorManager mSensorManager;

    TextView tvHeading;

    private int n =0;

    private static double longitute = 0.0;
    private static double latitute = 0.0;

    private static double init_longitute = 31.20732161;
    private static double init_latitute =  29.92452655;

    private com.firebase.client.DataSnapshot point;

    private String dest;
    private int destIndex = -1;
    private int v =0;

    final ArrayList<com.firebase.client.DataSnapshot> points =  new ArrayList<com.firebase.client.DataSnapshot>();
    final ArrayList<com.firebase.client.DataSnapshot> sp =  new ArrayList<com.firebase.client.DataSnapshot>();
    final ArrayList<com.firebase.client.DataSnapshot> adjecent_matrix=  new ArrayList<com.firebase.client.DataSnapshot>();
    final private HashMap<Integer,Integer > myMap = new HashMap<Integer, Integer>();
     double graph[][] ;
    int parent[];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationManager = IALocationManager.create(this);

        // our compass image
        image = (ImageView) findViewById(R.id.imageViewCompass);

        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.tvHeading);

        Intent intent = getIntent();
        tvHeading.setText("Dest: " +  intent.getStringExtra("dest"));
        dest = intent.getStringExtra("dest");

       // double dest_id=(double)points.\



        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //   Firebase.setandroid
        Firebase.setAndroidContext(this);
        Firebase ref = new Firebase("https://map1-ab0da.firebaseio.com/points");

        Query queryRef = ref.orderByChild("name").equalTo(dest);
        System.out.println("YA RAB RABNA YHDEK W TTB3 AY 7AGA "+queryRef);
        queryRef.addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    destIndex =Integer.parseInt( String.valueOf(child.getKey()));
                    System.out.println( "henaa" + destIndex);
                    System.out.println( "henaa" + child.getKey());

                }

            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }

        });


        Firebase ref1 = new Firebase("https://map1-ab0da.firebaseio.com/adjecent matrix");

        ref1.addValueEventListener(new com.firebase.client.ValueEventListener(){
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                int i = 0;
                for (com.firebase.client.DataSnapshot data: dataSnapshot.getChildren()) {
                    System.out.println("alaaaaaaa" + data);
                    adjecent_matrix.add(data);
                    System.out.println("dinaaaaaaaaaa" + adjecent_matrix.get(v).child("neighbour1"));

                    myMap.put(v,Integer.valueOf(data.getKey()));
                    //System.out.println("dinaaaaaaaaaa"+v);
                    v++;
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }

        });



       // com.firebase.client.DataSnapshot check = ref.child("points").

        ref.addValueEventListener(new com.firebase.client.ValueEventListener() {

            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                int i = 0;
                for (com.firebase.client.DataSnapshot data: dataSnapshot.getChildren()) {
                    addToArrayList(data);

                }
                if(points.size()==111){
                   // point = findSrc(init_longitute, init_latitute);
                   // System.out.println("Nearest point" + point.child("id").getValue());
                   // System.out.println(countPoints());
                }
                else System.out.println("zeroooo");

              if(adjecent_matrix.size() == v){
                    System.out.println(" hangrb hena");
                     double[][] matrix = create_Matrix();
                     print_graph(matrix);
                     shortestPath();

             }


            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });



    }

    IALocationListener mLocationListener = new IALocationListener() {
        @Override
        public void onLocationChanged(IALocation iaLocation) {
            //TextView txtLoc = (TextView) findViewById(R.id.myTextView);
            //txtLoc.setText(String.valueOf(iaLocation.getLatitude() + " ," + iaLocation.getLongitude()));
            longitute = iaLocation.getLongitude();
            latitute = iaLocation.getLatitude();

            if ((init_latitute == -1) && (init_longitute == -1)){
                init_latitute = latitute;
                init_longitute = longitute;
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }
    };

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


       // double distance = showPath(  29.92451850,31.20733452,0);
      //  if(destIndex!=-1) {
       //     printPath(parent, destIndex);
       // }
       // String test= Double.toString(distance);
         //text.setText(test);
       // Log.i("MainActivity" ,"hello"+ test) ;
        Log.i("MainActivity" ,"size hereeeeeee plz"+ points.size()) ;


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


    private int findSrc(double longitute, double latitute){
        double min = 0.0;
        com.firebase.client.DataSnapshot point = null;
        int i;
        for ( i = 0; i < points.size(); i++){
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
        return i;
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

//    //private double showPath(double lat,double longitude,double headX){
//        Log.i("MainActivity" ,"size hereeeeeee plz"+ points.size()) ;
//        if(points.size()==111) {
//            double lat1 =(double) sp.get(n).child("lat").getValue();
//            double  long1 =(double) sp.get(n).child("long").getValue();
//            double  dist1 = getDistance(lat,longitude,lat1,long1);
//            if (dist1 <= 1 ) {
//                n++;
//            }
//           // Log.i("MainActivity" ,"checkkkkkkk"+ lat2 + ".............."+long2) ;
//            return getDegrees(lat, longitude, lat1, long1, headX);
//        }
//        return 0;
//    }

    private double getDistance(double long1,double lat1,double long2,double lat2){
        Location locationA = new Location("point A");
        locationA.setLatitude(long1);
        locationA.setLongitude(lat1);
        Location locationB = new Location("point B");
        locationB.setLatitude(long2);
        locationB.setLongitude(lat2);
        return locationA.distanceTo(locationB) ;


    }

    private int index(int key ){
        for(int i =0 ;i<points.size();i++){
            if(key == Integer.valueOf(points.get(i).getKey())){
                return i;
            }
        }
        return 0;
    }

    private int getIndex(int key ){
        for(int i =0 ;i<myMap.size();i++){
            if(key == myMap.get(i)){
                return i;
            }
        }
        return 0;
    }

    private double[][] create_Matrix(){

        graph = new double [v][v];


        for(int i = 0;i< v;i++){
            int key = myMap.get(i);
            int key1= index(key);
            System.out.println("keyyyyyyyyyyyyyyyy "+ key);
            int neighbour1 =  Integer.parseInt(String.valueOf( adjecent_matrix.get(i).child("neighbour1").getValue()));
            // System.out.println("datasnap"+adjecent_matrix.get(j));
            // System.out.println("datasnap child"+adjecent_matrix.get(j).child("neighbour1"));
//                System.out.println("datasnap child value"+adjecent_matrix.get(j).child("neighbour1").getValue());
            int neighbour2 = Integer.parseInt(String.valueOf(adjecent_matrix.get(i).child("neighbour2").getValue()));
            int neighbour3 =Integer.parseInt(String.valueOf( adjecent_matrix.get(i).child("neighbour3").getValue()));
            int neighbour4 =Integer.parseInt(String.valueOf( adjecent_matrix.get(i).child("neighbour4").getValue()));
            int neighbour5 =Integer.parseInt(String.valueOf( adjecent_matrix.get(i).child("neighbour5").getValue()));
            int neighbour6 =Integer.parseInt(String.valueOf( adjecent_matrix.get(i).child("neighbour6").getValue()));
            int neighbour7 =Integer.parseInt(String.valueOf(adjecent_matrix.get(i).child("neighbour7").getValue()));
            int neighbour8 =Integer.parseInt(String.valueOf( adjecent_matrix.get(i).child("neighbour8").getValue()));
            for(int j=0 ;j < v ;j++){
                int dest = myMap.get(j);
                System.out.println("desttttttttttttttttttt "+ dest);
                if( dest==neighbour1 || dest==neighbour2 ||dest==neighbour3 ||dest==neighbour4 ||dest==neighbour5 ||dest==neighbour6  ){
                    graph[i][j] = getDistance((double)points.get(key1).child("lat").getValue(),(double)points.get(key1).child("long").getValue(),
                                   (double)points.get(index(dest)).child("lat").getValue(),(double)points.get(index(dest)).child("long").getValue());
               }
                else {
                    graph[i][j]=0;
                }


            }
       }
       return graph;
    }

    private double [][] print_graph(double[][] graph){
        double [][] print = new double [v][v];
        for(int i = 0;i< v ;i++){
            for(int j =0 ; j<v ;j++){
                System.out.print(" graph"+graph[i][j] + "  ");
            }
            System.out.println();
            System.out.println();
        }
        return print;
    }

    private int minDistance(double dist[], boolean sptSet[])
    {

        // Initialize min value
        double min = 100000000;
        int min_index=-1;

        for (int i = 0; i < v; i++)
            if (sptSet[i] == false && dist[i] <= min) {
                min = dist[i];
                min_index = i;
            }

        return min_index;
    }

    void printPath(int parent[], int j)
    {
        // Base Case : If j is source
        if (parent[j]==-1)
            return;

        printPath(parent, parent[j]);
        int key =myMap.get(j);
     //   sp.add(points.get(index(key)));
        System.out.println(key + "ya rab yb2o sa7 " + points.get(index(key)));
        System.out.print( " ana parent meeen " + key);
    }

    private void printSolution(double dist[], int n, int parent[])
    {
        int src = 4;
        System.out.println("Vertex\t  Distance\tPath");
       // if(destIndex != -1) {
            //int destPoint = Integer.valueOf(String.valueOf(dest));
           // int destPointIndex = getIndex(destPoint);
        for (int i = 0; i < v; i++)
        {
            System.out.println("    " + myMap.get(src) + "  ->    " + i + "   " + dist[i]);
            printPath(parent, i);
        }

    }

    private void shortestPath(){
        double dist[] = new double[v];
        boolean sptSet[] = new boolean[v];
         parent = new int [v];
        int src = getIndex(21);

        for (int i = 0; i < v; i++)
        {
            parent[src] = -1;
            dist[i] = (int) Double.POSITIVE_INFINITY;
            sptSet[i] = false;
        }


        System.out.println("src" + src);

       dist[src] = 0;

        for (int count = 0; count < v-1; count++)
        {

            int u = minDistance(dist, sptSet);
            sptSet[u] = true;
            for (int i = 0; i < v; i++)

                // Update dist[v] only if is not in sptSet, there is
                // an edge from u to v, and total weight of path from
                // src to v through u is smaller than current value of
                // dist[v]
                if (!sptSet[i] && (graph[u][i] > 0.000000001 )&&
                        dist[u] + graph[u][i] < dist[i])
                {
                    parent[i]  = u;
                    dist[i] = dist[u] + graph[u][i] ;
                }
        }

        // print the constructed distance array
        printSolution(dist, v, parent);
    }


}