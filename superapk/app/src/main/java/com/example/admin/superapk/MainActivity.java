package com.example.admin.superapk;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.async.DeviceSensorAsyncMessage;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.async.DeviceSensorAsyncMessage;
import com.orbotix.common.internal.AsyncMessage;

import java.lang.Math;
import java.lang.String;

import static java.lang.Math.sqrt;

public class MainActivity extends Activity implements View.OnClickListener, RobotChangedStateListener {

    private static final float ROBOT_VELOCITY = 0.6f;

    private ConvenienceRobot mRobot;

    private Button mBtn0;
    private Button mBtn90;
    private Button mBtn180;
    private Button mBtn270;
    private Button mBtnStop;

    public double pos_x,pos_y;
    public double start_x,start_y;
    public double end_x,end_y;
    public double distance,first_dist;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        /*
            Associate a listener for robot state changes with the DualStackDiscoveryAgent.
            DualStackDiscoveryAgent checks for both Bluetooth Classic and Bluetooth LE.
            DiscoveryAgentClassic checks only for Bluetooth Classic robots.
            DiscoveryAgentLE checks only for Bluetooth LE robots.
       */
        DualStackDiscoveryAgent.getInstance().addRobotStateListener( this );

        initViews();
    }

    private void initViews() {
        mBtn0 = (Button) findViewById( R.id.btn_0 );
        mBtn90 = (Button) findViewById( R.id.btn_90 );
        mBtn180 = (Button) findViewById( R.id.btn_180 );
        mBtn270 = (Button) findViewById( R.id.btn_270 );
        mBtnStop = (Button) findViewById( R.id.btn_stop );

        mBtn0.setOnClickListener( this );
        mBtn90.setOnClickListener( this );
        mBtn180.setOnClickListener( this );
        mBtn270.setOnClickListener( this );
        mBtnStop.setOnClickListener( this );
    }

    @Override
    protected void onStart() {
        super.onStart();

        //If the DiscoveryAgent is not already looking for robots, start discovery.
        if( !DualStackDiscoveryAgent.getInstance().isDiscovering() ) {
            try {
                DualStackDiscoveryAgent.getInstance().startDiscovery( this );
            } catch (DiscoveryException e) {
                Log.e("Sphero", "DiscoveryException: " + e.getMessage());
            }
        }

        start_x=0.0;
        start_y=0.0;
        pos_x=0.0;
        pos_y=0.0;
        end_x=100.0;
        end_y=100.0;

        first_dist=sqrt((start_x-end_x)*(start_x-end_x)+(start_y-end_y)*(start_y-end_y));
        mRobot.enableLocator(true);
    }

    @Override
    protected void onStop() {
        //If the DiscoveryAgent is in discovery mode, stop it.
        if( DualStackDiscoveryAgent.getInstance().isDiscovering() ) {
            DualStackDiscoveryAgent.getInstance().stopDiscovery();
        }

        //If a robot is connected to the device, disconnect it
        if( mRobot != null ) {
            mRobot.disconnect();
            mRobot = null;
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DualStackDiscoveryAgent.getInstance().addRobotStateListener( null );
    }

    @Override
    public void onClick(View v) {
        //If the robot is null, then it is probably not connected and nothing needs to be done
        if( mRobot == null ) {
            return;
        }

        /*
            When a heading button is pressed, set the robot to drive in that heading.
            All directions are based on the back LED being considered the back of the robot.
            0 moves in the opposite direction of the back LED.
         */
        switch( v.getId() ) {
            case R.id.btn_0: {
                //Forward
                mRobot.drive( 0.0f, ROBOT_VELOCITY );
                break;
            }
            case R.id.btn_90: {
                //To the right
                mRobot.drive( 90.0f, ROBOT_VELOCITY );

                break;
            }
            case R.id.btn_180: {
                //Backward
                mRobot.drive( 180.0f, ROBOT_VELOCITY );
                break;
            }
            case R.id.btn_270: {
                //To the left
                mRobot.drive( 270.0f, ROBOT_VELOCITY );
                break;
            }
            case R.id.btn_stop: {
                //Stop the robot
                mRobot.stop();
                break;
            }
        }
        
        pos_x=((DeviceSensorAsyncMessage) asyncMessage).getAsyncData().get(0).getLocatorData().getPositionX();
        pos_y=((DeviceSensorAsyncMessage) asyncMessage).getAsyncData().get(0).getLocatorData().getPositionY();
        distance=sqrt((pos_x-end_x)*(pos_x-end_x)+(pos_y-end_y)*(pos_y-end_y));
        double r,g,b,s;
        s=distance/first_dist;

        r=1.0+s*(-1.0);
        g=0.0+s*(1.0);
        b=0.0;
        //x + s(y-x).

        Log.e("Sphero","dist="+Double.toString(distance));
        mRobot.setLed((float)r,(float)g,(float)b);
    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType type) {
        switch (type) {
            case Online: {
                //Save the robot as a ConvenienceRobot for additional utility methods
                mRobot = new ConvenienceRobot(robot);
                break;
            }
        }
    }
}
