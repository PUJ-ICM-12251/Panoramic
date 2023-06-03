package com.example.panoramic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.panoramic.R;
import com.example.panoramic.model.Badge;
import com.example.panoramic.paths.DatabasePaths;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class BadgesAdapter extends ArrayAdapter<Badge> {
    public BadgesAdapter(Context context, ArrayList<Badge> badge) {
        super(context, 0, badge);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Grab the badge to render
        Badge badge = getItem(position);
        // Check if amn existing view is being reused, otherwise inflate the view
        if (convertView == null) {
//            convertView = binding.getRoot();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.badge_adapter_layout, parent, false);
        }
        StorageReference mStorage = FirebaseStorage.getInstance().getReference();
        String storage_path = DatabasePaths.BADGE + badge.id;
        StorageReference imageRef = mStorage.child(storage_path);
        ImageView profileImage = convertView.findViewById(R.id.badgeImage);
        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(getContext())
                    .load(uri)
                    .into(profileImage);
        });
        // Get all the fields from the adapter
        TextView name = convertView.findViewById(R.id.badgeName);
        // Format and set the values in the view
        name.setText(badge.name);
        return convertView;
    }
}
