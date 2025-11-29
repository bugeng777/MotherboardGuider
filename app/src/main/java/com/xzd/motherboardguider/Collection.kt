package com.xzd.motherboardguider

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView

class Collection : ComponentActivity(){
    private lateinit var hardwareListView:RecyclerView
    private lateinit var collectionBackButton:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection)
        hardwareListView=findViewById(R.id.hardwareListView)
        collectionBackButton=findViewById(R.id.collectionBackButton)
        collectionBackButton.setOnClickListener(object:OnClickListener{
            override fun onClick(v: View?) {
                val intent= Intent(baseContext,MainActivity::class.java)
                startActivity(intent)
            }
        })
    }
}