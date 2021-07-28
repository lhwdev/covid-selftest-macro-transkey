@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lhwdev.selfTestMacro.transkey

import com.lhwdev.selfTestMacro.decodeBase64
import com.lhwdev.selfTestMacro.seed.seedEncrypt
import com.lhwdev.selfTestMacro.seed.seedRoundKey
import java.io.ByteArrayInputStream
import java.security.PublicKey
import java.security.cert.CertificateFactory
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor
import kotlin.random.Random



private val UByte.hexPair: Pair<Byte, Byte>
	get() = (toInt() shr 4 and 0b1111).toByte() to (toInt() and 0b1111).toByte()

private fun decodeX509DerPublicKey(x509PublicKeyEncoding: ByteArray): PublicKey =
	CertificateFactory.getInstance("X.509")
		.generateCertificate(ByteArrayInputStream(x509PublicKeyEncoding))
		.publicKey


class Crypto(random: Random, publicKey: String) {
	private val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding")
	private val key = decodeX509DerPublicKey(decodeBase64(publicKey))
	
	init {
		cipher.init(Cipher.ENCRYPT_MODE, key/*, spec*/)
	}
	
	
	val uuid = random.nextBytes(32).toHexString()
	
	private val genSessionKeyBytes = random.nextBytes(8)
	private val genSessionKey = genSessionKeyBytes.toHexString()
	private val hexByteSessionKey = genSessionKey.toByteArray()
	val encryptedKey = encryptRsa(hexByteSessionKey)
	private val seedSessionKey = genSessionKey.map { it.digitToInt(radix = 16).toByte() }.toByteArray()
	
	
	fun encryptRsa(data: ByteArray): String {
		return cipher.doFinal(data).toHexString()
	}
	
	fun encryptSeed(iv: ByteArray, data: ByteArray): ByteArray {
		val newData = pad(data)
		for(i in 0 until 16) {
			newData[i] = newData[i] xor iv[i]
		}
		
		val roundKey = IntArray(32)
		seedRoundKey(seedSessionKey, roundKey)
		val outData = ByteArray(16)
		seedEncrypt(newData, outData, roundKey)
		return outData
	}
	
	private fun pad(data: ByteArray): ByteArray {
		if(data.size < 16) {
			return data.copyOf(newSize = 16)
		}
		return data.copyOf()
	}
	
	fun hmacDigest(message: ByteArray): String {
		println("message: ${message.toHexString()}")
		println("key: ${hexByteSessionKey.toString(Charsets.UTF_8)}")
		// return readLine()!!
		// hmac.new( // equivalent code:
		//             msg=msg,
		//             key=self.genSessionKey.encode(),
		//             digestmod=hashlib.sha256
		//         ).hexdigest()
		val hmacSha256 = "HmacSHA256"
		val signingKey = SecretKeySpec(hexByteSessionKey, hmacSha256)
		val mac = Mac.getInstance(hmacSha256)
		mac.init(signingKey)
		return mac.doFinal(message).toHexString()
	}
}
