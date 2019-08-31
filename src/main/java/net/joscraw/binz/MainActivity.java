package net.joscraw.binz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firestore.admin.v1beta1.Progress;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Login";

    private FirebaseAuth auth;
    private TextView emailField, passwordField;
    private Button submitButton, signUpButton;
    private ProgressBar logInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Fields
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);

        // Button
        submitButton = findViewById(R.id.submit);
        signUpButton = findViewById(R.id.signUp);

        logInProgress = findViewById(R.id.logInBar);
        logInProgress.setVisibility(View.INVISIBLE);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logInProgress.setVisibility(View.VISIBLE);
                logIn(emailField.getText().toString(), passwordField.getText().toString());
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toSignUp();
            }
        });

        auth = FirebaseAuth.getInstance();



    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = auth.getCurrentUser();

        if(user != null) {
            updateUI(user);
        }

    }

    private boolean checkForms() {
        boolean vaild = true;

        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        if(email.isEmpty()) {
            vaild = false;
            emailField.setError("Insert Email");
        } else {
            emailField.setError(null);
        }

        if(password.isEmpty()) {
            vaild = false;
            passwordField.setError("Enter Password");
        } else {
            passwordField.setError(null);
        }

        return vaild;
    }

    private void logIn(String email, final String password) {
        Log.d(TAG, "Login, " + email);
        if (!checkForms()) {
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Login Success");
                            FirebaseUser user = auth.getCurrentUser();
                            logInProgress.setVisibility(View.INVISIBLE);
                            updateUI(user);
                        } else {
                            Log.d(TAG, "Login Failed");
                            logInProgress.setVisibility(View.INVISIBLE);
                            passwordField.setText("");
                            Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                        }

                        if (!task.isSuccessful()) {
                            logInProgress.setVisibility(View.INVISIBLE);
                            Log.d(TAG, "FireBase Failed");
                        }
                    }
                });
    }

    private void toSignUp() {
        Intent intent = new Intent(this, SignUp.class);
        startActivity(intent);
    }


    private void updateUI(FirebaseUser user) {
        if(user != null) {
            Intent intent = new Intent(this, BinPin.class);
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
        }
    }
}
