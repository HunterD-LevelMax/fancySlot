package com.sample.samplewebview.utils

fun crypt(text: String): String {
    if (text.isEmpty()) return ""

    val chars = text.toCharArray()

    for (i in 2 until chars.size step 3) {
        chars[i] = (chars[i].code xor 0x5A).toChar()
    }

    val mid = chars.size / 2
    val firstPart = chars.sliceArray(0 until mid)
    val secondPart = chars.sliceArray(mid until chars.size)

    val shuffled = StringBuilder()
    val maxLen = maxOf(firstPart.size, secondPart.size)
    for (i in 0 until maxLen) {
        if (i < secondPart.size) shuffled.append(secondPart[i])
        if (i < firstPart.size) shuffled.append(firstPart[i])
    }

    return shuffled.toString()
}

fun deCrypt(text: String): String {
    if (text.isEmpty()) return ""

    val chars = text.toCharArray()

    val firstPart = mutableListOf<Char>()
    val secondPart = mutableListOf<Char>()

    for (i in chars.indices) {
        if (i % 2 == 0) {
            secondPart.add(chars[i])
        } else {
            firstPart.add(chars[i])
        }
    }

    val restored = (firstPart + secondPart).toCharArray()

    for (i in 2 until restored.size step 3) {
        restored[i] = (restored[i].code xor 0x5A).toChar()
    }

    return String(restored)
}