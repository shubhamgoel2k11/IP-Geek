package com.collegeApplication.ipgeek;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.collegeApplication.ipgeek.Adapters.PostAdapter;
import com.collegeApplication.ipgeek.Model.Post;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawer_Layout;
    private Toolbar toolbar;
    private FloatingActionButton fab;

    private RecyclerView recyclerView;
    private ProgressBar progress_circular;

    private PostAdapter postAdapter;
    private List<Post> postList;

    private CircleImageView navHeaderImage;
    private TextView navHeaderEmail, navHeaderName;

    private DatabaseReference userRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fab=findViewById(R.id.fab);

        drawer_Layout=findViewById(R.id.drawer_layout);
        toolbar=findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("IP Geek");

        progress_circular=findViewById(R.id.progressCircular);
        recyclerView=findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        NavigationView navigationView=findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle= new ActionBarDrawerToggle(this,drawer_Layout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer_Layout.addDrawerListener(toggle);
        toggle.syncState();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent= new Intent(HomeActivity.this,AskAQuestion.class);
                startActivity(intent);

            }
        });

        navHeaderEmail=navigationView.getHeaderView(0).findViewById(R.id.nav_header_email);
        navHeaderName=navigationView.getHeaderView(0).findViewById(R.id.nav_header_username);
        navHeaderImage=navigationView.getHeaderView(0).findViewById(R.id.nav_header_profile_image);

        userRef=FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                navHeaderName.setText(snapshot.child("username").getValue().toString());
                navHeaderEmail.setText(snapshot.child("email").getValue().toString());

                Glide.with(HomeActivity.this).load(snapshot.child("profileimageurl").getValue()).into(navHeaderImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        postList=new ArrayList<>();
        postAdapter=new PostAdapter(HomeActivity.this,postList);
        recyclerView.setAdapter(postAdapter);

        readQuestionsPosts();
    }

    private void readQuestionsPosts(){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("questions posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren())
                {
                    Post post= dataSnapshot.getValue(Post.class);
                    postList.add(post);
                }
                
                postAdapter.notifyDataSetChanged();
                progress_circular.setVisibility(View.GONE);
                
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
             
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.nav_cse:
                Intent intent= new Intent(HomeActivity.this,CategorySelectedActivity.class);
                intent.putExtra("Title","Computer Science");
                startActivity(intent);
                break;
            case R.id.nav_it:
                Intent intentS= new Intent(HomeActivity.this,CategorySelectedActivity.class);
                intentS.putExtra("Title","Information Technology");
                startActivity(intentS);
                break;
            case R.id.nav_ece:
                Intent intentF= new Intent(HomeActivity.this,CategorySelectedActivity.class);
                intentF.putExtra("Title","Electronics and Communications");
                startActivity(intentF);
                break;

        }
        drawer_Layout.closeDrawer(GravityCompat.START);

        return true;
    }


    @Override
    public void onBackPressed(){
        if(drawer_Layout.isDrawerOpen(GravityCompat.START)){
            drawer_Layout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }


}