package com.jaytaravia.olxcloneapp.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.jaytaravia.olxcloneapp.RvListenerCategory
import com.jaytaravia.olxcloneapp.databinding.RowCategoryBinding
import com.jaytaravia.olxcloneapp.models.ModelCategory
import java.util.*
import kotlin.collections.ArrayList

class AdapterCategory(
    private val context: Context,
    private val categoryArrayList: ArrayList<ModelCategory>,
    private val rvListenerCategory: RvListenerCategory

) : Adapter<AdapterCategory.HolderCategory>() {


    private lateinit var binding: RowCategoryBinding

    private companion object {
        private const val TAG = "ADAPTER_CATEGORY_TAG"
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {

        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderCategory(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {

        //this section of Code is private



    }

    override fun getItemCount(): Int {

        return categoryArrayList.size

    }


    inner class HolderCategory (itemView : View) : ViewHolder(itemView){

        var categoryIconIv = binding.categoryIconIv
        var categoryTv = binding.categoryTv


    }

}