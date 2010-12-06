//package com.tuto.location;
//
//import android.content.Context;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Bundle;
//import android.os.SystemClock;
//
//public class Periodic {
//	private long elapsedTime;
//	private long timeBetweenFixes;
//	private long periodicTime;
//	private int count;
//	private boolean running;
//	private LocationManager locManager;
//
//	
//	public Periodic(){
//        elapsedTime = SystemClock.elapsedRealtime();
//        timeBetweenFixes = 3000;
//        count = 0;
//        running = true;
//        
//        locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
//        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0, locationListener);
//        Location location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//	}
//	
//    private void readPeriodic() {
//    	while(running)
//    	SystemClock.sleep(timeBetweenFixes);
//
//		sendData("Distance,"+count+","+count+","+longString+","+latString);
//
//    }
//    
//    public void stop()
//    {
//    	running = false;
//    }
//    
//    private final LocationListener locationListener = new LocationListener() {
//        public void onLocationChanged(Location location) {
//        	count++;
////            updateWithNewLocation(location);
//        }
//
//        public void onProviderDisabled(String provider) {
////            updateWithNewLocation(null);
//        }
//
//        public void onProviderEnabled(String provider) {
//        }
//
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//        }
//    };
//}
