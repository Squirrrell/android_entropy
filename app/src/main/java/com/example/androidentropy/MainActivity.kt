package com.example.androidentropy

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import kotlin.math.ln

class MainActivity : AppCompatActivity() {
	private var lastHValue: String? = null
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		// Force night mode to ensure dark theme background
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
		setContentView(R.layout.activity_main)

		val input = findViewById<EditText>(R.id.inputP)
		val btn = findViewById<Button>(R.id.btnCompute)
	val out = findViewById<TextView>(R.id.txtResult)
	val btnCopy = findViewById<Button>(R.id.btnCopyResult)

		btn.setOnClickListener {
			val text = input.text.toString().trim()
			val p = parseProbability(text)
			if (p == null) {
				out.text = "Enter p in [0,1], e.g. 0.3 or 1/3"
				return@setOnClickListener
			}
			if (p < 0.0 || p > 1.0) {
				out.text = "Probability must be between 0 and 1"
				return@setOnClickListener
			}
			val h = entropyBinary(p)
			val hStr = String.format("%.6f", h)
			out.text = String.format("p=%.6f, 1-p=%.6f\nEntropy H=%s bits", p, 1.0 - p, hStr)
			lastHValue = hStr
			btnCopy.isEnabled = true
		}

		btnCopy.setOnClickListener {
			val text = lastHValue ?: ""
			if (text.isBlank()) {
				Toast.makeText(this, "No H value to copy", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}
			val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
			val clip = ClipData.newPlainText("H", text)
			clipboard.setPrimaryClip(clip)
			Toast.makeText(this, "Copied H to clipboard", Toast.LENGTH_SHORT).show()
		}
	}

	private fun parseProbability(raw: String): Double? {
		if (raw.isEmpty()) return null
		val s = raw.replace(" ", "")
		// fraction a/b
		if (s.contains('/')) {
			val parts = s.split('/')
			if (parts.size != 2) return null
			val num = parts[0].toDoubleOrNull() ?: return null
			val den = parts[1].toDoubleOrNull() ?: return null
			if (den == 0.0) return null
			return num / den
		}
		// decimal
		return s.toDoubleOrNull()
	}

	private fun entropyBinary(p: Double): Double {
		// base-2 entropy for binary distribution
		fun term(x: Double): Double {
			return if (x <= 0.0) 0.0 else -x * (ln(x) / ln(2.0))
		}
		return term(p) + term(1.0 - p)
	}
}
