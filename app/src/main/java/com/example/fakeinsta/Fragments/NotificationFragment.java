package com.example.fakeinsta.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.fakeinsta.Adapters.NotificationAdapter;
import com.example.fakeinsta.Models.Notification;
import com.example.fakeinsta.R;
import com.example.fakeinsta.Widgets.CustomRecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationFragment extends Fragment {

    private List<Notification> mNotif;
    private CustomRecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(lm);

        View empty_view = view.findViewById(R.id.empty_view);
        TextView empty_text = empty_view.findViewById(R.id.empty_text);
        empty_text.setText("No Notifications");

        recyclerView.hideIfEmpty(recyclerView);
        recyclerView.showIfEmpty(empty_view);

        mNotif = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(getContext(), mNotif);
        recyclerView.setAdapter(notificationAdapter);

        readNotifications();

        return view;
    }

    private void readNotifications() {
        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Notifications")
                .child(fuser.getUid());

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mNotif.clear();

                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    mNotif.add(notification);
                }

                Collections.reverse(mNotif);
//                Log.d(TAG, "onDataChange: size: " + mNotif.size());
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
