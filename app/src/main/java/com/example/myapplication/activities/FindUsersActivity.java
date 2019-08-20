package com.example.myapplication.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import com.example.myapplication.MyConstant;
import com.example.myapplication.R;
import com.example.myapplication.adapters.UserAdapter;
import com.example.myapplication.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FindUsersActivity extends AppCompatActivity {
    private static final String TAG = "FindUsersActivity";
    List<User> userList = new ArrayList<>();
    private UserAdapter userAdapter;
    private ArrayList<String> followersIdList = new ArrayList<>();
    private List<User> followersList = new ArrayList<>();
    private ArrayList<String> followingIdList = new ArrayList<>();
    private List<User> followingList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_users);

        //this activity called from more than one source so we differ with intent
        if (getIntent().getStringExtra("source").equals("find"))
            loadAllUsers();
        else if (getIntent().getStringExtra("source").equals("followers"))
            loadFollowers();
        else if (getIntent().getStringExtra("source").equals("following"))
            loadFollowing();
    }

    private void loadFollowing() {
        final DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("users").child(MyConstant.loggedInUserId);
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingIdList.clear();
                if (dataSnapshot.hasChild("followingUID")) {
                    users.child("followingUID").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                followingIdList.add(ds.getValue().toString());

                            }
                            getFollowingData();
                            Log.i(TAG, "onDataChange: followingIddddList " + followingIdList.size());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                } else {
                    //loged in user has no followers
                    Log.i(TAG, "onDataChange: no following");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getFollowingData() {
        DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("users");
        users.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                followingList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    for (int i = 0; i < followingIdList.size(); i++) {
                        if (followingIdList.get(i).equals(ds.getKey())) {
                            User user = new User();
                            user.setName(ds.child("name").getValue().toString());
                            user.setPhoto(ds.child("photo").getValue().toString());
                            user.setFollowers(ds.child("followers").getValue().toString());
                            user.setFollowing(ds.child("following").getValue().toString());
                            user.setId(ds.child("id").getValue().toString());
                            followingList.add(user);
                        }
                    }
                }

                Log.i(TAG, "onDataChange: followingList" + followingList.size());
                setupRecycler("getFollowingData");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadFollowers() {

        final DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("users").child(MyConstant.loggedInUserId);
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followersIdList.clear();
                if (dataSnapshot.hasChild("followersUID")) {
                    users.child("followersUID").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                followersIdList.add(ds.getValue().toString());

                            }
                            getFollowersData();
                            Log.i(TAG, "onDataChange: followersIddddList " + followersIdList.size());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                } else {
                    //loged in user has no followers
                    Log.i(TAG, "onDataChange: no followers");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getFollowersData() {

        DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("users");
        users.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                followersList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    for (int i = 0; i < followersIdList.size(); i++) {
                        if (followersIdList.get(i).equals(ds.getKey())) {

                            User user = new User();
                            user.setName(ds.child("name").getValue().toString());
                            user.setPhoto(ds.child("photo").getValue().toString());
                            user.setFollowers(ds.child("followers").getValue().toString());
                            user.setFollowing(ds.child("following").getValue().toString());
                            user.setId(ds.child("id").getValue().toString());
                            followersList.add(user);
                        }
                    }
                }

                Log.i(TAG, "onDataChange: followersList" + followersList.size());
                setupRecycler("getFollowersData");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadAllUsers() {
        FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child("id").getValue().equals(MyConstant.loggedInUserId)) {
                        //don't show this user in list since it's the logged in user
                    } else {
                        User user = new User();
                        user.setName(ds.child("name").getValue().toString());
                        user.setPhoto(ds.child("photo").getValue().toString());
                        user.setFollowers(ds.child("followers").getValue().toString());
                        user.setFollowing(ds.child("following").getValue().toString());
                        user.setId(ds.child("id").getValue().toString());
                        userList.add(user);
                    }

                }
                setupRecycler("loadAllUsers");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupRecycler(String source) {

        RecyclerView recyclerView = findViewById(R.id.usersRecycler);
        if (source.equals("getFollowersData")) {
            userAdapter = new UserAdapter(FindUsersActivity.this, followersList);
        } else if (source.equals("loadAllUsers")) {
            userAdapter = new UserAdapter(FindUsersActivity.this, userList);
        } else if (source.equals("getFollowingData")) {
            userAdapter = new UserAdapter(FindUsersActivity.this, followingList);
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FindUsersActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(userAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //for filtering
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_users_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        //hide search button from keyboard since it does nothing and we filter on text change
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                userAdapter.getFilter().filter(s);
                return true;
            }
        });

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

