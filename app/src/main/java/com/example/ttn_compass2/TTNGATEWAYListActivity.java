package com.example.ttn_compass2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class TTNGATEWAYListActivity extends AppCompatActivity {

private String TAG = TTNGATEWAYListActivity.class.getSimpleName();
private ListView lv;
    TextView txtView;

ArrayList<HashMap<String, String>> contactList;

@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttngatewaylist);

        contactList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);
        txtView = (TextView)findViewById(R.id.largetext);

        new GetContacts().execute();
        }

private class GetContacts extends AsyncTask<Void, Void, Void> {
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Toast.makeText(TTNGATEWAYListActivity.this,"Json Data is downloading",Toast.LENGTH_LONG).show();

    }

    @Override
    protected Void doInBackground(Void... arg0) {
        HttpHandler sh = new HttpHandler();
        // Making a request to url and getting response
        String url1 = "https://api.androidhive.info/contacts/";
        String url2 = "https://www.thethingsnetwork.org/gateway-data/country/es";
        String jsonStr = sh.makeServiceCall(url1);

        String sFileName = "ttn_spain_1.json";

        String note = "Hello";

        try {

            FileOutputStream fos = null;
            fos = openFileOutput(sFileName, MODE_PRIVATE);
            fos.write(note.getBytes());

            //Toast.makeText(TTNGATEWAYListActivity.this, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }



        Log.e(TAG, "Response from url: " + jsonStr);
        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);

                // Getting JSON Array node
                JSONArray contacts = jsonObj.getJSONArray("contacts");

                // looping through All Contacts
                for (int i = 0; i < contacts.length(); i++) {
                    JSONObject c = contacts.getJSONObject(i);
                    String id = c.getString("id");
                    String name = c.getString("name");
                    String email = c.getString("email");
                    String address = c.getString("address");
                    String gender = c.getString("gender");

                    // Phone node is JSON Object
                    JSONObject phone = c.getJSONObject("phone");
                    String mobile = phone.getString("mobile");
                    String home = phone.getString("home");
                    String office = phone.getString("office");

                    // tmp hash map for single contact
                    HashMap<String, String> contact = new HashMap<>();

                    // adding each child node to HashMap key => value
                    contact.put("id", id);
                    contact.put("name", name);
                    contact.put("email", email);
                    contact.put("mobile", mobile);

                    // adding contact to contact list
                    contactList.add(contact);
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });

            }

        } else {
            Log.e(TAG, "Couldn't get json from server.");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Couldn't get json from server. Check LogCat for possible errors!",
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        txtView.setText("");

        ListAdapter adapter = new SimpleAdapter(TTNGATEWAYListActivity.this, contactList,
                R.layout.list_item, new String[]{ "email","mobile"},
                new int[]{R.id.id, R.id.location});
        lv.setAdapter(adapter);
    }
}
}