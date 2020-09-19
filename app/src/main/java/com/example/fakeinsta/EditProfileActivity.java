package com.example.fakeinsta;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.fakeinsta.Models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    ImageView close, profile_image;
    TextView save, change_dp;
    MaterialEditText bio, username, fullname;
    ProgressBar imageUp_pb;

    RelativeLayout profile_container;

    FirebaseUser fuser;
    StorageTask uploadTask;
    StorageReference storageReference;

    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        close = findViewById(R.id.close);
        profile_image = findViewById(R.id.profile_image);
        save = findViewById(R.id.save);
        change_dp = findViewById(R.id.change_dp);
        imageUp_pb = findViewById(R.id.imageUp_pb);
        profile_container = findViewById(R.id.profile_container);
        bio = findViewById(R.id.bio);
        username = findViewById(R.id.username);
        fullname = findViewById(R.id.fullname);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("Uploads");
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(fuser.getUid());

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                bio.setText(user.getBio());
                fullname.setText(user.getFullname());
                username.setText(user.getUsername());
                Glide.with(getApplicationContext()).load(user.getImageUrl()).into(profile_image);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        change_dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .start(EditProfileActivity.this);
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .start(EditProfileActivity.this);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(fullname.getText().toString().trim(),
                        username.getText().toString().trim(),
                        bio.getText().toString().trim());

                finish();
            }
        });
    }

    private void updateProfile(String fullname, String username, String bio) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> m = new HashMap<>();
        m.put("username", username);
        m.put("fullname", fullname);
        m.put("bio", bio);
        dbRef.updateChildren(m);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(uploadTask != null && uploadTask.isInProgress()) {

            Toast.makeText(this, "Uploading in progress", Toast.LENGTH_SHORT).show();

        } else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE || resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(result != null) {
                imageUri = result.getUri();
                imageUp_pb.setVisibility(View.VISIBLE);

                disableAll();
                uploadImage();
            }
        } else {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    private void disableAll() {
        fullname.setEnabled(false);
        username.setEnabled(false);
        bio.setEnabled(false);
    }

    private void enableAll() {
        fullname.setEnabled(true);
        username.setEnabled(true);
        bio.setEnabled(true);
    }

    private String getFileExtension(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }

    private void uploadImage() {
        if(imageUri != null) {
            final StorageReference stRef = storageReference.child(fuser.getUid()
                    + "." + getFileExtension(imageUri));
            uploadTask = stRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(EditProfileActivity.this,
                                    "Image uploaded successfully", Toast.LENGTH_SHORT).show();

                                enableAll();
                                imageUp_pb.setVisibility(View.GONE);

                            stRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    DatabaseReference dbRef = FirebaseDatabase.getInstance()
                                            .getReference("Users").child(fuser.getUid());
                                    HashMap<String, Object> m = new HashMap<>();
                                    m.put("imageUrl", downloadUrl);
                                    dbRef.updateChildren(m);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }
}
