package com.infinity.wallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(packageName, InfinityWallpaperService::class.java.name)
            )
        }
        startActivity(intent)
        finish()
    }
}
