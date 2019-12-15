package com.giz.androiduikit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.giz.android.uikit.ExpandableCircleMenu
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ecm_layout.mMenuItemClickListener = object : ExpandableCircleMenu.OnMenuItemClickListener {
            override fun onClick(v: View, menuId: Int) {
                when(menuId) {
                    R.id.ecm_item1 -> Toast.makeText(this@MainActivity, "Item1", Toast.LENGTH_SHORT).show()
                    R.id.ecm_item2 -> Toast.makeText(this@MainActivity, "Item2", Toast.LENGTH_SHORT).show()
                    R.id.ecm_item3 -> Toast.makeText(this@MainActivity, "Item3", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
