package com.example.linkup.CommunityOperation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.example.linkup.ProfileOperation.SettingActivity;
import com.example.linkup.ProfileOperation.UpdateProfile;
import com.example.linkup.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CommunityMenu extends BottomSheetDialogFragment {

    CardView btnMyArticles, btnSavedArticles;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.community_bottom_menu,null);
        btnMyArticles = view.findViewById(R.id.btnMyArticles );
        btnSavedArticles = view.findViewById(R.id.btnSavedArticles);

        btnMyArticles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUI("MyArticles");
            }
        });

        btnSavedArticles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUI("savedArticles");
            }
        });
        // [END layout component function]
        // this line must be finalized
        return view;
    }

    // [START Method]
    // handling UI update
    private void updateUI(String screen) {
        Intent intent = null;
        if (screen.equals("MyArticles")) {
            intent = new Intent(getContext(), MyArticlesActivity.class);
        } else if (screen.equals("savedArticles")) {
            intent = new Intent(getContext(), SavedArticlesActivity.class);
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
    // [END Method]
}
