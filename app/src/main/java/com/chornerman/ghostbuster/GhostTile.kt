package com.chornerman.ghostbuster

import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import java.io.DataOutputStream

class GhostTile : TileService() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        if (!sharedPreferences.getBoolean("IS_SCREEN_LIFTED", false)) {
            setTileNormalScreen()
        } else {
            setTileLiftedScreen()
        }
        qsTile.updateTile()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        setTileNormalScreen()
        qsTile.updateTile()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        if (qsTile.state == Tile.STATE_ACTIVE) {
            resetScreen()
        }
    }

    override fun onClick() {
        super.onClick()
        if (qsTile.state == Tile.STATE_INACTIVE) {
            setTileLiftedScreen()
            liftScreen()
        } else {
            setTileNormalScreen()
            resetScreen()
        }
        qsTile.updateTile()
    }

    private fun setTileLiftedScreen() {
        qsTile.state = Tile.STATE_ACTIVE
        qsTile.label = "Screen lifted"
    }

    private fun setTileNormalScreen() {
        qsTile.state = Tile.STATE_INACTIVE
        qsTile.label = "Screen normal"
    }

    private fun liftScreen() {
        executor("wm overscan 0,0,0,80")
        setFlag(true)
    }

    private fun resetScreen() {
        executor("wm overscan reset")
        setFlag(false)
    }

    private fun setFlag(value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("IS_SCREEN_LIFTED", value)
        editor.apply()
    }

    private fun executor(command: String) {
        val process = Runtime.getRuntime().exec("su")
        val dataOutputStream = DataOutputStream(process.outputStream)
        dataOutputStream.writeBytes("$command\nexit\n")
        dataOutputStream.flush()
        dataOutputStream.close()
    }
}