package net.joscraw.binz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class BinPin extends AppCompatActivity {

    private static final String TAG = "Database";

    private FirebaseAuth auth;
    private FirebaseFirestore data;

    private TextView counter, nameZone;
    private Button enter, signOut;
    private ProgressBar binCheckBar;
    private EditText codeBox;

    private DocumentReference tempBins;

    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bin_pin);

        counter = findViewById(R.id.counter);
        codeBox = findViewById(R.id.binPin);
        enter = findViewById(R.id.submit);
        signOut = findViewById(R.id.signOut);
        binCheckBar = findViewById(R.id.binProgessBar);
        nameZone = findViewById(R.id.BinTitle);

        binCheckBar.setVisibility(View.INVISIBLE);

        auth = FirebaseAuth.getInstance();
        data = FirebaseFirestore.getInstance();


        nameZone.setText("Enter a code to start");

        final DocumentReference docRef = data.collection("Users").document(auth.getCurrentUser().getUid());

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    try {
                        if(document.exists()) {
                            Log.d(TAG, "DocumentData" + document.getData());
                            counter.setText(document.getData().get("Count").toString());
                        } else {
                            Log.d(TAG, "Document not found");
                        }
                    } catch (NullPointerException e) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Failed to get count", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                } else {
                    Log.d(TAG, "Failed: " + task.getException());
                }
            }
        });

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Log.w(TAG, "Listen Failed");
                    return;
                }
                try {
                    if(documentSnapshot != null && documentSnapshot.exists()) {
                        counter.setText(documentSnapshot.getData().get("Count").toString());
                    }
                } catch (NullPointerException ex) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Failed to get count", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = codeBox.getText().toString();
                if(code.matches("")) {
                    codeBox.setError("Please Enter Pin");
                    return;
                }
                binCheckBar.setVisibility(View.VISIBLE);
                getBin(code);
                setupBin();
                codeBox.setText("");
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                toMainPage();
            }
        });
    }

    private void getBin(String id) {

        boolean inUse = false;

        tempBins = data.collection("tempBins").document(id);
        tempBins.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("inUse", true);
                        data.put("user", auth.getCurrentUser().getUid());
                        tempBins.set(data, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    binCheckBar.setVisibility(View.INVISIBLE);
                                    Toast toast = Toast.makeText(getApplicationContext(), "Succesfully got Bin!", Toast.LENGTH_SHORT);
                                    toast.show();
                                } else {
                                    binCheckBar.setVisibility(View.INVISIBLE);
                                    Toast toast = Toast.makeText(getApplicationContext(), "Failed to get Bin", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }
                        });
                    } else {
                        binCheckBar.setVisibility(View.INVISIBLE);
                        Toast toast = Toast.makeText(getApplicationContext(), "Bin does not exist", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
        });
    }

    private String getIdData() {
        String id = null;

        if(codeBox.getText().toString() != "") {
            id = codeBox.getText().toString();
            codeBox.setText("");
        } else {
            codeBox.setError("Enter Code");
            return null;
        }

        return id;
    }

    private void setupBin() {
        tempBins.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    try {
                        if (doc.exists()) {
                            final String binCode = doc.getData().get("bin").toString();

                            final DocumentReference mainBinDoc = data.collection("bins").document(binCode);

                            mainBinDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot binDoc = task.getResult();
                                        try {
                                            if (binDoc.exists()) {
                                                String binName = binDoc.getData().get("name").toString();
                                                nameZone.setText("Acsessing bin in: " + binName);
                                            }
                                        } catch (NullPointerException e) {
                                            Log.w(TAG, e.getMessage());
                                        }
                                    }
                                }
                            });
                        }
                    } catch (NullPointerException e) {
                        Log.w(TAG, e.getMessage());
                    }
                }
            }
        });

        registration = tempBins.addSnapshotListener(BinPin.this ,new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen Failed");
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    if (documentSnapshot.getData().get("closed").toString() == "true") {
                        Log.d(TAG, "Got data");
                        nameZone.setText("Enter a code to start");
                        registration.remove();
                    }
                }
            }
        });
    }

    private void toMainPage() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
