package com.attendance.clockme;

import com.google.firebase.database.*;

import java.util.HashMap;

public class FirebaseHelper {

    private DatabaseReference databaseReference;

    public FirebaseHelper() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public interface FirebaseNodeCallback {
        void onCallback(HashMap<String, HashMap<String, String>> allData);
    }

    public void fetchDataFromNodes(FirebaseNodeCallback callback) {
        // Nodes to fetch data from
        String[] nodes = {"ClockInPermission", "ClockOutPermission", "LateInRequests", "LateOutRequests"};
        HashMap<String, HashMap<String, String>> allData = new HashMap<>();

        for (String node : nodes) {
            databaseReference.child(node).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        String team = childSnapshot.child("team").getValue(String.class);
                        String status = childSnapshot.child("status").getValue(String.class);

                        HashMap<String, String> nodeData = new HashMap<>();
                        nodeData.put("team", team);
                        nodeData.put("status", status);

                        allData.put(node, nodeData);
                    }

                    // Call the callback after all data is fetched
                    if (allData.size() == nodes.length) {
                        callback.onCallback(allData);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Handle errors here
                    System.out.println("Error fetching data from " + node + ": " + error.getMessage());
                }
            });
        }
    }
}
