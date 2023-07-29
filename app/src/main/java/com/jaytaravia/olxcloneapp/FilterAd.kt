package com.jaytaravia.olxcloneapp

import android.widget.Filter
import com.jaytaravia.olxcloneapp.adapters.AdapterAd
import com.jaytaravia.olxcloneapp.models.ModelAd
import java.util.*
import kotlin.collections.ArrayList


//adapter - AdapterAd instance to be passed when this constructor is created
//filterList - ad arraylist to be passed when this constructor is created

class FilterAd(
    private val adapter: AdapterAd,
    private val filterList: ArrayList<ModelAd>
) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {

        var constraint = constraint
        val results = FilterResults()

        if (!constraint.isNullOrEmpty()){

            constraint = constraint.toString().uppercase(Locale.getDefault())

            val filteredModels = ArrayList<ModelAd>()
            for (i in filterList.indices){

                if (filterList[i].brand.uppercase(Locale.getDefault()).contains(constraint) ||
                    filterList[i].category.uppercase(Locale.getDefault()).contains(constraint) ||
                    filterList[i].condition.uppercase(Locale.getDefault()).contains(constraint) ||
                    filterList[i].title.uppercase(Locale.getDefault()).contains(constraint)
                ) {
                    filteredModels.add(filterList[i])
                }
            }

            results.count = filteredModels.size
            results.values = filteredModels
        } else{

            results.count = filterList.size
            results.values = filterList
        }

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {

        adapter.adArrayList = results.values as ArrayList<ModelAd>

        adapter.notifyDataSetChanged()
    }

}