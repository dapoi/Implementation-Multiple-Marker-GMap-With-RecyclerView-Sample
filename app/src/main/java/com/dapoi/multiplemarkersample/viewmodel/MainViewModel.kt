package com.dapoi.multiplemarkersample.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dapoi.multiplemarkersample.data.ApiClient
import com.dapoi.multiplemarkersample.data.HospitalResponseItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel : ViewModel() {
    private val _hospitalData = MutableLiveData<List<HospitalResponseItem>>()
    val hospitalData: LiveData<List<HospitalResponseItem>> = _hospitalData

    fun getDataHospital() {
        ApiClient.getClient().getLocationHospital()
            .enqueue(object : Callback<List<HospitalResponseItem>> {
                override fun onFailure(call: Call<List<HospitalResponseItem>>, t: Throwable) {
                    Log.d("error", t.message.toString())
                }

                override fun onResponse(
                    call: Call<List<HospitalResponseItem>>,
                    response: Response<List<HospitalResponseItem>>
                ) {
                    if (response.isSuccessful) {
                        _hospitalData.value = response.body()
                    }
                }
            })
    }
}