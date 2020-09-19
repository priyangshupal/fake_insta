package com.example.fakeinsta;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fakeinsta.Adapters.CommentAdapter;
import com.example.fakeinsta.Models.Comment;
import com.example.fakeinsta.Models.Post;
import com.example.fakeinsta.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;

    private List<Comment> mComments;

    private EditText addComment;
    private ImageView profile_image;
    private TextView post;

    private String postid, publisherid;

    private FirebaseUser fuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                finish();
            }
        });

        Intent intent = getIntent();
        postid = intent.getStringExtra("postid");
        publisherid = intent.getStringExtra("publisherid");

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        addComment = findViewById(R.id.comment);

        mComments = new ArrayList<>();

        readComments();

        commentAdapter = new CommentAdapter(getApplicationContext(), mComments, postid);

        recyclerView.setAdapter(commentAdapter);

        profile_image = findViewById(R.id.profile_image);
        post = findViewById(R.id.post);

        getImage();

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_comment = addComment.getText().toString().trim();

                if (TextUtils.isEmpty(txt_comment)) {
                    Toast.makeText(CommentsActivity.this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    add_comment();
                }
            }
        });
    }

    private void addNotifications() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Notifications")
                .child(publisherid);

        HashMap<String, Object> m = new HashMap<>();
        m.put("userid", fuser.getUid());
        m.put("text", "commented " + addComment.getText().toString().trim());
        m.put("postid", postid);
        m.put("ispost", true);
        dbRef.push().setValue(m);
    }

    private void readComments() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Comments")
                .child(postid);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mComments.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    mComments.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void add_comment() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Comments")
                .child(postid);

        String commentid = dbRef.push().getKey();

        HashMap<String, Object> m = new HashMap<>();
        m.put("comment", addComment.getText().toString().trim());
        m.put("publisher", fuser.getUid());
        m.put("commentid", commentid);

        dbRef.child(commentid).setValue(m);
        addNotifications();
        addComment.setText("");
    }

    private void getImage() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(user.getImageUrl()).into(profile_image);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
