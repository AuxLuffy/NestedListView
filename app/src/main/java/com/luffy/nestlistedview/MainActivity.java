package com.luffy.nestlistedview;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private NestedListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (NestedListView) findViewById(R.id.list_view);
        String[] arr = new String[100];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i + "";
        }
        mListView.setAdapter(new ArrayAdapter(this, R.layout.list_item_layout, R.id.text_name, arr));
    }
}
