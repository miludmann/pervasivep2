package com.tuto.location;

import com.tuto.location.NumberPicker.OnChangedListener;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

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



	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setContentView(R.layout.main);
        
        closeApp = (Button) findViewById(R.id.my_button);
        
        nbp = (NumberPicker) findViewById(R.id.nbp_button);
        nbp.setRange(0, 3600);
        nbp.setCurrent(0);

        locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,timeBetweenFixes,0, locationListener);
        Location location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        closeApp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
//				System.exit(0);
				finish();
			}
        	
        });
        
        final TextView time = (TextView)findViewById(R.id.TextView08);
        int tmp = (int) (timeBetweenFixes / 1000);
        time.setText(tmp+" second(s) between fixes");
        nbp.setOnChangeListener(new OnChangedListener (){
			@Override
			public void onChanged(NumberPicker picker, int oldVal, int newVal) {
				timeBetweenFixes = newVal * 1000;
				time.setText(newVal+" second(s) between fixes");	
				locManager.removeUpdates(locationListener);
				locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,timeBetweenFixes,0, locationListener);
			}       	
        });

      }
	      
    public void init(){
    	 count = 0;
    	 gpsCount = 0;
         m_state = state.periodic;
         timeBetweenFixes = 0;
         timeElapsed = SystemClock.elapsedRealtime();
    }


    private void updateWithNewLocation(Location location) {
        TextView myLatText = (TextView)findViewById(R.id.TextView04);
        TextView myLongText = (TextView)findViewById(R.id.TextView02);

        String latString = "";
        String longString = "";

        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
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
    }
    




	private void readPeriodic(String lng, String lat) {
		if(SystemClock.elapsedRealtime() - timeElapsed > timeBetweenFixes)
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