package com.creativeapps.schoolbusdriver.data;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

import com.creativeapps.schoolbusdriver.ui.activity.main.MainActivity;
import com.google.gson.Gson;

/*Some utility functions used by many classes in the application*/
public class Util {

    public static final String WEB_SERVER_URL = "YOUR_WEB_SERVER_URL";


    public static final int CHECK_IN_FLAG = 3;
    public static final int CHECK_OUT_FLAG = 4;
    /*A function to serialize an object using json and save it to SharedPreference*/
    public static void saveObjectToSharedPreference(Context context, String preferenceFileName,
                                                    String serializedObjectKey, Object object) {
        //get the SharedPreference from context
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFileName, 0);
        //start the SharedPreference editor
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        //serialize the object to json
        final Gson gson = new Gson();
        String serializedObject = gson.toJson(object);
        //save the serialized object with the provided key
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject);
        //apply changes to the SharedPreference editor
        sharedPreferencesEditor.apply();
    }

    /*A function to read an object that is represented as json from SharedPreference and deserialize it*/
    public static <GenericClass> GenericClass getSavedObjectFromPreference(Context context,
                                                                           String preferenceFileName,
                                                                           String preferenceKey,
                                                                           Class<GenericClass> classType) {
        //get the SharedPreference from context
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFileName, 0);
        //check if the SharedPreference contains the provided key
        if (sharedPreferences.contains(preferenceKey)) {
            //read the object in json format and deserialize it
            final Gson gson = new Gson();
            return gson.fromJson(sharedPreferences.getString(preferenceKey, ""), classType);
        }
        //object with provided key not found
        return null;
    }

    /*go to an activity*/
    public static void redirectToActivity(AppCompatActivity currentActivity, Class NextActivityClass) {
        Intent intent = new Intent(currentActivity, NextActivityClass);
        currentActivity.startActivity(intent);
    }

    /*display a message with ok button and optional app exit if the user presses the ok button*/
    public static void displayExitMessage(String message, final Activity current, final boolean exitWithOk)
    {
        // Create the object of AlertDialog Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(current);

        // Set the message show for the Alert
        builder.setMessage(message);

        // Set Alert Title
        builder.setTitle("Alert !");

        // Set Cancelable false so when the user clicks on the outside the Dialog Box,
        // it will remain visible
        builder.setCancelable(false);

        // Set the positive button with ok name and set OnClickListener method (defined
        // in DialogInterface interface).

        builder.setPositiveButton(
                "Ok",
                new DialogInterface
                        .OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which)
                    {
                        if(exitWithOk)
                        {
                            // When the user click yes button, then app will close
                            current.finishAffinity();
                        }
                    }
                });

        // Create the Alert dialog
        AlertDialog alertDialog = builder.create();

        // Show the Alert Dialog box
        alertDialog.show();
    }
}
