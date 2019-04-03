package akvindian.mhelper;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class BGDetector extends Service {

    Timer timer;
    TimerTask timerTask;
    int counter=580;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("Done","IBinder");
        return null;
    }

    public BGDetector(){
        super();
        Log.i("Done","Constructor");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("Done","Created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.i("Done","onStartCommand");
        createService(this);
        //stoptimer();
        //startTimer();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        Log.i("Done","StopService");
        startService();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        startService();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        startService();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        startService();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        startService();
    }

    public void startService(){
        Intent alarmIntent = new Intent(getApplicationContext(), StartService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 133, alarmIntent, 0);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 0);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
    }

    public void createService(Context context){

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("TextHelper Services")
                .setTicker("TextHelper")
                .setContentText("are running in Background !!!")
                .setSmallIcon(R.drawable.my_ic_launcher)
                .setContentIntent(pendingIntent).build();
        startForeground(101,
                notification);



        Toast.makeText(getApplicationContext(), "Text Helper Service is Running !!!", Toast.LENGTH_SHORT).show();

    }

    public void startTimer(){
        timer = new Timer();
        initializeTimer();
        timer.schedule(timerTask, 1000, 1000);
    }

    public void initializeTimer(){
        timerTask = new TimerTask() {
            @Override
            public void run() {

                counter++;

                Log.i("Done",(counter%1000)+"");
            }
        };
    }

    public void stoptimer(){
        if(timer!=null){
            timer.cancel();
            timer=null;
        }
    }
}
