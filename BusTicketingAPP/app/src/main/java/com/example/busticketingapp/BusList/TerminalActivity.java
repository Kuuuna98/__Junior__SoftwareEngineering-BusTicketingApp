package com.example.busticketingapp.BusList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.busticketingapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;


public class TerminalActivity extends AppCompatActivity {
    Intent gotoSelectSeat;
    String getId;
    boolean getMember;
    String getName;

    String departureTerminal;
    String destinationTerminal;
    String departureDateString;
    boolean standardArrive;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference().child("Bus");
    ArrayList<Bus> busArrayList = new ArrayList<Bus>();

    TextView departureTerminalName;
    TextView destinationTerminalName;
    TextView departureDate;
    Button sortingStandard;

    private CustomAdapter_forBusList mAdapter;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLinearLayoutManager;
    private int count = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buslist_select_bus);
        departureTerminalName = findViewById(R.id.DepartureTermianal);
        destinationTerminalName = findViewById(R.id.DestinationTermianal);
        departureDate = findViewById(R.id.Date);
        sortingStandard = findViewById(R.id.Sorting);
        sortingStandard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(standardArrive){ // 현재 도착순 정렬 서비스 이용중 -> 출발순
                    ((Button)v).setText("도착순 정렬");
                    for(int i=0;i<busArrayList.size();i++){
                        busArrayList.get(i).standardArrive=false;
                    }
                    standardArrive =false;
                }else{ // 현재 출발순정렬 서비스 이용중 -> 도착순
                    ((Button)v).setText("출발순 정렬");
                    for(int i=0;i<busArrayList.size();i++){
                        busArrayList.get(i).standardArrive=true;
                    }
                    standardArrive = true;
                }
                Collections.sort(busArrayList);
                mAdapter.notifyDataSetChanged();
            }
        });

        departureTerminal = getIntent().getStringExtra("Departure");
        destinationTerminal = getIntent().getStringExtra("Destination");

        departureDateString = getIntent().getStringExtra("Date").replaceAll("/", "");
        getId = getIntent().getStringExtra("Id");
        getMember = getIntent().getBooleanExtra("Member", false);
        getName = getIntent().getStringExtra("UserName");
        Log.v("Subin", "Date check : " + departureDateString);

        departureTerminalName.setText(departureTerminal.split(":")[1]);
        destinationTerminalName.setText(destinationTerminal.split(":")[1]);
        String tempDate = getIntent().getStringExtra("Date");
        tempDate= (tempDate.split("/")[0])+"년 "+ tempDate.split("/")[1]+"월 "+tempDate.split("/")[2]+"일";
        departureDate.setText(tempDate);


        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_bus_list);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);


        mAdapter = new CustomAdapter_forBusList(busArrayList);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new CustomAdapter_forBusList.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                Bus returnBus = busArrayList.get(pos);
/*
                int year = Integer.parseInt(returnBus.getDepartureDate().substring(0,4));
                int month = Integer.parseInt(returnBus.getDepartureDate().substring(4,6));
                int day = Integer.parseInt(returnBus.getDepartureDate().substring(6,8));
                int hour = Integer.parseInt(returnBus.getDepartureTime().split("-")[0].split(":")[0]);
                int min = Integer.parseInt(returnBus.getDepartureTime().split("-")[0].split(":")[1]);
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd@kk:mm");
                String formatDate = sdfNow.format(date);
                int nowYear = Integer.parseInt(formatDate.split("-|@")[0]);
                int nowMonth =Integer.parseInt(formatDate.split("-|@")[1]);
                int nowDay = Integer.parseInt(formatDate.split("-|@")[2]);
                int nowHour = Integer.parseInt(formatDate.split("-|@|:")[3]);
                int nowMin = Integer.parseInt(formatDate.split("-|@|:")[4]);

                Log.v("Test", "현재 : "+ nowYear+"-"+nowMonth+"-"+nowDay+"//"+nowHour+":"+nowMin+":");

                Log.v("Test", "버스 : "+ year+"-"+month+"-"+day+"//"+hour+":"+min+":");

                if(nowYear > year || nowMonth > month || nowDay > day || nowHour > hour || nowMin > min ){
                    Toast.makeText(TerminalActivity.this, "출발시간이 지났습니다.",Toast.LENGTH_SHORT).show();
                    return;
                }*/
                gotoSelectSeat = new Intent(TerminalActivity.this, SelectSeatActivity_General.class);
                gotoSelectSeat.putExtra("Departure", returnBus.getDepartureTerminal());
                gotoSelectSeat.putExtra("Destination", returnBus.getDestinationTerminal());
                gotoSelectSeat.putExtra("Date", returnBus.getDepartureDate());
                gotoSelectSeat.putExtra("Time", returnBus.getDepartureTime());
                gotoSelectSeat.putExtra("Company", returnBus.getBusCompany());
                gotoSelectSeat.putExtra("SeatNum", returnBus.getRemainSeat());

                gotoSelectSeat.putExtra("Id",getId);
                gotoSelectSeat.putExtra("Member",getMember);
                gotoSelectSeat.putExtra("UserName", getName);
                startActivity(gotoSelectSeat);

            }
        });

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLinearLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        myRef.child(departureTerminal).child(destinationTerminal).child(departureDateString).addValueEventListener(valueEventListener);

    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            busArrayList.clear();
            Log.v("Subin", "dataSnapshot getKey : " + dataSnapshot.getKey());
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Log.v("Subin", snapshot.getKey());


                String time = snapshot.getKey();
                for (DataSnapshot snapshotchild : snapshot.getChildren()) {
                    String busCompany = snapshotchild.getKey();
                    int seatNum = 0;
                    for (DataSnapshot snapshotSeatNum : snapshotchild.getChildren()) {
                        if (snapshotSeatNum.getValue().toString().equals("true")) seatNum++;
                    }
                    Bus bus = new Bus();
                    bus.setDepartureTerminal(departureTerminal);
                    bus.setDestinationTerminal(destinationTerminal);
                    bus.setDepartureDate(departureDateString);
                    bus.setBusCompany(busCompany);
                    bus.setDepartureTime(time);
                    bus.setRemainSeat(seatNum);
                    bus.standardArrive = standardArrive;

                    busArrayList.add(bus);
                    Log.v("Subin", "busArrayList Size : " + busArrayList.size());
                }
            }
            Log.v("Subin", "busArrayList Size : " + busArrayList.size());
            Collections.sort(busArrayList);
            mAdapter.notifyDataSetChanged();
            Log.v("Subin", "mAdapter Item Count : " + mAdapter.getItemCount());
        }


        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };


}

