package com.jaytaravia.olxcloneapp.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jaytaravia.olxcloneapp.RvListenerCategory
import com.jaytaravia.olxcloneapp.Utils
import com.jaytaravia.olxcloneapp.adapters.AdapterAd
import com.jaytaravia.olxcloneapp.adapters.AdapterCategory
import com.jaytaravia.olxcloneapp.databinding.FragmentHomeBinding
import com.jaytaravia.olxcloneapp.models.ModelAd
import com.jaytaravia.olxcloneapp.models.ModelCategory


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private companion object {
        private const val TAG = "HOME_TAG"

    }


    private lateinit var mContext: Context

    private lateinit var adArrayList: ArrayList<ModelAd>

    private lateinit var adapterAd: AdapterAd


    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(mContext), container, false)
        Log.d(TAG, "arrived")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCategories()

        loadAds("All")

//        adapterAd

        binding.searchEt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.d(TAG, "onTextChanged: Query: $s")
                try{
                    Log.d(TAG, "No Problem")
                    val query = s.toString()
                    //!! nata pela
                    adapterAd.filter.filter(query)
                } catch (e: Exception){
                    Log.d(TAG, "Error")
                    Log.e(TAG, "onTextChanged: ", e)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })


    }

//    private val locationPickerActivityResultLauncher = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ){ result->
//
//       if (result.resultCode == Activity.RESULT_OK){
//           Log.d(TAG, "locationPickerActivityResultLauncher: RESULT_OK")
//
//           val data = result.data
//
//           if (data != null){
//               Log.d(TAG, "locationPickerActivityResultLauncher: Location Picked!")
//
//
//           }
//
//       }
//    }

    private fun loadAds(category: String){
        Log.d(TAG, "loadAds: category: $category")

        adArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                adArrayList.clear()

                for (ds in snapshot.children){
                    try {
                        val modelAd = ds.getValue(ModelAd::class.java)

                        if (category  == "All"){

                            adArrayList.add(modelAd!!)

                            Log.d(TAG, "All  category Loaded")
                        }else{
                            if (modelAd!!.category.equals(category)){

                                adArrayList.add(modelAd)

                                Log.d(TAG, "All  category Loaded Error")
                            }
                        }

                    }catch (e: Exception){

                        Log.e(TAG, "onDataChange: ", e)
                    }
                }

                //setup adapter and set to recyclerView
                adapterAd = AdapterAd(mContext, adArrayList)
                binding.adsRv.adapter = adapterAd

                Log.d(TAG, "loadAds completed")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "loadAds onCancelled")
            }
        })
    }

    private fun loadCategories(){

        Log.d(TAG, "Category loded")
        val categoryArrayList = ArrayList<ModelCategory>()

        for (i in 0 until Utils.categories.size){

            val modelCategory = ModelCategory(Utils.categories[i], Utils.categoryIcons[i])

            categoryArrayList.add(modelCategory)
        }

        //init/setup AdapterCategory
        val adapterCategory = AdapterCategory(mContext, categoryArrayList, object:
            RvListenerCategory {
            override fun onCategoryClick(modelCategory: ModelCategory) {
                //get selected category
                val selectedCategory = modelCategory.category
                //load ads based on selected category
                loadAds(selectedCategory)
            }
        })

        //set adapter to the RecyclerView i.e. categoriesRv
        binding.categoriesRv.adapter = adapterCategory

    }
}