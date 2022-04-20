package com.dapoi.multiplemarkersample.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.dapoi.multiplemarkersample.data.HospitalResponseItem
import com.dapoi.multiplemarkersample.databinding.ItemListHospitalBinding

class MainAdapter : RecyclerView.Adapter<MainAdapter.MainViewHolder>(), Filterable {

    private var listHospital = ArrayList<HospitalResponseItem>()
    private var listHospitalFiltered = ArrayList<HospitalResponseItem>()

    private lateinit var onItemClickCallback: OnItemClickListener

    internal fun setOnItemClick(onItemClickCallback: OnItemClickListener) {
        this.onItemClickCallback = onItemClickCallback
    }

    fun setData(data: List<HospitalResponseItem>) {
        listHospital = data as ArrayList<HospitalResponseItem>
        listHospitalFiltered = listHospital
        notifyDataSetChanged()
    }

    inner class MainViewHolder(private val binding: ItemListHospitalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: HospitalResponseItem) {
            with(binding) {
                tvNamaJalan.text = data.alamat
                tvNamaLokasi.text = data.nama
                cvHospital.setOnClickListener {
                    onItemClickCallback.onItemClick(data)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            ItemListHospitalBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bind(listHospitalFiltered[position])
    }

    override fun getItemCount(): Int {
        return listHospitalFiltered.size
    }

    interface OnItemClickListener {
        fun onItemClick(item: HospitalResponseItem)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    listHospitalFiltered = listHospital
                } else {
                    val filteredList = ArrayList<HospitalResponseItem>()
                    for (row in listHospital) {
                        if (row.nama.toString().lowercase().contains(charString.lowercase()) ||
                            row.alamat.toString().lowercase().contains(charString.lowercase()) ||
                            row.wilayah.toString().lowercase().contains(charString.lowercase())
                        ) {
                            filteredList.add(row)
                        }
                    }
                    listHospitalFiltered = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = listHospitalFiltered
                return filterResults
            }

            @SuppressLint("RestrictedApi")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                listHospitalFiltered = filterResults.values as ArrayList<HospitalResponseItem>
                notifyDataSetChanged()
            }
        }
    }
}