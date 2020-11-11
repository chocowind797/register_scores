package com.example.school;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Activities {
    public static class HomeActivity extends AppCompatActivity {
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if(user == null){
                setContentView(R.layout.home_activity);

                Button register = findViewById(R.id.user_register);
                Button login = findViewById(R.id.user_login);

                register.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivity.this, RegisterActivity.class);
                        startActivity(intent);
                    }
                });

                login.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                });
            } else {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }

        }
    }

    public static class LoginActivity extends AppCompatActivity {
        Button login;
        TextInputEditText passwordEdit;
        TextInputEditText accountEdit;
        TextInputLayout accountLayout;
        TextInputLayout passwordLayout;
        FirebaseAuth auth;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity);
            setTitle("登入");

            initView();

            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String account = accountEdit.getText().toString();
                    String password = passwordEdit.getText().toString();

                    if(TextUtils.isEmpty(account)){
                        accountLayout.setError("請輸入帳號");
                        passwordLayout.setError("");
                        return;
                    }

                    if(TextUtils.isEmpty(password)){
                        accountLayout.setError("");
                        passwordLayout.setError("請輸入密碼");
                        return;
                    }

                    accountLayout.setError("");
                    passwordLayout.setError("");

                    auth.signInWithEmailAndPassword(account, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(LoginActivity.this, "登入成功", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }else {
                                        if(task.getException().getMessage().equals("The email address is badly formatted."))
                                            Toast.makeText(LoginActivity.this, "電子郵件格式錯誤", Toast.LENGTH_SHORT).show();
                                        else if(task.getException().getMessage().equals("There is no user record corresponding to this identifier. The user may have been deleted."))
                                            Toast.makeText(LoginActivity.this, "查無此帳號", Toast.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });
        }

        private void initView(){
            auth = FirebaseAuth.getInstance();
            login = findViewById(R.id.button);
            passwordEdit = findViewById(R.id.password_edit);
            accountEdit = findViewById(R.id.account_edit);
            accountLayout = findViewById(R.id.account_layout);
            passwordLayout = findViewById(R.id.password_layout);

            login.setText("登入");

            passwordLayout.setErrorEnabled(true);
            accountLayout.setErrorEnabled(true);
        }
    }

    public static class RegisterActivity extends AppCompatActivity {
        Button register;
        TextInputEditText passwordEdit;
        TextInputEditText accountEdit;
        TextInputLayout accountLayout;
        TextInputLayout passwordLayout;
        FirebaseAuth auth;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity);
            setTitle("註冊");

            initView();

            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String account = accountEdit.getText().toString();
                    String password = passwordEdit.getText().toString();

                    if(TextUtils.isEmpty(account)){
                        accountLayout.setError("請輸入帳號");
                        passwordLayout.setError("");
                        return;
                    }

                    if(TextUtils.isEmpty(password)){
                        accountLayout.setError("");
                        passwordLayout.setError("請輸入密碼");
                        return;
                    }

                    accountLayout.setError("");
                    passwordLayout.setError("");

                    auth.createUserWithEmailAndPassword(account, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "註冊成功", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }else {
                                        if(task.getException().getMessage().equals("The email address is badly formatted."))
                                            Toast.makeText(RegisterActivity.this, "電子郵件格式錯誤", Toast.LENGTH_SHORT).show();
                                        else if(task.getException().getMessage().equals("The given password is invalid. [ Password should be at least 6 characters ]"))
                                            Toast.makeText(RegisterActivity.this, "密碼需大於6個字元", Toast.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });
        }

        private void initView(){
            auth = FirebaseAuth.getInstance();
            register = findViewById(R.id.button);
            passwordEdit = findViewById(R.id.password_edit);
            accountEdit = findViewById(R.id.account_edit);
            accountLayout = findViewById(R.id.account_layout);
            passwordLayout = findViewById(R.id.password_layout);

            register.setText("註冊");

            passwordLayout.setErrorEnabled(true);
            accountLayout.setErrorEnabled(true);
        }
    }
}
