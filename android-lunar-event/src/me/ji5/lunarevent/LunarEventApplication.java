package me.ji5.lunarevent;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;

import me.ji5.utils.Log;

/**
 * Created by ohjongin on 14. 1. 20.
 */
public class LunarEventApplication extends Application {
    private static final String APPLICATION_ID = "Zb6D1g3x6pNfRpKL19U1SrGPwJcEdiYppg0qQjUS";
    private static final String CLIENT_KEY = "G6a8ls3klNOaqEPTYRJpMm7tZ9ZPUQTOeXXnNpUA";

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, APPLICATION_ID, CLIENT_KEY);


        ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
        defaultACL.setPublicReadAccess(false);
        ParseACL.setDefaultACL(defaultACL, true);

        Log.setLogTag(getPackageName());
        Log.setDebugMode(true);
    }
}
