package com.example.fakeinsta.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fakeinsta.Adapters.MyPhotosAdapter;
import com.example.fakeinsta.EditProfileActivity;
import com.example.fakeinsta.FollowersActivity;
import com.example.fakeinsta.Models.Post;
import com.example.fakeinsta.Models.User;
import com.example.fakeinsta.R;
import com.example.fakeinsta.StartActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static android.view.View.GONE;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private ImageView profile_image, options;
    private TextView followers, following, posts, fullname, bio, username;
    private Button edit_profile;
    private ImageButton my_photos, saved_photos;

    private FirebaseUser fuser;

    private String profileid;

    private RecyclerView recyclerView, recyclerView_saves;
    private MyPhotosAdapter myPhotosAdapter, myPhotosAdapter_saves;

    private List<Post> mPosts, mPosts_saves;
    private List<String> mySaves;

    public ProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = prefs.getString("profileid", "none");

        profile_image = view.findViewById(R.id.profile_image);
        options = view.findViewById(R.id.options);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        fullname = view.findViewById(R.id.fullname);
        edit_profile = view.findViewById(R.id.edit_profile);
        bio = view.findViewById(R.id.bio);
        username = view.findViewById(R.id.username);
        my_photos = view.findViewById(R.id.my_photos);
        saved_photos = view.findViewById(R.id.saved_photos);
        posts = view.findViewById(R.id.posts);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager lm = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(lm);
        mPosts = new ArrayList<>();
        myPhotosAdapter = new MyPhotosAdapter(getContext(), mPosts);
        recyclerView.setAdapter(myPhotosAdapter);

        recyclerView_saves = view.findViewById(R.id.recycler_view_saves);
        recyclerView_saves.setHasFixedSize(true);
        LinearLayoutManager lms = new GridLayoutManager(getContext(), 3);
        recyclerView_saves.setLayoutManager(lms);
        mPosts_saves = new ArrayList<>();
        myPhotosAdapter_saves = new MyPhotosAdapter(getContext(), mPosts_saves);
        recyclerView_saves.setAdapter(myPhotosAdapter_saves);

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getContext(), v);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.option_logout:
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(getContext(), StartActivity.class);
                                getActivity().finishAffinity();
                                startActivity(intent);
                                return true;
                            case R.id.option_settings:
                                Toast.makeText(getContext(), "No settings", Toast.LENGTH_SHORT).show();
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.options_menu);
                popupMenu.show();
            }
        });

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_btn = edit_profile.getText().toString();

                if (txt_btn.equals("Edit Profile")) {
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                } else if (txt_btn.equals("Follow")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(fuser.getUid())
                            .child("Following").child(profileid).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("Followers").child(fuser.getUid()).setValue(true);

                    addNotifications();
                } else if (txt_btn.equals("Following")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(fuser.getUid())
                            .child("Following").child(profileid).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("Followers").child(fuser.getUid()).removeValue();
                }
            }
        });

        userInfo();
        getFollowers();
        getNrPosts();
        myPhotos();
        mysaves();

        if (profileid.equals(fuser.getUid())) {
            edit_profile.setText("Edit Profile");
        } else {
            checkFollow();
            saved_photos.setVisibility(GONE);
        }

        my_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView_saves.setVisibility(View.GONE);
            }
        });

        saved_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView_saves.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });

        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title", "Followers");
                startActivity(intent);
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title", "Following");
                startActivity(intent);
            }
        });

        return view;
    }

    private void userInfo() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(profileid);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                Glide.with(getContext()).load(user.getImageUrl()).into(profile_image);
                username.setText(user.getUsername());
                fullname.setText(user.getFullname());
                bio.setText(user.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNotifications() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Notifications")
                .child(profileid);

        HashMap<String, Object> m = new HashMap<>();
        m.put("userid", fuser.getUid());
        m.put("postid", "");
        m.put("text", "started following you");
        m.put("ispost", false);
        dbRef.push().setValue(m);
    }

    private void checkFollow() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(fuser.getUid()).child("Following");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(profileid).exists()) {
                    edit_profile.setText("Following");
                } else {
                    edit_profile.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowers() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(profileid).child("Followers");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followers.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        dbRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(profileid).child("Following");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                following.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getNrPosts() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Posts");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post.getPublisher().equals(profileid)) {
                        count++;
                    }
                }
                posts.setText("" + count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void myPhotos() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Posts");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mPosts.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post.getPublisher().equals(profileid)) {
                        mPosts.add(post);
                    }
                }
                Collections.reverse(mPosts);
                myPhotosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void mysaves() {
        mySaves = new ArrayList<>();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Saves")
                .child(fuser.getUid());

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mySaves.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mySaves.add(snapshot.getKey());
                }

                readSaves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readSaves() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Posts");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mPosts_saves.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);

                    for (String id : mySaves) {
                        if (post.getPostid().equals(id)) {
                            mPosts_saves.add(post);
                        }
                    }
                }
                myPhotosAdapter_saves.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
