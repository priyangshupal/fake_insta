package com.example.fakeinsta;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class AddStoryActivity extends AppCompatActivity {

    private static final String TAG = "AddStoryActivity";

    Uri imageUri;
    String myUrl;
    private StorageTask storageTask;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        storageReference = FirebaseStorage.getInstance().getReference("Story");

        Log.d(TAG, "onCreate: Creating");

        CropImage.activity()
                .setAspectRatio(9, 16)
                .start(AddStoryActivity.this);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(storageTask != null && storageTask.isInProgress()) {
            Toast.makeText(this, "Uploading in progress", Toast.LENGTH_SHORT).show();
        } else {
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE || resultCode == RESULT_OK
                    && data != null && data.getData() != null) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (result != null) {
                    imageUri = result.getUri();
                    publishStory();
                } else {
                    finishAffinity();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            } else {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                finishAffinity();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        }
    }

    private String getFileExtension(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }

    private void publishStory() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();

        if (imageUri != null) {

            final StorageReference ref = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            storageTask = ref.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    myUrl = uri.toString();
                                    String myid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Story")
                                            .child(myid);
                                    String storyid = dbRef.push().getKey();
                                    long timeend = System.currentTimeMillis() + 86400000;

                                    HashMap<String, Object> m = new HashMap<>();
                                    m.put("imageUrl", myUrl);
                                    m.put("timestart", ServerValue.TIMESTAMP);
                                    m.put("timeend", timeend);
                                    m.put("storyid", storyid);
                                    m.put("userid", myid);
                                    dbRef.child(storyid).setValue(m);

                                    Toast.makeText(AddStoryActivity.this, "Story added", Toast.LENGTH_SHORT).show();

                                    pd.dismiss();

                                    finish();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddStoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }
}
