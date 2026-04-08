package com.intelligentdata.syncapp;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText etName, etCity;
    private Button btnSave, btnSync;
    private ListView recordList;

    private DatabaseHelper dbHelper;
    private ArrayList<String> listData;
    private ArrayAdapter<String> adapter;
    private RequestQueue requestQueue;

    // TODO: Replace this with your actual PHP API URL.
    // If testing on an Android Emulator using XAMPP/WAMP, use http://10.0.2.2/your_folder/sync_api.php
    private static final String API_URL = "https://alphabetswebtech.com/inqtelApi/sync_api.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        etName = findViewById(R.id.etName);
        etCity = findViewById(R.id.etCity);
        btnSave = findViewById(R.id.btnSave);
        btnSync = findViewById(R.id.btnSync);
        recordList = findViewById(R.id.recordList);

        // Initialize Database, List, and Volley Request Queue
        dbHelper = new DatabaseHelper(this);
        listData = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        recordList.setAdapter(adapter);
        requestQueue = Volley.newRequestQueue(this);

        // Load existing records from SQLite on startup
        loadRecords();

        // Save Button Click Listener
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String city = etCity.getText().toString().trim();

            if (!name.isEmpty() && !city.isEmpty()) {
                boolean isInserted = dbHelper.addRecord(name, city);
                if (isInserted) {
                    Toast.makeText(MainActivity.this, "Saved Locally", Toast.LENGTH_SHORT).show();
                    etName.setText("");
                    etCity.setText("");
                    loadRecords(); // Refresh the list to show the new unsynced item
                }
            } else {
                Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });

        // Sync Button Click Listener
        btnSync.setOnClickListener(v -> syncToServer());
    }

    /**
     * Loads all records from the local SQLite database and displays them in the ListView.
     */
    private void loadRecords() {
        listData.clear();
        Cursor cursor = dbHelper.getAllRecords();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
                String city = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CITY));
                int syncStatus = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SYNC_STATUS));

                String statusText = (syncStatus == 1) ? "(Synced)" : "(Pending)";
                listData.add(name + " - " + city + " " + statusText);
            }
        }
        adapter.notifyDataSetChanged();
        cursor.close();
    }

    /**
     * Fetches unsynced records from SQLite and sends them to the PHP MySQL API via Volley.
     */
    private void syncToServer() {
        Cursor cursor = dbHelper.getUnsyncedRecords();

        if (cursor.getCount() == 0) {
            Toast.makeText(this, "All records are already synced!", Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }

        while (cursor.moveToNext()) {
            final int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
            String city = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CITY));

            // Create JSON payload for this specific record
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("name", name);
                jsonBody.put("city", city);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Make Volley POST Request to your PHP API
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, API_URL, jsonBody,
                    response -> {
                        // On Success: Update SQLite sync_status to 1 (Synced)
                        dbHelper.updateSyncStatus(id);
                        loadRecords(); // Refresh UI to update status text from (Pending) to (Synced)
                    },
                    error -> {
                        // On Error: Leave status as 0 (Pending) so it can be retried later
                        Toast.makeText(MainActivity.this, "Sync failed for " + name + ". Check connection.", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    });

            // Add the request to the Volley queue
            requestQueue.add(request);
        }
        Toast.makeText(this, "Syncing started...", Toast.LENGTH_SHORT).show();
        cursor.close();
    }
}