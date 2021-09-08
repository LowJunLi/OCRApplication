package com.example.ocrapplication;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.util.Locale;

//this is a class that contains only static member and function to help in managing application locale,
//this class should not be instantiate
public class LanguageManager
{

    private LanguageManager() //private constructor to avoid instantiation
    {

    }

    /**
     * Set locale for the entire application
     *
     * @param fragment     the fragment which is going to change the locale
     * @param languageCode the language code selected by the user
     */
    public static void setLocale(@NonNull Fragment fragment, String languageCode)
    {
        if(fragment.getActivity()!= null)
        {
            // refer to https://stackoverflow.com/questions/4985805/set-locale-programmatically
            Locale locale = new Locale(languageCode);
            Locale.setDefault(locale);
            Configuration config = fragment.getActivity().getBaseContext().getResources().getConfiguration();
            config.setLocale(locale);
            fragment.getActivity().getBaseContext().getResources().updateConfiguration(config,
                    fragment.getActivity().getBaseContext().getResources().getDisplayMetrics());
        }
    }

    /**
     * Set locale for the entire application
     *
     * @param activity     the activity which is going to change the locale
     * @param languageCode the language code selected by the user
     */
    public static void setLocale(@NonNull Activity activity, String languageCode)
    {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = activity.getBaseContext().getResources().getConfiguration();
        config.setLocale(locale);
        activity.getBaseContext().getResources().updateConfiguration(config,
                activity.getBaseContext().getResources().getDisplayMetrics());
    }

    /**
     * If the locale has changed, recreate the activity to reflect locale changed
     *
     * @param activity  the activity that may need to be recreate to reflect locale changed
     */
    public static void recreateIfLocaleChanged(Activity activity)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        String settingLocale = pref.getString("language", "en-US");
        Locale currentLocale = activity.getResources().getConfiguration().locale;
        if(!settingLocale.equals(currentLocale)) //locale changed
        {
            activity.finish();
            activity.recreate();
        }

    }
}
