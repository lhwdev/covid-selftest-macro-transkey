package com.lhwdev.selfTestMacro.transkey

import com.lhwdev.selfTestMacro.decodeBase64
import kotlin.random.Random


private val iv = byteArrayOf(
	0x4d, 0x6f, 0x62, 0x69, 0x6c, 0x65, 0x54, 0x72,
	0x61, 0x6e, 0x73, 0x4b, 0x65, 0x79, 0x31, 0x30
)


class KeyPad(
	val crypto: Crypto,
	keyType: String,
	val skipData: List<Char>,
	val keys: List<Pair<String, String>>,
	val random: Random,
	val initTime: String,
	val decInitTime: String?,
	val useSession: Boolean,
	val keyIndex: String
) {
	init {
		require(keyType == "number") { "only number" }
	}
	
	
	fun getGeo(message: String): List<Pair<String, String>> =
		message.map { keys[skipData.indexOf(it)] }
	
	fun encryptGeos(geos: List<Pair<String, String>>): String = buildString {
		val encryptedDecInitTime = if(decInitTime != null) {
			crypto.encryptSeed(iv, decInitTime.toByteArray(Charsets.US_ASCII))
				.toHexStringNotFixed(',')
		} else {
			null
		}
		
		val initTimeBytes = if(useAsyncTranskey) {
			initTime.map { if(it.isLetter()) it.code.toByte() else it.digitToInt().toByte() }.toByteArray()
		} else {
			null
		}
		
		println(geos)
		for(geo in geos) {
			append('$')
			
			val (x, y) = geo
			
			val xBytes = x.map { it.digitToInt().toByte() }.toByteArray()
			val yBytes = y.map { it.digitToInt().toByte() }.toByteArray()
			
			val data = if(/*useAsyncTranskey*/ initTimeBytes != null) {
				val arr = byteArrayOf(
					*xBytes,
					' '.asciiByte(),
					*yBytes,
					' '.asciiByte(),
					*initTimeBytes,
					' '.asciiByte(),
					'%'.asciiByte(),
					'b'.asciiByte()
				)
				val newArr = arr.copyOf(newSize = 48)
				if(stateless) {
					val data = decodeBase64(readLine()!!)
					data.copyInto(newArr, destinationOffset = arr.size)
				} else
					for(i in arr.size until newArr.size) {
						println("randomBytes ${i - arr.size}")
						newArr[i] = random.nextInt(0, 101).toByte()
					}
				newArr
			} else {
				byteArrayOf(
					*xBytes,
					' '.asciiByte(),
					*yBytes,
					' '.asciiByte(),
					'e'.asciiByte(),
					random.nextInt(100).toByte()
				)
			}
			println(data.toString(Charsets.US_ASCII))
			println(data.toHexStringNotFixed(' '))
			
			val encrypted = crypto.encryptSeed(iv, data)
			append(encrypted.toHexStringNotFixed(','))
			
			if(encryptedDecInitTime != null) {
				append('$')
				append(encryptedDecInitTime)
			}
		}
	}.also(::println)
	
	fun encryptPassword(password: String): String {
		val geos = getGeo(password)
		return encryptGeos(geos)
	}
}
