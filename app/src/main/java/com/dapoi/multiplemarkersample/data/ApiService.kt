package com.dapoi.multiplemarkersample.data

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("public/api/rs.json")
    fun getLocationHospital(): Call<List<HospitalResponseItem>>
}