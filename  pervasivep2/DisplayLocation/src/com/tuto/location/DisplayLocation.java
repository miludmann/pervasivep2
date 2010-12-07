package com.tuto.location;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.tuto.location.NumberPicker.OnChangedListener;

public class DisplayLocation extends Activity {
	protected Button closeApp;
	protected NumberPicker nbp;
	private int count;
	private int gpsCount;
	private enum state{periodic, distance, maxSpeed, accel};	
	private state m_state;
	private long timeBetweenFixes;
	private LocationManager locManager;
	private long timeElapsed;
	private Location oldLocation;
	private int nbpValue; // Value in the spinbox
	private float distanceBetweenFixes;
	private RadioButton rb1, rb2;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setContentView(R.layout.main);

    	rb1=(RadioButton)findViewById(R.id.periodic);
    	rb2=(RadioButton)findViewById(R.id.distance);

        closeApp = (Button) findViewById(R.id.my_button);
        
        nbp = (NumberPicker) findViewById(R.id.nbp_button);
        nbp.setRange(0, 3600);
        nbp.setCurrent(0);

        locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,timeBetweenFixes,0, locationListener);
        Location location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        closeApp.setOnClickListener(new OnClickListener() {
        	/*
        	 * Write the number of GPS fixes (real and actually sent) in a file
        	 * located at the root of the external storage (sdcard)
        	 * before closing the application
        	 * @see android.view.View.OnClickListener#onClick(android.view.View)
        	 */
			public void onClick(View arg0) {
				String filename = "nb_GPS_fixes.txt";
				String nbFixesString = Integer.toString(count)+", "+Integer.toString(gpsCount);
				
				try {
				    File root = Environment.getExternalStorageDirectory();
				    if (root.canWrite()){
				        File f = new File(root, filename);
				        FileWriter fwriter = new FileWriter(f);
				        BufferedWriter out = new BufferedWriter(fwriter);
				        out.write(nbFixesString);
				        out.close();
				    }
				} catch (IOException e) {
				    System.out.println("Could not write file " + e.getMessage());
				}

				// Close the program
				System.exit(0);
				finish();
			}
        });
        
        final TextView time = (TextView)findViewById(R.id.TextView08);
        int tmp = (int) (timeBetweenFixes / 1000);
        time.setText(tmp+" second(s) between fixes");
        nbp.setOnChangeListener(new OnChangedListener (){
        	/*
        	 * Handling the spinbox NumberPicker
        	 * @see com.tuto.location.NumberPicker.OnChangedListener#onChanged(com.tuto.location.NumberPicker, int, int)
        	 */
        	public void onChanged(NumberPicker picker, int oldVal, int newVal) {
				nbpValue = newVal;
				if(m_state == state.periodic){
					timeBetweenFixes = newVal * 1000;
					time.setText(newVal+" second(s) between fixes");	
					locManager.removeUpdates(locationListener);
					locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,timeBetweenFixes,0, locationListener);
				} else if(m_state == state.distance) {
					distanceBetweenFixes = newVal;
					time.setText(newVal+" meter(s) between fixes");	
				}
				
			}       	
        });
        
        rb1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked){
					m_state = state.periodic;
					locManager.removeUpdates(locationListener);
					locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,nbpValue*1000,0, locationListener);
				}
			}
        });
        rb2.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked){
					m_state = state.distance;
					locManager.removeUpdates(locationListener);
					locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0, locationListener);
				}
			}
        });
      }
	      
    public void init(){
    	 count = 0;
    	 gpsCount = 0;
         m_state = state.periodic;
         timeBetweenFixes = 0;
         timeElapsed = SystemClock.elapsedRealtime();
         oldLocation = null;
         distanceBetweenFixes = 0F;
    }

    private void updateWithNewLocation(Location location) {
        TextView myLatText = (TextView)findViewById(R.id.TextView04);
        TextView myLongText = (TextView)findViewById(R.id.TextView02);

        String latString = "";
        String longString = "";
        double lat = 0.0;
        double lng = 0.0;

        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
            latString = Double.toString(lat);
            longString = Double.toString(lng);
        } else {
            latString = "No location found";
            longString = "No location found";
        }
        myLatText.setText(latString);
		myLongText.setText(longString);
	    
		if(m_state == state.periodic)
			readPeriodic(longString, latString);
		if(m_state == state.distance)
			readDistance(location);
    }
    
	private void readDistance(Location newLocation) {
		float distance = 0F;
		if(oldLocation != null && newLocation != null)
		{
			TextView distText = (TextView)findViewById(R.id.TextView10);
			distance = newLocation.distanceTo(oldLocation);
			distText.setText(distance+" meter(s)");
		} else if(newLocation != null){ //First fix since the program has started
			oldLocation = newLocation;
		}
		if(distance >= distanceBetweenFixes)
		{
			count++; // counter of gps fix actually sent
			sendData("Distance,"+count+","+gpsCount+","+newLocation.getLongitude()+","+newLocation.getLatitude());
			oldLocation = newLocation;
		}
	}

	private void readPeriodic(String lng, String lat) {
		if(SystemClock.elapsedRealtime() - timeElapsed >= timeBetweenFixes)
		{
			count++; // counter of gps fix actually sent
			timeElapsed = SystemClock.elapsedRealtime();
			sendData("Distance,"+count+","+gpsCount+","+lng+","+lat);
		}
	}

	private void sendData(String data){
		 /**
		  * Communication with server
		  */
		 TextView debug = (TextView)findViewById(R.id.TextView06);
		 String msg = "unknown";
		 int status = GpsReader.sendDataToServer(data);
		 if(status == 0){
	 		msg = "Connection to server ok";
		    debug.setText(msg);	 
		 } else {
			 msg = "Connection to server failed ";
	 		 debug.setText(msg);
		 }
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
        	gpsCount++;
            updateWithNewLocation(location);
        }

        public void onProviderDisabled(String provider) {
            updateWithNewLocation(null);
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };



}