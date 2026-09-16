package com.revlog.domain

object PowerConversion {
    fun kwToPs(kw: Int): Int = (kw * 1.35962).toInt()

    fun psToKw(ps: Int): Int = (ps / 1.35962).toInt()
}
