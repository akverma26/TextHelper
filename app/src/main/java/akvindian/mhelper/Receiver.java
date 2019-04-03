package akvindian.mhelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("done","Message Recieved");

        Intent check = checkSMS(intent, context);

        if(check!=null){

            Toast.makeText(context,"Message Received", Toast.LENGTH_SHORT).show();

            Log.i("done","Enter1");

            PackageManager pm = context.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage("akvindian.mhelper");
            launchIntent.putExtra("sendTo",check.getStringExtra("sender"));
            launchIntent.putExtra("message",check.getStringExtra("message"));
            launchIntent.putExtra("pass_length",check.getIntExtra("pass_length",0));
            context.startActivity(launchIntent);
        }
    }

    public Intent checkSMS(Intent intent, Context context){

        Log.i("done","Enter0");

        Bundle bundle = intent.getExtras();
        SmsMessage[] smsm = null;
        String sms_str ="";

        if (bundle != null) {

            // Get the SMS message
            Object[] pdus = (Object[]) bundle.get("pdus");
            smsm = new SmsMessage[pdus.length];

            for (int i=0; i<smsm.length; i++){

                smsm[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                sms_str += "";
                sms_str += smsm[i].getMessageBody().toString();

                String Sender = smsm[i].getOriginatingAddress();
                Intent smsIntent = new Intent("otp");
                smsIntent.putExtra("message",sms_str);
                smsIntent.putExtra("sender",Sender);

                String pass="";

                SetPassKey setPassKey = new SetPassKey();
                pass = setPassKey.RetrievePassKey(context);

                smsIntent.putExtra("pass_length",pass.length());

                if(sms_str.contains("Help "+pass)) return smsIntent;

                Log.i("done","Enter2");
            }
        }

        return null;
    }

}
