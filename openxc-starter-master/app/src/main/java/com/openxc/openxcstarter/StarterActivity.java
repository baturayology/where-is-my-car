package com.openxc.openxcstarter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.openxc.measurements.IgnitionStatus;
import com.openxc.measurements.Odometer;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxcplatform.openxcstarter.R;
import com.openxc.VehicleManager;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.EngineSpeed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StarterActivity extends Activity {
    private static final String TAG = "StarterActivity";

    private VehicleManager mVehicleManager;
    private Button startButton;
    private TextView steeringAngle;
    private TextView odometer;
    private ImageView drawArea;
    private ImageView parklogo;

    private int startOdo;
    private int previousOdo;
    private int currentOdo;
    private int lastOdo;
    private Double sAngle;
    private Double threshold = 360.0; //was 45.0     // tried 25, 75

    private String currentCoord;


    List xCoord = new ArrayList<Integer>();
    List yCoord = new ArrayList<Integer>();
    List odoValues = new ArrayList<Integer>();

    private int startinX = 50;
    private int startinY = 100;

    private int direction; //1 is left, 0 is right
    private int turnCount = 0;
    private int odoCount = -1;

    private String way = "up";
    private String ignitionStatus = "ON";

    private boolean startButtonClicked = false;
    private boolean turning = false;

    private boolean turnStart = false;
    private boolean turnCont = false;
    private boolean turnEnd = false;

    private boolean gameover = false;

    //List edges = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);
        // grab a reference to the engine speed text object in the UI, so we can
        // manipulate its value later from Java code
        startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(buttonListener);
        steeringAngle = (TextView) findViewById(R.id.steeringAngle);
        steeringAngle.setVisibility(View.INVISIBLE);
        odometer = (TextView) findViewById(R.id.odometer);
        //odometer.setVisibility(View.INVISIBLE);
        drawArea = (ImageView) findViewById(R.id.drawArea);
        drawArea.setVisibility(View.INVISIBLE);
        parklogo = (ImageView) findViewById(R.id.parklogo);
        xCoord.add(startinX);
        yCoord.add(startinY);

    }

    @Override
    public void onPause() {
        super.onPause();
        // When the activity goes into the background or exits, we want to make
        // sure to unbind from the service to avoid leaking memory
        if(mVehicleManager != null) {
            Log.i(TAG, "Unbinding from Vehicle Manager");
            // Remember to remove your listeners, in typical Android
            // fashion.

            unbindService(mConnection);
            mVehicleManager = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // When the activity starts up or returns from the background,
        // re-connect to the VehicleManager so we can receive updates.
        if(mVehicleManager == null) {
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /* This is an OpenXC measurement listener object - the type is recognized
     * by the VehicleManager as something that can receive measurement updates.
     * Later in the file, we'll ask the VehicleManager to call the receive()
     * function here whenever a new EngineSpeed value arrives.
     */

    View.OnClickListener buttonListener = new View.OnClickListener() {
        //boolean clicked = false;
        //int numClicks = 0;

        @Override
        public void onClick(View v) {
            //setContentView(R.layout.track);
            startButton.setVisibility(View.INVISIBLE);
            steeringAngle.setVisibility(View.VISIBLE);
            odometer.setVisibility(View.VISIBLE);
            drawArea.setVisibility(View.VISIBLE);
            parklogo.setVisibility(View.VISIBLE);
            odoValues.add(currentOdo);
            odoCount++;
            startButtonClicked = true;
            ignitionStatus = new String("ON");
            //startOdo = currentOdo;
            //drawLine();
        }
    };



    public void checkThreshold (Double angle) {

        int distance;

        //if (ignitionStatus.equals("OFF")) {
            //if (startButtonClicked == true) {

            //}
        //}

        if (ignitionStatus.equals("OFF")) {
            if (startButtonClicked == true) {
                gameover = true;
                odoValues.add(lastOdo);
                odoCount++;
                distance = (Integer)odoValues.get(odoCount) - (Integer)odoValues.get(odoCount-1);
//                    Log.i("currentOdo:",Integer.toString(lastOdo));
                if (way.equals("up")) {
                    xCoord.add((Integer)xCoord.get(turnCount));
                    yCoord.add((Integer)yCoord.get(turnCount)-distance);
                }

                else if (way.equals("right")) {
                    xCoord.add((Integer)xCoord.get(turnCount)+distance);
                    yCoord.add((Integer)yCoord.get(turnCount));
                }

                else if (way.equals("down")) {
                    xCoord.add((Integer)xCoord.get(turnCount));
                    yCoord.add((Integer)yCoord.get(turnCount)+distance);
                }

                else if (way.equals("left")) {
                    xCoord.add((Integer)xCoord.get(turnCount)-distance);
                    yCoord.add((Integer)yCoord.get(turnCount));
                }

                drawLine();
                return;
            }
        }


        if (angle > threshold || angle < -threshold) {

            if (odoCount < 0) {
                return;
            }

            if (ignitionStatus.equals("OFF")) {
                if (startButtonClicked == true) {
//                    odoValues.add(lastOdo);
//                    odoCount++;
//                    distance = (Integer)odoValues.get(odoCount) - (Integer)odoValues.get(odoCount-1);
////                    Log.i("currentOdo:",Integer.toString(lastOdo));
//                    if (way.equals("up")) {
//                        xCoord.add((Integer)xCoord.get(turnCount));
//                        yCoord.add((Integer)yCoord.get(turnCount)-distance);
//                    }
//
//                    else if (way.equals("right")) {
//                        xCoord.add((Integer)xCoord.get(turnCount)+distance);
//                        yCoord.add((Integer)yCoord.get(turnCount));
//                    }
//
//                    else if (way.equals("down")) {
//                        xCoord.add((Integer)xCoord.get(turnCount));
//                        yCoord.add((Integer)yCoord.get(turnCount)+distance);
//                    }
//
//                    else if (way.equals("left")) {
//                        xCoord.add((Integer)xCoord.get(turnCount)-distance);
//                        yCoord.add((Integer)yCoord.get(turnCount));
//                    }
//
//                    drawLine();
                    return;
                }
            }

            //Log.i("angle",Double.toString(sAngle));
            //Log.i("odo",Integer.toString(currentOdo));
            //Log.i("ig",ignitionStatus);
            //Log.i("way",way);


            if (turnStart) {
                turnCont = true;
            }

            if (turnStart == false) {
                turnStart = true;
            }



//            calcDistance(angle);


            //Log.i("way:",way);
            //Log.i("turnCount":,turnCount);


        } else {

            if (turnCont) {
                calcDistance(angle);
//                turnEnd = true;
            }

            turnStart = false;
            turnCont = false;
        }

    }

    public void calcDistance(Double angle) {

        int distance;


        if (ignitionStatus.equals("OFF")) {
            if (startButtonClicked == true) {
                return;
            }
        }

        odoValues.add(currentOdo);
        odoCount++;
        distance = (Integer)odoValues.get(odoCount) - (Integer)odoValues.get(odoCount-1);


        if (way.equals("up")) {
            xCoord.add((Integer)xCoord.get(turnCount));
            yCoord.add((Integer)yCoord.get(turnCount)-distance);
        }

        else if (way.equals("right")) {
            xCoord.add((Integer)xCoord.get(turnCount)+distance);
            yCoord.add((Integer)yCoord.get(turnCount));
        }

        else if (way.equals("down")) {
            xCoord.add((Integer)xCoord.get(turnCount));
            yCoord.add((Integer)yCoord.get(turnCount)+distance);
        }

        else if (way.equals("left")) {
            xCoord.add((Integer)xCoord.get(turnCount)-distance);
            yCoord.add((Integer)yCoord.get(turnCount));
        }

        if (angle > 0) { //turned to right
            if (way.equals("up"))
                way = new String("right");
            else if (way.equals("right"))
                way = new String ("down");
            else if (way.equals("down"))
                way = new String ("left");
            else if (way.equals("left"))
                way = new String ("up");
        }

        else { // turned to left
            if (way.equals("up"))
                way = new String("left");
            else if (way.equals("right"))
                way = new String ("up");
            else if (way.equals("down"))
                way = new String ("right");
            else if (way.equals("left"))
                way = new String ("down");
        }

        turnCount++;
        drawLine();

    }

    public void drawLine() {
//         drawArea = (ImageView) thisView.findViewById(R.id.angleDrawArea);
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GREEN);
//       Bitmap icon = BitmapFactory.decodeResource(this.getResources().getDrawable(R.drawable.p),
//                R.drawable.);
//        canvas.draw
        int i = 0;
//        Log.i("array size:",Integer.toString(xCoord.size()));
        while (i < xCoord.size()) {
//            Log.i("i:",Integer.toString(i));
            if (i+1 < xCoord.size())
                canvas.drawLine((Integer) xCoord.get(i), (Integer) yCoord.get(i), (Integer) xCoord.get(i + 1), (Integer) yCoord.get(i + 1), paint);
            i++;
        }

//        canvas.drawCircle((float) xCoord.get(i-2), (float) yCoord.get(i-2),5,paint);

//        i = 0;
//        while (i< xCoord.size()) {
//            Log.i("xcoord " + i, xCoord.get(i).toString());
//            Log.i("ycoord " + i, yCoord.get(i).toString());
//            i++;
//        }
        //canvas.drawLine(50,100,50,70,paint);
        //canvas.drawLine(50,70,55,70,paint);
        //canvas.drawLine(55,70,55,60,paint);
        //canvas.drawLine(55,60,5,60,paint);
        //canvas.drawLine(5,60,5,80,paint);

        //canvas.drawLine(50, 50, (float) ((float) 50 + (1.1111 * angle)), 0, paint);

//        } else if (angle > 90.0 && angle <= 135.0) {
//            canvas.drawLine(50, 50, 100, ((float) (1.1111 * (angle - 45.0))), paint);
//        }
        drawArea.setImageBitmap(bitmap);
    }

    SteeringWheelAngle.Listener mSteeringListener = new SteeringWheelAngle.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final SteeringWheelAngle angle = (SteeringWheelAngle) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    String s = angle.getValue().toString();
                    String[] sp = s.split(" ");
                    sAngle = Double.parseDouble(sp[0]);
                    //Log.i("angle",sAngle.toString());
                    if (!gameover) {
                        steeringAngle.setText("SteeringAngle: " + sp[0]);
                        checkThreshold(sAngle);
                    }
                }
            });
        }
    };


    Odometer.Listener mOdometerListener = new Odometer.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final Odometer meter = (Odometer) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    String s = meter.getValue().toString();
                    String[] sp = s.split("\\.");
                    String[] sp2 = sp[1].split(" ");
                    //Log.i("odometer:",sp2[0]);
                    String m = new String(sp2[0]+"000");
                    m = m.substring(0,3);
                    //previousOdo = currentOdo;
                    currentOdo = Integer.parseInt(m);
                    if (!gameover) {
                        odometer.setText("Meter: " + m);
                    }
                }
            });
        }

    };

    IgnitionStatus.Listener mIgnitionStatusListener = new IgnitionStatus.Listener() {

        @Override
        public void receive(Measurement measurement) {
            final IgnitionStatus igStatus = (IgnitionStatus) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    ignitionStatus = igStatus.getValue().toString();
                    if (ignitionStatus.equals("OFF"))
                        lastOdo=currentOdo;
                    //Log.i("ignition status:",s);
                    //String[] sp = s.split("\\.");
                    //String[] sp2 = sp[1].split(" ");
                    //Log.i("odometer:",sp2[0]);
                    //String m = sp2[0].substring(0,3);
                    //odometer.setText("Meter: " + m);
                    //previousOdo = currentOdo;
                    //currentOdo = Integer.parseInt(m);


                }
            });
        }

    };


    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the VehicleManager service is
        // established, i.e. bound.
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            // When the VehicleManager starts up, we store a reference to it
            // here in "mVehicleManager" so we can call functions on it
            // elsewhere in our code.
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();

            mVehicleManager.addListener(SteeringWheelAngle.class,mSteeringListener);
            mVehicleManager.addListener(Odometer.class,mOdometerListener);
            mVehicleManager.addListener(IgnitionStatus.class,mIgnitionStatusListener);
            // We want to receive updates whenever the EngineSpeed changes. We
            // have an EngineSpeed.Listener (see above, mSpeedListener) and here
            // we request that the VehicleManager call its receive() method
            // whenever the EngineSpeed changes

        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service  disconnected unexpectedly");
            mVehicleManager = null;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.starter, menu);
        return true;
    }
}
