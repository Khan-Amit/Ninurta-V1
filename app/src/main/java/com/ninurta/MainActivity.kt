package com.ninurta

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var etData: EditText
    private lateinit var btnWrite: Button
    private lateinit var btnRead: Button

    // Load native library
    init {
        System.loadLibrary("ninurta")
    }

    external fun nativeFormat(path: String, sizeMb: Int): Boolean
    external fun nativeWrite(domain: Int, desire: Float, data: String): Boolean
    external fun nativeRead(offset: Int): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        etData = findViewById(R.id.etData)
        btnWrite = findViewById(R.id.btnWrite)
        btnRead = findViewById(R.id.btnRead)

        // Create a test arena on first launch if needed
        val arenaPath = File(filesDir, "csfs_arena.bin").absolutePath
        if (!File(arenaPath).exists()) {
            nativeFormat(arenaPath, 50) // 50 MB arena
            tvStatus.text = "Arena formatted at $arenaPath"
        } else {
            tvStatus.text = "Arena exists at $arenaPath"
        }

        btnWrite.setOnClickListener {
            val data = etData.text.toString()
            if (data.isNotEmpty()) {
                val success = nativeWrite(1, 0.8f, data)
                if (success) {
                    tvStatus.text = "Written: $data"
                    etData.text.clear()
                } else {
                    Toast.makeText(this, "Write failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnRead.setOnClickListener {
            val result = nativeRead(0)
            tvStatus.text = "Read slice: $result"
        }
    }
}
