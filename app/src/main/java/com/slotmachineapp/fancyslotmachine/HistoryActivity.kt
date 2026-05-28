package com.slotmachineapp.fancyslotmachine

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val content = findViewById<TextView>(R.id.historyContent)
        content.text = buildHistoryText()
    }

    private fun buildHistoryText(): String {
        val prefs = getSharedPreferences("slot_prefs", MODE_PRIVATE)
        val raw = prefs.getString("win_history", "").orEmpty()
        if (raw.isBlank()) return "No wins yet.\n\nSpin to start building your history."

        val formatter = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
        return raw.split(";")
            .mapNotNull { item ->
                val parts = item.split("|")
                if (parts.size < 3) return@mapNotNull null
                val ts = parts[0].toLongOrNull() ?: return@mapNotNull null
                val amount = parts[1]
                val label = parts[2]
                "• ${formatter.format(Date(ts))}  $label  +$amount"
            }
            .joinToString("\n\n")
    }
}