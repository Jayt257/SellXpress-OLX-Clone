package com.jaytaravia.olxcloneapp.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jaytaravia.olxcloneapp.adapters.AdapterAd
import com.jaytaravia.olxcloneapp.databinding.FragmentMyAdsFavBinding
import com.jaytaravia.olxcloneapp.models.ModelAd

class MyAdsFavFragment : Fragment() {

    private lateinit var binding : FragmentMyAdsFavBinding

    companion object {

        private const val TAG = "FAV_ADS_TAG"
    }

    private lateinit var mContext: Context

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var adArrayList: ArrayList<ModelAd>

    private lateinit var adapterAd: AdapterAd

    override fun onAttach(context: Context) {
        //get and init the context for this fragment class
        this.mContext = context
        super.onAttach(context)
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentMyAdsFavBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Firebase Auth for auth related tasks
        firebaseAuth = FirebaseAuth.getInstance()

        //functions call to load ads by currently logged-in users
        loadAds()

        //add text change listener to searchEt to search ads using filter applied in AdapterAd class
        binding.searchEt.addTextChangedListener(object: TextWatcher{

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //this function is called whenever user type a letter, search based on what user typed
                try {
                    val query = s.toString()
                    //natu
                    adapterAd.filter.filter(query)
                } catch (e: Exception){
                    Log.e(TAG, "onTextChanged", e)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun loadAds(){
        Log.d(TAG, "loadAds: ")

        adArrayList = ArrayList()

        val favRef = FirebaseDatabase.getInstance().getReference("Users")
        favRef.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    adArrayList.clear()

                    for (ds in snapshot.children){
                        //get the id of the Ad. e.g. Users > uid > Favorites > adId
                        val adId = "${ds.child("adId").value}"
                        Log.d(TAG, "onDataChange: adId: $adId")

                        //Firebase DB listener to load Ad details based on id of the Ad we just got
                        val adRef = FirebaseDatabase.getInstance().getReference("Ads")
                        adRef.child(adId)
                            .addListenerForSingleValueEvent(object : ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {

                                    try {

                                        //prepare ModelAd with all data from Firebase DB
                                        val modelAd = snapshot.getValue(ModelAd::class.java)

                                        adArrayList.add(modelAd!!)
                                    } catch (e: Exception){
                                        Log.e(TAG, "onDataChange: ", e)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                    }

                    //sometimes fav ads were not loading due to nested db listeners because we are getting data using 2 paths so added some delay e.g. half second
                    Handler().postDelayed({
                        //init/setup AdapterAd class and set to recyclerview
                        adapterAd = AdapterAd(mContext, adArrayList)
                        binding.adsRv.adapter = adapterAd
                    }, 500)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

}