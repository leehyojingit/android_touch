package org.techtown.touch_tool;


//test 0514
import android.Manifest;
import android.content.pm.PackageManager;


import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import static java.util.Locale.KOREA;
import static org.techtown.touch_tool.R.id.RB_ABS;
import static org.techtown.touch_tool.R.id.RB_Jitter;
import static org.techtown.touch_tool.R.id.RB_Mux;
import static org.techtown.touch_tool.R.id.RB_Open;
import static org.techtown.touch_tool.R.id.RB_P2P;
import static org.techtown.touch_tool.R.id.RB_RAW;
import static org.techtown.touch_tool.R.id.RB_Short;
import static org.techtown.touch_tool.R.id.TB_Abs;
import static org.techtown.touch_tool.R.id.TB_Jitter;
import static org.techtown.touch_tool.R.id.TB_Message;
import static org.techtown.touch_tool.R.id.TB_Mux;
import static org.techtown.touch_tool.R.id.TB_Open;
import static org.techtown.touch_tool.R.id.TB_P2P;
import static org.techtown.touch_tool.R.id.TB_Raw;
import static org.techtown.touch_tool.R.id.TB_Short;

public class MainActivity extends AppCompatActivity {


    ImageView Grid_View;
    TextView TB_raw,TB_short,TB_jitter,TB_abs,TB_p2p,TB_mux,TB_open;
    TextView TB_message;
    Button   BT_Start, BT_CNT, BT_Exit, BT_Set;
    RadioButton RB_raw, RB_short, RB_jitter, RB_abs, RB_p2p, RB_mux, RB_open;
    RadioGroup RG_Item;
    ProgressBar PG_bar;

    private ScaleGestureDetector mScaleGestureDetector;
    private float   mScaleFactor = 1.0f;
    Bitmap bitmap;


    //Raw Data Array
    private int[][] ar_raw;
//    private int[] ar_Jitter;
//    private int[] ar_short;
//    private int[] ar_abs;
//    private int[] ar_p2p;
//    private int[] ar_mux;
//    private int[] ar_open;
    private  int [] ar_temp;


    private String FW_Version, R_FW_Version;
    private int raw_min, raw_max, jitter_min, jitter_max, short_min, short_max;
    private int abs_min, abs_max, p2p_min, p2p_max, mux_min, mux_max, open_min, open_max;
    private int delay;


    ///USB
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final int DEBUG = 1;
    private PendingIntent mPermissionIntent;
    private UsbManager mUsbManager;
    private UsbDevice mDeviceFound;
    private UsbDeviceConnection mConnectionRead;
    private UsbDeviceConnection mConnection;
    private UsbDeviceConnection mConnectionWrite;
    private UsbInterface mUsbInterface = null;
    private UsbEndpoint mInputEndpoint = null;
    private UsbEndpoint mOutputEndpoint = null;

    private UsbEndpoint mEndpointRead;
    private UsbEndpoint mEndpointWrite;

    public String i = "";
    private int count ;


    public int[] max_val;
    public int[] min_val;
    public int[] avg_val;
    public int[] avg_open;
    public boolean result;
    boolean[] b_define;

    byte [] command;
    byte [] get_infor_0;            byte [] get_infor_1;            byte [] get_version0;
    byte [] get_version1;           byte [] test_set;
    byte [] check_mode0;            byte [] check_mode1;
    byte [] ready_check0;           byte [] ready_check1;
    byte [] test_mode_raw;          byte [] test_start;
    byte [] test_mode_jitter;       byte [] test_mode_short;        byte [] test_mode_abs;
    byte [] test_mode_p2p;          byte [] test_mode_mux;          byte [] test_mode_open;

    byte [] test_complete0;         byte [] test_complete1;         byte [] test_complete2;
    byte [] test_finish0;         byte [] test_finish1;         byte [] test_finish2;

    ///


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //For Permission Request Under Android Q(~9)//test 0514
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }

        //화변 세로고정
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

