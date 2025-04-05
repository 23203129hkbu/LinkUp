package com.example.linkup.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.Adapter.UserAdapter;
import com.example.linkup.CommunityOperation.SavedArticlesActivity;
import com.example.linkup.HomeOperation.SearchUser;
import com.example.linkup.HomeOperation.UserProfile;
import com.example.linkup.Object.Articles;
import com.example.linkup.Object.Users;
import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;

// ✅
public class ChatFragment extends Fragment {
    View view;
    // Layout objects
    SearchView searchBar;
    Button btnAll, btnFavorites;
    RecyclerView chatRoomRV;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseUserRef,databaseChatRoomRef, databaseLikedChatRoomRef; // real-time db ref ;
    // User list and adapter
    ArrayList<Users> usersArrayList = new ArrayList<>(); // store all user data
    ArrayList<Users> filteredUsers = new ArrayList<>(); // Update the result (after filter)
    UserAdapter userAdapter;
    // checker (favourite)
    Boolean isFavourite = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_chat_fragment, container, false);
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user");
        databaseChatRoomRef = Rdb.getReference().child("chatRoom").child(auth.getUid());
        databaseLikedChatRoomRef = Rdb.getReference().child("likedChatRoom").child(auth.getUid());
        // [END config_firebase reference]

        // [START gain layout objects]
        btnAll = view.findViewById(R.id.btnAll);
        btnFavorites = view.findViewById(R.id.btnFavorites);
        searchBar = view.findViewById(R.id.searchBar);
        chatRoomRV = view.findViewById(R.id.chatRoomRV);
        // [END gain]

        // [START config_layout]
        // Load users from Firebase (but do not display them initially)
        databaseChatRoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (isFavourite){
                    loadChatRoom(databaseLikedChatRoomRef);
                }else{
                    loadChatRoom(databaseChatRoomRef);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Initialize Adapter with an empty list
        userAdapter = new UserAdapter(getContext(), filteredUsers, "ChatRoom"); // Only filteredUsers are shown
        chatRoomRV.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRoomRV.setHasFixedSize(true);
        chatRoomRV.setAdapter(userAdapter);
        // [END config_layout]

        // [START layout component function]
        // change to favourites
        btnFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnFavorites.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.green_1));
                btnFavorites.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.black));
                btnAll.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.black));
                btnAll.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.white));
                isFavourite = true;
                loadChatRoom(databaseLikedChatRoomRef);
            }
        });
        //
        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAll.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.green_1));
                btnAll.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.black));
                btnFavorites.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.black));
                btnFavorites.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.white));
                isFavourite = false;
                loadChatRoom(databaseChatRoomRef);
            }
        });
        // Search functionality
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });
        // [END layout component function]

        // this line must be finalized
        return view;
    }
    // [START Method]
    // Filter users based on search input
    // essential for case-insensitive searchc
    private void filterUsers(String text) {
        filteredUsers.clear();
        if (text.isEmpty()) {
            filteredUsers.addAll(usersArrayList);
        } else {
            for (Users user : usersArrayList) {
                if (user.getUsername().toLowerCase().contains(text.toLowerCase())) {
                    filteredUsers.add(user);
                }
            }
        }
        userAdapter.notifyDataSetChanged(); // Refresh the adapter with the filtered list
    }
    // load all chat room / load liked chat room
    private void loadChatRoom(DatabaseReference currentRef) {
        currentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                usersArrayList.clear();
                filteredUsers.clear();
                if (snapshot.exists()){
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String uid = child.getKey();
                        if (uid != null) {
                            databaseUserRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        Users user = dataSnapshot.getValue(Users.class);
                                        if (user != null) {
                                            usersArrayList.add(user);
                                            filteredUsers.add(user);
                                            userAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getContext(), "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }else{
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // [END Method]
}
