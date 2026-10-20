package com.infinity.wallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0A0A1A"))
        }

        val button = Button(this).apply {
            text = "S8 Infinity Duvar Kağıdını Uygula"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#1A73E8"))
            textSize = 16f
            setPadding(40, 25, 40, 25)
            setOnClickListener {
                setLiveWallpaper()
            }
        }

        layout.addView(button)
        setContentView(layout)
    }

    private fun setLiveWallpaper() {
        try {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(packageName, "$packageName.InfinityWallpaperService")
                )
            }
            startActivity(intent)
        } catch (e: Exception) {
            val fallbackIntent = Intent(Intent.ACTION_SET_WALLPAPER)
            startActivity(fallbackIntent)
        }
    }
}
