package com.lhwdev.selfTestMacro.transkey

import kotlin.random.Random


private val iv = byteArrayOf(
	0x4d, 0x6f, 0x62, 0x69, 0x6c, 0x65, 0x54, 0x72,
	0x61, 0x6e, 0x73, 0x4b, 0x65, 0x79, 0x31, 0x30
)

private fun ByteArray.toHexString(separator: Char): String = buildString {
	for(index in this@toHexString.indices) {
		val byte = this@toHexString[index]
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
private inline fun Char.asciiByte(): Byte = code.toByte()


class KeyPad(
	val crypto: Crypto,
	keyType: String,
	val skipData: List<Char>,
	val keys: List<Pair<String, String>>,
	val random: Random,
	val decInitTime: String,
	val useSession: Boolean
) {
	init {
		require(keyType == "number") { "only number" }
	}
	
	
	fun getGeo(message: String): List<Pair<String, String>> =
		message.map { keys[skipData.indexOf(it)] }
	
	fun encryptGeos(geos: List<Pair<String, String>>): String = buildString {
		val encryptedDecInitTime = crypto.encryptSeed(iv, decInitTime.toByteArray(Charsets.US_ASCII))
			.toHexString(',')
		
		println(geos)
		for(geo in geos) {
			append('$')
			
			val (x, y) = geo
			
			val xBytes = x.map { it.digitToInt().toByte() }.toByteArray()
			val yBytes = y.map { it.digitToInt().toByte() }.toByteArray()
			
			var data = byteArrayOf(
				*xBytes,
				' '.asciiByte(),
				*yBytes,
				' '.asciiByte(),
				'e'.asciiByte(),
				random.nextInt(256).toByte()
			)
			println(data.toString(Charsets.UTF_8))
			
			val encrypted = crypto.encryptSeed(iv, data)
			append(encrypted.toHexString(','))
			
			if(!useSession) {
				append('$')
				append(encryptedDecInitTime)
			}
		}
	}
	
	fun encryptPassword(password: String): String {
		val geos = getGeo(password)
		return encryptGeos(geos)
	}
}
