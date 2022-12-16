package org.techtown.touch_tool;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SubActivity extends MainActivity {

    EditText etb_raw_min;EditText etb_jitter_min;EditText etb_short_min;EditText etb_abs_min;
    EditText etb_p2p_min;EditText etb_mux_min;EditText etb_open_min;
    EditText etb_raw_max;EditText etb_jitter_max;EditText etb_short_max;EditText etb_abs_max;
    EditText etb_p2p_max;EditText etb_mux_max;EditText etb_open_max; EditText etd_fw;
    EditText etb_delay;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        etb_raw_min = findViewById(R.id.edt_rawmin);    etb_raw_max = findViewById(R.id.edt_rawmax);
        etb_jitter_min = findViewById(R.id.edt_jittermin);    etb_jitter_max = findViewById(R.id.edt_jittermax);
        etb_short_min = findViewById(R.id.edt_shortrmin);    etb_short_max = findViewById(R.id.edt_shortmax);
        etb_abs_min = findViewById(R.id.edt_absmin);    etb_abs_max = findViewById(R.id.edt_absmax);
        etb_p2p_min = findViewById(R.id.edt_p2pmin);    etb_p2p_max = findViewById(R.id.edt_p2pmax);
        etb_mux_min = findViewById(R.id.edt_muxmin);    etb_mux_max = findViewById(R.id.edt_muxmax);
        etb_open_min = findViewById(R.id.edt_openmin);    etb_open_max = findViewById(R.id.edt_openmax);
        etd_fw = findViewById(R.id.edt_fw);                 etb_delay = findViewById(R.id.edt_Delay);
        
        //initialize file 생성 및 Loading함수 연동
        String path = "/storage/emulated/0/Download";
        File myDir = new File(path + "/myApps/spec.ini");
        BufferedReader bw = null;

            //Read Ini file
            try
            {
                bw = new BufferedReader(new FileReader(myDir.getPath()));
                String [] line = new String[17];
                int n=0;
                while((line[n] = bw.readLine()) != null)
                {
                    n++;
                }
                if(line[15] != null)
                {
                    etd_fw.setText(line[0]);
                    etb_raw_min.setText(line[1]);   etb_raw_max.setText(line[2]);
                    etb_jitter_min.setText(line[3]);    etb_jitter_max.setText(line[4]);
                    etb_short_min.setText(line[5]);    etb_short_max.setText(line[6]);
                    etb_abs_min.setText(line[7]);    etb_abs_max.setText(line[8]);
                    etb_p2p_min.setText(line[9]);    etb_p2p_max.setText(line[10]);
                    etb_mux_min.setText(line[11]);    etb_mux_max.setText(line[12]);
                    etb_open_min.setText(line[13]);    etb_open_max.setText(line[14]);
                    etb_delay.setText(line[15]);
                }
                else
                {
                    Toast.makeText(this,"INI File is not setted!!!!", Toast.LENGTH_SHORT).show();
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

    public void onClickSave(View v)
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("raw_min",etb_raw_min.getText().toString()); //Loading을 위한 name, Loading Data
        intent.putExtra("raw_max",etb_raw_max.getText().toString());
        intent.putExtra("jitter_min",etb_jitter_min.getText().toString());
        intent.putExtra("jitter_max",etb_jitter_max.getText().toString());
        intent.putExtra("short_min",etb_short_min.getText().toString());
        intent.putExtra("short_max",etb_short_max.getText().toString());
        intent.putExtra("abs_min",etb_abs_min.getText().toString());
        intent.putExtra("abs_max",etb_abs_max.getText().toString());
        intent.putExtra("p2p_min",etb_p2p_min.getText().toString());
        intent.putExtra("p2p_max",etb_p2p_max.getText().toString());
        intent.putExtra("mux_min",etb_mux_min.getText().toString());
        intent.putExtra("mux_max",etb_mux_max.getText().toString());
        intent.putExtra("open_min",etb_open_min.getText().toString());
        intent.putExtra("open_max",etb_open_max.getText().toString());
        intent.putExtra("fw", etd_fw.getText().toString());
        intent.putExtra("delay", etb_delay.getText().toString());
//        startActivity(intent);
        Context c = v.getContext();
        Vibrator vib = ( Vibrator ) c.getSystemService( Context.VIBRATOR_SERVICE);

        String path = "/storage/emulated/0/Download";
        File myDir = new File(path + "/myApps/spec.ini");
        BufferedWriter bw = null;
        try
        {
            bw = new BufferedWriter(new FileWriter(myDir.getPath(), false));
            bw.write(etd_fw.getText().toString()+"\n");
            bw.write(etb_raw_min.getText().toString()+"\n");bw.write(etb_raw_max.getText().toString()+"\n");
            bw.write(etb_jitter_min.getText().toString()+"\n");bw.write(etb_jitter_max.getText().toString()+"\n");
            bw.write(etb_short_min.getText().toString()+"\n");bw.write(etb_short_max.getText().toString()+"\n");
            bw.write(etb_abs_min.getText().toString()+"\n");bw.write(etb_abs_max.getText().toString()+"\n");
            bw.write(etb_p2p_min.getText().toString()+"\n");bw.write(etb_p2p_max.getText().toString()+"\n");
            bw.write(etb_mux_min.getText().toString()+"\n");bw.write(etb_mux_max.getText().toString()+"\n");
            bw.write(etb_open_min.getText().toString()+"\n");bw.write(etb_open_max.getText().toString()+"\n");
            bw.write(etb_delay.getText().toString()+"\n");
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        vib.vibrate(300);
        finish();
    }
    public void onClickLoad(View v)
    {

    }
    public void onClickBack(View v)
    {
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
        finish();
    }
}
