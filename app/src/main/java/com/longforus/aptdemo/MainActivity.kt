package com.longforus.aptdemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import com.longforus.voidbindapi.VoidBind

class MainActivity : AppCompatActivity() {
    //目前kotlin无效,生成的代码提示字段是private的
    //@BindView(R.id.btn)
    public var btn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        VoidBind.bind(this)
        btn?.setOnClickListener {
            Toast.makeText(this, "success", Toast.LENGTH_LONG).show()
        }
    }
}