//        Toast.makeText(this, "Main Initial", Toast.LENGTH_SHORT).show();

    // Instance claim
        Grid_View = findViewById(R.id.imageView);

        BT_Start = findViewById(R.id.BT_Start);
        BT_CNT = findViewById(R.id.BT_CNT);
        BT_Exit = findViewById(R.id.BT_Exit);
        BT_Set = findViewById(R.id.BT_Set);

        RB_raw = findViewById(RB_RAW);
        RB_short = findViewById(RB_Short);
        RB_jitter = findViewById(RB_Jitter);
        RB_abs = findViewById(RB_ABS);
        RB_p2p = findViewById(RB_P2P);
        RB_mux = findViewById(RB_Mux);
        RB_open = findViewById(RB_Open);

        TB_raw = findViewById(TB_Raw);
        TB_short = findViewById(TB_Short);
        TB_jitter = findViewById(TB_Jitter);
        TB_abs = findViewById(TB_Abs);
        TB_p2p = findViewById(TB_P2P);
        TB_mux = findViewById(TB_Mux);
        TB_open = findViewById(TB_Open);

        TB_message = findViewById(TB_Message);
        RG_Item = findViewById(R.id.RD_Item);

        RG_Item.setOnCheckedChangeListener(mRadioCheck);

        PG_bar = findViewById(R.id.PG_INSP);

        TB_message.setMovementMethod(new ScrollingMovementMethod());

        initialize();
        drawing(10);

        count = 0;
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);



        BT_CNT.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v)
            {
                final HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

                while (deviceIterator.hasNext())
                {
                    final UsbDevice device = deviceIterator.next();
                    mDeviceFound = device;
                    i =
//                          "DeviceID: " + device.getDeviceId() + "\n" +
                            "DeviceName: " + device.getDeviceName() +"_"+
                            "VendorID: " + device.getVendorId() +"_"+
                            "ProductID: " + device.getProductId();
                }
                if(mDeviceFound == null)    Toast.makeText(getApplicationContext(), "No device found", Toast.LENGTH_SHORT).show();
                else checkInfo(mDeviceFound);
            }
        });
        ini_create();
        TB_message.append(Integer.toString(delay));
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
            // ScaleGestureDetector에서 factor를 받아 변수로 선언한 factor에 넣고
            mScaleFactor *= scaleGestureDetector.getScaleFactor();

            // 최대 10배, 최소 10배 줌 한계 설정
            mScaleFactor = Math.max(0.1f,
                    Math.min(mScaleFactor, 10.0f));

            // 이미지뷰 스케일에 적용
            Grid_View.setScaleX(mScaleFactor);
            Grid_View.setScaleY(mScaleFactor);
            return true;
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        //변수로 선언해 놓은 ScaleGestureDetector
        mScaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    public void onClickButtonSet(View v)
    {
        Intent intent = new Intent(this, SubActivity.class);
        startActivity(intent);
//        finish();
    }


    RadioGroup.OnCheckedChangeListener mRadioCheck =
        new RadioGroup.OnCheckedChangeListener(){
            public void onCheckedChanged(RadioGroup group, int checkedId){
                if (group.getId() == R.id.RD_Item){
                    switch (checkedId){
                        case RB_RAW:
                            TB_raw.setTextColor(Color.BLUE);
                            TB_jitter.setTextColor(Color.BLACK);
                            TB_abs.setTextColor(Color.BLACK);
                            TB_short.setTextColor(Color.BLACK);
                            TB_p2p.setTextColor(Color.BLACK);
                            TB_mux.setTextColor(Color.BLACK);
                            TB_open.setTextColor(Color.BLACK);
                            drawing(0);
                            break;
                        case RB_Jitter:
                            TB_jitter.setTextColor(Color.BLUE);
                            TB_raw.setTextColor(Color.BLACK);
                            TB_abs.setTextColor(Color.BLACK);
                            TB_short.setTextColor(Color.BLACK);
                            TB_p2p.setTextColor(Color.BLACK);
                            TB_mux.setTextColor(Color.BLACK);
                            TB_open.setTextColor(Color.BLACK);
                            drawing(2);
                            break;
                        case RB_ABS:
                            TB_abs.setTextColor(Color.BLUE);
                            TB_raw.setTextColor(Color.BLACK);
                            TB_jitter.setTextColor(Color.BLACK);
                            TB_short.setTextColor(Color.BLACK);
                            TB_p2p.setTextColor(Color.BLACK);
                            TB_mux.setTextColor(Color.BLACK);
                            TB_open.setTextColor(Color.BLACK);
                            drawing(3);
                            break;
                        case RB_Short:
                            TB_short.setTextColor(Color.BLUE);
                            TB_raw.setTextColor(Color.BLACK);
                            TB_jitter.setTextColor(Color.BLACK);
                            TB_abs.setTextColor(Color.BLACK);
                            TB_p2p.setTextColor(Color.BLACK);
                            TB_mux.setTextColor(Color.BLACK);
                            TB_open.setTextColor(Color.BLACK);
                            drawing(1);
                            break;
                        case RB_P2P:
                            TB_p2p.setTextColor(Color.BLUE);
                            TB_raw.setTextColor(Color.BLACK);
                            TB_jitter.setTextColor(Color.BLACK);
                            TB_abs.setTextColor(Color.BLACK);
                            TB_short.setTextColor(Color.BLACK);
                            TB_mux.setTextColor(Color.BLACK);
                            TB_open.setTextColor(Color.BLACK);
                            drawing(4);
                            break;
                        case RB_Mux:
                            TB_mux.setTextColor(Color.BLUE);
                            TB_raw.setTextColor(Color.BLACK);
                            TB_jitter.setTextColor(Color.BLACK);
                            TB_abs.setTextColor(Color.BLACK);
                            TB_short.setTextColor(Color.BLACK);
                            TB_p2p.setTextColor(Color.BLACK);
                            TB_open.setTextColor(Color.BLACK);
                            drawing(5);
                            break;
                        case RB_Open:
                            TB_open.setTextColor(Color.BLUE);
                            TB_raw.setTextColor(Color.BLACK);
                            TB_jitter.setTextColor(Color.BLACK);
                            TB_abs.setTextColor(Color.BLACK);
                            TB_short.setTextColor(Color.BLACK);
                            TB_p2p.setTextColor(Color.BLACK);
                            TB_mux.setTextColor(Color.BLACK);
                            drawing(7);

                            break;
                    }
                }
            }
        };

    private void update_command(byte[] val)
    {
        for(int i=0; i<12; i++)
        {
            command[i] = val[i];
        }
    }

    //Buffer and setting initialize
    private void initialize()
    {
        Toast.makeText(this, "Initialize",Toast.LENGTH_SHORT).show();
        //Data declare
        ar_raw = new int[8][5760];
        avg_open = new int[60];
//        ar_Jitter = new int[5760];
//        ar_short = new int[5760];
//        ar_abs = new int[5760];
//        ar_p2p = new int[5760];
//        ar_mux = new int[5760];
//        ar_open = new int[5760];

        ar_temp = new int[5760];
        max_val = new int[8];
        min_val = new int[8];
        avg_val = new int[8];

        for(int i=0; i<60; i++)
        {
            avg_open[i] = 0;
        }

        for(int i=0; i<8; i++)
        {
            max_val[i] = 0;        min_val[i] = 9999;       avg_val[i] = 0;
        }
        FW_Version = "";
        R_FW_Version="";

        b_define = new boolean[5760];
        for(int i = 0; i<5760; i++)
        {
            b_define[i] = false;
        }

        PG_bar.setProgress(0);

        ////////////////////////////////////////////////////////////////////////////////////////////////
        //USB CMD Initialize
        ////////////////////////////////////////////////////////////////////////////////////////////////
        command = new byte[64];
        for(int j=0; j<64; j++)
        {
            command[j] = 0x00;
        }

        get_infor_0 = new byte[]{0x09, 0x68, 0x02, 0x00, 0x01, 0x50, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        get_infor_1 = new byte[]{0x09, 0x69, 0x0e, 0x00, 0x01, 0x50, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        get_version0 = new byte[]{0x09, 0x68, 0x02, 0x00, 0x01, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        get_version1 = new byte[]{0x09, 0x69, 0x04, 0x00, 0x01, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        test_set = new byte[]{0x09, 0x68, 0x03, 0x00, 0x06, 0x10, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00};
        check_mode0 = new byte[]{0x09, 0x68, 0x02, 0x00, 0x06, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        check_mode1 = new byte[]{0x09, 0x69, 0x01, 0x00, 0x06, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        ready_check0 = new byte[]{0x09, 0x68, 0x02, 0x00, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        ready_check1 = new byte[]{0x09, 0x69, 0x01, 0x00, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        test_mode_raw = new byte[]{0x09, 0x68, 0x03, 0x00, 0x0a, 0x10, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00};
        test_start = new byte[]{0x09, 0x68, 0x03, 0x00, 0x06, 0x22, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00};

        test_complete0 = new byte[]{0x09, 0x68, 0x03, 0x00, 0x06, 0x22, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        test_complete1 = new byte[]{0x09, 0x68, 0x02, 0x00, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        test_complete2 = new byte[]{0x09, 0x69, 0x01, 0x00, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        test_finish0 = new byte[]{0x09, 0x68, 0x03, 0x00, 0x06, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        test_finish1 = new byte[]{0x09, 0x68, 0x02, 0x00, 0x06, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        test_finish2 = new byte[]{0x09, 0x69, 0x01, 0x00, 0x06, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        test_mode_jitter = new byte[]{0x09, 0x68, 0x03, 0x00, 0x0a, 0x10, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00};
        test_mode_short = new byte[]{0x09, 0x68, 0x03, 0x00, 0x0a, 0x10, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00};
        test_mode_abs = new byte[]{0x09, 0x68, 0x03, 0x00, 0x0a, 0x10, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00};
        test_mode_p2p = new byte[]{0x09, 0x68, 0x03, 0x00, 0x0a, 0x10, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00};
        test_mode_mux = new byte[]{0x09, 0x68, 0x03, 0x00, 0x0a, 0x10, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00};
        test_mode_open = new byte[]{0x09, 0x68, 0x03, 0x00, 0x0a, 0x10, 0x13, 0x00, 0x00, 0x00, 0x00, 0x00};

//        update_command(test_start);

        ////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////

//        for(int t_num=0; t_num<8; t_num++)
//        {
//            for(int i=0; i<5760; i++)
//            {
//                ar_raw[t_num][i] = 0;
//            }
//        }

        //TEST
        for(int i=0; i<5760; i++)
        {
            ar_raw[0][i] = 2000;            ar_raw[1][i] = 100;
            ar_raw[2][i] = 50;            ar_raw[3][i] = 2000;
            ar_raw[4][i] = 50;            ar_raw[5][i] = -100;
            ar_raw[6][i] = 8190;            ar_raw[7][i] = 0;
        }


        for(int i=0; i<5760; i++)
        {
            ar_temp[i] = 2000;
        }

        Intent intent = getIntent();
        String s_fw = intent.getStringExtra("fw");
        String s_raw_min = intent.getStringExtra("raw_min");        String s_raw_max = intent.getStringExtra("raw_max");
        String s_jitter_min = intent.getStringExtra("jitter_min");        String s_jitter_max = intent.getStringExtra("jitter_max");
        String s_short_min = intent.getStringExtra("short_min");        String s_short_max = intent.getStringExtra("short_max");
        String s_abs_min = intent.getStringExtra("abs_min");        String s_abs_max = intent.getStringExtra("abs_max");
        String s_p2p_min = intent.getStringExtra("p2p_min");        String s_p2p_max = intent.getStringExtra("p2p_max");
        String s_mux_min = intent.getStringExtra("mux_min");        String s_mux_max = intent.getStringExtra("mux_max");
        String s_open_min = intent.getStringExtra("open_min");        String s_open_max = intent.getStringExtra("open_max");
        String s_delay = intent.getStringExtra("delay");

        String path = "/storage/emulated/0/Download";
        File myDir = new File(path + "/myApps/spec.ini");
        BufferedReader bw = null;
        if(s_raw_min == null)
        {
            //Read Ini file
            try
            {
                bw = new BufferedReader(new FileReader(myDir.getPath()));
                String [] line = new String[17];
                int n = 0;
                while((line[n] = bw.readLine()) != null)
                {
                    n++;
                }
                if(line[15] != null)
                {
                    FW_Version = line[0];
                    raw_min = Integer.parseInt(line[1]);    raw_max = Integer.parseInt(line[2]);
                    jitter_min = Integer.parseInt(line[3]);    jitter_max = Integer.parseInt(line[4]);
                    short_min = Integer.parseInt(line[5]);    short_max = Integer.parseInt(line[6]);
                    abs_min = Integer.parseInt(line[7]);    abs_max = Integer.parseInt(line[8]);
                    p2p_min = Integer.parseInt(line[9]);    p2p_max = Integer.parseInt(line[10]);
                    mux_min = Integer.parseInt(line[11]);    mux_max = Integer.parseInt(line[12]);
                    open_min = Integer.parseInt(line[13]);    open_max = Integer.parseInt(line[14]);
                    delay = Integer.parseInt(line[15]);
                }
                else
                {
                    TB_message.append("Spec is not initialize!!\n");
                }
//                bw.readLine(etd_fw.getText().toString()+"\n");
//                bw.write(etb_raw_min.getText().toString()+"\n");bw.write(etb_raw_max.getText().toString()+"\n");
//                bw.write(etb_jitter_min.getText().toString()+"\n");bw.write(etb_jitter_max.getText().toString()+"\n");
//                bw.write(etb_short_min.getText().toString()+"\n");bw.write(etb_short_max.getText().toString()+"\n");
//                bw.write(etb_abs_min.getText().toString()+"\n");bw.write(etb_abs_max.getText().toString()+"\n");
//                bw.write(etb_p2p_min.getText().toString()+"\n");bw.write(etb_p2p_max.getText().toString()+"\n");
//                bw.write(etb_mux_min.getText().toString()+"\n");bw.write(etb_mux_max.getText().toString()+"\n");
//                bw.write(etb_open_min.getText().toString()+"\n");bw.write(etb_open_max.getText().toString()+"\n");
                bw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
//            TB_message.setText(s_raw_min);
            FW_Version = s_fw;
            raw_min = Integer.parseInt(s_raw_min);  raw_max = Integer.parseInt(s_raw_max);
            jitter_min = Integer.parseInt(s_jitter_min);  jitter_max = Integer.parseInt(s_jitter_max);
            short_min = Integer.parseInt(s_short_min);  short_max = Integer.parseInt(s_short_max);
            abs_min = Integer.parseInt(s_abs_min);  abs_max = Integer.parseInt(s_abs_max);
            p2p_min = Integer.parseInt(s_p2p_min);  p2p_max = Integer.parseInt(s_p2p_max);
            mux_min = Integer.parseInt(s_mux_min);  mux_max = Integer.parseInt(s_mux_max);
            open_min = Integer.parseInt(s_open_min);  open_max = Integer.parseInt(s_open_max);
            delay = Integer.parseInt(s_delay);
        }
        TB_message.append("Initialized \n");
    }

    private void spec_update()
    {
        String path = "/storage/emulated/0/Download";
        File myDir = new File(path + "/myApps/spec.ini");
        BufferedReader bw = null;
        try
        {
            bw = new BufferedReader(new FileReader(myDir.getPath()));
            String[] line = new String[17];
            int n = 0;
            while ((line[n] = bw.readLine()) != null)
            {
                n++;
            }
            if (line[15] != null) {
                FW_Version = line[0];
                raw_min = Integer.parseInt(line[1]);
                raw_max = Integer.parseInt(line[2]);
                jitter_min = Integer.parseInt(line[3]);
                jitter_max = Integer.parseInt(line[4]);
                short_min = Integer.parseInt(line[5]);
                short_max = Integer.parseInt(line[6]);
                abs_min = Integer.parseInt(line[7]);
                abs_max = Integer.parseInt(line[8]);
                p2p_min = Integer.parseInt(line[9]);
                p2p_max = Integer.parseInt(line[10]);
                mux_min = Integer.parseInt(line[11]);
                mux_max = Integer.parseInt(line[12]);
                open_min = Integer.parseInt(line[13]);
                open_max = Integer.parseInt(line[14]);
                delay = Integer.parseInt(line[15]);
            }
            else
            {
                TB_message.append("Spec is not initialize!!\n");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ini_create()
    {
        String path = "/storage/emulated/0/Download";
        File myDir = new File(path + "/myApps");
        boolean success = true;
        if(!myDir.exists())
        {
            success = myDir.mkdir();

            if(!success){
                TB_message.append("Directory 생성 실패");
            }
            else{
//                TB_message.append("Directory 생성 성공");
            }
        }

        File file_ini = new File(myDir.getPath()  + "/spec.ini");
        if(!file_ini.exists())
        {
            try
            {
                BufferedWriter bw = new BufferedWriter(new FileWriter(file_ini.getPath() , false));
                bw.close();
                TB_message.append("ini file is newly Created!!");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private void csv_writing()
    {
        TB_message.append("Data Writing Start\n");
        long now = System.currentTimeMillis(); //현재시간 Loading
        Date mDate =  new Date(now);    // Date형태로 변경
        //Custom Style 변경
        SimpleDateFormat formatdate = new SimpleDateFormat("yyyyMMddHHmmss", KOREA);
        // String Type 변경
        String getTime = formatdate.format(mDate);

//        FileWriter writer;
        try{
            String path = "/storage/emulated/0/Download";

            //0513 Change
            String file_name = "";
            if(result == true)
            {
                 file_name = "/" + getTime+ "_Touch_Result_FAIL"+".csv";
            }
            else
            {
                 file_name = "/" + getTime+ "_Touch_Result_PASS"+".csv";
            }
            //0513 Change
            File myDir = new File(path + "/myApps");
            boolean success = true;
            if(!myDir.exists())
            {
                success = myDir.mkdir();
            }
            if(!success){
                TB_message.append("Directory 생성 실패\n");
            }
            else{
//                TB_message.append("Directory 생성 성공\n");
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(myDir.getPath() + file_name, true));
            bw.write("FW version : " + ","+ R_FW_Version +"\n");
            for(int t_num=0; t_num<8; t_num++) {
                if(t_num ==0)       bw.write("Raw Data Result \n");
                else if(t_num ==1)   bw.write("Short Data Result \n");
                else if(t_num ==2)   bw.write("Jitter Data Result \n");
                else if(t_num ==3)   bw.write("ABS_RAW Data Result \n");
                else if(t_num ==4)   bw.write("ABS_P2P Data Result \n");
                else if(t_num ==5)   bw.write("MUX Short Data Result \n");
                else if(t_num ==6)   bw.write("OPEN Data Result \n");
                else if(t_num ==7)   bw.write("OPEN_DEV Data Result \n");

                int sum = 0;
                for (int i = 0; i < 60; i++) {
                    for (int j = 0; j < 96; j++) {
//                    bw.write(ar_temp[(i*96)+j]+",");
                        bw.write(ar_raw[t_num][(i * 96) + (95 - j)] + ",");
                        min_val[t_num] = Math.min(min_val[t_num], ar_raw[t_num][(i * 96) + (95 - j)]);
                        max_val[t_num] = Math.max(max_val[t_num], ar_raw[t_num][(i * 96) + (95 - j)]);
                    }
                    bw.write("\n");
                }
                bw.write("Min : " + min_val[t_num] + "," + "Max : " + max_val[t_num]);
                bw.write("\n");

                if(t_num ==0)
                {
                    if((min_val[t_num] < raw_min)|(max_val[t_num]> raw_max) )
                    {
                        TB_raw.setBackgroundColor(Color.RED);
                    }
                    else
                    {
                        TB_raw.setBackgroundColor(Color.GREEN);
                    }

                    TB_raw.setText("Min : "+ min_val[t_num]+" // " + "Max : " + max_val[t_num]);
                }
                else if(t_num ==1)
                {
                    if((min_val[t_num] < short_min)|(max_val[t_num]> short_max) )
                    {
                        TB_short.setBackgroundColor(Color.RED);
                    }
                    else {
                        TB_short.setBackgroundColor(Color.GREEN);
                    }
                    TB_short.setText("Min : "+ min_val[t_num]+" // " + "Max : " + max_val[t_num]);
                }
                else if(t_num ==2)
                {
                    if((min_val[t_num] < jitter_min)|(max_val[t_num]> jitter_max) )
                    {
                        TB_jitter.setBackgroundColor(Color.RED);
                    }
                    else
                    {
                        TB_jitter.setBackgroundColor(Color.GREEN);
                    }
                    TB_jitter.setText("Min : "+ min_val[t_num]+" // " + "Max : " + max_val[t_num]);
                }
                else if(t_num ==3)
                {
                    if((min_val[t_num] < abs_min)|(max_val[t_num]> abs_max) )
                    {
                        TB_abs.setBackgroundColor(Color.RED);
                    }
                    else
                    {
                        TB_abs.setBackgroundColor(Color.GREEN);
                    }
                    TB_abs.setText("Min : "+ min_val[t_num]+" // " + "Max : " + max_val[t_num]);
                }
                else if(t_num ==4)
                {
                    if((min_val[t_num] < p2p_min)|(max_val[t_num]> p2p_max) )
                    {
                        TB_p2p.setBackgroundColor(Color.RED);
                    }
                    else
                    {
                        TB_p2p.setBackgroundColor(Color.GREEN);
                    }
                    TB_p2p.setText("Min : "+ min_val[t_num]+" // " + "Max : " + max_val[t_num]);
                }
                else if(t_num ==5)
                {
                    if((min_val[t_num] < mux_min)|(max_val[t_num]> mux_max) )
                    {
                        TB_mux.setBackgroundColor(Color.RED);
                    }
                    else
                    {
                        TB_mux.setBackgroundColor(Color.GREEN);
                    }
                    TB_mux.setText("Min : "+ min_val[t_num]+" // " + "Max : " + max_val[t_num]);
                }
                else if(t_num ==7)
                {
                    if((min_val[t_num] < open_min)|(max_val[t_num]> open_max) )
                    {
                        TB_open.setBackgroundColor(Color.RED);
                    }
                    else
                    {
                        TB_open.setBackgroundColor(Color.GREEN);
                    }
                    TB_open.setText("Min : "+ min_val[t_num]+" // " + "Max : " + max_val[t_num]);
                }
                bw.write("\n");
            }
            bw.close();
            TB_message.append("Inspection Log Saving Complete.\n");
//            TB_message.append("저장되었습니다.\n");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            TB_message.append("Data Writing Error\n");
        }
//        TB_message.append("Data Writing Complete\n");
        PG_bar.setProgress(100);

        Context c = Grid_View.getContext();
        Vibrator vib = ( Vibrator ) c.getSystemService( Context.VIBRATOR_SERVICE);
        vib.vibrate(300);

    }

    private void tb_init()
    {
        TB_raw.setBackgroundColor(Color.LTGRAY);    TB_raw.setText("");
        TB_short.setBackgroundColor(Color.LTGRAY);  TB_short.setText("");
        TB_jitter.setBackgroundColor(Color.LTGRAY); TB_jitter.setText("");
        TB_abs.setBackgroundColor(Color.LTGRAY);    TB_abs.setText("");
        TB_p2p.setBackgroundColor(Color.LTGRAY);    TB_p2p.setText("");
        TB_mux.setBackgroundColor(Color.LTGRAY);    TB_mux.setText("");
        TB_open.setBackgroundColor(Color.LTGRAY);   TB_open.setText("");
    }

    //USB Connection Check
    private void checkInfo(UsbDevice device)
    {
        count++;
        if(count == 1) {
            mUsbManager.requestPermission(device, mPermissionIntent);
            TB_message.append(i+"\n");
        } else
            Toast.makeText(this, "Already connected", Toast.LENGTH_SHORT).show();
    }

    // checking
    @Override
    protected void onPause() {
        super.onPause();
        count=0;
        try {
            unregisterReceiver(mUsbReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // App's USB Author Check Receiver
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action))
            {
                synchronized (this)
                {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
                    {
                        Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show();
                        connectUsb(device);
                    } else
                    {
                        Log.d(TAG, "permission denied for device " + device);
                        Toast.makeText(context, "permission denied for device" + device, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    // ConnectUSB and Transfer Test
    private void connectUsb(UsbDevice device) {
        boolean BulkDeviceDetect = false;
        String temp_s = "";       String temp_s1="";
        int    temp_t = 0;        int    temp_t1 = 0;

        TB_message.setText("");
        result = false;

        for(int i = 0; i<5760; i++)
        {
            b_define[i] = false;
        }

        if(device != null)
        {
            for(int i=0;i<device.getInterfaceCount();i++){
                mUsbInterface = device.getInterface(i);
                UsbEndpoint tOut = null;
                UsbEndpoint tIn = null;
                int usbEndPointCount = mUsbInterface.getEndpointCount();
                if(usbEndPointCount >=2){
                    for(int j =0;j<usbEndPointCount;j++){
                        if(mUsbInterface.getEndpoint(j).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK){
                            if(mUsbInterface.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_OUT){
                                tOut = mUsbInterface.getEndpoint(j);
                            }else if(mUsbInterface.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_IN){
                                tIn = mUsbInterface.getEndpoint(j);
                            }
                        }
                        else if(mUsbInterface.getEndpoint(j).getType() == UsbConstants.USB_ENDPOINT_XFER_INT)
                        {
//                            TB_message.append( j + " : EP is Interrupt Type \n");
                        }
                    }
                    if(tIn!=null & tOut !=null){
                        mInputEndpoint = tIn;
                        mOutputEndpoint = tOut;
//                        TB_message.append("EP Bulk Endpoint Detect \n");
                        BulkDeviceDetect = true;
                    }
                    else
                    {
//                        TB_message.append( "This device only have interrupt Endpoint \n");
                    }
                }
//                TB_message.append( "USB Endpoint Count : " +usbEndPointCount+ "\n");
                if(mUsbInterface.getEndpoint(0).getType() == UsbConstants.USB_ENDPOINT_XFER_INT)
                {
                    TB_message.append( "This device only have interrupt Endpoint \n");
                }
            }
            if(BulkDeviceDetect == true)
            {
                mConnection = mUsbManager.openDevice(device);
                if (mConnection.claimInterface(mUsbInterface, true)) {
//                    TB_message.append("Connected to Bulk Device \n");
                    byte[] byteArray_Rd = new byte[64];
                    for(int i=0; i<64; i++)
                    {
                        byteArray_Rd[i] = 0x00;
                    }
///////////////////////////////////////////////////////////////
//Data Transfer (Task 추가 필요)
///////////////////////////////////////////////////////////////
//Test information READ
                    update_command(get_infor_0);
                    int dataTransfered = mConnection.bulkTransfer(mOutputEndpoint,command,command.length, 0);
                    update_command(get_infor_1);
                    dataTransfered = mConnection.bulkTransfer(mOutputEndpoint,command,command.length, 0);
                    int dataReadCount = mConnection.bulkTransfer(mInputEndpoint,byteArray_Rd,byteArray_Rd.length,0);
//                    TB_message.append("Infor Get Success\n");

//Test Version Read
                    update_command(get_version0);
                    dataTransfered = mConnection.bulkTransfer(mOutputEndpoint,command,command.length, 0);
                    update_command(get_infor_1);
                    dataTransfered = mConnection.bulkTransfer(mOutputEndpoint,command,command.length, 0);
                    dataReadCount = mConnection.bulkTransfer(mInputEndpoint,byteArray_Rd,byteArray_Rd.length,0);

                    //Check FW Version and Compare
                    temp_t = byteArray_Rd[7];   temp_t1 = byteArray_Rd[6];
                    if(temp_s1.length() == 1) temp_s1 = "0"+ temp_s1;
                    temp_s = Integer.toHexString(temp_t) + Integer.toHexString(temp_t1);
                    R_FW_Version = temp_s;
                    if(temp_s == FW_Version)                        TB_message.append("Version Check OK : " + temp_s + "\n");
                    else                                            TB_message.append("Version Check NG : " + temp_s + "\n");

//Test Setting and Mode check
                    update_command(test_set);
                    dataTransfered = mConnection.bulkTransfer(mOutputEndpoint,command,command.length, 0);
                    update_command(check_mode0);
                    dataTransfered = mConnection.bulkTransfer(mOutputEndpoint,command,command.length, 0);
                    update_command(check_mode1);
                    dataTransfered = mConnection.bulkTransfer(mOutputEndpoint,command,command.length, 0);
                    try{ Thread.sleep(100);}
                    catch (InterruptedException e){}
                    dataReadCount = mConnection.bulkTransfer(mInputEndpoint,byteArray_Rd,byteArray_Rd.length,0);

//                    temp_t = 0; temp_s = "";
//                    for(int i=0; i<12; i++)
//                    {
//                        temp_t = byteArray_Rd[i];
//                        temp_s += Integer.toHexString(temp_t) + ",";
//                    }
//                    TB_message.append("Mode Set : " + temp_s + "/n");

//                    READY Status Check a0 check need.

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    PG_bar.setProgress(5);
                for(int t_num=0; t_num<7; t_num++)
                 {
                     update_command(ready_check0);
                     dataTransfered = mConnection.bulkTransfer(mOutputEndpoint, command, command.length, 0);
                     update_command(ready_check1);
                     dataTransfered = mConnection.bulkTransfer(mOutputEndpoint, command, command.length, 0);
                     try {
                         Thread.sleep(300);
                     } catch (InterruptedException e) {}
                     dataReadCount = mConnection.bulkTransfer(mInputEndpoint, byteArray_Rd, byteArray_Rd.length, 0);
                     int a = (int) byteArray_Rd[4];
                     temp_s = Integer.toHexString(a);
                     if (temp_s == "a0") {}
                     else {
//                         TB_message.append("READY ERROR!!");
                     }
                     switch (t_num)
                     {
                         case 0:
                             update_command(test_mode_raw);
                             break;
                         case 1:
                             update_command(test_mode_short);
                             break;
                         case 2:
                             update_command(test_mode_jitter);
                             break;
                         case 3:
                             update_command(test_mode_abs);
                             break;
                         case 4:
                             update_command(test_mode_p2p);
                             break;
                         case 5:
                             update_command(test_mode_mux);
                             break;
                         case 6:
                             update_command(test_mode_open);
                             break;
                     }
                     dataTransfered = mConnection.bulkTransfer(mOutputEndpoint, command, command.length, 0);
                     update_command(test_start);
                     dataTransfered = mConnection.bulkTransfer(mOutputEndpoint, command, command.length, 0);
                     TB_message.append("Test Raw Data Get Start!! \n");

                     ByteBuffer buffer = ByteBuffer.allocate(64);
                     UsbRequest inRequest = new UsbRequest();
                     int count = 0;
                     int count_open = 0;
                     int val = 0;

                     try {Thread.sleep(500);}
                     catch (InterruptedException e) {}
                     for (int i = 0; i < 192; i++) {
//                         if(i == 0)
//                         {
//                             TB_message.setBackgroundColor(Color.GREEN);
//                         }
 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 //Async
 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                        inRequest.initialize(mConnection, mInputEndpoint);
//                        if(inRequest.queue(buffer,byteArray_Rd.length)){
//                            mConnection.requestWait();
//                            for(int m=0; m<30; m++)
//                            {
//                                byteArray_Rd = buffer.array();
//                                val = (int)((byteArray_Rd[(m*2)+5]<<8) + (byteArray_Rd[(m*2)+4]));
//                                ar_raw[t_num][count] = val;
//                                count++;
//                            }
//                        }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Sync
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                         dataReadCount = mConnection.bulkTransfer(mInputEndpoint, byteArray_Rd, byteArray_Rd.length, 0);
                         for (int m = 0; m < 30; m++) {

                             if(delay <= 5)
                             {
                                 temp_t = 0; temp_t1 = 0;
                                 temp_s = ""; temp_s1 ="";

                                 temp_t = byteArray_Rd[(m * 2) + 5];
                                 temp_t1 = byteArray_Rd[(m * 2)+ 4];
                                 temp_s = Integer.toHexString(0xFF & temp_t);
                                 temp_s1 = Integer.toHexString(0xFF & temp_t1);
                                 if(temp_s1.length() == 1) temp_s1 = "0"+ temp_s1;
                                 val = Integer.parseInt(temp_s+ temp_s1,16);
                                 //0513 Change
                                 if((val & 0x8000) != 0)
                                 {
                                     val = (val - 65535)-1;
                                 }
                                 if(t_num == 6)
                                 {
                                     if((count % 96) == 0)
                                     {
                                         if(count !=0)
                                         {
                                             count_open++;
                                         }
                                     }
                                     avg_open[count_open] = avg_open[count_open] + val;
                                 }
                                 //0513 Change
                             }
                             else
                             {
                                 val = (int) ((byteArray_Rd[(m * 2) + 5] << 8) + (byteArray_Rd[(m * 2) + 4]));
                             }
                             ar_raw[t_num][count] = val;
                             count++;
                             }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                         try {Thread.sleep(delay);}
                         catch (InterruptedException e) {}
                     }

                     //0513 Change
                     for(int i= 0; i<60; i++)
                     {
                         avg_open[i] = avg_open[i] / 96;
                     }
                     for(int i=0; i<60; i++)
                     {
                         for(int j=0; j<96; j++)
                         {
                             ar_raw[7][(i*96)+j] = ar_raw[6][(i*96)+j] - avg_open[i];
                         }
                     }
                     //0513 Change

                     try {Thread.sleep(100);}
                     catch (InterruptedException e) {}
                     //Final Value check
                     dataReadCount = mConnection.bulkTransfer(mInputEndpoint, byteArray_Rd, byteArray_Rd.length, 0);
                     TB_message.append("Data Transfer Complete : " + Integer.toString(t_num) + "\n");

                     try {Thread.sleep(100);}
                     catch (InterruptedException e) {}
                     // complete Test
                     update_command(test_complete0);
                     dataTransfered = mConnection.bulkTransfer(mOutputEndpoint, command, command.length, 0);
                     TB_message.setBackgroundColor(Color.BLACK);
                     PG_bar.setProgress((t_num+1)*12);
                 }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    update_command(test_complete1);
                    dataTransfered = mConnection.bulkTransfer(mOutputEndpoint,command,command.length, 0);
                    update_command(test_complete2);
                    dataTransfered = mConnection.bulkTransfer(mOutputEndpoint,command,command.length, 0);
                    try{ Thread.sleep(300);}
                    catch (InterruptedException e){}
                    dataReadCount = mConnection.bulkTransfer(mInputEndpoint,byteArray_Rd,byteArray_Rd.length,0);

                    // Finishi Inspection
                    update_command(test_finish0);
                    dataTransfered = mConnection.bulkTransfer(mOutputEndpoint,command,command.length, 0);
                    update_command(test_finish1);
                    dataTransfered = mConnection.bulkTransfer(mOutputEndpoint,command,command.length, 0);
                    update_command(test_finish2);
                    dataTransfered = mConnection.bulkTransfer(mOutputEndpoint,command,command.length, 0);
                    try{ Thread.sleep(100);}
                    catch (InterruptedException e){}
                    dataReadCount = mConnection.bulkTransfer(mInputEndpoint,byteArray_Rd,byteArray_Rd.length,0);

                    update_command(test_complete1);
                    dataTransfered = mConnection.bulkTransfer(mOutputEndpoint,command,command.length, 0);
                    update_command(test_complete2);
                    dataTransfered = mConnection.bulkTransfer(mOutputEndpoint,command,command.length, 0);
                    try{ Thread.sleep(300);}
                    catch (InterruptedException e){}
                    dataReadCount = mConnection.bulkTransfer(mInputEndpoint,byteArray_Rd,byteArray_Rd.length,0);

                    TB_message.append("Inspection Finish. (Normal Mode Changed) \n");
                    PG_bar.setProgress(90);
//                int controlTransfer = mConnection.controlTransfer( UsbConstants.USB_DIR_OUT, 1,0,0,byteArray,byteArray.length,0);
//                Toast.makeText(this, "controlTransfer " +controlTransfer, Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "Could not connect!");
                    TB_message.append("Connection Fail \n");
//                    Toast.makeText(this, "Could not connect", Toast.LENGTH_SHORT).show();
                    TB_message.append("Could not Connect Device\n");
                }
            }
        }
        else{
            TB_message.append("Device is null\n");
//            temp_t = 0; temp_t1 = 0;
//            temp_s = ""; temp_s1 ="";
//            byte[] byteArray_Rd1 = new byte[2];
//            byteArray_Rd1[0]= (byte) 0x1F;
//            byteArray_Rd1[1]= (byte) 0xF0;
//
//            temp_t = byteArray_Rd1[0];
//            temp_t1 = byteArray_Rd1[1];
//            temp_s = Integer.toHexString(0xFF & temp_t);
//            temp_s1 = Integer.toHexString(0xFF & temp_t1);
//            if(temp_s1.length() == 1) temp_s1 = "0"+ temp_s1;
//            int val = Integer.parseInt(temp_s+ temp_s1,16);
//            if((val&0x8000)!=0 )
//            {
//                val = (val-65535)-1;
//                TB_message.append(Integer.toString(val) +"\n");
//            }
//            TB_message.append(temp_s+temp_s1 +"\n");
//            TB_message.append(Integer.toString(val) +"\n");

//            int test = 0;
//            int counttt =0;
//            for(int i= 0; i<5760; i++)
//            {
//                test = i%96;
//                if(test == 0 )
//                {
//                    if(i !=0)
//                    {
//                        counttt++;
//                    }
//                }
//                avg_open[counttt] = avg_open[counttt] + ar_raw[6][i];
//            }
//            for(int i=0; i<60; i++)
//            {
//                avg_open[i] = avg_open[i]/96;
//            }
//            for(int i=0; i<60; i++)
//            {
//                for(int j=0; j<96; j++)
//                {
//                    ar_raw[7][(i*96)+j] = ar_raw[6][(i*96)+j] - avg_open[i];
//                }
//            }
//            TB_message.append(counttt +"\n");
        }
    }

    // Drawing GRID Table
    public void drawing(int a)
    {
        bitmap = Bitmap.createBitmap(1480,1200,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
//        canvas.drawColor(Color.LTGRAY);
        Grid_View.setImageBitmap(bitmap);

        int r_width = 15;
        int r_height = 16;
        int left = 0;
        int top = 120;
        Paint fill_1 = new Paint();
        fill_1.setStyle(Paint.Style.FILL_AND_STROKE);

        fill_1.setColor(Color.CYAN);

        Paint fill_3 = new Paint();
        fill_3.setStyle(Paint.Style.FILL_AND_STROKE);
        fill_3.setColor(Color.RED);

        Paint fill_2 = new Paint();
        fill_2.setStyle(Paint.Style.STROKE);
        fill_2.setColor(Color.GRAY);

        Paint fill_4 = new Paint();
        fill_4.setStyle(Paint.Style.STROKE);
        fill_4.setColor(Color.BLUE);


        Paint txt_1 = new Paint();
        txt_1.setStyle(Paint.Style.FILL);
        txt_1.setColor(Color.BLACK);

        fill_1.setStrokeWidth(2);
        fill_2.setStrokeWidth(2);
        fill_3.setStrokeWidth(2);
        fill_4.setStrokeWidth(2);
        txt_1.setStrokeWidth(2);
        txt_1.setTextSize(20);
        Typeface myFont;

        if(a >= 1)
        {
//            TB_message.setBackgroundColor(Color.YELLOW);
        }

        int[] spec_min = new int[8];
        int[] spec_max = new int[8];
        for(int n=0; n<8; n++)
        {
            if(n == 0){spec_min[n] = raw_min;            spec_max[n] = raw_max;}
            else if(n==1){spec_min[n] = short_min;            spec_max[n] = short_max;}
            else if(n==2){spec_min[n] = jitter_min;            spec_max[n] = jitter_max;}
            else if(n==3){spec_min[n] = abs_min;            spec_max[n] = abs_max;}
            else if(n==4){spec_min[n] = p2p_min;            spec_max[n] = p2p_max;}
            else if(n==5){spec_min[n] = mux_min;            spec_max[n] = mux_max;}
            else if(n==6){spec_min[n] = open_min;            spec_max[n] = open_max;}
            else if(n==7){spec_min[n] = open_min;            spec_max[n] = open_max;}
            else    {spec_min[n] = raw_min;            spec_max[n] = raw_max;}
        }


       // for(int t_num =0; t_num<7; t_num++)
       // {
            for (int j = 0; j < 60; j++)
            {
                for (int i = 0; i < 96; i++)
                {
                    if (a == 10) {
                        canvas.drawRect(left + (i * r_width), top + (j * r_height), r_width + (i * r_width), top + (j * r_height) + r_height, fill_2);
                    } else
                    {
                        if ((ar_raw[a][(j * 96) + (95 - i)] < spec_min[a]) |(ar_raw[a][(j * 96) + (95 - i)] > spec_max[a]))        //LR Reverse
                        {
                            result = true;
                            b_define[(j * 96) + (95 - i)] = true;
                            canvas.drawRect(left + (i * r_width), top + (j * r_height), r_width + (i * r_width), top + (j * r_height) + r_height, fill_3);
                        }
//                        else if (ar_raw[t_num][(j * 96) + (95 - i)] > spec_max[t_num])
//                        {
//                            canvas.drawRect(left + (i * r_width), top + (j * r_height), r_width + (i * r_width), top + (j * r_height) + r_height, fill_3);
//                        }
                        else
                        {
                            if(b_define[(j * 96) + (95 - i)] == false)   canvas.drawRect(left + (i * r_width), top + (j * r_height), r_width + (i * r_width), top + (j * r_height) + r_height, fill_2);
                        }
                    }

                }
            }


       // }
        //Drawing Tap Line
        canvas.drawLine(0,top+(29*r_height)+r_height,r_width+(r_width*95),top+(29*r_height)+r_height,fill_4);
        canvas.drawLine(0,top+(59*r_height)+r_height,r_width+(r_width*95),top+(59*r_height)+r_height,fill_4);
        canvas.drawLine(0,top,r_width+(r_width*95),top,fill_4);

        for(int i=0; i<13; i++)
        {
            canvas.drawLine(i*(r_width*8), top, i*(r_width*8), top + (59 * r_height) + r_height, fill_4);
            String num = Integer.toString(i+1);//Data Change
            if(i!=12)            canvas.drawText(num,i*(r_width*8)+(r_width*3),100, txt_1);
        }
    }

    public void onClickButtonStart(View v)
    {
        tb_init();
        //Thread Using for Data get and CSV
        ExampleThread thread = new ExampleThread();
        thread.start();

    }

    // App Terminate
    public void onClickButtonExit(View v)
    {
        finish();
    }

    private class ExampleThread extends Thread {
        private static final String TAG = "ExampleThread";

        public ExampleThread() {
            // 초기화 작업
        }
        public void run() {
            spec_update();
            connectUsb(mDeviceFound);
            drawing(0);
            csv_writing();
        }
    }

}