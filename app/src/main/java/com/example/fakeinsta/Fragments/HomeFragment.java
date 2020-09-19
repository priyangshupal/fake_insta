package com.example.fakeinsta.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fakeinsta.Adapters.PostAdapter;
import com.example.fakeinsta.Adapters.StoryAdapter;
import com.example.fakeinsta.Models.Post;
import com.example.fakeinsta.Models.Story;
import com.example.fakeinsta.R;
import com.example.fakeinsta.Widgets.CustomRecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private List<Post> mPosts;
    private List<String> followingList;
    private PostAdapter postAdapter;
    private ProgressBar pb;

    private StoryAdapter storyAdapter;
    private List<Story> storyList;

    private ImageView status;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        CustomRecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        View empty_view = view.findViewById(R.id.empty_view);
        TextView empty_text = empty_view.findViewById(R.id.empty_text);
        empty_text.setText("No posts here");

        recyclerView.showIfEmpty(empty_view);
        recyclerView.hideIfEmpty(recyclerView);

        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        lm.setReverseLayout(true);
        lm.setStackFromEnd(true);

        pb = view.findViewById(R.id.content_loading);
        RecyclerView recyclerView_story = view.findViewById(R.id.recycler_view_story);
        recyclerView_story.setHasFixedSize(true);
        LinearLayoutManager lmh = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL,
                false);
        recyclerView_story.setLayoutManager(lmh);
        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext(), storyList);
        recyclerView_story.setAdapter(storyAdapter);

        mPosts = new ArrayList<>();

        recyclerView.setLayoutManager(lm);

        postAdapter = new PostAdapter(getContext(), mPosts);

        checkFollowing();

        recyclerView.setAdapter(postAdapter);

        status = view.findViewById(R.id.status);
        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Feature under development!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void checkFollowing() {
        followingList = new ArrayList<>();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Following");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                followingList.add(FirebaseAuth.getInstance().getCurrentUser().getUid());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    followingList.add(snapshot.getKey());
                }

                readPosts();
                readStory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readStory() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Story");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long timecurrent = System.currentTimeMillis();
                storyList.clear();

                storyList.add(new Story("", "",
                        FirebaseAuth.getInstance().getCurrentUser().getUid(), 0, 0));

                for(String id: followingList) {
                    int countStory = 0;
                    Story story = null;
                    for(DataSnapshot snapshot: dataSnapshot.child(id).getChildren()) {
                        story = snapshot.getValue(Story.class);
                        if(timecurrent > story.getTimestart() && timecurrent < story.getTimeend()) {
                            countStory++;
                        }
                    }
                    if(countStory > 0) {
                        storyList.add(story);
                    }
                }

                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readPosts() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Posts");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mPosts.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);

                    for (String id : followingList) {
                        if (post.getPublisher().equals(id)) {
                            mPosts.add(post);
                            break;
                        }
                    }
                }
                pb.setVisibility(View.GONE);
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
