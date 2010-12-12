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
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.tuto.location.NumberPicker.OnChangedListener;

public class DisplayLocation extends Activity implements AccelerometerListener{
	protected Button closeApp;
	protected NumberPicker nbp;
	protected NumberPicker nbp2;
	private int count;
	private int gpsCount;
	private enum state{periodic, distance, maxSpeed, accelerometer};	
	private state m_state;
	private long timeBetweenFixes;
	private LocationManager locManager;
	private long timeElapsed;
	private Location oldLocation;
	private int nbp1Value; // Value in the spinbox
	private int nbp2Value; // Value in the spinbox
	private float distanceBetweenFixes;
	private RadioButton rb1, rb2, rb3, rb4;
	private float maxSpeed;
	private TextView countValText;
    private TextView gpsCountValText;
    private boolean distTilCheckPointReached;
    private static Context CONTEXT;
    private int nbSamples = 500;
    private int valueSampled = 0;
    private float valueG = 0;
    private boolean isMoving;
    private long tTot, tTmp, tStreak;
    private int strength, strenghtIn, strenghtOut = 10;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setContentView(R.layout.main);

    	rb1=(RadioButton)findViewById(R.id.periodic);
    	rb2=(RadioButton)findViewById(R.id.distance);
    	rb3=(RadioButton)findViewById(R.id.maxSpeed);
    	rb4=(RadioButton)findViewById(R.id.accelerometer);

        closeApp = (Button) findViewById(R.id.my_button);
        
        nbp = (NumberPicker) findViewById(R.id.nbp_button);
        nbp.setRange(0, 3600);
        nbp.setCurrent(1);
        
        nbp2 = (NumberPicker) findViewById(R.id.nbp2_button);
        nbp2.setRange(0, 80);
        nbp2.setCurrent(10);
        
