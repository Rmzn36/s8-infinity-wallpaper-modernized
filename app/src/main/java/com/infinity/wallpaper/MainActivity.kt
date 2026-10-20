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
import android.widget.Toast

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
                openLiveWallpaperPicker()
            }
        }

        layout.addView(button)
        setContentView(layout)
    }

    private fun openLiveWallpaperPicker() {
        val componentName = ComponentName(packageName, "$packageName.InfinityWallpaperService")
        
        // Yöntem 1: Doğrudan servisin önizleme ekranını tetikleme
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, componentName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Yöntem 2: Samsung özel duvar kağıdı yönlendirmesi
            try {
                val samsungIntent = Intent("android.service.wallpaper.CHANGE_LIVE_WALLPAPER").apply {
                    putExtra("android.service.wallpaper.extra.LIVE_WALLPAPER_COMPONENT", componentName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(samsungIntent)
            } catch (ex: Exception) {
                // Yöntem 3: Genel canlı duvar kağıdı listesini açma
                try {
                    val chooserIntent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(chooserIntent)
                } catch (lastEx: Exception) {
                    Toast.makeText(this, "Canlı duvar kağıdı seçicisi başlatılamadı.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
