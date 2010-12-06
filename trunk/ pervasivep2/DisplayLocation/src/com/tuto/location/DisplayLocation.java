package com.tuto.location;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DisplayLocation extends Activity {
	protected Button closeApp;
	private int count;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        count = 0;
        LocationManager locManager;
        setContentView(R.layout.main);
        
        closeApp = (Button) findViewById(R.id.my_button);
        locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0, locationListener);
        Location location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        
        if(location != null)                                
        {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
        }  

        closeApp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				System.exit(0);
			}
        	
        });

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
	     
         /**
          * Communication with server
          */
	      String hostname = "camel04.cs.au.dk"; //camel04 : 130.225.16.123
	      int port = 15340;
//	      hostname = "84.238.67.223"; //Home PC
	  	  TextView debug = (TextView)findViewById(R.id.TextView06);
	      String msg = "unknown";	      
	      Socket socket = new Socket();
	      SocketAddress addr = new InetSocketAddress(hostname, port);
	      try {
		      	msg = "Connecting...";
		      	debug.setText(msg);		      	
				socket.connect(addr);				
			    msg = "Connection ok";
			    debug.setText(msg);	      	
				OutputStream os = socket.getOutputStream();
				DataOutputStream dos = new DataOutputStream(os);
				dos.writeUTF("Distance,"+count+","+count+","+longString+","+latString);
				dos.close();
				socket.close();
			} catch (IOException e) {
				msg = "Connection failed ";
		    	debug.setText(msg);
				e.printStackTrace();
			}
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
        	count++;
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