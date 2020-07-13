/* RC Car, Team G.2
 * Android App
 * Written by Kyle Rathman
 */

package com.example.krathman257.rc_car_gui_prototype;

//Import necessary packages
import android.support.v7.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.ActivityInfo;
import android.view.MotionEvent;
import android.widget.ImageView;
import java.lang.reflect.Method;
import android.widget.TextView;
import android.widget.SeekBar;
import android.content.Intent;
import java.io.OutputStream;
import java.io.IOException;
import android.view.View;
import android.os.Bundle;
import java.util.Set;

public class CarControl extends AppCompatActivity {

    //Declare variables
    BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
    String connectionStatus = "Not Connected";
    BluetoothDevice btd;
    BluetoothSocket bts;
    OutputStream btos;
    SeekBar speedBar;
    ImageView circle;
    float angle = 0;
    TextView text1;
    TextView text2;
    TextView text3;
    TextView text4;
    int speed = 0;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    //Runs when the app starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_control);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //Set graphic variables
        speedBar = findViewById(R.id.customseekBar);
        circle = findViewById(R.id.imageview_test);
        text1 = findViewById(R.id.textView2);
        text2 = findViewById(R.id.textView4);
        text3 = findViewById(R.id.textView6);
        text4 = findViewById(R.id.textView7);

        //Attempt Bluetooth connection with the paired device
        openConnection();

        //Listen to the seekbar
        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            //When the seekbar is moved, update the speed
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                speed = (i - 80) / 20 + 100;

                //Try to send the speed to the paired device as a byte
                if(btos != null){
                    try {
                        btos.write((byte) speed);
                        text4.setText((byte) speed + " written");
                    }
                    catch (IOException e) {
                        text4.setText(e.toString());
                    }
                }

                //If no output stream exists, try to create one
                else{
                    openConnection();
                }
            }

            //When the user stops touching the seekbar, reset to the 'stop' position
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(80);
                speed = 100;

                //Try to send the speed to the paired device as a byte
                if(btos != null){
                    try {
                        btos.write((byte) speed);
                        text4.setText((byte) speed + " written");
                    }
                    catch (IOException e) {
                        text4.setText(e.toString());
                    }
                }

                //If no output stream exists, try to create one
                else{
                    openConnection();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
        });

        //Listen to the wheel
        circle.setOnTouchListener(new View.OnTouchListener() {
            double pa, ca;
            int modangle;

            public boolean onTouch(View v, MotionEvent me) {

                //When the user moves their touch
                if (me.getActionMasked() == MotionEvent.ACTION_MOVE){

                    //Rotate the wheel based on how the user drags their touch
                    ca = getAngle(me.getRawX(), me.getRawY());
                    angle = rotateCircle((float) (pa - ca));

                    //Try to send the mathematically modified angle to the paired device as a byte
                    if(btos != null) {
                        try {
                                modangle = (int)(angle * (9f / 14f) + 45f);
                                btos.write((byte) modangle);
                                text4.setText((byte) modangle + " written");
                        } catch (IOException e) {
                            text4.setText(e.toString());
                        }
                    }

                    //If no output stream exists, try to create one
                    else{
                        openConnection();
                    }
                    pa = ca;
                }

                //When the user first touches the wheel
                else if (me.getActionMasked() == MotionEvent.ACTION_DOWN){
                    pa = getAngle(me.getRawX(), me.getRawY());
                }

                //When the user stops touching the wheel
                else if (me.getActionMasked() == MotionEvent.ACTION_UP){

                    //Reset the wheel to point straight forward
                    angle = rotateCircle(-1 * angle);

                    //Try to send the mathematically modified angle to the paired device as a byte
                    if(btos != null) {
                        try {
                                modangle = (int)(angle * (9f / 14f) + 45f);
                                btos.write((byte) modangle);
                                text4.setText((byte) modangle + " written");
                        } catch (IOException e) {
                            text4.setText(e.toString());
                        }
                    }

                    //If no output stream exists, try to create one
                    else{
                        openConnection();
                    }
                }
                return true;
            }
        });
    }

    //Attempt a Bluetooth connection
    public boolean connect() throws IOException {
        bta.cancelDiscovery();
        if (bts != null) {
            bts.close();
        }

        //Try to open a Bluetooth socket
        try {
            Method m = btd.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            bts = (BluetoothSocket) m.invoke(btd, 1);
        } catch (Exception e) {
            text4.setText(e.toString());
        }
        if (bts == null) {
            return false;
        }
        bts.connect();

        //If all is successful, set the Bluetooth output stream
        btos = bts.getOutputStream();
        return true;
    }

    //Try to open a connection
    public void openConnection(){
        if (bta != null) {
            text1.setText(bta.getName());

            //If Bluetooth is disabled, ask to enable it
            if (!bta.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }

            //Get the paired device
            Set<BluetoothDevice> pairedDevices = bta.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    btd = device;
                    text2.setText(btd.getName());
                }

                //If a device is found, try to connect
                if (btd != null) {
                    try {
                        connect();
                    } catch (IOException e) {
                        text4.setText(e.toString());
                    }
                    if (bts.isConnected()) {
                        connectionStatus = "Connected";
                    }
                    text3.setText(connectionStatus);
                }
            }
        }
    }

    //Return the angle from the center of the wheel to calculate the amount to rotate
    public float getAngle(double tx, double ty){
        double x = tx - (circle.getWidth() / 2);
        double y = circle.getHeight() - ty - (circle.getHeight() / 5);
        float base = (float) (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);

        switch (getQuadrant(x, y)){
            case 1:
                return base;
            case 2:
                return 180 - base;
            case 3:
                return 180 + (-1 * base);
            case 4:
                return 360 + base;
            default:
                return 0;
        }
    }

    //Return the quadrant of the circle given the input coordinates
    public int getQuadrant(double x, double y){
        if (x >= 0){
            return y >= 0 ? 1 : 4;
        }
        else{
            return y >= 0 ? 2 : 3;
        }
    }

    //Rotate the wheel a certain angle
    public float rotateCircle(float a){
        if((circle.getRotation() + a) > -70 && (circle.getRotation() + a) < 70) {
            circle.setRotation(angle + a);
            return angle + a;
        }
        else {
            return angle;
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}