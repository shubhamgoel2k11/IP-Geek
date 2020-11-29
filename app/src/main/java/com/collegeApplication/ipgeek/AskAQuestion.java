package com.collegeApplication.ipgeek;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;


import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.util.Date;
import java.util.HashMap;

public class AskAQuestion extends AppCompatActivity {

    private Toolbar toolbar;
    private Spinner spinner;
    private EditText questionBox;
    private ImageView imageView;
    private Button cancelBtn, postQuestionBtn;

    private String askedByName="";
    private DatabaseReference askedByRef;
    private ProgressDialog loader;
    private String myUrl="";
    StorageTask uploadTask;
    StorageReference storageReference;
    private Uri imageuri;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String onlineUserId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_ask_a_question);

        toolbar=findViewById(R.id.question_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Ask a Question");


        spinner=findViewById(R.id.spinner);
        questionBox=findViewById(R.id.question_text);
        imageView=findViewById(R.id.questionImage);
        cancelBtn=findViewById(R.id.cancel);
        postQuestionBtn=findViewById(R.id.PostQuestion);

        loader=new ProgressDialog(this);
        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        onlineUserId=mUser.getUid();

        askedByRef= FirebaseDatabase.getInstance().getReference("users").child(onlineUserId);

        askedByRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                askedByName=snapshot.child("fullname").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        storageReference= FirebaseStorage.getInstance().getReference("questions");

        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.topics));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(spinner.getSelectedItem().equals("Select Topic")){
                    Toast.makeText(AskAQuestion.this, "Please Select a Valid Topic", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        postQuestionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performValidation();
            }
        });
        
    }

    String getQuestionText(){
        return questionBox.getText().toString().trim();
    }

    String getTopic(){
        return spinner.getSelectedItem().toString();
    }

    String mDate= DateFormat.getDateInstance().format(new Date());
    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("questions posts");

    private void performValidation(){

        if(getQuestionText().isEmpty()){
            questionBox.setError("Question Required");
        }
        else if(getTopic().equals("Select Topic")){
            Toast.makeText(this, "Select a Valid Topic", Toast.LENGTH_SHORT).show();
        }
        else if(!getQuestionText().isEmpty() && !getTopic().equals("") && imageuri==null){
            uploadQuestionwithNoImage();
        }
        else if(!getQuestionText().isEmpty() && !getTopic().equals("") && imageuri!=null){
            uploadQuestionwithImage();
        }
    }

    private void startLoader(){
        loader.setMessage("posting your question");
        loader.setCanceledOnTouchOutside(false);
        loader.show();
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadQuestionwithNoImage(){
        startLoader();
        String postid= ref.push().getKey();

        HashMap<String,Object> hashmap= new HashMap<>();
        hashmap.put("postid",postid);
        hashmap.put("question",getQuestionText());
        hashmap.put("publisher",onlineUserId);
        hashmap.put("topic",getTopic());
        hashmap.put("askedby",askedByName);
        hashmap.put("date",mDate);

        ref.child(postid).setValue(hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(AskAQuestion.this, "Question Posted Successfully", Toast.LENGTH_SHORT).show();
                    loader.dismiss();
                    startActivity(new Intent(AskAQuestion.this,HomeActivity.class));
                    finish();
                }
                else{
                    Toast.makeText(AskAQuestion.this, "Could Not Upload Image"+task.getException().toString(), Toast.LENGTH_SHORT).show();
                    loader.dismiss();
                }
            }
        });
    }

    private void uploadQuestionwithImage(){
        startLoader();
        final StorageReference fileReference;
        fileReference=storageReference.child(System.currentTimeMillis()+"."+getFileExtension(imageuri));
        uploadTask= fileReference.putFile(imageuri);
        uploadTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if(!task.isComplete()){
                    throw task.getException();
                }

                return fileReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    Uri downloadUri= (Uri) task.getResult();
                    myUrl=downloadUri.toString();

                    String postid= ref.push().getKey();

                    HashMap<String,Object> hashmap= new HashMap<>();
                    hashmap.put("postid",postid);
                    hashmap.put("question",getQuestionText());
                    hashmap.put("publisher",onlineUserId);
                    hashmap.put("topic",getTopic());
                    hashmap.put("askedby",askedByName);
                    hashmap.put("questionimage",myUrl);
                    hashmap.put("date",mDate);
                    
                    ref.child(postid).setValue(hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(AskAQuestion.this, "Question Posted Successfully", Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                                startActivity(new Intent(AskAQuestion.this,HomeActivity.class));
                                finish();
                            }
                            else{
                                Toast.makeText(AskAQuestion.this, "Could Not Upload Image"+task.getException().toString(), Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            }
                        }
                    });
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AskAQuestion.this, "Failed to upload a question", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null){
            imageuri=data.getData();
            imageView.setImageURI(imageuri);
        }
    }
}