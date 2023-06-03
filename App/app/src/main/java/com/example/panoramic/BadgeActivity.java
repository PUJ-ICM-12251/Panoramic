package com.example.panoramic;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.panoramic.databinding.ActivityBadgeBinding;
import com.example.panoramic.model.Badge;
import com.example.panoramic.paths.DatabasePaths;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class BadgeActivity extends AppCompatActivity {


    private Badge badge;


    private ActivityBadgeBinding binding;


    private TextView badgeTitle;

    private ImageView badgeQR;


    private TextView badgeName;

    private TextView badgeLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Binding
        binding = ActivityBadgeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        badge = (Badge) getIntent().getSerializableExtra("badge");

        badgeQR = binding.imageViewQR;
        badgeName = binding.textViewBadgeName;
        badgeLink = binding.textViewBadgeLink;

        badgeTitle = binding.textTitle;
        badgeTitle.setText(badge.name);

        StorageReference mStorage = FirebaseStorage.getInstance().getReference();
        String storage_path = DatabasePaths.QR + badge.id + ".png";
        StorageReference imageRef = mStorage.child(storage_path);

        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(view.getContext())
                    .load(uri)
                    .into(badgeQR);
        });
        badgeName.setText(badge.emoji);
        String text = String.format("<a href=\"%s\">%s</a> ", badge.url, badge.name);
        badgeLink.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
        badgeLink.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
    }


}