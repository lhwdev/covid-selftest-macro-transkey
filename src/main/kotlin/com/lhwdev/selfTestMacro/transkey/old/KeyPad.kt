package com.lhwdev.selfTestMacro.transkey.old

import com.lhwdev.selfTestMacro.transkey.appendHexStringNotFixed
import com.lhwdev.selfTestMacro.transkey.asciiByte
import kotlin.random.Random



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
		val encryptedDecInitTime = if(decInitTime != null) buildString {
			appendHexStringNotFixed(crypto.encryptSeed(decInitTime.toByteArray(Charsets.US_ASCII)), ',')
		} else {
			null
		}
		
		val initTimeBytes = if(useAsyncTranskey) {
			initTime.map { if(it.isLetter()) it.code.toByte() else it.digitToInt().toByte() }.toByteArray()
		} else {
			null
		}
		
		for(geo in geos) {
			append('$')
			
			val (x, y) = geo
			
			val xBytes = x.map { it.digitToInt().toByte() }.toByteArray()
			val yBytes = y.map { it.digitToInt().toByte() }.toByteArray()
			
			val data = if(/*useAsyncTranskey*/ initTimeBytes != null) {
				val arr = byteArrayOf(
					*xBytes,
					' '.asciiByte,
					*yBytes,
					' '.asciiByte,
					*initTimeBytes,
					' '.asciiByte,
					'%'.asciiByte,
					'b'.asciiByte
				)
				val newArr = arr.copyOf(newSize = 48)
				
				for(i in arr.size until newArr.size) {
					newArr[i] = random.nextInt(0, 101).toByte()
				}
				newArr
			} else {
				byteArrayOf(
					*xBytes,
					' '.asciiByte,
					*yBytes,
					' '.asciiByte,
					'e'.asciiByte,
					random.nextInt(100).toByte()
				)
			}
			val encrypted = crypto.encryptSeed(data)
			appendHexStringNotFixed(encrypted, ',')
			
			if(encryptedDecInitTime != null) {
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
