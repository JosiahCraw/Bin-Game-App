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
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class BinPin extends AppCompatActivity {

    private static final String TAG = "Database";

    private FirebaseAuth auth;
    private FirebaseFirestore data;

    private TextView counter;
    private Button enter, signOut;
    private EditText codeBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bin_pin);

        counter = findViewById(R.id.counter);
        codeBox = findViewById(R.id.binPin);
        enter = findViewById(R.id.submit);
        signOut = findViewById(R.id.signOut);

        auth = FirebaseAuth.getInstance();
        data = FirebaseFirestore.getInstance();


        final DocumentReference docRef = data.collection("Users").document(auth.getCurrentUser().getUid());

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        Log.d(TAG, "DocumentData" + document.getData());
                        counter.setText(document.getData().get("Count").toString());
                    } else {
                        Log.d(TAG, "Document not found");
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

               if(documentSnapshot != null && documentSnapshot.exists()) {
                   counter.setText(documentSnapshot.getData().get("Count").toString());
               }
            }
        });

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String id = getIdData();

                getBin(id);
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

        final DocumentReference tempBins = data.collection("tempBins").document(id);
        tempBins.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("inUse", true);
                        data.put("user", auth.getCurrentUser().getUid());
                        tempBins.set(data);
                    }
                }
            }
        });
    }

    private String getIdData() {
        String id = null;

        if(codeBox.getText().toString() != null) {
            id = codeBox.getText().toString();
            codeBox.setText("");
        }

        return id;
    }

    private void toMainPage() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
