package net.joscraw.binz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import javax.annotation.Nullable;

public class BinPin extends AppCompatActivity {

    private static final String TAG = "Database";

    private FirebaseAuth auth;
    private FirebaseFirestore data;

    private TextView counter;
    private Button enter;
    private EditText codeBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bin_pin);

        counter = findViewById(R.id.counter);
        codeBox = findViewById(R.id.binPin);
        enter = findViewById(R.id.submit);

        auth = FirebaseAuth.getInstance();
        data = FirebaseFirestore.getInstance();


        final DocumentReference docRef = data.collection("Counts").document(auth.getCurrentUser().getUid());

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        Log.d(TAG, "DocumentData" + document.getData());
                        counter.setText(document.getData().toString());
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
                   counter.setText(documentSnapshot.getData().toString());
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
    }

    private void getBin(String id) {

    }

    private boolean inUse(String binId) {
        boolean inUse = false;

        DocumentReference tempBins = data.collection("tempBins").document(binId);
        tempBins.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        return document.getData();
                    }
                }
            }
        });
    }

    private String getIdData() {
        String id = null;

        if(codeBox.getText().toString() != null) {
            id = codeBox.getText().toString();
        }

        return id;
    }
}
