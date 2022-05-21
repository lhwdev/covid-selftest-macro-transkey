package com.lhwdev.selfTestMacro.transkey


internal fun StringBuilder.appendHexStringNotFixed(array: ByteArray, separator: Char) {
	for(index in array.indices) {
		val byte = array[index]
		if(index != 0) append(separator)
		append((byte.toInt() shr 4 and 0b1111).toString(radix = 16))
		append((byte.toInt() and 0b1111).toString(radix = 16))
	}
}

fun ByteArray.toHexString(): String = buildString {
	for(index in this@toHexString.indices) {
		val byte = this@toHexString[index]
		append((byte.toInt() shr 4 and 0b1111).toString(radix = 16))
		append((byte.toInt() and 0b1111).toString(radix = 16))
	}
}

@Suppress("NOTHING_TO_INLINE")
inline val Char.asciiByte
	get() = code.toByte()
