package com.example.fakeinsta;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fakeinsta.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    TextInputLayout til_email, til_pass, til_username, til_full_name;
    TextInputEditText txt_email, txt_pass, txt_username, txt_full_name;
    TextView register;
    ProgressBar pb;

    DatabaseReference dbRef;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbRef = FirebaseDatabase.getInstance().getReference("Users");
        auth = FirebaseAuth.getInstance();

        pb = findViewById(R.id.pb);

        til_username = findViewById(R.id.username);
        til_full_name = findViewById(R.id.full_name);
        til_email = findViewById(R.id.email);
        til_pass = findViewById(R.id.pass);

        txt_full_name = findViewById(R.id.txt_fullname);
        txt_email = findViewById(R.id.txt_email);
        txt_pass = findViewById(R.id.txt_pass);
        txt_username = findViewById(R.id.txt_username);
        register = findViewById(R.id.btn_register);

        txt_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String str = s.toString();

                if(str.length() > 0) {
                    Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("username")
                            .startAt(str)
                            .endAt(str + "\uf8ff");

                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getChildrenCount() == 0) {
                                til_username.setErrorEnabled(false);
                                til_username.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                                til_username.setEndIconDrawable(R.drawable.ic_check);
                                til_username.setTag("valid");
                            } else {
                                til_username.setErrorEnabled(true);
                                til_username.setError("Username taken");
                                til_username.setTag("invalid");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    til_username.setErrorEnabled(false);
                    til_username.setEndIconMode(TextInputLayout.END_ICON_NONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                til_email.setErrorEnabled(false);
                til_full_name.setErrorEnabled(false);
                til_pass.setErrorEnabled(false);
                til_username.setErrorEnabled(false);

                final String text_username = txt_username.getText().toString().trim();
                String text_email = txt_email.getText().toString().trim();
                String text_pass = txt_pass.getText().toString().trim();
                final String text_full_name = txt_full_name.getText().toString().trim();

                if (TextUtils.isEmpty(text_username) || TextUtils.isEmpty(text_email) || TextUtils.isEmpty(text_pass)
                        || TextUtils.isEmpty(text_full_name)) {
                    String empty_field_error = "This field is required";

                    if (TextUtils.isEmpty(text_username)) {
                        til_username.setError(empty_field_error);
                    }
                    if (TextUtils.isEmpty(text_email)) {
                        til_email.setError(empty_field_error);
                    }
                    if (TextUtils.isEmpty(text_pass)) {
                        til_pass.setError(empty_field_error);
                    }
                    if (TextUtils.isEmpty(text_full_name)) {
                        til_full_name.setError(empty_field_error);
                    }
                } else if (!checkEmail(text_email)) {
                    til_email.setError("Please enter a valid email");
                } else if (text_pass.length() < 6) {
                    til_pass.setError("Password must be at least 6 characters");
                } else {
                    pb.setVisibility(View.VISIBLE);

                    disableAll();

                    auth.createUserWithEmailAndPassword(text_email, text_pass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser fuser = auth.getCurrentUser();
                                        String userId = fuser.getUid();
                                        HashMap<String, Object> m = new HashMap<>();
                                        m.put("id", userId);
                                        m.put("fullname", text_full_name);
                                        m.put("imageUrl", "https://firebasestorage.googleapis.com/v0/b/fake-insta-c685d.appspot.com/o/default.jpg?alt=media&token=7aa1fef0-1bb7-4be0-ad6c-ab7dfcaab8ff");
                                        m.put("username", text_username.toLowerCase());
                                        m.put("bio", "");

                                        dbRef.child(fuser.getUid()).setValue(m).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    pb.setVisibility(View.GONE);

                                                    auth.getCurrentUser().sendEmailVerification()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()) {
                                                                        Toast.makeText(RegisterActivity.this, "Please verify email and login", Toast.LENGTH_SHORT).show();
                                                                        finish();
                                                                    } else {
                                                                        Toast.makeText(RegisterActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        });
                                    } else {
                                        pb.setVisibility(View.GONE);
                                        enableAll();
                                        Toast.makeText(RegisterActivity.this, "Please try a different username or email", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }

    private void disableAll() {
        til_email.setEnabled(false);
        til_full_name.setEnabled(false);
        til_pass.setEnabled(false);
        til_username.setEnabled(false);
        register.setEnabled(false);
    }

    private void enableAll() {
        til_email.setEnabled(true);
        til_full_name.setEnabled(true);
        til_pass.setEnabled(true);
        til_username.setEnabled(true);
        register.setEnabled(true);
    }

    private boolean checkEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
