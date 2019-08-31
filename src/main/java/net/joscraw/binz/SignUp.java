package net.joscraw.binz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUp extends AppCompatActivity {


    private Button signUp, logIn;
    private EditText name, email, password;
    private ProgressBar signUpProgress;
    private FirebaseAuth auth;

    private static final String TAG = "Sign Up";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signUp = findViewById(R.id.signUp);
        logIn = findViewById(R.id.signIn);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signUpProgress = findViewById(R.id.signUpBar);

        signUpProgress.setVisibility(View.INVISIBLE);

        auth = FirebaseAuth.getInstance();

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpProgress.setVisibility(View.VISIBLE);
                signUp();
            }
        });

    }

    private void signUp() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            Toast signedIn = Toast.makeText(getApplicationContext(), "Already Signed in!", Toast.LENGTH_SHORT);
            signedIn.show();
            signIn();
            return;
        }

        String nameStr, emailStr, passwordStr;

        nameStr = name.getText().toString();
        emailStr = email.getText().toString();
        passwordStr = password.getText().toString();

        if(nameStr.matches("")) {
            name.setError("Name Required");
            return;
        }
        if(emailStr.matches("")) {
            email.setError("Email Required");
            return;
        }
        if(passwordStr.matches("")) {
            password.setError("Password Required");
            return;
        }

        auth.createUserWithEmailAndPassword(emailStr, passwordStr).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "SignUpEmailSuccess");
                    Toast toast = Toast.makeText(getApplicationContext(), "Sign Up Succesful!", Toast.LENGTH_SHORT);
                    toast.show();

                    FirebaseUser user = auth.getCurrentUser();

                    UserProfileChangeRequest updateProfile = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name.getText().toString())
                            .build();

                    user.updateProfile(updateProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Log.d(TAG, "Name set Successful");
                            }
                        }
                    });

                    signUpProgress.setVisibility(View.INVISIBLE);

                    signIn();
                } else {
                    Log.d(TAG, "SignUpEmailFailure");
                    Toast toast = Toast.makeText(getApplicationContext(), "Sign Up Failed Try Again", Toast.LENGTH_SHORT);
                    toast.show();

                    name.setText("");
                    email.setText("");
                    password.setText("");

                    signUpProgress.setVisibility(View.INVISIBLE);

                }
            }
        });


    }

    private void signIn() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


}
