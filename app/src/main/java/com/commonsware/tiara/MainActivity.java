package com.commonsware.tiara;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.jtransforms.fft.DoubleFFT_1D;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // GUI Components
    private TextView mBluetoothStatus;
    private TextView mReadBuffer;
    private Button mScanBtn;
    private Button mOffBtn;
    private Button mListPairedDevicesBtn;
    private Button mDiscoverBtn;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;
    private TextView gyros;
    private CheckBox mLED1;
    private TextView view;
    private TextView view1;
    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path
    private String TAG = "MyApplication";
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    final int cou=0;
    DoubleFFT_1D fft = new DoubleFFT_1D(1024); // 1024 is size of array
    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status
    private static int datatoread = 0;
    private static double x=0;
    private static double x1=0;
    private static double x2=0;
    private static double x3=0;
    private static double y=0;
    private int count =0;
    private static char h;
    private static char i;
    private static double z=0;
    private double gyro[]=new double[2];
    private int counter =0;
    private int fs=0;
    private int N=0;
    private double ne[];
    private double freq=0;
    private double co=0;
    private int k=0;
    private int j=0;
    private double Xre=0;
    private double Xim=0;
    GraphView graph2;
    GraphView graph3;
    GraphView graph1;
    GraphView graph;
    GraphView graph4;
    GraphView graph5;
    GraphView graph6;
    LineGraphSeries<DataPoint> series;
    LineGraphSeries<DataPoint> series1;
    LineGraphSeries<DataPoint> series2;
    LineGraphSeries<DataPoint> series3;
    LineGraphSeries<DataPoint> series4;
    LineGraphSeries<DataPoint> series5;
    LineGraphSeries<DataPoint> series6;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("TIARA");

        mBluetoothStatus = (TextView)findViewById(R.id.bluetoothStatus);
        mReadBuffer = (TextView) findViewById(R.id.readBuffer);
        mScanBtn = (Button)findViewById(R.id.scan);
        mOffBtn = (Button)findViewById(R.id.off);
        mDiscoverBtn = (Button)findViewById(R.id.discover);
        mListPairedDevicesBtn = (Button)findViewById(R.id.PairedBtn);
        mLED1 = (CheckBox)findViewById(R.id.checkboxLED1);
        view = (TextView) findViewById(R.id.check);
        view1 = (TextView) findViewById(R.id.check1);
        gyros = (TextView) findViewById(R.id.gyros);

        mBTArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

        mDevicesListView = (ListView)findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);


        //graph = (GraphView) findViewById(R.id.graph);
       // graph.getViewport().setScrollable(false);
       // graph1 = (GraphView) findViewById(R.id.graph1);
        //graph1.getViewport().setScrollable((true));
        graph2 = (GraphView) findViewById(R.id.graph2);
        graph2.getViewport().setScrollable((true));
        //graph3 = (GraphView) findViewById(R.id.graph3);
       // graph3.getViewport().setScrollable((true));
        //graph4 = (GraphView) findViewById(R.id.graph4);
        //graph4.getViewport().setScrollable((true));
        //graph5 = (GraphView) findViewById(R.id.graph5);
       // graph5.getViewport().setScrollable((true));
       // graph6 = (GraphView) findViewById((R.id.graph6));
       // graph6.getViewport().setScrollable((true));
       // series = new LineGraphSeries<>();
      //  series1 = new LineGraphSeries<>();
        series2 = new LineGraphSeries<>();
       // series3 = new LineGraphSeries<>();
        //series4 = new LineGraphSeries<>();
       // series5 = new LineGraphSeries<>();
        //series6 = new LineGraphSeries<>();
        //series.setTitle("EEG");
        //series1.setTitle("Blood oxygen");
        series2.setTitle("Temperature");
        //series3.setTitle("Pulse");
        //series4.setTitle("Time Domain");
        //series5.setTitle("Filtered Signal");
        //series6.setTitle("Fourier Transformed Signal");
      /*  series.setColor(Color.BLUE);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(5);
        series.setThickness(8);*/
        graph2.addSeries(series2);
       // graph3.addSeries(series3);
       // graph1.addSeries(series1);
        //graph.addSeries(series);
        //graph4.addSeries(series4);
        //graph5.addSeries(series5);
        //graph6.addSeries(series6);

       /* graph.getLegendRenderer().setVisible(true);
        graph1.getLegendRenderer().setVisible(true);
        graph2.getLegendRenderer().setVisible(true);
        graph3.getLegendRenderer().setVisible(true);
        graph4.getLegendRenderer().setVisible(true);
        graph5.getLegendRenderer().setVisible(true);
        graph6.getLegendRenderer().setVisible(true);
        graph.getGridLabelRenderer().setGridColor(Color.BLUE);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph1.getGridLabelRenderer().setGridColor(Color.BLUE);
        graph1.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph1.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph1.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph1.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);*/
        graph2.getGridLabelRenderer().setGridColor(Color.BLUE);
        graph2.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph2.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph2.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph2.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
       /* graph3.getGridLabelRenderer().setGridColor(Color.BLUE);
        graph3.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph3.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph3.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph3.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph4.getGridLabelRenderer().setGridColor(Color.BLUE);
        graph4.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph4.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph4.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph4.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph5.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph5.getGridLabelRenderer().setGridColor(Color.BLUE);
        graph5.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph5.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph5.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph6.getGridLabelRenderer().setGridColor(Color.BLUE);
        graph6.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph6.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph6.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph6.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);*/

        fs=16000;
        N=1000;
        freq=800;
        graph2.getViewport().setMinY(2000);
        graph2.getViewport().setMaxY(2400);
        graph2.getViewport().setYAxisBoundsManual(true);
       /* graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(1000);
        graph2.getViewport().setXAxisBoundsManual(true);*/
        double[] ne = new double[fs];


        /*for(k=0;k<fs;k++)
        {
            double t = k * (1 / (double)fs);
            ne[k]= Math.sin(2 * Math.PI * freq * t)+Math.sin((2*Math.PI * (freq+500)*t));
            Log.d(TAG, "SINEVALUE= " + ne[k]);

        }
        */
       /* int holder=0;

        for(holder=0;holder< (ne.length/32);holder++)
        {
            double t = holder * (1 / (double)fs);
            DataPoint datapoint = new DataPoint(t, ne[holder]);
            series4.appendData(datapoint, false, 10000);

            Log.d(TAG, "BACKTOTIME= " + ne[holder]);
        }
        fft.realForward(ne);
        int jj=0;
        //send result to tograph to be displayed late
        double localMax = Double.MIN_VALUE;
        int maxValueFreq = -1;
        double[] result = new double[ne.length / 2];
        for(int s = 0; s < result.length; s++) {
            //result[s] = Math.abs(signal[2*s]);
            jj= jj + (16000/1024);
            double re = ne[s * 2];
            double im = ne[s * 2 + 1];
            if(jj > 1000){
                result[s]=0;
                ne[s*2] = 0;
                ne[s*2 +1] = 0;
            }
            else {
                result[s] = (double) Math.sqrt(re * re + im * im) / result.length;
            }
            if(result[s] > localMax) {
                maxValueFreq = s;
            }
            localMax = Math.max(localMax, result[s]);
            Log.d(TAG, "FFT= " + result[s]);
        }
        int counterz=0;

        for( int gg=0;gg<1024/2;gg++) {
            counterz = counterz + (16000 / 1024);
            if (counterz < 1000) {
                DataPoint datapoint = new DataPoint(counterz, result[gg]);
                series5.appendData(datapoint, false, 10000);
            }
            else
            {
                DataPoint datapoint = new DataPoint(counterz, 0);
                series5.appendData(datapoint, false, 10000);
            }
        }*/
       /* fft.realInverse(ne,true);
        for(int timed=0;timed< (ne.length/32);timed++)
        {
            double t = timed * (1 / (double)fs);
            DataPoint datapoint = new DataPoint(t, ne[timed]);
            series6.appendData(datapoint, false, 10000);

            Log.d(TAG, "BACKTOTIME= " + ne[timed]);
        }//reset Xim & Xre
*/


        mHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == MESSAGE_READ) {
                    String readMessage = null;
                    String x1a="";
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }


                   /* if (readMessage.charAt(0)=='A') {
                        mReadBuffer.setText(readMessage);
                        x = x + 1;
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)!=' ')
                        {
                            x1a = readMessage.substring(2, 3);
                        }
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' '&& readMessage.charAt(3)!=' ' ) {
                            x1a = readMessage.substring(2, 5);
                        }
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ' && readMessage.charAt(3)==' '&& readMessage.charAt(4)!=' ' ) {
                            x1a = readMessage.substring(2, 7);
                        }

                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ' && readMessage.charAt(3)==' ' && readMessage.charAt(4)==' '&& readMessage.charAt(5)!=' ' ){
                            x1a = readMessage.substring(2, 9);

                        }

                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ' && readMessage.charAt(3)==' ' && readMessage.charAt(4)==' '&& readMessage.charAt(5)==' ' && readMessage.charAt(6)!=' '){
                            x1a = readMessage.substring(2, 11);

                        }


                        String x2a = x1a.trim();
                        view.setText(x2a);
                        try {
                            y = Double.parseDouble(x1a);
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "ERROR " + e.getMessage());
                        }
                        *//*graph.getViewport().setMinY(2000);
                        graph.getViewport().setMaxY(2400);
                        graph.getViewport().setYAxisBoundsManual(true);*//*
                        DataPoint datapoint = new DataPoint(x, y);
                        if(y < 1500)
                        {

                        }
                        else {

                            series.appendData(datapoint, false, 10000);
                        }
                        if (x > 60) {
                            graph.getViewport().setMinX(x - 60); // 40 for end of screen scroll, 60 for leaving gap first and fill in then scroll
                            graph.getViewport().setMaxX(x);
                        }


                        Log.d(TAG, "DATAPOINTA" + datapoint);


                    }*/
                    if (readMessage.charAt(0)=='B') {
                        mReadBuffer.setText(readMessage);
                        x1 = x1 + 1;
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)!=' ')
                        {
                            x1a = readMessage.substring(2, 3);
                        }
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ' && readMessage.charAt(3)!=' '  ) {
                            x1a = readMessage.substring(2, 5);
                        }
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ' && readMessage.charAt(3)==' '&& readMessage.charAt(4)!=' ') {
                            x1a = readMessage.substring(2, 7);
                        }
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ' && readMessage.charAt(3)==' ' && readMessage.charAt(4)==' '&& readMessage.charAt(5)!=' ') {
                            x1a = readMessage.substring(2, 9);
                        }
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ' && readMessage.charAt(3)==' ' && readMessage.charAt(4)==' '&& readMessage.charAt(5)==' ' && readMessage.charAt(6)!=' '){
                            x1a = readMessage.substring(2, 11);

                        }

                        String x2a = x1a.trim();

                        try {
                            y = Double.parseDouble(x1a);
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "ERROR " + e.getMessage());
                        }
                        if(y>80 && y<100)
                        view.setText("BLOOD OXYGEN="+y);

                      /* DataPoint datapoint = new DataPoint(x1, y);
                        series1.appendData(datapoint, false, 1000);

                        if (x1 > 60) {
                            graph1.getViewport().setMinX(x1 - 60); // 40 for end of screen scroll, 60 for leaving gap first and fill in then scroll
                            graph1.getViewport().setMaxX(x1);
                        }


                        Log.d(TAG, "DATAPOINTB" + datapoint);
*/
                    }
                    if (readMessage.charAt(0)=='H') {
                        mReadBuffer.setText(readMessage);
                        x1 = x1 + 1;
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)!=' ')
                        {
                            x1a = readMessage.substring(2, 3);
                        }
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ' && readMessage.charAt(3)!=' '  ) {
                            x1a = readMessage.substring(2, 5);
                        }
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ' && readMessage.charAt(3)==' '&& readMessage.charAt(4)!=' ') {
                            x1a = readMessage.substring(2, 7);
                        }
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ' && readMessage.charAt(3)==' ' && readMessage.charAt(4)==' '&& readMessage.charAt(5)!=' ') {
                            x1a = readMessage.substring(2, 9);
                        }
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ' && readMessage.charAt(3)==' ' && readMessage.charAt(4)==' '&& readMessage.charAt(5)==' ' && readMessage.charAt(6)!=' '){
                            x1a = readMessage.substring(2, 11);

                        }

                        String x2a = x1a.trim();

                        try {
                            y = Double.parseDouble(x2a);
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "ERROR " + e.getMessage());
                        }
                        if(y<50|| y>180)
                        {
                        }
                        else
                        view1.setText("HEART RATE="+y);
                      /* DataPoint datapoint = new DataPoint(x1, y);
                        series1.appendData(datapoint, false, 1000);

                        if (x1 > 60) {
                            graph1.getViewport().setMinX(x1 - 60); // 40 for end of screen scroll, 60 for leaving gap first and fill in then scroll
                            graph1.getViewport().setMaxX(x1);
                        }


                        Log.d(TAG, "DATAPOINTB" + datapoint);
*/
                    }
                    if (readMessage.charAt(0)=='C') {
                        mReadBuffer.setText(readMessage);
                        x2 = x2 + 1;
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)!=' ')
                        {
                            x1a = readMessage.substring(2, 3);
                        }
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ') {
                            x1a = readMessage.substring(2, 5);
                        }
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ' && readMessage.charAt(3)==' ') {
                            x1a = readMessage.substring(2, 7);
                        }
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ' && readMessage.charAt(3)==' ' && readMessage.charAt(4)==' ') {
                            x1a = readMessage.substring(2, 9);
                        }
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ' && readMessage.charAt(3)==' ' && readMessage.charAt(4)==' '&& readMessage.charAt(5)==' ' && readMessage.charAt(6)!=' '){
                            x1a = readMessage.substring(2, 11);

                        }
                        String x2a = x1a.trim();

                       // view.setText(x2a);
                        try {
                            y = Double.parseDouble(x1a);
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "ERROR " + e.getMessage());
                        }
                        if(y<1000)
                        {

                        }

                        else {
                            DataPoint datapoint = new DataPoint(x2, y);
                            series2.appendData(datapoint, false, 1000);

                           if (x2 > 60) {
                                graph2.getViewport().setMinX(x2 - 60); // 40 for end of screen scroll, 60 for leaving gap first and fill in then scroll
                                graph2.getViewport().setMaxX(x2);
                            }


                            Log.d(TAG, "DATAPOINTC" + datapoint);
                        }
                    }
                   /* if (readMessage.charAt(0)=='D') {
                        mReadBuffer.setText(readMessage);
                        x3 = x3 + 1;
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)!=' ')
                        {
                            x1a = readMessage.substring(2, 3);
                        }
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ') {
                            x1a = readMessage.substring(2, 5);
                        }
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ' && readMessage.charAt(3)==' ') {
                            x1a = readMessage.substring(2, 7);
                        }
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ' && readMessage.charAt(3)==' ' && readMessage.charAt(4)==' ') {
                            x1a = readMessage.substring(2, 9);
                        }
                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ' && readMessage.charAt(3)==' ' && readMessage.charAt(4)==' '&& readMessage.charAt(5)==' ' && readMessage.charAt(6)!=' '){
                            x1a = readMessage.substring(2, 11);

                        }
                        String x2a = x1a.trim();
                        view1.setText(x2a);
                        try {
                            y = Double.parseDouble(x1a);
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "ERROR " + e.getMessage());
                        }


                        DataPoint datapoint = new DataPoint(x3, y);
                        *//*series3.appendData(datapoint, false, 1000);

                        if (x3 > 60) {
                            graph3.getViewport().setMinX(x3 - 60); // 40 for end of screen scroll, 60 for leaving gap first and fill in then scroll
                            graph3.getViewport().setMaxX(x3);
                        }


                        Log.d(TAG, "DATAPOINTD" + datapoint);
*//*
                    }*/
                    if(readMessage.charAt(0)=='E') {
                        mReadBuffer.setText(readMessage);


                        if(readMessage.charAt(1)==' ' && readMessage.charAt(2)==' ' && readMessage.charAt(3)==' ' && readMessage.charAt(4)==' '&& readMessage.charAt(5)==' ' ){
                            x1a = readMessage.substring(2, 11);

                        }
                        String x2a = x1a.trim();
                       //view.setText(x2a);
                        try {
                            y = Double.parseDouble(x1a);
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "ERROR " + e.getMessage());
                        }
                        if (counter < 2) {
                            try {
                                gyro[counter] = y;
                                counter++;
                            }
                            catch(NullPointerException e){
                                Log.e(TAG,"ERROR " +e.getMessage());
                            }
                        }
                        else
                        {
                            if((gyro[1]-gyro[0])<10000)
                            {
                                gyros.setText("GYRO STATUS: ROLLING TIME="+count);
                            }
                            else
                            {
                                count++;
                                gyros.setText("GYRO STATUS: ROLLING TIME="+count);
                            }
                            counter=0;
                            gyro[0]=0;
                            gyro[1]=0;


                        }
                    }

                }
                if(msg.what == CONNECTING_STATUS){
                    if(msg.arg1 == 1)
                        mBluetoothStatus.setText("Connected to Device: " + (String)(msg.obj));
                    else
                        mBluetoothStatus.setText("Connection Failed");
                }
            }


        };
        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            mBluetoothStatus.setText("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(),"Bluetooth device not found!",Toast.LENGTH_SHORT).show();
        }
        else {



            /*mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  //  bluetoothOn(v);
                }
            });*/
            Button btn = (Button)findViewById(R.id.open_activity_button);

            /*btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, Main2Activity.class));
                }
            });*/

           /* mOffBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                 // bluetoothOff(v);
                }
            });*/

            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    listPairedDevices(v);
                }
            });

            /*mDiscoverBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                  // discover(v);
                }
            });*/
        }

    }
    private void Init()
    {

        try {
            series.appendData(new DataPoint(x, y), true, 100);

            Log.d(TAG, "x " + x + "y" + y);
        }
        catch( NullPointerException e)
        {
            Log.e(TAG, "ERROR" + e.getMessage());
        }
    }
/*
    private void bluetoothOn(View view){
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mBluetoothStatus.setText("Bluetooth enabled");
            Toast.makeText(getApplicationContext(),"Bluetooth turned on",Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }*/

    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data){
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                mBluetoothStatus.setText("Enabled");
            }
            else
                mBluetoothStatus.setText("Disabled");
        }
    }

  /* private void bluetoothOff(View view){
        mBTAdapter.disable(); // turn off
        mBluetoothStatus.setText("Bluetooth disabled");
        Toast.makeText(getApplicationContext(),"Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }*/

/*
    private void discover(View view){
        // Check if the device is already discovering
        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"Discovery stopped",Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private void listPairedDevices(View view){
        mPairedDevices = mBTAdapter.getBondedDevices();
        if(mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            if(!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }

            mBluetoothStatus.setText("Connecting...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0,info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread()
            {
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(fail == false) {
                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[16384];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        //SystemClock.sleep(10); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
