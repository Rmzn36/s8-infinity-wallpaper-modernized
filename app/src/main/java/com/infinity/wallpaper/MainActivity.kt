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
            text = "Duvar Kağıdı Ayarlarını Aç"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#1A73E8"))
            textSize = 16f
            setPadding(40, 25, 40, 25)
            setOnClickListener {
                openWallpaperSettings()
            }
        }

        layout.addView(button)
        setContentView(layout)
    }

    private fun openWallpaperSettings() {
        val componentName = ComponentName(packageName, "$packageName.InfinityWallpaperService")
        
        val intents = arrayOf(
            Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, componentName)
            },
            Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER),
            Intent(Intent.ACTION_SET_WALLPAPER)
        )

        var success = false
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                success = true
                break
            } catch (e: Exception) {
                // Sonraki yöntemi dene
            }
        }

        if (!success) {
            Toast.makeText(this, "Lütfen Ayarlar > Duvar Kağıdı menüsünden seçin.", Toast.LENGTH_LONG).show()
        }
    }
}
