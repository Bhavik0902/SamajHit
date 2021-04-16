package com.example.loginhack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    String emailText,phoneNum, personName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        EditText name = findViewById(R.id.firstName);
        EditText last_name = findViewById(R.id.lastName);
        EditText email = findViewById(R.id.email);
        EditText pass = findViewById(R.id.password);
        EditText con_pass = findViewById(R.id.conPass);
        EditText phone = findViewById(R.id.editTextPhone);

        Button create = findViewById(R.id.signUpAccButton);


        TextView logInView = findViewById(R.id.loginView);
        logInView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailText = email.getText().toString();
                phoneNum = phone.getText().toString();
                personName = name.getText().toString() + " " + last_name.getText().toString();
                String passWord = pass.getText().toString();

                if(name.getText().toString().isEmpty()) {
                    name.setError("First Name Required");
                    name.requestFocus();
                    return;
                }
                if(last_name.getText().toString().isEmpty()) {
                    last_name.setError("Last Name Required");
                    last_name.requestFocus();
                    return;
                }
                if(phoneNum.length()!=10) {
                    phone.setError("Valid Mobile Reqired");
                    phone.requestFocus();
                    return;
                }
                if(email.getText().toString().isEmpty()) {
                    email.setError("Email Required");
                    email.requestFocus();
                    return;
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
                    email.setError("Valid Email Required");
                    email.requestFocus();
                    return;
                }
                if(pass.getText().toString().isEmpty()) {
                    pass.setError("Password Required");
                    pass.requestFocus();
                    return;
                }
                if(pass.getText().toString().length()<6) {
                    pass.setError("Min 6 char required");
                    pass.requestFocus();
                    return;
                }
                if(con_pass.getText().toString().isEmpty() || !con_pass.getText().toString().equals(pass.getText().toString())) {
                    con_pass.setError("Password does not matches !!");
                    con_pass.requestFocus();
                    return;
                }
                registerUser(emailText,passWord);
            }
        });
    }

    public void registerUser(String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            //SignUp success

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            Map<String, String> em = new HashMap<>();
                            em.put("phone",phoneNum);
                            db.collection("Email").document(emailText).set(em);

                            Intent intent = new Intent(SignUpActivity.this,LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(SignUpActivity.this, "Email ID already exists..", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}