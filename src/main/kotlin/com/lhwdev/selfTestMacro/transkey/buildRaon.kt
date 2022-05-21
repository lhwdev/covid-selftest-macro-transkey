/*
 * Referenced https://github.com/kimcore/hcs.js/blob/7b65941042d8e0067ef248ea030ee1759c2f7918/src/util/buildRaon.ts
 * which is under MIT License.
 */
package com.lhwdev.selfTestMacro.transkey

import com.lhwdev.fetch.Bodies
import com.lhwdev.fetch.form
import com.lhwdev.fetch.get
import com.lhwdev.fetch.getText
import com.lhwdev.fetch.http.Session
import com.lhwdev.io.jsonObjectString
import java.net.URL
import kotlin.random.Random


private val sBaseUrl = URL("https://hcs.eduro.go.kr/transkeyServlet")

private val keysX = run {
	val n125 = byteArrayOf(1, 2, 5)
	val n165 = byteArrayOf(1, 6, 5)
	val n45 = byteArrayOf(4, 5)
	val n85 = byteArrayOf(8, 5)
	arrayOf(
		n125, n165, n165, n165,
		n165, n125, n85, n45,
		n45, n45, n45, n85
	)
}

private val keysY = run {
	val n27 = byteArrayOf(2, 7)
	val n67 = byteArrayOf(6, 7)
	val n107 = byteArrayOf(1, 0, 7)
	val n147 = byteArrayOf(1, 4, 7)
	arrayOf(
		n27, n27, n67, n107,
		n147, n147, n147, n147,
		n107, n67, n27, n27
	)
}

private val sInitTimeRegex = Regex("var initTime='([0-9a-fA-F]*)';")

private val crypto by lazy { Crypto(Random) }


suspend fun Session.buildRaon(password: String): String {
	val crypto = crypto
	
	val initTime = sInitTimeRegex.find(
		fetch(sBaseUrl["op" to "getInitTime"]).getText()
	)!!.groupValues[1]
	
	val keyIndex = fetch(
		sBaseUrl,
		body = Bodies.form {
			"op" set "getKeyIndex"
			"name" set "password"
			"keyboardType" set "number"
			"initTime" set initTime
		}
	).getText()
	
	val keys = fetch(
		sBaseUrl,
		body = Bodies.form {
			"op" set "getDummy"
			"keyboardType" set "number"
			"fieldType" set "password"
			"keyIndex" set keyIndex
			"talkBack" set "true"
		}
	).getText().split(' ').map { it.single() }
	
	// now, byte operations!
	val initTimeBytes = ByteArray(initTime.length) {
		val char = initTime[it]
		if(char.isLetter()) char.code.toByte() else char.digitToInt().toByte()
	}
	
	val geos = StringBuilder()
	
	for(char in password) {
		val index = keys.indexOf(char)
		if(index == -1) error("unknown key string $char (in '$password'): should only contain numbers")
		
		geos.append('$')
		val arr = byteArrayOf(
			*keysX[index],
			' '.asciiByte,
			*keysY[index],
			' '.asciiByte,
			*initTimeBytes,
			' '.asciiByte,
			'%'.asciiByte,
			'b'.asciiByte
		).copyOf(newSize = 48)
		
		val encrypted = crypto.encryptSeed(arr)
		geos.appendHexStringNotFixed(encrypted, ',')
	}
	
	val result = geos.toString()
	val hmac = crypto.hmacDigest(result.toByteArray())
	
	return jsonObjectString {
		"raon" jsonArray {
			addObject {
				"id" set "password"
				"enc" set result
				"hmac" set hmac
				"keyboardType" set "number"
				"keyIndex" set keyIndex
				"fieldType" set "password"
				"seedKey" set crypto.encryptedKey
				"initTime" set initTime
			}
		}
	}
}
