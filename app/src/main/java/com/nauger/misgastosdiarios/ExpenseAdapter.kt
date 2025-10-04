package com.nauger.misgastosdiarios

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class ExpenseAdapter(
    private val onDelete: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.VH>(DIFF) {

    // Decido usar contenido para areItemsTheSame porque la Activity hace submitList(...) creando nuevas instancias.
    // Si más adelante tuviéramos un id estable, lo usaría acá.
    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Expense>() {
            override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
                // Uso igualdad estructural (data class) para considerar "el mismo ítem" si todo su contenido coincide.
                // En un caso real, preferiría un ID único y comparar ese ID.
                return oldItem == newItem
            }
            override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
                return oldItem == newItem
            }
        }
    }

    // (Opcional) IDs estables para mejor reuso/animaciones
    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        // Elijo un ID derivado del contenido para que sea estable entre diffs.
        // Si luego agregamos 'id: Long' al modelo, devolvería ese id directamente.
        return getItem(position).hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return VH(v, onDelete)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        itemView: View,
        private val onDelete: (Expense) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvLeft: TextView = itemView.findViewById(R.id.tvLeft)
        private val tvRight: TextView = itemView.findViewById(R.id.tvRight)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        // Prefiero instanciar el formateador una vez por ViewHolder con Locale es-AR para consistencia visual.
        private val ars: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

        fun bind(e: Expense) {
            // Decido mostrar "Categoría • Nota" si hay nota; sino solo categoría.
            tvLeft.text = if (e.note.isBlank()) e.category else "${e.category} • ${e.note}"

            // Elijo formatear el monto como ARS para que coincida con el resumen de la pantalla.
            tvRight.text = ars.format(e.amount)

            // Si el XML no lo define, doy una contentDescription básica por accesibilidad.
            if (btnDelete.contentDescription == null) {
                btnDelete.contentDescription = itemView.context.getString(R.string.cd_delete_expense)
            }

            btnDelete.setOnClickListener { onDelete(e) }
        }
    }
}
