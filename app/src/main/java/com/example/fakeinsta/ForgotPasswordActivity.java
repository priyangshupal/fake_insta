package com.example.fakeinsta;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity {

    TextView btn_send_email;
    ProgressBar pb;
    TextInputEditText txt_email;
    TextInputLayout til_email;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        til_email = findViewById(R.id.email);
        txt_email = findViewById(R.id.txt_email);
        btn_send_email = findViewById(R.id.send_mail);
        pb = findViewById(R.id.pb);

        auth = FirebaseAuth.getInstance();

        btn_send_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                til_email.setErrorEnabled(false);

                String text_email = txt_email.getText().toString().trim();

                if (TextUtils.isEmpty(text_email)) {
                    til_email.setErrorEnabled(true);
                    til_email.setError("This field is required");
                } else if (!validEmail(text_email)) {
                    til_email.setErrorEnabled(true);
                    til_email.setError("Enter a valid email");
                } else {
                    pb.setVisibility(View.VISIBLE);
                    disableAll();

                    auth.sendPasswordResetEmail(text_email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(ForgotPasswordActivity.this, "Failed !", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pb.setVisibility(View.GONE);
                                    enableAll();
                                    Toast.makeText(ForgotPasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }

    private void enableAll() {
        til_email.setEnabled(true);
        txt_email.setEnabled(true);
    }

    private void disableAll() {
        til_email.setEnabled(false);
        txt_email.setEnabled(false);
    }

    private boolean validEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }
}
