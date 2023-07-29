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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.jaytaravia.olxcloneapp.adapters.AdapterAd
import com.jaytaravia.olxcloneapp.databinding.FragmentMyAdsAdsBinding
import com.jaytaravia.olxcloneapp.models.ModelAd

class MyAdsAdsFragment : Fragment() {

    private lateinit var binding: FragmentMyAdsAdsBinding

    companion object{

        private const val TAG = "MY_ADS_TAG"
    }

    //Context for this fragment class
    private lateinit var mContext: Context

    //Firebase Auth for auth related tasks
    private lateinit var firebaseAuth: FirebaseAuth

    //adArrayList to hold ads list by currently logged-in user to show in RecyclerView
    private lateinit var adArrayList: ArrayList<ModelAd>

    //Adapter class instance to set to Recyclerview to show Ads list
    private lateinit var adapterAd: AdapterAd

    override fun onAttach(context: Context) {
        //get and init the context for this fragment class
        this.mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentMyAdsAdsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Firebase Auth for auth related tasks
        firebaseAuth = FirebaseAuth.getInstance()

        //function call to load ads by currently logged-in users
        loadAds()
        //add text change listener t searchEt to search ads using filter applied in AdapterAd class
        binding.searchEt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //this functionis called whenever user type a letter, serach based on what user typed
                try {
                    val query = s.toString()
                    //!! natu
                    adapterAd.filter.filter(query)

                } catch (e: Exception){
                    Log.e(TAG, "onTextChanged: ", e)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun loadAds(){
        Log.d(TAG, "loadAds: ")
        adArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.orderByChild("uid").equalTo(firebaseAuth.uid)
            .addValueEventListener(object: ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {

                    adArrayList.clear()

                    for(ds in snapshot.children){

                        try {

                            val modelAd = ds.getValue(ModelAd::class.java)

                            adArrayList.add(modelAd!!)

                        } catch (e: Exception){
                            Log.e(TAG, "onDataChange: ", e)
                        }

                    }
                    //init/setup AdapterAd class and set to recyclerView
                    adapterAd = AdapterAd(mContext, adArrayList)
                    binding.adsRv.adapter = adapterAd
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

}