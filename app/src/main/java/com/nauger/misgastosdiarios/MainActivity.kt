package com.nauger.misgastosdiarios

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.LinearLayout.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

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

    // Reemplazo ScrollView + LinearLayout por RecyclerView (rendimiento y reuso de vistas).
    private lateinit var recyclerExpenses: RecyclerView
    private lateinit var expensesAdapter: ExpenseAdapter

    // Gráfico por categoría (mock simple con barras)
    private lateinit var chartContainer: LinearLayout

    // FAB
    private lateinit var fabMenu: com.google.android.material.floatingactionbutton.FloatingActionButton

    // Estado
    private var budget = 0.0
    private var spent = 0.0
    private var remainingDefaultColor: Int = 0

    // Fuente de datos en memoria (mantengo simple por alcance académico).
    private val expenses = mutableListOf<Expense>()

    // Acumulados por categoría
    private val categoryTotals = linkedMapOf<String, Double>()

    // Colores por categoría (dejo android.R.*; si queremos, los paso a @color/cat_*).
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

    // Formateadores (moneda y porcentaje) en es-AR.
    private val ars: NumberFormat by lazy { NumberFormat.getCurrencyInstance(Locale("es", "AR")) }
    private val pct: NumberFormat by lazy {
        NumberFormat.getNumberInstance(Locale("es", "AR")).apply { maximumFractionDigits = 1 }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupSpinner()
        initRecycler()
        initCategoryTotals()
        setupListeners()

        // Al inicio: deshabilito inputs hasta fijar presupuesto.
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

        recyclerExpenses = findViewById(R.id.recyclerExpenses)
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

    private fun initRecycler() {
        recyclerExpenses.layoutManager = LinearLayoutManager(this)
        expensesAdapter = ExpenseAdapter(
            onDelete = { expense ->
                // Elijo centralizar aquí la eliminación para mantener el estado consistente.
                expenses.remove(expense)
                recalcTotalsFromScratch()
                expensesAdapter.submitList(expenses.toList())
                updateSummary()
                updateCategoryChart()
                toast(getString(R.string.msg_deleted))
            }
        )
        recyclerExpenses.adapter = expensesAdapter
        expensesAdapter.submitList(expenses.toList())
    }

    private fun setupListeners() {
        btnSetBudget.setOnClickListener {
            // ✅ Corrijo posible error: parseo coma o punto (p. ej., "123,45" funciona).
            val value = parseAmount(etBudget.text?.toString().orEmpty())
            if (value == null || value <= 0.0) {
                // Doy feedback directo en el campo y también un toast breve.
                etBudget.error = getString(R.string.err_budget_invalid)
                toast(getString(R.string.err_budget_invalid))
                return@setOnClickListener
            }
            budget = value
            setInputsEnabled(true)
            updateSummary()
            updateCategoryChart()
            hideKeyboard()
            toast(getString(R.string.msg_budget_set))
        }

        btnAdd.setOnClickListener {
            // Bloqueo carga sin presupuesto válido (evito edge-case de división por 0 en gráfico).
            if (budget <= 0.0) {
                etBudget.error = getString(R.string.err_budget_invalid)
                toast(getString(R.string.err_budget_invalid))
                etBudget.requestFocus()
                return@setOnClickListener
            }

            val amount = parseAmount(etAmount.text?.toString().orEmpty())
            if (amount == null || amount <= 0.0) {
                etAmount.error = getString(R.string.err_amount_invalid)
                toast(getString(R.string.err_amount_invalid))
                return@setOnClickListener
            }

            val category = spCategory.selectedItem?.toString().orEmpty().ifBlank { "Otros" }
            val note = etNote.text?.toString().orEmpty().trim()

            // Inserto gasto (mantengo orden de carga por simplicidad).
            val expense = Expense(category = category, amount = amount, note = note)
            expenses.add(expense)

            // Actualizo acumulados de forma incremental (O(1)).
            spent += amount
            categoryTotals[category] = (categoryTotals[category] ?: 0.0) + amount

            // Refresco UI.
            expensesAdapter.submitList(expenses.toList())
            updateSummary()
            updateCategoryChart()

            // Limpieza y UX.
            etAmount.text?.clear()
            etNote.text?.clear()
            etAmount.clearFocus()
        }

        fabMenu.setOnClickListener { showFabMenu(it) }
    }

    private fun setInputsEnabled(enabled: Boolean) {
        etAmount.isEnabled = enabled
        spCategory.isEnabled = enabled
        etNote.isEnabled = enabled
        btnAdd.isEnabled = enabled
        // Mantengo editable el presupuesto para correcciones manuales.
        etBudget.isEnabled = true
    }

    private fun showFabMenu(anchor: android.view.View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 1, 0, getString(R.string.menu_clear_expenses))
        popup.menu.add(0, 2, 1, getString(R.string.menu_recalc_summary))
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                1 -> { clearAll(); true }
                2 -> { recalcTotalsFromScratch(); updateSummary(); updateCategoryChart(); true }
                else -> false
            }
        }
        popup.show()
    }

    private fun clearAll() {
        expenses.clear()
        spent = 0.0
        categoryTotals.keys.toList().forEach { categoryTotals[it] = 0.0 }
        expensesAdapter.submitList(expenses.toList())
        updateSummary()
        updateCategoryChart()
        toast(getString(R.string.msg_expenses_cleared))
    }

    private fun initCategoryTotals() {
        resources.getStringArray(R.array.categories).forEach { cat ->
            categoryTotals.putIfAbsent(cat, 0.0)
        }
    }

    private fun recalcTotalsFromScratch() {
        spent = 0.0
        categoryTotals.keys.toList().forEach { categoryTotals[it] = 0.0 }
        expenses.forEach { e ->
            spent += e.amount
            categoryTotals[e.category] = (categoryTotals[e.category] ?: 0.0) + e.amount
        }
    }

    private fun updateSummary() {
        tvBudget.text = getString(R.string.lbl_budget, ars.format(budget))
        tvSpent.text = getString(R.string.lbl_spent, ars.format(spent))

        val remaining = budget - spent // permito negativo para reflejar sobregasto
        tvRemaining.text = getString(R.string.lbl_remaining, ars.format(remaining))

        if (remaining < 0) {
            // Si definiste R.color.expense_negative, podés usarlo aquí.
            tvRemaining.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        } else {
            tvRemaining.setTextColor(remainingDefaultColor)
        }
    }

    private fun updateCategoryChart() {
        chartContainer.removeAllViews()

        if (budget <= 0.0) {
            val hint = TextView(this).apply {
                text = getString(R.string.hint_chart_needs_budget)
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
                text = getString(
                    R.string.fmt_chart_line,
                    category,
                    ars.format(total),
                    "${pct.format(percent)}%"
                )
                setTypeface(typeface, Typeface.BOLD)
                setPadding(dp(8), dp(6), dp(8), dp(2))
            }
            chartContainer.addView(label)

            // Barra horizontal (si >100%, roja).
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
                text = getString(R.string.hint_chart_no_expenses)
                setPadding(dp(8), dp(4), dp(8), dp(4))
                setTextColor(tvBudget.currentTextColor)
            }
            chartContainer.addView(empty)
        }
    }

    // ===== Helpers que agrego para robustez/UX =====

    /** Decido normalizar coma/punto para que el usuario pueda escribir “123,45” o “123.45”. */
    private fun parseAmount(raw: String): Double? =
        raw.replace(",", ".").trim().toDoubleOrNull()

    /** Prefiero cerrar el teclado en momentos clave (ej., al fijar presupuesto). */
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        currentFocus?.let { imm?.hideSoftInputFromWindow(it.windowToken, 0) }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

/** Modelo simple para un gasto.
 *  Elijo mantenerlo mínimo para cumplir con el parcial sin sobre-diseñar.
 */
data class Expense(
    val category: String,
    val amount: Double,
    val note: String
)
