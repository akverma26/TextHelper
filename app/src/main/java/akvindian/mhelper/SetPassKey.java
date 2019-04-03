package akvindian.mhelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;

public class SetPassKey extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.setpasskey, container, false);

        return rootView;
    }

    public void SetPassKey( final Context context, Button set, Button cancel, final View view1, final AlertDialog alertDialog){

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Toast.makeText(context, RetrievePassKey(context), Toast.LENGTH_SHORT).show();
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                alertDialog.hide();
            }
        });

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText editText = view1.findViewById(R.id.passEdit);
                String passkey_string = editText.getText().toString();


                if(passkey_string.length()>0){

                    try {
                        SharedPreferences sharedPreferences = context.getSharedPreferences("akvindian.mhelper", Context.MODE_PRIVATE);
                        sharedPreferences.edit().putString("PassKey", passkey_string).apply();
                        Toast.makeText(context,"PassKey is Set", Toast.LENGTH_LONG).show();
                        alertDialog.hide();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(context, "Please Enter Apprpriate PassKey", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public String RetrievePassKey(Context context){

        String passkey_string = "";

        try{

            SharedPreferences sharedPreferences = context.getSharedPreferences("akvindian.mhelper", MODE_PRIVATE);
            passkey_string = sharedPreferences.getString("PassKey","");
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return passkey_string;
    }
}
