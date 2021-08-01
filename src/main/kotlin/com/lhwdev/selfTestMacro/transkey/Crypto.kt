@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lhwdev.selfTestMacro.transkey

import com.lhwdev.selfTestMacro.decodeBase64
import org.kisa.seed.KISA_SEED_CBC
import java.io.ByteArrayInputStream
import java.security.PublicKey
import java.security.cert.CertificateFactory
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random


private fun decodeX509DerPublicKey(x509PublicKeyEncoding: ByteArray): PublicKey =
	CertificateFactory.getInstance("X.509")
		.generateCertificate(ByteArrayInputStream(x509PublicKeyEncoding))
		.publicKey


class Crypto(random: Random, certification: String) {
	private val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding")
	private val key = decodeX509DerPublicKey(decodeBase64(certification))
	
	init {
		cipher.init(Cipher.ENCRYPT_MODE, key/*, spec*/)
	}
	
	
	val uuid = random.nextBytes(32).toHexString()
	
	private val genSessionKeyBytes = random.nextBytes(8)
	private val genSessionKey = genSessionKeyBytes.toHexString()
	private val hexByteSessionKey = genSessionKey.toByteArray()
	val encryptedKey = encryptRsa(hexByteSessionKey)
	private val seedSessionKey =
		genSessionKey.map { it.digitToInt(radix = 16).toByte() }.toByteArray()
	
	
	fun encryptRsa(data: ByteArray): String {
		if(stateless) {
			println("encryptRsa")
			Throwable().printStackTrace()
			val hex = readLine()!!
			return hex
		}
		return cipher.doFinal(data).toHexString()
	}
	
	fun encryptSeed(iv: ByteArray, data: ByteArray): ByteArray {
		// return seedEncryptCbc(data, seedRoundKey, iv)
		// println("size" + data.size)
		// if(data.size > 16) error("wow")
		return KISA_SEED_CBC.SEED_CBC_Encrypt(seedSessionKey, iv, data, 0, data.size)
	}
	
	
	fun hmacDigest(message: ByteArray): String {
		println("message: ${message.toHexString()}")
		println("key: ${hexByteSessionKey.toString(Charsets.UTF_8)}")
		val hmacSha256 = "HmacSHA256"
		val signingKey = SecretKeySpec(hexByteSessionKey, hmacSha256)
		val mac = Mac.getInstance(hmacSha256)
		mac.init(signingKey)
		return mac.doFinal(message).toHexString().also { println(it) }
		// return readLine()!!
		// hmac.new( // equivalent code:
		//             msg=msg,
		//             key=self.genSessionKey.encode(),
		//             digestmod=hashlib.sha256
		//         ).hexdigest()
	}
}
