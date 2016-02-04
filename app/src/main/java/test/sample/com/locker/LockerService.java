package test.sample.com.locker;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by Rubèn on 1/02/16.
 */
public class LockerService extends Service {
    private WindowManager windowManager;
    private ImageView button;
    WindowManager.LayoutParams params;
    private boolean consumeTouch;
    ComponentName compName;
    DevicePolicyManager deviceManger;

    @Override
    public void onCreate() {
        super.onCreate();
        compName = new ComponentName(this, MyAdmin.class);
        deviceManger = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        button = new ImageView(this);
        button.setImageResource(R.drawable.lock_button);

        params= new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;
        consumeTouch = false;

        //this code is for dragging the chat head
        button.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return false;
                    case MotionEvent.ACTION_UP:
                        boolean val = consumeTouch;
                        consumeTouch = false;
                        return val;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (event.getRawX() - initialTouchX);
                        int dy = (int) (event.getRawY() - initialTouchY);
                        if (dx > 20 || dy > 20 || dx < -20 || dy < -20) {
                            params.x = initialX + dx;
                            params.y = initialY + dy;
                            windowManager.updateViewLayout(button, params);
                            consumeTouch = true;
                            return true;
                        } else {
                            consumeTouch = false;
                            return false;
                        }

                }
                return false;
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Locker", "Button click");
                lock();
            }
        });

        windowManager.addView(button, params);
    }

    private void lock(){
        boolean active = deviceManger.isAdminActive(compName);
        if (active) {
            deviceManger.lockNow();
        }else{
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (button != null)
            windowManager.removeView(button);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
