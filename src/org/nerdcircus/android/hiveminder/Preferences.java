package org.nerdcircus.android.hiveminder;

import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        final PreferenceActivity pa = this;

        Preference logout = this.findPreference("logout");
        logout.setOnPreferenceClickListener( 
            new Preference.OnPreferenceClickListener(){
                public boolean onPreferenceClick(Preference p){
                    new HmClient(pa).clearSidCookie();
                    return true;
                }
            });
    }

}

