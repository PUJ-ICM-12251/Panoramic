package com.example.panoramic.services;

import android.content.Context;
import android.util.Log;

import com.example.panoramic.model.NightClub;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import lombok.Getter;

@Module
@InstallIn(ActivityComponent.class)
public class ClubService {
    public static final String TAG = ClubService.class.getName();
    public static final String CLUBS_FILE = "nightclubs.json";
    private final Context context;
    @Getter
    private static ArrayList<NightClub> clubs = new ArrayList<>();

    @Inject
    public ClubService(@ApplicationContext Context context) {
        this.context = context;
        loadClubsFromJson();
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = context.getAssets().open(CLUBS_FILE);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Log.e(TAG, String.format("loadJSONFromAsset: error reading the %s file.", CLUBS_FILE), ex);
            return null;
        }
        return json;
    }

    public void loadClubsFromJson() {
        JsonObject mainFile = new Gson().fromJson(loadJSONFromAsset(), JsonObject.class);
        clubs = new Gson().fromJson(mainFile.getAsJsonArray("Nightclubs").toString(), new TypeToken<List<NightClub>>() {
        }.getType());
        Log.d(TAG, String.format("loadCountriesByJson: loaded %d countries.", clubs.size()));
    }

    public static ArrayList<NightClub> getClubs() {
        return clubs;
    }
}
