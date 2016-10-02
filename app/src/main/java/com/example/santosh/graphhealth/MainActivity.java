package com.example.santosh.graphhealth;


import android.hardware.SensorEventListener;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//Motion Sensor
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.content.Context;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

// SQLITE Database handler and exception
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;


import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private Runnable timerRunnable;
    private Handler timerHandler = new Handler();
    //private boolean[] timerFlag = {false};
    private double lastValue = 21d;
    private String PatientName = "Patient";

    /**
     * Sensor Members
     */
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;

    //name, age, id, sex
    public static void setPatientText(String Name, String Age, String ID, String SEX,View view){

        TextView name = (TextView) view.findViewById(R.id.nameTextView);
        TextView age = (TextView) view.findViewById(R.id.ageTextView);
        TextView identity = (TextView) view.findViewById(R.id.IdTextView);
        TextView sex = (TextView) view.findViewById(R.id.sexTextView);

        name.setText(Name);
        age.setText(String.valueOf(Age));
        identity.setText(String.valueOf(ID));
        sex.setText(String.valueOf(SEX));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize sensors
        this.sensorInit();

        final GraphView graph = (GraphView) findViewById(R.id.graph);
//        Toast.makeText(this, "called in onCreate " , Toast.LENGTH_LONG).show();
//

        final LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(generateData());
        graph.addSeries(series);
        graph.setVisibility(View.GONE);


        Button start = (Button) findViewById(R.id.startButton);
        Button stop = (Button) findViewById(R.id.stopButton);
        Button add = (Button) findViewById(R.id.addButton);

        final TextView name = (TextView) findViewById(R.id.nameTextView);
        final TextView age = (TextView) findViewById(R.id.ageTextView);
        final TextView identity = (TextView) findViewById(R.id.IdTextView);
        final TextView sex = (TextView) findViewById(R.id.sexTextView);

        name.setText("Patient");
        age.setText(String.valueOf(22));
        identity.setText(String.valueOf(110));
        sex.setText(String.valueOf("M"));


        start.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                databaseinit(name.getText().toString(), identity.getText().toString(), age.getText().toString(), sex.getText().toString());



                graph.setVisibility(View.VISIBLE);
                graph.setTitle("Health Graph for "+name.getText().toString());
                    timerHandler.removeCallbacks(timerRunnable);
                    timerRunnable = new Runnable() {
                        @Override
                        public void run() {
                            lastValue += 1d;
                            series.appendData(new DataPoint(lastValue, getRandom()),true,40);
                            timerHandler.postDelayed(this, 300);

                        }
                    };
                    timerHandler.postDelayed(timerRunnable, 300);
            }


        });
        stop.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                timerHandler.removeCallbacks(timerRunnable);
                graph.setVisibility(View.GONE);
            }
        });
        add.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                timerHandler.removeCallbacks(timerRunnable);
                graph.setVisibility(View.GONE);
                AddNewPatient addpatient = new AddNewPatient();
                addpatient.show(getFragmentManager(),"Add patient alert");
            }
        });
    }



private void databaseinit(String name , String identity , String age, String sex) {
    try{
         SQLiteDatabase dbhandler = openOrCreateDatabase( "patient.db",MODE_PRIVATE, null );
        dbhandler.beginTransaction();
        try{

            dbhandler.execSQL("CREATE TABLE IF NOT EXISTS"
                    + name+"_"+ identity+"_"+ age+"_"+ sex+ " "
                    + "("
                    + " time_stamp double PRIMARY KEY , "
                    + " x_value float, "  // later change to int
                    + " y_value float, "
                    + " z_value float ); " );

            dbhandler.setTransactionSuccessful();
        }
        catch (SQLiteException e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        finally {
            dbhandler.endTransaction();
        }
    }catch (SQLException e){

        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
    }
    }

    private void databaseinsert(double timeStamp, float x, float y,float z) {
        try{
            SQLiteDatabase dbhandler = openOrCreateDatabase( "patient.db",MODE_PRIVATE, null );
            dbhandler.beginTransaction();

            try{

                dbhandler.execSQL( "insert into tblPat(name, age) values ('"+patientIDText+"', '"+ageText+"' );" );
                //db.setTransactionSuccessful(); //commit your changes.setTransactionSuccessful();
            }
            catch (SQLiteException e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            finally {
                dbhandler.endTransaction();
            }
        }catch (SQLException e){

            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public DataPoint[] generateData() {
        int count = 20;
        DataPoint[] values = new DataPoint[count];
        for (int i=0; i<count; i++) {
            double x = i;
            double f = mRand.nextDouble()*0.15+0.3;
            double y = Math.sin(i*f+2) + mRand.nextDouble()*0.3;
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }

    double mLastRandom = 2;
    Random mRand = new Random();
    public double getRandom() {
        return mLastRandom += mRand.nextDouble()*0.5 - 0.25;
    }

    /**
     * Sensor code block
     */

    protected void sensorInit(){
        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //rate is used in this function
        this.sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * This is the event handler for sensor changes
     * @param event : event fed from the android sensor
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor healthMonitorSensor = event.sensor;
        float x, y, z;
        long timeStamp, delta;
        //implementing only for accelerometer
        if(healthMonitorSensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];

            timeStamp = System.currentTimeMillis();
            delta = timeStamp - lastUpdate;
            if(delta > 100){
                lastUpdate = timeStamp;
                last_x = x;
                last_y = y;
                last_z = z;

                Toast.makeText(this, "Bitch @: " + x + " : " + y + " : " + z , Toast.LENGTH_LONG).show();
            }
        databaseinsert(timeStamp, x,y,z);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * this code block is for unbinding the listeners during application switches
     */
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