        countValText = (TextView)findViewById(R.id.TextCountVal);
        gpsCountValText = (TextView)findViewById(R.id.TextGpsCountVal);
        
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
				nbp1Value = newVal;
				if(m_state == state.periodic){
					timeBetweenFixes = newVal * 1000;
					time.setText(newVal+" second(s) between fixes");	
					locManager.removeUpdates(locationListener);
					locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,timeBetweenFixes,0, locationListener);
				} else if(m_state == state.distance || m_state == state.accelerometer) {
					distanceBetweenFixes = newVal;
					time.setText(newVal+" meter(s) between fixes");	
				} else if(m_state == state.maxSpeed){
					distanceBetweenFixes = newVal;
					long timeBetweenFixesSeconds = (long) (distanceBetweenFixes/maxSpeed);
					timeBetweenFixes = timeBetweenFixesSeconds * 1000;
					time.setText("Min "+timeBetweenFixesSeconds+" s. between fixes");					}
			}       	
        });
        nbp2.setOnChangeListener(new OnChangedListener (){
        	/*
        	 * Handling the spinbox NumberPicker2
        	 * @see com.tuto.location.NumberPicker.OnChangedListener#onChanged(com.tuto.location.NumberPicker, int, int)
        	 */
        	public void onChanged(NumberPicker picker, int oldVal, int newVal) {
				nbp2Value = newVal;
				if(m_state == state.maxSpeed){
					maxSpeed = newVal;
					long timeBetweenFixesSeconds = (long) (distanceBetweenFixes/maxSpeed);
					timeBetweenFixes = timeBetweenFixesSeconds * 1000;
					time.setText("Min "+timeBetweenFixesSeconds+" s. between fixes");	
					locManager.removeUpdates(locationListener);
					locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,timeBetweenFixes,0, locationListener);
				} 
				
			}       	
        });
        
        rb1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked){
					stopAccelerometer();
					m_state = state.periodic;
					nbp.setCurrent(1);
					timeBetweenFixes = nbp.getCurrent()*1000;
					time.setText(nbp.getCurrent()+" second(s) between fixes");
					locManager.removeUpdates(locationListener);
					locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,timeBetweenFixes,0, locationListener);
				}
			}
        });
        rb2.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked){
					stopAccelerometer();
					m_state = state.distance;
					nbp.setCurrent(50);
					distanceBetweenFixes = nbp.getCurrent();
					time.setText(distanceBetweenFixes+" meter(s) between fixes");
					locManager.removeUpdates(locationListener);
					locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0, locationListener);
				}
			}
        });
        rb3.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked){
					stopAccelerometer();
					m_state = state.maxSpeed;
					nbp.setCurrent(50);
					distanceBetweenFixes = nbp.getCurrent();
					nbp2.setCurrent(10);
					maxSpeed = nbp2.getCurrent();
					long timeBetweenFixesSeconds = (long) (distanceBetweenFixes/maxSpeed);
					timeBetweenFixes = timeBetweenFixesSeconds * 1000;
					time.setText("Min "+timeBetweenFixesSeconds+" s. between fixes");					
					locManager.removeUpdates(locationListener);
					locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,timeBetweenFixes,0, locationListener);
				}
			}
        });
        rb4.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked){
	                onResume(); // start listening to Accelerometer sensor
					m_state = state.accelerometer;
					nbp.setCurrent(50);
					distanceBetweenFixes = nbp.getCurrent();
					time.setText(distanceBetweenFixes+" meter(s) between fixes");
					locManager.removeUpdates(locationListener);
				}
			}
        });
      }

    public void stopAccelerometer()
    {
    	if (AccelerometerManager.isListening()) {
    		AccelerometerManager.stopListening();
    	}
    	((TextView) findViewById(R.id.accelerometer1)).setText(String.valueOf("Accelerometer sensor is"));
        ((TextView) findViewById(R.id.accelerometer2)).setText(String.valueOf("OFF"));
    }
    public static Context getContext() {
        return CONTEXT;
    }
    
    public void init(){
         CONTEXT = this;
    	 count = 0;
    	 gpsCount = 0;
         m_state = state.periodic;
         timeBetweenFixes = 1;
         timeElapsed = SystemClock.elapsedRealtime();
         oldLocation = null;
         distanceBetweenFixes = 50F;
         maxSpeed = 10F;
         distTilCheckPointReached = false;
         setTTot(0);
         setStrenght(5);
//         stopAccelerometer();
    }

    private void updateWithNewLocation(Location location) {
//    	if(location != null && m_state == state.maxSpeed && !distTilCheckPointReached)
//    	{
//			locManager.removeUpdates(locationListener); // we do not want more than one GPS fix
//    	}
    	gpsCount++;
    	gpsCountValText.setText(Integer.toString(gpsCount));
    	
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
		if(m_state == state.maxSpeed)
			readMaxSpeed(location);
		if(m_state == state.accelerometer)
			readDistance(location);
    }
    
	private void readAccelerometer() {
		if(isMoving){
			locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0, locationListener);
		} else {
			locManager.removeUpdates(locationListener);
		}
	}

	private void readMaxSpeed(Location newLocation) {
		float distance = 0F;
		if(oldLocation != null && newLocation != null)
		{
			TextView distText = (TextView)findViewById(R.id.TextView10);
			distance = newLocation.distanceTo(oldLocation);
			distText.setText(distance+" meter(s)");
		}
		if((distance >= distanceBetweenFixes || oldLocation == null) && newLocation != null) // OK, we are done for this fix, proceed
		{
			count++; // counter of gps fix actually sent
			countValText.setText(Integer.toString(count));
			timeElapsed = SystemClock.elapsedRealtime();
			sendData("MaxSpeed,"+count+","+gpsCount+","+newLocation.getLongitude()+","+newLocation.getLatitude());
			oldLocation = newLocation;
			// Resume procedure : no fix until having reached the estimated distance
			locManager.removeUpdates(locationListener);
			locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,timeBetweenFixes,0, locationListener);
			distTilCheckPointReached = false;
		} else if(SystemClock.elapsedRealtime() - timeElapsed >= timeBetweenFixes && !distTilCheckPointReached){ // We have just reached the point where we have to start looking for fixes until reaching the good distance
			distTilCheckPointReached = true;
			locManager.removeUpdates(locationListener);
			locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0, locationListener);
		}
	}

	private void readDistance(Location newLocation) {
		float distance = 0F;
		if(oldLocation != null && newLocation != null)
		{
			TextView distText = (TextView)findViewById(R.id.TextView10);
			distance = newLocation.distanceTo(oldLocation);
			distText.setText(distance+" meter(s)");
		}
		if((distance >= distanceBetweenFixes || oldLocation == null) && newLocation != null) //Send also first fix since the program has started
		{
			count++; // counter of gps fix actually sent
			countValText.setText(Integer.toString(count));
			sendData("Distance,"+count+","+gpsCount+","+newLocation.getLongitude()+","+newLocation.getLatitude());
			oldLocation = newLocation;
		}
	}

	private void readPeriodic(String lng, String lat) {
		if(SystemClock.elapsedRealtime() - timeElapsed >= timeBetweenFixes)
		{
			count++; // counter of gps fix actually sent
			countValText.setText(Integer.toString(count));
			timeElapsed = SystemClock.elapsedRealtime();
			sendData("Periodic,"+count+","+gpsCount+","+lng+","+lat);
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


	@Override
	public void onAccelerationChanged(float x, float y, float z) {
        
        double norm = Math.sqrt(x*x+y*y+z*z);
        
        
        if ( valueSampled < nbSamples )
        {
                valueG = (float) ((valueG*valueSampled+norm)/(valueSampled+1));
                valueSampled++;
        }
        else
        {
                //((TextView) findViewById(R.id.n)).setText(String.valueOf(valueG));
//                ((TextView) findViewById(R.id.n)).setText(String.valueOf(norm));
                ((TextView) findViewById(R.id.accelerometer1)).setText(String.valueOf(isMoving));
                readAccelerometer();
                if ( Math.abs(norm-valueG) > 0.2 )
                {
                        //count++;
                        
                        if ( ! isMoving )
                        {
                                if ( strenghtIn == 0 )
                                {
                                        isMoving = true;
                                        strenghtIn = getStrenght();
                                        tTmp = System.currentTimeMillis();
                                }
                                else
                                {
                                        strenghtIn--;
                                }
                        }
                        
                        strenghtOut = getStrenght();
                }
                else
                {
                        if ( isMoving )
                        {
                                tStreak = System.currentTimeMillis() - tTmp;

                                ((TextView) findViewById(R.id.accelerometer2)).setText(String.valueOf(tTot+tStreak + ", " + tStreak));
                                
                                if ( strenghtOut == 0 )
                                {
                                        isMoving = false;
                                        tTot = tTot + tStreak;
                                }
                                else
                                {
                                        strenghtOut--;
                                }
                        }
                }
        }
	}

	protected void onResume() {
        super.onResume();
        if (AccelerometerManager.isSupported() && rb4.isChecked()) {
                AccelerometerManager.startListening(this);
                ((TextView) findViewById(R.id.accelerometer1)).setText(String.valueOf("Accelerometer sensor is"));
                ((TextView) findViewById(R.id.accelerometer2)).setText(String.valueOf("ON"));
        }
    }
    
    protected void onDestroy() {
        super.onDestroy();
        if (AccelerometerManager.isListening()) {
                AccelerometerManager.stopListening();
        }

    }
	
	public long getTTot() {
		return this.tTot;
    }
    
    public void setTTot(long t){
        this.tTot = t;
    }
    
    public int getStrenght() {
    	return this.strength;
    }
    
    public void setStrenght(int i){
        this.strength = i;
    }
    
    public boolean getIsMoving() {
    	return this.isMoving;
    }
        
	@Override
	public void onShake(float force) {
        Toast.makeText(this, "Phone shaked : " + force, 1000).show();		
	}



}