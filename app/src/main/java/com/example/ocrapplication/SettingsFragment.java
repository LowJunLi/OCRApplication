package com.example.ocrapplication;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import java.util.Locale;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public final static String KEY_PREF_LANGUAGE = "language";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.settings, rootKey);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals(KEY_PREF_LANGUAGE))
        {
            String languageCode =
                    sharedPreferences.getString(KEY_PREF_LANGUAGE, "en-US");
            LanguageManager.setLocale(this, languageCode);
            if(getActivity()!=null)
            {
                getActivity().recreate(); //recreate activity to see language changed
            }
        }

    }

    @Override
    public void onStop()
    {
        super.onStop();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}