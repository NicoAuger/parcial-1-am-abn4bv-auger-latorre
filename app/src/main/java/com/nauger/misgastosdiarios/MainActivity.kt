package com.nauger.misgastosdiarios

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import java.text.NumberFormat
import java.util.Locale

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

    private var budget: Double = 0.0
    private var spent: Double = 0.0

    private val currency: NumberFormat by lazy {
        NumberFormat.getCurrencyInstance(Locale("es", "AR"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupSpinner()
        setupActions()
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
        ArrayAdapter.createFromResource(
            this,
            R.array.categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spCategory.adapter = adapter
        }
    }

    private fun setupActions() {
        btnSetBudget.setOnClickListener {
            val value = etBudget.text.toString().replace(",", ".").trim()
            budget = value.toDoubleOrNull() ?: 0.0
            if (budget <= 0) {
                toast("Ingresá un presupuesto válido.")
                return@setOnClickListener
            }
            updateSummary()
            hideKeyboard(it)
        }

        btnAdd.setOnClickListener {
            val amountValue = etAmount.text.toString().replace(",", ".").trim()
            val amount = amountValue.toDoubleOrNull()
            if (amount == null || amount <= 0.0) {
                toast("Ingresá un monto válido.")
                return@setOnClickListener
            }
            val category = spCategory.selectedItem?.toString() ?: "Otros"
            val note = etNote.text?.toString()?.trim().orEmpty()

            addExpenseRow(amount, category, note)
            etAmount.text?.clear()
            etNote.text?.clear()
            hideKeyboard(it)
        }
    }

    private fun addExpenseRow(amount: Double, category: String, note: String) {
        // Contenedor de la fila
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8, 0, 8) }
            setPadding(8, 8, 8, 8)
            setBackgroundColor(Color.parseColor("#11000000")) // leve sombreado
            gravity = Gravity.CENTER_VERTICAL
        }

        // Columna texto (monto + categoría + nota)
        val textCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvLine1 = TextView(this).apply {
            text = "${currency.format(amount)}  •  $category"
            textSize = 16f
            setPadding(0, 0, 0, 2)
        }

        val tvLine2 = TextView(this).apply {
            text = if (note.isNotEmpty()) note else "—"
            textSize = 13f
        }

        textCol.addView(tvLine1)
        textCol.addView(tvLine2)

        // Botón eliminar
        val btnDelete = Button(this).apply {
            text = "Eliminar"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        row.addView(textCol)
        row.addView(btnDelete)

        // Lógica de totales
        spent += amount
        updateSummary()

        // Eliminar fila
        btnDelete.setOnClickListener {
            containerExpenses.removeView(row)
            spent -= amount
            if (spent < 0) spent = 0.0
            updateSummary()
        }

        containerExpenses.addView(row)
    }

    private fun updateSummary() {
        tvBudget.text = "Presupuesto: ${currency.format(budget)}"
        tvSpent.text = "Gastado: ${currency.format(spent)}"
        val remaining = (budget - spent)
        tvRemaining.text = "Restante: ${currency.format(remaining)}"
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    private fun hideKeyboard(view: View) {
        val imm = getSystemService<InputMethodManager>()
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}