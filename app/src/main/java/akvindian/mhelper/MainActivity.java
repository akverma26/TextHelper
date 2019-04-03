package akvindian.mhelper;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity {

    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    TextView logo;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ReplaceNavigationFragmentView(new Home(),"TextHelper");

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        logo = findViewById(R.id.logo);

        intent = getIntent();

        checkAndRequestPermissions();

        if(!isMyServiceRunning(BGDetector.class)){
            startService(new Intent(this, BGDetector.class));
        }

        String sender = intent.getStringExtra("sendTo");
        if(sender!=null){

            String msg = intent.getStringExtra("message");
            int len = intent.getIntExtra("pass_length",0);

            String task = "";
            int i=0;
            if(len==0){
                i=5;
                while(i<msg.length() && msg.charAt(i)!=' '){
                    task+=msg.charAt(i);
                    i++;
                }
                i++;
            }
            else{
                i=6+len;
                while(i<msg.length() && msg.charAt(i)!=' '){
                    task+=msg.charAt(i);
                    i++;
                }
                i++;
            }

            Toast.makeText(MainActivity.this, task, Toast.LENGTH_SHORT).show();

            switch (task){
                case "1":
                    AudioManager am;
                    am= (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
                    am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    am.setStreamVolume(AudioManager.STREAM_RING,am.getStreamMaxVolume(AudioManager.STREAM_RING),0);
                    break;
                case "2":
                    String contact="";
                    if(i<msg.length())
                        contact = msg.substring(i,msg.length());

                    Toast.makeText(this, contact, Toast.LENGTH_LONG).show();

                    contact = get_Number(contact,this);
                    sendSMS(sender,this,contact);
                    break;

                default:
                        break;
            }

            Toast.makeText(this, "Beneficiary: "+sender,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {

        Log.i("Done","DestroyApp");
        ServiceRestart();
        super.onDestroy();
    }

    @Override
    protected void onResume() {

        Log.i("Done","Resumed");
        ServiceRestart();
        //LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("otp"));
        super.onResume();
    }

    @Override
    protected void onRestart() {

        ServiceRestart();
        super.onRestart();
    }

    @Override
    protected void onPause() {

        Log.i("Done","Paused");
        ServiceRestart();
        super.onPause();
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    public void NavigationFragmentation(View view){

        Fragment fragment = null;
        String title = "";

        switch (view.getId()){
            case R.id.home:
                fragment= new Home();
                title="TextHelper";
                break;
            case R.id.setPassKeyMenu:
                fragment = new SetPassKey();
                title="Set Pass Key";
                break;
            case R.id.about:
                fragment = new About();
                title="About this App";
                break;
            default:
                return ;
        }

        ReplaceNavigationFragmentView(fragment, title);
    }

    public void ReplaceNavigationFragmentView(Fragment fragment, String title){
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        setTitle(title);
        try {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setPass(View view){

        View view1 = getLayoutInflater().inflate(R.layout.set_passkey_dialoge,null);
        Button cancel = view1.findViewById(R.id.cancel);
        Button set =  view1.findViewById(R.id.setPassKey);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(view1);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        alertDialog.show();

        SetPassKey setPassKey = new SetPassKey();
        setPassKey.SetPassKey(MainActivity.this, set, cancel, view1, alertDialog);
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Done Running?", true+"");
                return true;
            }
        }

        Log.i ("Done Running?", false+"");
        return false;
    }

    public boolean checkAndRequestPermissions() {

        int permissionSendMessage = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);

        int receiveSMS = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);

        int readSMS = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);

        int phoneStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (receiveSMS != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECEIVE_MMS);
        }

        if (readSMS != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_SMS);
        }

        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.SEND_SMS);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
            return false;
        }
        return true;
    }

    public void ServiceRestart(){

        startService(new Intent(getApplicationContext(), StartService.class));

        Intent alarmIntent = new Intent(getApplicationContext(), BGDetector.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 133, alarmIntent, 0);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 0);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
    }

    public void sendSMS(String sender, Context context, String contact) {

        Log.i("done","Enter4");

        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> parts = smsManager.divideMessage(contact);
        for(int i=0;i<parts.size();i++){
            try {

                smsManager.sendTextMessage(sender, null, String.valueOf(parts.get(i)), null, null);

                Toast.makeText(context, "Message Sent", Toast.LENGTH_SHORT).show();
            }
            catch (Exception ex) {
                Toast.makeText(context,ex.getMessage().toString(), Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }

        }

    }

    public String get_Number(String name,Context context) {

        Log.i("done","Enter3");

        if(name=="") return "Please Enter a Name";

        name = name.toLowerCase();

        String number="";

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor people = context.getContentResolver().query(uri, projection, null, null, null);

        int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        people.moveToFirst();
        do {
            String Name   = people.getString(indexName);
            String Number = people.getString(indexNumber);

            if(Name.toLowerCase().contains(name)){
                Number = Number.replace("(","");
                Number = Number.replace(")","");
                Number = Number.replace("-","");
                Number = Number.replace(" ","");
                number+=Name;
                number+=" ";
                number+=Number;
                number+="\n";
            }
        } while (people.moveToNext());

        if(number=="") return "No Contact Found !!!";
        return number;
    }
}
