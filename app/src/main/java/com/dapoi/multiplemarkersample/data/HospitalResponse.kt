package com.dapoi.multiplemarkersample.data

import com.squareup.moshi.Json

data class HospitalResponse(

    @Json(name = "HospitalResponse")
    val hospitalResponse: List<HospitalResponseItem?>? = null
)

data class Lokasi(

    @Json(name = "lon")
    val lon: Double? = null,

    @Json(name = "lat")
    val lat: Double? = null
)

data class HospitalResponseItem(

    @Json(name = "tempat_tidur")
    val tempatTidur: Int? = null,

    @Json(name = "nama")
    val nama: String? = null,

    @Json(name = "lokasi")
    val lokasi: Lokasi? = null,

    @Json(name = "telepon")
    val telepon: String? = null,

    @Json(name = "wilayah")
    val wilayah: String? = null,

    @Json(name = "tipe")
    val tipe: String? = null,

    @Json(name = "kode_rs")
    val kodeRs: String? = null,

    @Json(name = "alamat")
    val alamat: String? = null
)
