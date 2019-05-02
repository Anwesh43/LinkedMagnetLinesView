package com.anwesh.uiprojects.linkedmagnetlinesview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.magnetlinesview.MagnetLinesView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MagnetLinesView.create(this)
    }
}
