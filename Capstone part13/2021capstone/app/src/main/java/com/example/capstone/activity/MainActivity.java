package com.example.capstone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstone.PostInfo;
import com.example.capstone.R;
import com.example.capstone.adapter.GalleryAdapter;
import com.example.capstone.adapter.MainAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends BasicActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            myStartActivity(SignUpActivity.class);
        } else {          //회원가입 or 로그인 을 해서 메인 화면으로 넘어 온 경우
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            } else {
                                Log.d(TAG, "No such document");
                                myStartActivity(MemberinitActivity.class);
                            }
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });


            db.collection("posts")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                ArrayList<PostInfo> postList = new ArrayList<>();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    postList.add(new PostInfo(
                                            document.getData().get("title").toString(),
                                            (ArrayList<String>) document.getData().get("contents"),
                                            document.getData().get("publisher").toString(),
                                            new Date(document.getDate("createdAt").getTime())));
                                }
                                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                                recyclerView.setHasFixedSize(true);
                                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

                                RecyclerView.Adapter mAdapter = new MainAdapter(MainActivity.this, postList);
                                recyclerView.setAdapter(mAdapter);
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });


        }

        findViewById(R.id.floatingActionButton).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                /*
                case R.id.LogoutButton:
                    FirebaseAuth.getInstance().signOut();
                    myStartActivity(SignUpActivity.class);
                    break;
                    */
                case R.id.floatingActionButton:
                    myStartActivity(WritePostActivity.class);
                    break;

            }
        }
    };


    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //part5 영상 보다 보니까 이거 필요없다고 지우길래 지웠습니다. - 수진
        startActivity(intent);
    }
}
