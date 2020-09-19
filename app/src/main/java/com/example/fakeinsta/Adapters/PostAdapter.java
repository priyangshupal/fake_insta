package com.example.fakeinsta.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fakeinsta.CommentsActivity;
import com.example.fakeinsta.FollowersActivity;
import com.example.fakeinsta.Fragments.PostDetailsFragment;
import com.example.fakeinsta.Fragments.ProfileFragment;
import com.example.fakeinsta.Models.Post;
import com.example.fakeinsta.Models.User;
import com.example.fakeinsta.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context mContext;
    private List<Post> mPosts;

    private FirebaseUser fuser;

    public PostAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Post post = mPosts.get(position);

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        Glide.with(mContext).load(post.getPostimage()).into(holder.post_image);

        if(post.getDescription().equals("")) {
            holder.des.setVisibility(View.GONE);
        } else {
            holder.des.setText(post.getDescription());
        }

        publisherInfo(holder.profile_image, holder.username, holder.publisher, post.getPublisher());
        isLikes(post.getPostid(), holder.like);
        numLikes(holder.likes, post.getPostid());

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.like.getTag().equals("Like")) {
                    FirebaseDatabase.getInstance().getReference("Likes").child(post.getPostid())
                            .child(fuser.getUid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference("Likes").child(post.getPostid())
                            .child(fuser.getUid()).removeValue();
                }
                addNotifications(post.getPublisher(), post.getPostid());
            }
        });

        holder.post_image.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onSingleClick(View v) {

            }

            @Override
            public void onDoubleClick(View v) {
                if(holder.like.getTag().equals("Like")) {
                    FirebaseDatabase.getInstance().getReference("Likes").child(post.getPostid())
                            .child(fuser.getUid()).setValue(true);
                    addNotifications(post.getPublisher(), post.getPostid());

                } else {
                    FirebaseDatabase.getInstance().getReference("Likes").child(post.getPostid())
                            .child(fuser.getUid()).removeValue();
                }
            }
        });

        getComments(post.getPostid(), holder.comments);

        holder.commment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid", post.getPostid());
                intent.putExtra("publisherid", post.getPublisher());
                mContext.startActivity(intent);
            }
        });

        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid", post.getPostid());
                intent.putExtra("publisherid", post.getPublisher());
                mContext.startActivity(intent);
            }
        });

//        holder.publisher_info.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
//                editor.putString("profileid", post.getPublisher());
//                editor.apply();
//                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.fragment_container, new ProfileFragment()).commit();
//            }
//        });

        holder.profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();
                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        });
        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();
                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        });

        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.save.getTag().equals("save")) {
                    FirebaseDatabase.getInstance().getReference("Saves")
                            .child(fuser.getUid()).child(post.getPostid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference("Saves")
                            .child(fuser.getUid()).child(post.getPostid()).removeValue();
                }
            }
        });

        holder.likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FollowersActivity.class);
                intent.putExtra("id", post.getPostid());
                intent.putExtra("title", "Likes");
                mContext.startActivity(intent);
            }
        });

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mContext, v);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.edit:
                                editPost(post.getPostid());
                                return true;
                            case R.id.delete:
                                FirebaseDatabase.getInstance().getReference("Posts").child(post.getPostid())
                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            Toast.makeText(mContext, "Post removed ", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                return true;
                            case R.id.report:
                                Toast.makeText(mContext, "Reported post!", Toast.LENGTH_SHORT).show();
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.post_menu);
                if(!post.getPublisher().equals(fuser.getUid())) {
                    popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                } else {
                    popupMenu.getMenu().findItem(R.id.report).setVisible(false);
                }
                popupMenu.show();
            }
        });

        isSaved(post.getPostid(), holder.save);
    }

    private void editPost(final String postid) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Edit Post");

        View myView = LayoutInflater.from(mContext).inflate(R.layout.alert_edit_text, null);
        final EditText myEditText = myView.findViewById(R.id.post_description);
        getText(postid, myEditText);

        alertDialog.setView(myView);

        alertDialog.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HashMap<String, Object> m = new HashMap<>();
                m.put("description", myEditText.getText().toString().trim());

                FirebaseDatabase.getInstance().getReference("Posts")
                        .child(postid).updateChildren(m);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void getText(String postid, final EditText editText) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Posts")
                .child(postid);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                editText.setText(dataSnapshot.getValue(Post.class).getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNotifications(String userid, String postid) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Notifications")
                .child(userid);

        HashMap<String, Object> m = new HashMap<>();
        m.put("userid", fuser.getUid());
        m.put("postid", postid);
        m.put("text", "liked your post");
        m.put("ispost", true);
        dbRef.push().setValue(m);
    }

    private void isSaved(final String postid, final ImageView save) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Saves")
                .child(fuser.getUid());
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postid).exists()) {
                    save.setImageResource(R.drawable.ic_saved);
                    save.setTag("saved");
                } else {
                    save.setImageResource(R.drawable.ic_save_black);
                    save.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getComments(String postid, final TextView comments) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Comments")
                .child(postid);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                if(count > 0) {
                    comments.setText("View all " + count + " comments");
                } else {
                    comments.setText("No comments to show");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isLikes(String postid, final ImageView imageView) {
        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Likes").child(postid);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(fuser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("Liked");
                } else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void numLikes(final TextView likes, String postid) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Likes").child(postid);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                likes.setText(dataSnapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    private void publisherInfo(final ImageView profile_image, final TextView username, final TextView publisher, String userId) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(mContext).load(user.getImageUrl()).into(profile_image);
                username.setText(user.getUsername());
                publisher.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public abstract class DoubleClickListener implements View.OnClickListener {

        private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds

        long lastClickTime = 0;

        @Override
        public void onClick(View v) {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA){
                onDoubleClick(v);
                lastClickTime = 0;
            } else {
                onSingleClick(v);
            }
            lastClickTime = clickTime;
        }

        public abstract void onSingleClick(View v);
        public abstract void onDoubleClick(View v);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView profile_image, like, commment, save, post_image, more;
        private TextView des, likes, username, publisher, comments;
        private LinearLayout publisher_info;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            des = itemView.findViewById(R.id.description);
            likes = itemView.findViewById(R.id.likes);
            username = itemView.findViewById(R.id.username);
            comments = itemView.findViewById(R.id.comments);
            publisher = itemView.findViewById(R.id.publisher);

            profile_image = itemView.findViewById(R.id.profile_image);
            more = itemView.findViewById(R.id.more);
            like = itemView.findViewById(R.id.like);
            commment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            post_image = itemView.findViewById(R.id.post_image);

            publisher_info = itemView.findViewById(R.id.publisher_info);
        }
    }
}
