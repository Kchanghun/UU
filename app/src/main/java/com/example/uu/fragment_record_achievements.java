package com.example.uu;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Semaphore;

public class fragment_record_achievements extends Fragment {

    GridView gridView;

    DatabaseHelper dbHelper;
    SQLiteDatabase sqLiteDb;

    // data for checking achievement complete
    private int total_distance=0;
    private int max_distance=0;
    private int total_time=0;
    private int max_time=0;
    private int total_recruit_time=0;
    private int level;

    //region variable for checking each achievements
    private boolean flag_maxDistance_3=false;
    private boolean flag_maxDistance_5=false;
    private boolean flag_maxDistance_10=false;
    private boolean flag_totalDistance_10=false;
    private boolean flag_totalDistance_50=false;
    private boolean flag_totalDistance_100=false;
    private boolean flag_maxTime_20=false;
    private boolean flag_maxTime_40=false;
    private boolean flag_maxTime_60=false;
    private boolean flag_totalTime_300=false;
    private boolean flag_totalTime_500=false;
    private boolean flag_totalTime_1000=false;
    private boolean flag_recruitJoinTime_5=false;
    private boolean flag_recruitJoinTime_10=false;
    private boolean flag_recruitJoinTime_30=false;

    // percentage of achievement
    private float percentage_maxDistance_3=0;
    private float percentage_maxDistance_5=0;
    private float percentage_maxDistance_10=0;
    private float percentage_totalDistance_10=0;
    private float percentage_totalDistance_50=0;
    private float percentage_totalDistance_100=0;
    private float percentage_maxTime_20=0;
    private float percentage_maxTime_40=0;
    private float percentage_maxTime_60=0;
    private float percentage_totalTime_300=0;
    private float percentage_totalTime_500=0;
    private float percentage_totalTime_1000=0;
    private float percentage_recruitJoinTime_5=0;
    private float percentage_recruitJoinTime_10=0;
    private float percentage_recruitJoinTime_30=0;

    private ProgressBar levelBar;
    private TextView levelPercentText;
    private ImageView levelImage;
    private TextView levelText;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseAuth mFirebaseAuth;
    private Integer getUserRecruitJoinNumber;
    private achievementAdapter adapter;
    private Integer getNum;

    public interface OnGetDataListener {
        //this is for callbacks
        void onSuccess(DataSnapshot dataSnapshot);
        void onStart();
        void onFailure();
    }

    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView=(ViewGroup) inflater.inflate(R.layout.fragment_record_achievements, container, false);
        gridView=(GridView) rootView.findViewById(R.id.achievementContainer);
        database = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        databaseReference = database.getReference("UU");
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

        adapter=new achievementAdapter();

        level = 1;

        levelBar = (ProgressBar) rootView.findViewById(R.id.levelProgressBar);
        levelImage = rootView.findViewById(R.id.levelImage);
        levelText = rootView.findViewById(R.id.levelText);
        levelPercentText = rootView.findViewById(R.id.levelPercentText);


