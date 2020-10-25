package com.buddman1208.ecowell.utils

fun Int.getLsb() = this and 0xFF

fun Int.getMsb() = (this shr 8) and 0xFF