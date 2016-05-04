package com.ricky.screensaver;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ricky.screensaver.service.ScreenSaverService;
import com.ricky.screensaver.util.ContentUriUtil;
import com.ricky.screensaver.util.MyUtil;

public class MainActivity extends Activity {

    /**
     * 示例
     */
    private static final int FILE_CHOOSER_CODE = 2001;
    public static final String SP_NAME = "SSConfig";
    public static final String SPKEY_TIME = "ss_period";
    public static final String SPKEY_URI = "ss_uri";

    private Button btnStartService, btnChooseFile;
    private Spinner spinner;
    private TextView txtPath;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    private int[] timePeriod = { 10, 30, 60, 120, 180, 240, 300 };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSharedPreferences();
        initView();
        setEvent();
    }

    private void initSharedPreferences() {
        mSharedPreferences = getSharedPreferences(SP_NAME, 0);
        mEditor = mSharedPreferences.edit();
    }

    //将屏保时间记入缓存中
    private void configTime(int period) {
        if (mSharedPreferences != null) {
            mEditor.putInt(SPKEY_TIME, period);
            mEditor.commit();
        }
    }

    //将选择的文件uri存入缓存中
    private void configUri(String uriStr) {
        if (mSharedPreferences != null) {
            mEditor.putString(SPKEY_URI, uriStr);
            mEditor.commit();
        }
    }

    //初始化视图
    private void initView() {

        txtPath = (TextView) findViewById(R.id.txt_ss_path);
        btnChooseFile = (Button) findViewById(R.id.btn_file_chooser);
        btnStartService = (Button) findViewById(R.id.start_service);
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.screen_saver_period,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    //初始化事件监听
    private void setEvent() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                // TODO Auto-generated method stub
                ScreenSaverService.setScreenSaverTimePeriod(timePeriod[position]);
                configTime(timePeriod[position]);
                Log.e("period time", timePeriod[position] + "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        btnStartService.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (txtPath.getText().toString().trim().equalsIgnoreCase("")) {
                    Toast.makeText(MainActivity.this, "请选择屏保文件",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (!MyUtil.isServiceRunning(MainActivity.this,
                        "com.ricky.screensaver.service.ScreenSaverService")) {
                    Intent ssIntent = new Intent(MainActivity.this,
                            ScreenSaverService.class);
                    startService(ssIntent);
                }
                finish();
            }
        });

        btnChooseFile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent in = new Intent(Intent.ACTION_GET_CONTENT);
                in.addCategory(Intent.CATEGORY_OPENABLE);
                in.setType("*/*");
                Intent wrapIn = Intent.createChooser(in, "请选择屏保文件");
                startActivityForResult(wrapIn, FILE_CHOOSER_CODE);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == FILE_CHOOSER_CODE && resultCode == RESULT_OK) {
            Uri myUri = data.getData();
            String uriStr = myUri.toString();
            if(uriStr.startsWith("content:/")){
                uriStr = ContentUriUtil.getPath(MainActivity.this, myUri);
            }
            txtPath.setText(uriStr);
            configUri(uriStr);
            Log.e("Uri", uriStr);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