        readData(databaseReference.child("UserAccount").child(firebaseUser.getUid()).child("userRecruitJoinNumber"), new OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {

                levelImage.setImageResource(R.drawable.level1);
                levelText.setText("Level 1");
                levelPercentText.setText("0%");
                levelBar.setProgress(0);
                getNum = dataSnapshot.getValue(Integer.class);
                total_recruit_time = getNum;

                getRecordData();
                setFlags();
                setPercentage();
                setGrid();
            }
            @Override
            public void onStart() {

            }

            @Override
            public void onFailure() {

            }
        });
        
        return rootView;
    }

    private void setGrid(){
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

        adapter.addItem(new achievementObject("?????????","????????? 3km ?????????",flag_maxDistance_3,Math.round(percentage_maxDistance_3*1000)/10));
        adapter.addItem(new achievementObject("?????? ????????????","????????? 5km ?????????",flag_maxDistance_5,Math.round(percentage_maxDistance_5*1000)/10));
        adapter.addItem(new achievementObject("????????? ???","????????? 10km ?????????",flag_maxDistance_10,Math.round(percentage_maxDistance_10*1000)/10));

        adapter.addItem(new achievementObject("????????? ?????????!","?????? ???????????? ?????? ??? ??????!\n\n??? 10KM??? ????????? ????????? ????????????\n\n??????????????????!!",flag_totalDistance_10,Math.round(percentage_totalDistance_10*1000)/10));
        adapter.addItem(new achievementObject("????????? ?????????","??? 50km ?????????",flag_totalDistance_50,Math.round(percentage_totalDistance_50*1000)/10));
        adapter.addItem(new achievementObject("???????????? ??????","??? 100km ?????????",flag_totalDistance_100,Math.round(percentage_totalDistance_100*1000)/10));

        adapter.addItem(new achievementObject("?????? ??????..!","20????????? ?????????",flag_maxTime_20,Math.round(percentage_maxTime_20*1000)/10));
        adapter.addItem(new achievementObject("??? ?????? ???????","40????????? ?????????",flag_maxTime_40,Math.round(percentage_maxTime_40*1000)/10));
        adapter.addItem(new achievementObject("????????? ?????????????","60????????? ?????????",flag_maxTime_60,Math.round(percentage_maxTime_60*1000)/10));

        adapter.addItem(new achievementObject("?????? ??????","??? ?????? ?????? 300???",flag_totalTime_300,Math.round(percentage_totalTime_300*1000)/10));
        adapter.addItem(new achievementObject("??? ??? ??????????","??? ?????? ?????? 500???",flag_totalTime_500,Math.round(percentage_totalTime_500*1000)/10));
        adapter.addItem(new achievementObject("??? ?????????\n?????? ????????????","??? ?????? ?????? 1000???",flag_totalTime_1000,Math.round(percentage_totalTime_1000*1000)/10));

        adapter.addItem(new achievementObject("?????? ?????? ?????????!","??????????????? ?????? 5???",flag_recruitJoinTime_5,Math.round(percentage_recruitJoinTime_5*1000)/10));
        adapter.addItem(new achievementObject("?????? ?????? ???????","??????????????? ?????? 10???",flag_recruitJoinTime_10,Math.round(percentage_recruitJoinTime_10*1000)/10));
        adapter.addItem(new achievementObject("??????????????? ??????","??????????????? ?????? 30???",flag_recruitJoinTime_30,Math.round(percentage_recruitJoinTime_30*1000)/10));
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                achievementObject item=(achievementObject) adapter.getItem(i);

                Dialog achievementDlg=new Dialog(getActivity());
                achievementDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
                achievementDlg.setContentView(R.layout.dialog_achievement);
                achievementDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                TextView title=achievementDlg.findViewById(R.id.achTitle);
                TextView body=achievementDlg.findViewById(R.id.achBody);
                TextView percentage=achievementDlg.findViewById(R.id.achPercentage);
                Button button=achievementDlg.findViewById(R.id.achBtn);

                title.setText(item.getObjName());
                body.setText(item.getDescription());
                percentage.setText("????????? : "+item.getAchievement()+"%");

                achievementDlg.show();

                button.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) { achievementDlg.dismiss(); }});



            }
        });

        updateLevelImg();
        databaseReference.child("UserAccount").child(firebaseUser.getUid()).child("userLevel").setValue(level);

    }

    private void getRecordData()
    {
        dbHelper=new DatabaseHelper(getContext());
        sqLiteDb=dbHelper.getReadableDatabase();


        // Query
        String queryDistanceSum="SELECT SUM("+DatabaseHelper.RUNNING_DISTANCE+") FROM "+DatabaseHelper.TABLE_NAME+";";
        String queryDistanceMax="SELECT MAX("+DatabaseHelper.RUNNING_DISTANCE+") FROM "+DatabaseHelper.TABLE_NAME+";";
        String queryTimeSum="SELECT SUM("+DatabaseHelper.RUNNING_TIME+") FROM "+DatabaseHelper.TABLE_NAME+";";
        String queryTimeMax="SELECT MAX("+DatabaseHelper.RUNNING_TIME+") FROM "+DatabaseHelper.TABLE_NAME+";";

        Cursor cursor;

        cursor = sqLiteDb.rawQuery(queryDistanceSum,null);
        cursor.moveToFirst();
        total_distance=cursor.getInt(0);

        cursor = sqLiteDb.rawQuery(queryDistanceMax,null);
        cursor.moveToFirst();
        max_distance=cursor.getInt(0);

        cursor = sqLiteDb.rawQuery(queryTimeSum,null);
        cursor.moveToFirst();
        total_time=cursor.getInt(0);

        cursor = sqLiteDb.rawQuery(queryTimeMax,null);
        cursor.moveToFirst();
        max_time=cursor.getInt(0);

        cursor.close();
        sqLiteDb.close();
    }

    private void setFlags()
    {
        if(max_distance>3000) {
            flag_maxDistance_3 = true;
            levelBar.incrementProgressBy(25);
            checkLevelBar();
        }

        if(max_distance>5000) {
            flag_maxDistance_5 = true;
            levelBar.incrementProgressBy(25);
            checkLevelBar();
        }
        if(max_distance>10000) {
            flag_maxDistance_10 = true;
            levelBar.incrementProgressBy(25);
            checkLevelBar();
        }

        if(total_distance>10000) {
            flag_totalDistance_10 = true;
            levelBar.incrementProgressBy(25);
            checkLevelBar();
        }

        if(total_distance>50000) {
            flag_totalDistance_50 = true;
            levelBar.incrementProgressBy(25);
            checkLevelBar();
        }

        if(total_distance>100000) {
            flag_totalDistance_100 = true;
            levelBar.incrementProgressBy(25);
            checkLevelBar();
        }

        if(max_time>20) {
            flag_maxTime_20 = true;
            levelBar.incrementProgressBy(25);
            checkLevelBar();
        }

        if(max_time>40) {
            flag_maxTime_40 = true;
            levelBar.incrementProgressBy(25);
            checkLevelBar();
        }

        if(max_time>60) {
            flag_maxTime_60 = true;
            levelBar.incrementProgressBy(25);
            checkLevelBar();
        }

        if(total_time>300) {
            flag_totalTime_300 = true;
            levelBar.incrementProgressBy(25);
            checkLevelBar();
        }

        if(total_time>500) {
            flag_totalTime_500 = true;
            levelBar.incrementProgressBy(25);
            checkLevelBar();
        }

        if(total_time>1000) {
            flag_totalTime_1000 = true;
            levelBar.incrementProgressBy(25);
            checkLevelBar();
        }

        if(total_recruit_time>=5) {
            flag_recruitJoinTime_5 = true;
            levelBar.incrementProgressBy(25);
            checkLevelBar();
        }

        if(total_recruit_time>=10) {
            flag_recruitJoinTime_10 = true;
            levelBar.incrementProgressBy(25);
            checkLevelBar();
        }

        if(total_recruit_time>=30) {
            flag_recruitJoinTime_30 = true;
            levelBar.incrementProgressBy(25);
            checkLevelBar();
        }


        levelPercentText.setText(levelBar.getProgress() + "%");
    }

    private void setPercentage(){
        if ( max_distance>=10000){
            percentage_maxDistance_3=1;
            percentage_maxDistance_5=1;
            percentage_maxDistance_10=1;
        }
        else if (max_distance>=5000){
            percentage_maxDistance_3=1;
            percentage_maxDistance_5=1;
            percentage_maxDistance_10=(float) max_distance/10000;
        }
        else if(max_distance>=3000){
            percentage_maxDistance_3=1;
            percentage_maxDistance_5=(float) max_distance/5000;
            percentage_maxDistance_10=(float) max_distance/10000;
        }
        else{
            percentage_maxDistance_3=(float) max_distance/3000;
            percentage_maxDistance_5=(float) max_distance/5000;
            percentage_maxDistance_10=(float) max_distance/10000;
        }

        if ( total_distance>=100000){
            percentage_totalDistance_10=1;
            percentage_totalDistance_50=1;
            percentage_totalDistance_100=1;
        }
        else if (total_distance>=50000){
            percentage_totalDistance_10=1;
            percentage_totalDistance_50=1;
            percentage_totalDistance_100=(float) total_distance/100000;
        }
        else if(total_distance>=10000){
            percentage_totalDistance_10=1;
            percentage_totalDistance_50=(float) total_distance/50000;
            percentage_totalDistance_100=(float) total_distance/100000;
        }
        else{
            percentage_totalDistance_10=(float) total_distance/10000;
            percentage_totalDistance_50=(float) total_distance/50000;
            percentage_totalDistance_100=(float) total_distance/100000;
        }

        if ( max_time>=60){
            percentage_maxTime_20=1;
            percentage_maxTime_40=1;
            percentage_maxTime_60=1;
        }
        else if (max_time>=40){
            percentage_maxTime_20=1;
            percentage_maxTime_40=1;
            percentage_maxTime_60=(float) max_time/60;
        }
        else if(max_time>=20){
            percentage_maxTime_20=1;
            percentage_maxTime_40=(float) max_time/40;
            percentage_maxTime_60=(float) max_time/60;
        }
        else{
            percentage_maxTime_20=(float) max_time/20;
            percentage_maxTime_40=(float) max_time/40;
            percentage_maxTime_60=(float) max_time/60;
        }

        if ( total_time>=1000){
            percentage_totalTime_300=1;
            percentage_totalTime_500=1;
            percentage_totalTime_1000=1;
        }
        else if (total_time>=500){
            percentage_totalTime_300=1;
            percentage_totalTime_500=1;
            percentage_totalTime_1000=(float) total_time/1000;
        }
        else if(total_time>=300){
            percentage_totalTime_300=1;
            percentage_totalTime_500=(float) total_time/500;
            percentage_totalTime_1000=(float) total_time/1000;
        }
        else{
            percentage_totalTime_300=(float) total_time/300;
            percentage_totalTime_500=(float) total_time/500;
            percentage_totalTime_1000=(float) total_time/1000;
        }

        if ( total_recruit_time>=30){
            percentage_recruitJoinTime_5=1;
            percentage_recruitJoinTime_10=1;
            percentage_recruitJoinTime_30=1;
        }
        else if (total_recruit_time>=10){
            percentage_recruitJoinTime_5=1;
            percentage_recruitJoinTime_10=1;
            percentage_recruitJoinTime_30=(float) total_recruit_time/30;
        }
        else if(total_recruit_time>=5){
            percentage_recruitJoinTime_5=1;
            percentage_recruitJoinTime_10=(float) total_recruit_time/10;
            percentage_recruitJoinTime_30=(float) total_recruit_time/30;
        }
        else{
            percentage_recruitJoinTime_5=(float) total_recruit_time/5;
            percentage_recruitJoinTime_10=(float) total_recruit_time/10;
            percentage_recruitJoinTime_30=(float) total_recruit_time/30;
        }
    }

    private void checkLevelBar(){
        int progress = levelBar.getProgress();

        if(progress == 100){
            levelBar.setProgress(0);
            level += 1;
            levelText.setText("Level " + level);
        }

    }
    private void updateLevelImg(){
        switch (level){
            case 2: levelImage.setImageResource(R.drawable.level2);
                break;
            case 3: levelImage.setImageResource(R.drawable.level3);
                break;
            case 4: levelImage.setImageResource(R.drawable.level4);
                break;
            case 5: levelImage.setImageResource(R.drawable.level5);
                break;
        }
    }

    public void readData(DatabaseReference ref, final OnGetDataListener listener) {
        listener.onStart();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listener.onSuccess(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure();
            }
        });
    }
}