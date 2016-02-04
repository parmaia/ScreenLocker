package test.sample.com.locker;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener {
    Intent svcIntent;
    private Button lock;
    private Button disable;
    private Button enable;
    SharedPreferences pref;
    DevicePolicyManager deviceManger;
    ActivityManager activityManager;
    ComponentName compName;
    static final int RESULT_ENABLE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = getSharedPreferences("lock_pref", Context.MODE_MULTI_PROCESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        svcIntent = new Intent(this, LockerService.class);
        findViewById(R.id.startService).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(svcIntent);
                finish();
            }
        });
        findViewById(R.id.stopService).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(svcIntent);
            }
        });
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.uninstall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uninstall();
            }
        });
        deviceManger = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);

//        findViewById(R.id.stopService).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                enable();
//            }
//        }, 1000);
        lock =(Button)findViewById(R.id.lock);
        lock.setOnClickListener(this);
        disable = (Button)findViewById(R.id.disable);
        enable =(Button)findViewById(R.id.enable);
        disable.setOnClickListener(this);
        enable.setOnClickListener(this);
        boolean isAdmin = deviceManger.isAdminActive(compName);
        if(pref.getBoolean("first_time", true) || !isAdmin){
            showDialog();
        }else{
            startService(svcIntent);
        }
    }

    private void uninstall() {
        disable();
        Uri packageURI = Uri.parse("package:"+getApplicationContext().getPackageName());
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        startActivity(uninstallIntent);
    }

    private void showDialog(){
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setTitle("Primera ejecución");
        bld.setMessage("Esta aplicación necesita permisos de administrador para poder funcionar. " +
                "Una vez otorgados la aplicacion no podra desinstalarse desde el administrador " +
                "de aplicaciones. Si quiere desinstalarla, utilice el boton \"Desinstalar\"\n\n" +
                "Quiere habilitar los permisos de administrador ahora?");
        bld.setPositiveButton("Habilitar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                enable();
                SharedPreferences pref = getSharedPreferences("lock_pref", Context.MODE_MULTI_PROCESS);
                pref.edit().putBoolean("first_time", false).commit();
            }
        });
        bld.setNegativeButton("Ahora no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        bld.setCancelable(false);
        Dialog d = bld.create();
        d.setCanceledOnTouchOutside(false);
        d.show();
    }

    @Override
    public void onClick(View v) {

        if(v == lock){
            lock();
        }

        if(v == enable){
            enable();
        }

        if(v == disable){
            disable();
        }
    }

    private void lock(){
        boolean active = deviceManger.isAdminActive(compName);
        if (active) {
            deviceManger.lockNow();
            disable();
        }else{
            enable();
        }
    }

    private void enable(){
        Log.d("Locker", "enable");
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why this needs to be added.");
        startActivityForResult(intent, RESULT_ENABLE);
    }

    private void disable() {
        deviceManger.removeActiveAdmin(compName);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i("DeviceAdminSample", "Admin enabled!");
                    startService(svcIntent);
                } else {
                    Log.i("DeviceAdminSample", "Admin enable FAILED!");
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
