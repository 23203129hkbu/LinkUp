package com.example.linkup.CommunityOperation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.example.linkup.Object.Articles;
import com.example.linkup.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UpdateCommunityPost extends BottomSheetDialogFragment {
    // layout object
    ImageView btnUpdate;
    EditText headline, content;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseArticleRef; // real-time db ref
    // Dialog
    ProgressDialog progressDialog;
    // Calendar & DateFormat
    Calendar date;
    SimpleDateFormat currentDate;
    // Article-Info
    Articles article = new Articles();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.form_update_communtiy_post,null);
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseArticleRef = Rdb.getReference().child("article");
        // [END config_firebase reference]
        // [START gain layout objects]
        btnMyArticles = view.findViewById(R.id.btnMyArticles );
        btnSavedArticles = view.findViewById(R.id.btnSavedArticles);
        // [END gain]

        // [START layout component function]
        // Switch the screen - User's articles
        btnMyArticles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUI("MyArticles");
            }
        });
        // Switch the screen - saved articles
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
