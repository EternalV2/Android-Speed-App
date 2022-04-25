package edu.gcu.shadluiter.Second2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity {

    private final int graphIntervalCounter = 0;
    // Initializes Text View
    TextView txt_CurrentAccel, txt_prevAccell, txt_acceleration;
    // Initializes Progress Bar
    ProgressBar prog_shakemeter;
    // Initializes Button
    Button navigation;
    LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {});
    // Declares Sensor Objects
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    // Declares Acceleration
    private double accelerationCurrentValue;
    private double previousValue;
    // Declares Graph Variables
    private int pointsPlotted = 5;
    // Declares Graph Field of Vision
    private Viewport veiwport;
    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            // Connects Sensor Values With X Y Z dimensions
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            // Counteracts Gravity
            y = (float) (y - 9.81);
            if(y<0) y = 0;

            // Calculates Acceleration and Previous Acceleration
            accelerationCurrentValue = Math.sqrt((x*x+y*y+z*z));
            double changeInAccellaration = Math.abs(accelerationCurrentValue - previousValue);
            previousValue = accelerationCurrentValue;

            // Updates Front End Acceleration
            txt_acceleration.setText("Acceleration Change = " + (int)changeInAccellaration);
            txt_CurrentAccel.setText("Current = " + (int)accelerationCurrentValue);
            txt_prevAccell.setText("Previous = " + (int)previousValue);

            // Updates Progress Bar
            prog_shakemeter.setProgress((int)changeInAccellaration);

            // Changes Color Based on Acceleration Intensity
            if (changeInAccellaration > 14){
                txt_acceleration.setBackgroundColor(Color.RED);
            }else if (changeInAccellaration > 5) {
                txt_acceleration.setBackgroundColor(Color.parseColor("#fcad03"));
            }else if (changeInAccellaration > 2) {
                txt_acceleration.setBackgroundColor(Color.YELLOW);
            }else {
                txt_acceleration.setBackgroundColor(getResources().getColor(com.google.android.material.R.color.design_default_color_background));
            }

            // Resets X coordinate when x > 1000
            int counter = 0;
            pointsPlotted++;
            if (pointsPlotted > 1000) {
                pointsPlotted = 1;
                series.resetData(new DataPoint[]{new DataPoint(0, 0)});
            }

            // Updates the Graph and Constrains Dimensions
            series.appendData(new DataPoint(pointsPlotted, accelerationCurrentValue), true, pointsPlotted);
            veiwport.setMaxX(pointsPlotted);
            veiwport.setMinX(pointsPlotted - 50);
            veiwport.setMaxY(20);
            veiwport.setMinY(0);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Defualt
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 3 Top Text Veiws
        txt_acceleration = findViewById(R.id.txt_accel);
        txt_CurrentAccel = findViewById(R.id.txt_currentAccel);
        txt_prevAccell = findViewById(R.id.txt_prevAccel);

        // Progress Bar (Measures Shake)
        prog_shakemeter = findViewById(R.id.prog_shakeMeter);

        // Button
        navigation = findViewById(R.id.navigation);

        // Initialize Sensor Object
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Connects Graph With Front End and Initializes It
        GraphView graph = (GraphView) findViewById(R.id.graph);
        veiwport = graph.getViewport();
        veiwport.setScrollable(true);
        veiwport.setXAxisBoundsManual(true);
        graph.addSeries(series);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(sensorEventListener);
    }

    public void navigation(View view) {
        Intent intent = new Intent(MainActivity.this, Navigation.class);
        startActivity(intent);
    }

}