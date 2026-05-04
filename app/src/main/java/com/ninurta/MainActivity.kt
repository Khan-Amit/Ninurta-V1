package com.ninurta

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var etData: EditText
    private lateinit var btnWrite: Button
    private lateinit var btnRead: Button
    private lateinit var seekDesire: SeekBar
    private lateinit var tvDesire: TextView

    private val arenaFile by lazy { File(filesDir, "csfs_arena.bin") }
    private val headerSize = 16
    private var head = 0L
    private var tail = 0L
    private var arenaSize = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        etData = findViewById(R.id.etData)
        btnWrite = findViewById(R.id.btnWrite)
        btnRead = findViewById(R.id.btnRead)
        seekDesire = findViewById(R.id.seekDesire)
        tvDesire = findViewById(R.id.tvDesire)

        seekDesire.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                tvDesire.text = "Desire: ${progress / 100.0}"
            }
            override fun onStartTrackingTouch(seek: SeekBar) {}
            override fun onStopTrackingTouch(seek: SeekBar) {}
        })
        seekDesire.progress = 80

        if (!arenaFile.exists()) {
            formatArena(5 * 1024 * 1024)
        } else {
            loadHeaders()
        }
        updateStatus()

        btnWrite.setOnClickListener {
            val data = etData.text.toString()
            if (data.isNotEmpty()) {
                val desire = seekDesire.progress / 100.0f
                writeSlice(1, desire, data)
                etData.text.clear()
                updateStatus()
            } else {
                Toast.makeText(this, "Enter some data", Toast.LENGTH_SHORT).show()
            }
        }

        btnRead.setOnClickListener {
            readAllSlices()
        }
    }

    private fun formatArena(sizeBytes: Int) {
        RandomAccessFile(arenaFile, "rw").use { raf ->
            raf.setLength(sizeBytes.toLong())
            raf.seek(0)
            raf.writeLong(0)
            raf.writeLong(0)
        }
        arenaSize = sizeBytes.toLong()
        head = 0
        tail = 0
        tvStatus.text = "Arena formatted (${sizeBytes / 1024 / 1024} MB)"
    }

    private fun loadHeaders() {
        RandomAccessFile(arenaFile, "r").use { raf ->
            raf.seek(0)
            head = raf.readLong()
            tail = raf.readLong()
            arenaSize = raf.length()
        }
    }

    private fun saveHeaders() {
        RandomAccessFile(arenaFile, "rw").use { raf ->
            raf.seek(0)
            raf.writeLong(head)
            raf.writeLong(tail)
        }
    }

    private fun writeSlice(domain: Int, desire: Float, data: String) {
        val timestamp = System.currentTimeMillis()
        val dataBytes = data.toByteArray()
        val baos = ByteArrayOutputStream()
        baos.write(intToBytes(domain))
        baos.write(floatToBytes(desire))
        baos.write(longToBytes(timestamp))
        baos.write(intToBytes(dataBytes.size))
        baos.write(dataBytes)
        val slice = baos.toByteArray()

        RandomAccessFile(arenaFile, "rw").use { raf ->
            raf.seek(headerSize + (head % (arenaSize - headerSize)))
            raf.write(slice)
            head += slice.size
            saveHeaders()
        }
        pruneIfNeeded()
    }

    private fun pruneIfNeeded() {
        val free = (arenaSize - headerSize) - (head - tail)
        if (free < (arenaSize - headerSize) * 0.1) {
            // For demo, just delete one slice at tail (simplified)
            RandomAccessFile(arenaFile, "rw").use { raf ->
                raf.seek(headerSize + (tail % (arenaSize - headerSize)))
                // Read slice length
                raf.skipBytes(4+4+8) // domain, desire, timestamp
                val lenBytes = ByteArray(4)
                raf.read(lenBytes)
                val len = bytesToInt(lenBytes)
                tail += (4+4+8+4+len)
                saveHeaders()
            }
        }
    }

    private fun readAllSlices() {
        val result = StringBuilder()
        var pos = tail
        while (pos < head) {
            RandomAccessFile(arenaFile, "r").use { raf ->
                raf.seek(headerSize + (pos % (arenaSize - headerSize)))
                val domainBytes = ByteArray(4)
                raf.read(domainBytes)
                val desireBytes = ByteArray(4)
                raf.read(desireBytes)
                val desire = bytesToFloat(desireBytes)
                raf.skipBytes(8) // timestamp
                val lenBytes = ByteArray(4)
                raf.read(lenBytes)
                val len = bytesToInt(lenBytes)
                val dataBytes = ByteArray(len)
                raf.read(dataBytes)
                val data = String(dataBytes)
                result.append("[$pos] desire=$desire, data=$data\n")
                pos += (4+4+8+4+len)
            }
        }
        tvStatus.text = result.ifEmpty { "No slices" }
    }

    private fun updateStatus() {
        val used = head - tail
        val free = (arenaSize - headerSize) - used
        tvStatus.text = "Head:$head Tail:$tail Used:${used/1024}KB Free:${free/1024}KB"
    }

    private fun floatToBytes(f: Float) = ByteBuffer.allocate(4).putFloat(f).array()
    private fun bytesToFloat(b: ByteArray) = ByteBuffer.wrap(b).float
    private fun longToBytes(l: Long) = ByteBuffer.allocate(8).putLong(l).array()
    private fun bytesToLong(b: ByteArray) = ByteBuffer.wrap(b).long
    private fun intToBytes(i: Int) = ByteBuffer.allocate(4).putInt(i).array()
    private fun bytesToInt(b: ByteArray) = ByteBuffer.wrap(b).int
}
