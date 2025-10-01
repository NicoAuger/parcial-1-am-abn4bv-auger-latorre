package com.nauger.misgastosdiarios

import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding

class MainActivity : AppCompatActivity() {

    private lateinit var etBudget: EditText
    private lateinit var btnSetBudget: Button
    private lateinit var tvBudget: TextView
    private lateinit var tvSpent: TextView
    private lateinit var tvRemaining: TextView

    private lateinit var etAmount: EditText
    private lateinit var spCategory: Spinner
    private lateinit var etNote: EditText
    private lateinit var btnAdd: Button
    private lateinit var containerExpenses: LinearLayout

    private var budget = 0.0
    private var spent = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupSpinner()
        setupListeners()
        updateSummary()
    }

    private fun bindViews() {
        etBudget = findViewById(R.id.etBudget)
        btnSetBudget = findViewById(R.id.btnSetBudget)
        tvBudget = findViewById(R.id.tvBudget)
        tvSpent = findViewById(R.id.tvSpent)
        tvRemaining = findViewById(R.id.tvRemaining)

        etAmount = findViewById(R.id.etAmount)
        spCategory = findViewById(R.id.spCategory)
        etNote = findViewById(R.id.etNote)
        btnAdd = findViewById(R.id.btnAdd)

        containerExpenses = findViewById(R.id.containerExpenses)
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.categories,                     // <-- tu array
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = adapter
    }

    private fun setupListeners() {
        btnSetBudget.setOnClickListener {
            val value = etBudget.text.toString().trim().toDoubleOrNull()
            if (value == null || value <= 0) {
                toast("Ingresá un presupuesto válido")
                return@setOnClickListener
            }
            budget = value
            updateSummary()
            toast("Presupuesto fijado")
        }

        btnAdd.setOnClickListener {
            val amount = etAmount.text.toString().trim().toDoubleOrNull()
            if (amount == null || amount <= 0) {
                toast("Monto inválido")
                return@setOnClickListener
            }
            val category = spCategory.selectedItem?.toString() ?: "Otros"
            val note = etNote.text.toString().trim()

            addExpenseRow(category, amount, note)

            spent += amount
            updateSummary()

            etAmount.text?.clear()
            etNote.text?.clear()
        }
    }

    private fun updateSummary() {
        tvBudget.text = "Presupuesto: $${money(budget)}"
        tvSpent.text = "Gastado: $${money(spent)}"
        val remaining = (budget - spent).coerceAtLeast(0.0)
        tvRemaining.text = "Restante: $${money(remaining)}"
    }

    private fun addExpenseRow(category: String, amount: Double, note: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8))
        }

        val tvLeft = TextView(this).apply {
            text = if (note.isBlank()) category else "$category • $note"
        }
        val tvRight = TextView(this).apply {
            text = "$${money(amount)}"
        }

        val leftParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        row.addView(tvLeft, leftParams)
        row.addView(tvRight)

        containerExpenses.addView(row)
    }

    private fun money(n: Double) = String.format("%.2f", n)
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
