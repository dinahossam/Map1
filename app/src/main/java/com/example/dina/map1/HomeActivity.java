package com.example.dina.map1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {

    String[] rooms = { "room8","room9","Java","C#","PHP"};
    private Button go;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        go = (Button) findViewById(R.id.button);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.select_dialog_singlechoice, rooms);
        //Find TextView control
        final AutoCompleteTextView acTextView = (AutoCompleteTextView) findViewById(R.id.rooms);
        //Set the number of characters the user must type before the drop down list is shown
        acTextView.setThreshold(1);
        //Set the adapter
        acTextView.setAdapter(adapter);

        go.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), MainActivity.class);
                i.putExtra("dest", acTextView.getText().toString());
                startActivity(i);
            }
        });
    }

}
