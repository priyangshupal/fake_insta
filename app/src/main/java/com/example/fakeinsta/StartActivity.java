package com.example.fakeinsta;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class StartActivity extends AppCompatActivity {

    private static final String TAG = "StartActivity";

    TextView login, forgot_pass;
    FirebaseUser fuser;
    FirebaseAuth auth;
    TextView signup;
    ProgressBar pb;

    TextInputEditText email, pass;
    TextInputLayout til_email, til_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        auth = FirebaseAuth.getInstance();
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        til_email = findViewById(R.id.email);
        til_pass = findViewById(R.id.pass);

        email = findViewById(R.id.txt_email);
        pass = findViewById(R.id.txt_pass);
        login = findViewById(R.id.btn_login);
        signup = findViewById(R.id.sign_up);
        pb = findViewById(R.id.pb_login);
        forgot_pass = findViewById(R.id.forgot_pass);

        til_email.setErrorEnabled(false);
        til_pass.setErrorEnabled(false);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                til_email.setErrorEnabled(false);
                til_pass.setErrorEnabled(false);

                String txt_email = email.getText().toString().trim();
                String txt_pass = pass.getText().toString().trim();
                if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_pass)) {
                    String empty_field_error = "This field is required";
                    if (TextUtils.isEmpty(txt_email) && TextUtils.isEmpty(txt_pass)) {
                        til_email.setErrorEnabled(true);
                        til_pass.setErrorEnabled(true);
                        til_email.setError(empty_field_error);
                        til_pass.setError(empty_field_error);
                    } else if (TextUtils.isEmpty(txt_email)) {
                        til_email.setErrorEnabled(true);
                        til_email.setError(empty_field_error);
                    } else {
                        til_pass.setErrorEnabled(true);
                        til_pass.setError(empty_field_error);
                    }
                } else if (!isValid(txt_email)) {
                    til_email.setErrorEnabled(true);
                    til_email.setError("Please enter a valid email");
                } else {
                    pb.setVisibility(View.VISIBLE);
                    disableAll();

                    auth.signInWithEmailAndPassword(txt_email, txt_pass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful() && auth.getCurrentUser().isEmailVerified()) {
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    } else {
                                        auth.getCurrentUser().sendEmailVerification()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(StartActivity.this, "Please verify the email sent now", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                        pb.setVisibility(View.GONE);
                                        enableAll();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pb.setVisibility(View.GONE);
                                    enableAll();

                                    Toast.makeText(StartActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        forgot_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class));
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            }
        });
    }

    private boolean isValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    private void disableAll() {
        til_email.setEnabled(false);
        til_pass.setEnabled(false);
        login.setEnabled(false);
    }

    private void enableAll() {
        til_email.setEnabled(true);
        til_pass.setEnabled(true);
        login.setEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (fuser != null && fuser.isEmailVerified()) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
