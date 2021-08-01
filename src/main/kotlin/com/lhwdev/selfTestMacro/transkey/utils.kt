package com.lhwdev.selfTestMacro.transkey


fun Char.splitTwo(): Pair<Byte, Byte> = (code shr 4 and 0b1111).toByte() to (code and 0b1111).toByte()

fun ByteArray.toHexStringNotFixed(separator: Char): String = buildString {
	for(index in this@toHexStringNotFixed.indices) {
		val byte = this@toHexStringNotFixed[index]
		if(index != 0) append(separator)
		append((byte.toInt() shr 4 and 0b1111).toString(radix = 16))
		append((byte.toInt() and 0b1111).toString(radix = 16))
		// append(byte.toUByte().toString(radix = 16))
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
inline fun Char.asciiByte(): Byte = code.toByte()
