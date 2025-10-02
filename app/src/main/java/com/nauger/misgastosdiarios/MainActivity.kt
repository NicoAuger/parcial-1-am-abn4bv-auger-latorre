package com.nauger.misgastosdiarios

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.*
import android.widget.LinearLayout.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding

class MainActivity : AppCompatActivity() {

    // Views principales
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

    // Gráfico por categoría
    private lateinit var chartContainer: LinearLayout

    // FAB
    private lateinit var fabMenu: com.google.android.material.floatingactionbutton.FloatingActionButton

    // Estado
    private var budget = 0.0
    private var spent = 0.0
    private var remainingDefaultColor: Int = 0

    // Acumulados por categoría
    private val categoryTotals = linkedMapOf<String, Double>()

    // Colores por categoría
    private val categoryColors by lazy {
        mapOf(
            "Comida" to android.R.color.holo_orange_dark,
            "Transporte" to android.R.color.holo_blue_dark,
            "Ocio" to android.R.color.holo_purple,
            "Salud" to android.R.color.holo_red_dark,
            "Educación" to android.R.color.holo_blue_bright,
            "Otros" to android.R.color.darker_gray
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupSpinner()
        initCategoryTotals()
        setupListeners()

        // Al inicio: deshabilitar inputs hasta fijar presupuesto
        setInputsEnabled(false)

        updateSummary()
        updateCategoryChart()
    }

    private fun bindViews() {
        etBudget = findViewById(R.id.etBudget)
        btnSetBudget = findViewById(R.id.btnSetBudget)
        tvBudget = findViewById(R.id.tvBudget)
        tvSpent = findViewById(R.id.tvSpent)
        tvRemaining = findViewById(R.id.tvRemaining)
        remainingDefaultColor = tvRemaining.currentTextColor

        etAmount = findViewById(R.id.etAmount)
        spCategory = findViewById(R.id.spCategory)
        etNote = findViewById(R.id.etNote)
        btnAdd = findViewById(R.id.btnAdd)
        containerExpenses = findViewById(R.id.containerExpenses)

        chartContainer = findViewById(R.id.chartContainer)
        fabMenu = findViewById(R.id.fabMenu)
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

    private fun setupListeners() {
        btnSetBudget.setOnClickListener {
            val value = etBudget.text.toString().trim().toDoubleOrNull()
            if (value == null || value <= 0) {
                toast("Ingresá un presupuesto válido")
                return@setOnClickListener
            }
            budget = value
            setInputsEnabled(true)
            updateSummary()
            updateCategoryChart()
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
            categoryTotals[category] = (categoryTotals[category] ?: 0.0) + amount

            updateSummary()
            updateCategoryChart()

            etAmount.text?.clear()
            etNote.text?.clear()
        }

        fabMenu.setOnClickListener { showFabMenu(it) }
    }

    private fun setInputsEnabled(enabled: Boolean) {
        etAmount.isEnabled = enabled
        spCategory.isEnabled = enabled
        etNote.isEnabled = enabled
        btnAdd.isEnabled = enabled
        // hint “grisado” si no hay presupuesto
        etBudget.isEnabled = true
    }

    private fun showFabMenu(anchor: android.view.View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 1, 0, "Limpiar gastos")
        popup.menu.add(0, 2, 1, "Recalcular resumen")
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                1 -> { clearAll(); true }
                2 -> { updateSummary(); updateCategoryChart(); true }
                else -> false
            }
        }
        popup.show()
    }

    private fun clearAll() {
        containerExpenses.removeAllViews()
        spent = 0.0
        categoryTotals.keys.toList().forEach { categoryTotals[it] = 0.0 }
        updateSummary()
        updateCategoryChart()
        toast("Gastos limpiados")
    }

    private fun initCategoryTotals() {
        resources.getStringArray(R.array.categories).forEach { cat ->
            categoryTotals.putIfAbsent(cat, 0.0)
        }
    }

    private fun updateSummary() {
        tvBudget.text = "Presupuesto: $${money(budget)}"
        tvSpent.text = "Gastado: $${money(spent)}"

        val remaining = budget - spent // permitir negativo
        tvRemaining.text = "Restante: $${money(remaining)}"

        if (remaining < 0) {
            tvRemaining.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        } else {
            tvRemaining.setTextColor(remainingDefaultColor)
        }
    }

    private fun updateCategoryChart() {
        chartContainer.removeAllViews()

        if (budget <= 0.0) {
            val hint = TextView(this).apply {
                text = "Fijá un presupuesto para ver porcentajes"
                setPadding(dp(8), dp(4), dp(8), dp(4))
                setTextColor(tvBudget.currentTextColor)
            }
            chartContainer.addView(hint)
            return
        }

        var any = false
        categoryTotals.forEach { (category, total) ->
            if (total <= 0) return@forEach
            any = true

            val percent = (total / budget * 100.0)

            // Etiqueta
            val label = TextView(this).apply {
                text = "$category — $${money(total)} (${money(percent)}%)"
                setTypeface(typeface, Typeface.BOLD)
                setPadding(dp(8), dp(6), dp(8), dp(2))
            }
            chartContainer.addView(label)

            // Barra horizontal (si >100%, se pinta roja)
            val bar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 100
                progress = percent.toInt().coerceIn(0, 100)
                val colorRes = if (percent > 100.0)
                    android.R.color.holo_red_dark
                else
                    (categoryColors[category] ?: android.R.color.darker_gray)
                progressTintList = ContextCompat.getColorStateList(this@MainActivity, colorRes)
            }
            chartContainer.addView(
                bar,
                LayoutParams(LayoutParams.MATCH_PARENT, dp(6)).apply {
                    leftMargin = dp(8); rightMargin = dp(8); bottomMargin = dp(8)
                }
            )
        }

        if (!any) {
            val empty = TextView(this).apply {
                text = "Aún no hay gastos para graficar"
                setPadding(dp(8), dp(4), dp(8), dp(4))
                setTextColor(tvBudget.currentTextColor)
            }
            chartContainer.addView(empty)
        }
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
