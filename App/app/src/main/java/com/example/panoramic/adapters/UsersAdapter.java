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
import com.example.panoramic.domain.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class UsersAdapter extends ArrayAdapter<User> {

    public UsersAdapter(Context context, ArrayList<User> people) {
        super(context, 0, people);
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Grab the person to render
        User person = getItem(position);
        // Check if amn existing view is being reused, otherwise inflate the view
        if (convertView == null) {
//            convertView = binding.getRoot();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_adapter_layout, parent, false);
        }
        StorageReference mStorage = FirebaseStorage.getInstance().getReference();
        String storage_path = "fotos/" + person.getNumID();
        StorageReference imageRef = mStorage.child(storage_path);
        ImageView profileImage = convertView.findViewById(R.id.peopleImage);
        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(getContext())
                    .load(uri)
                    .into(profileImage);
        });
        // Get all the fields from the adapter
        TextView firstName = convertView.findViewById(R.id.peopleFirstName);
        TextView lastName = convertView.findViewById(R.id.peopleLastName);
        TextView address = convertView.findViewById(R.id.peopleAddress);
        // Format and set the values in the view
        firstName.setText(person.getName());
        lastName.setText(person.getLastName());
        address.setText(String.format("%S , %S", person.getLatitude(), person.getLongitude()));
        return convertView;
    }
}

