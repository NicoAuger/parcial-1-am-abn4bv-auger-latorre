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

/*
   Adaptador que gestiona la lista de gastos en el RecyclerView.
   Usa ListAdapter y DiffUtil para optimizar actualizaciones de la lista.
   Recibe una función de callback para eliminar ítems desde la interfaz.
*/
class ExpenseAdapter(
    private val onDelete: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.VH>(DIFF) {

    /*
       Implementa la comparación entre elementos para que DiffUtil determine qué cambió.
       Se usa igualdad estructural de la data class porque no hay ID único definido.
    */
    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Expense>() {
            override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
                return oldItem == newItem
            }
        }
    }

    /*
       Habilita IDs estables para mejorar el rendimiento y las animaciones.
    */
    init {
        setHasStableIds(true)
    }

    /*
       Genera un ID único para cada elemento basándose en su contenido.
       Esto permite mantener consistencia visual al actualizar la lista.
    */
    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }

    /*
       Infla el layout de cada ítem de gasto (item_expense.xml)
       y crea un ViewHolder para administrarlo.
    */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return VH(v, onDelete)
    }

    /*
       Asocia los datos del gasto actual con las vistas del ViewHolder.
    */
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    /*
       ViewHolder que representa visualmente un gasto en la lista.
       Administra las vistas internas y el evento de eliminación.
    */
    class VH(
        itemView: View,
        private val onDelete: (Expense) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvLeft: TextView = itemView.findViewById(R.id.tvLeft)
        private val tvRight: TextView = itemView.findViewById(R.id.tvRight)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        /*
           Formateador de moneda configurado para pesos argentinos (ARS).
           Se crea una sola vez por ViewHolder.
        */
        private val ars: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

        /*
           Muestra los datos del gasto en el ítem correspondiente.
           Incluye la categoría, nota (si existe) y monto formateado.
        */
        fun bind(e: Expense) {
            tvLeft.text = if (e.note.isBlank()) e.category else "${e.category} • ${e.note}"
            tvRight.text = ars.format(e.amount)

            // Asigna descripción accesible si no está definida en el XML.
            if (btnDelete.contentDescription == null) {
                btnDelete.contentDescription = itemView.context.getString(R.string.cd_delete_expense)
            }

            // Acción de eliminar cuando se presiona el botón.
            btnDelete.setOnClickListener { onDelete(e) }
        }
    }
}
