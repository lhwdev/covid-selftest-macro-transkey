@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lhwdev.selfTestMacro.transkey

import com.lhwdev.io.decodeBase64
import org.kisa.seed.KISA_SEED_CBC
import java.io.ByteArrayInputStream
import java.security.PublicKey
import java.security.cert.CertificateFactory
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random


private val iv = byteArrayOf(
	0x4d, 0x6f, 0x62, 0x69, 0x6c, 0x65, 0x54, 0x72,
	0x61, 0x6e, 0x73, 0x4b, 0x65, 0x79, 0x31, 0x30
)


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
	
	private val seedRoundKey = KISA_SEED_CBC.SeedRoundKey(KISA_SEED_CBC.KISA_ENC_DEC.KISA_ENCRYPT, seedSessionKey, iv)
	
	
	fun encryptRsa(data: ByteArray): String {
		return cipher.doFinal(data).toHexString()
	}
	
	fun encryptSeed(data: ByteArray): ByteArray {
		return KISA_SEED_CBC.SEED_CBC_Encrypt(seedRoundKey, data, 0, data.size)
	}
	
	
	fun hmacDigest(message: ByteArray): String {
		val hmacSha256 = "HmacSHA256"
		val signingKey = SecretKeySpec(hexByteSessionKey, hmacSha256)
		val mac = Mac.getInstance(hmacSha256)
		mac.init(signingKey)
		return mac.doFinal(message).toHexString()
	}
}
