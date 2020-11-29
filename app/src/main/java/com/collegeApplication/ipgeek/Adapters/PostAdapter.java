package com.collegeApplication.ipgeek.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.collegeApplication.ipgeek.CommentsActivity;
import com.collegeApplication.ipgeek.Model.Post;
import com.collegeApplication.ipgeek.Model.User;
import com.collegeApplication.ipgeek.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{

    public Context mContext;
    public List<Post> mPostList;
    private FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPostList) {
        this.mContext = mContext;
        this.mPostList = mPostList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(mContext).inflate(R.layout.question_retrieved_layout,parent,false);


        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        final Post post = mPostList.get(position);
        if(post.getQuestionimage()==null){
            holder.questionImage.setVisibility(View.GONE);
        }
        else
        holder.questionImage.setVisibility(View.VISIBLE);

        Glide.with(mContext).load(post.getQuestionimage()).into(holder.questionImage);

        holder.expandableTextView.setText(post.getQuestion());
        holder.topicTextView.setText(post.getTopic());
        holder.askedOnTextView.setText(post.getDate());
        getLikes(holder.likes,post.getPostid());
        getDisLikes(holder.dislikes,post.getPostid());



        publisherInformation(holder.publisher_profile_image,holder.asked_by_TextView,post.getPublisher());
        isLiked(post.getPostid(), holder.like);
        isDisLiked(post.getPostid(),holder.dislike);

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.like.getTag().equals("Like") && holder.dislike.getTag().equals("Dislike")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid()).child(firebaseUser.getUid()).setValue(true);

                }
                else if (holder.like.getTag().equals("Like") && holder.dislike.getTag().equals("Disliked")){
                    FirebaseDatabase.getInstance().getReference().child("Dislikes").child(post.getPostid()).child(firebaseUser.getUid()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid()).child(firebaseUser.getUid()).setValue(true);


                }else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid()).child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.dislike.getTag().equals("Dislike") && holder.like.getTag().equals("Like")){
                    FirebaseDatabase.getInstance().getReference().child("Dislikes").child(post.getPostid()).child(firebaseUser.getUid()).setValue(true);
                }else if (holder.dislike.getTag().equals("Dislike") && holder.like.getTag().equals("Liked")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid()).child(firebaseUser.getUid()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Dislikes").child(post.getPostid()).child(firebaseUser.getUid()).setValue(true);
                }else {
                    FirebaseDatabase.getInstance().getReference().child("Dislikes").child(post.getPostid()).child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(mContext,CommentsActivity.class);
                intent.putExtra("postid",post.getPostid());
                intent.putExtra("publisher",post.getPublisher());
                mContext.startActivity(intent);
            }
        });
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid",post.getPostid());
                intent.putExtra("publisher",post.getPublisher());
                mContext.startActivity(intent);
            }
        });







    }

    @Override
    public int getItemCount() {
        return mPostList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView publisher_profile_image;
        public TextView asked_by_TextView, likes,dislikes, comments;
        public ImageView more, questionImage, like, dislike, comment, save;
        public TextView topicTextView, askedOnTextView;
        public ExpandableTextView expandableTextView;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            publisher_profile_image=itemView.findViewById(R.id.publisher_profile_image);
            asked_by_TextView=itemView.findViewById(R.id.asked_by_TextView);
            likes=itemView.findViewById(R.id.likes);
            dislikes=itemView.findViewById(R.id.dislikes);
            comments=itemView.findViewById(R.id.comments);
            more=itemView.findViewById(R.id.more);
            questionImage=itemView.findViewById(R.id.questionImage);
            like=itemView.findViewById(R.id.like);
            dislike=itemView.findViewById(R.id.dislike);
            comment=itemView.findViewById(R.id.comment);
            save=itemView.findViewById(R.id.save);
            topicTextView =itemView.findViewById(R.id.topicTextView);
            askedOnTextView=itemView.findViewById(R.id.askedOnTextView);
            expandableTextView=itemView.findViewById(R.id.expand_text_view);

        }
    }

    private void publisherInformation(final CircleImageView publisherImage, final TextView askedBy, String userId){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("users").child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user= snapshot.getValue(User.class);
                Glide.with(mContext).load(user.getProfileimageurl()).into(publisherImage);
                askedBy.setText(user.getFullname());


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void isLiked(String postid, final ImageView imageView){
        final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("Likes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.liked);
                    imageView.setTag("Liked");
                }
                else{
                    imageView.setImageResource(R.drawable.like);
                    imageView.setTag("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void isDisLiked(String postid, final ImageView imageView){
        final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("Dislikes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.disliked);
                    imageView.setTag("Disliked");
                }
                else{
                    imageView.setImageResource(R.drawable.dislike);
                    imageView.setTag("Dislike");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getLikes(final TextView Likes, String postid){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Likes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long numberOfLikes= snapshot.getChildrenCount();
                int NOL= (int) numberOfLikes;
                if(NOL>1){
                    Likes.setText(snapshot.getChildrenCount()+" Likes");
                }
                else if(NOL==0){
                    Likes.setText("0 Likes");
                }
                else{
                    Likes.setText(snapshot.getChildrenCount()+" Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDisLikes(final TextView DisLikes, String postid){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Dislikes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long numberOfDisLikes= snapshot.getChildrenCount();
                int NOD= (int) numberOfDisLikes;
                if(NOD>1){
                    DisLikes.setText(snapshot.getChildrenCount()+" Dislikes");
                }
                else if(NOD==0){
                    DisLikes.setText("0 Dislikes");
                }
                else{
                   DisLikes.setText(snapshot.getChildrenCount()+" Dislike");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
