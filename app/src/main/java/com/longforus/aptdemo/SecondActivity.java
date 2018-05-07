package com.longforus.aptdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.longforus.voidbindanno.BindView;
import com.longforus.voidbindapi.VoidBind;

public class SecondActivity extends AppCompatActivity {

    @BindView(R.id.button)
    Button mButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        VoidBind.bind(this);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SecondActivity.this,"success",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
