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

private val sCertification =
	"""-----BEGIN CERTIFICATE-----MIIDQzCCAiugAwIBAgIJAOYjCX4wgWNoMA0GCSqGSIb3DQEBCwUAMGcxCzAJBgNVBAYTAktSMR0wGwYDVQQKExRSYW9uU2VjdXJlIENvLiwgTHRkLjEaMBgGA1UECxMRUXVhbGl0eSBBc3N1cmFuY2UxHTAbBgNVBAMTFFJhb25TZWN1cmUgQ28uLCBMdGQuMB4XDTE2MDcxOTA5MDYxNloXDTQ2MDcxMjA5MDYxNlowPzELMAkGA1UEBhMCS1IxDTALBgNVBAoTBFJORDMxITAfBgNVBAMUGFQ9UCZEPVsqLnJhb25zZWN1cmUuY29tXTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMtaq7IBKFodF527juYjDIduoTRozWiUQXFgv1jY5I9ZmPxKzVQor1vdezRf1QXHMfKTp1c4/Xv/OmVDPw2gtNcsks2+SbKGVpaF6WwWGqnEfaJW3niPd9mxqNIbAj49aAeQD3HHoz/nNsv1oxpkn4VbsqVrKug6hqykO5nz/wqcWbb8wsJ2K3ogbJ5lcjf54d+oBzskupEvGf11OY4+0MGNC8FaXn8xtLe/7i9ej0yqZ1B5lwDfzuTvecLIS9AQwQN7dlg3DRo/ceYdR7BkJM21SEwfRGUmA22zMDdAfYHFFCa9K/sSFnF+zPaMcySkXuMaIqZ6o2SJSSw0Alkc6Z8CAwEAAaMaMBgwCQYDVR0TBAIwADALBgNVHQ8EBAMCBeAwDQYJKoZIhvcNAQELBQADggEBAB8POkPF95mHq8mP+/xHf6V4m4njvpMEUXK/bKtCQOUxqwUI84Lf9BuvMtXCOTbR7T6g35y5lKHaKFu2S4pi9u3wiZfXck76YpImrLGllvvviXgs4XLwaaewvsRTFCRSD8DpeMU/jf1q6+VqMa+wThJGXQ0e8bSdBXru0h7yCTjgW/E1OCBjz2WT9JecjqpCoDBneglLMU/krm1cDWTXEIWJm0hZM6EDSuAh15sp4AikxIE/AoZO1TKQjlGIG+87qc35hOJEbJQdDIVUuD46cUjO41oI0pcdSLrigc8D4QDD8bBih4LZbkZpAc/uvimOvij/m0GglpCFQjm8jkyZxkc=-----END CERTIFICATE-----""".trimIndent()

// TODO: simplify this; inline .publicKey.encode() (which is in der format)
private fun decodeX509DerPublicKey(x509PublicKeyEncoding: ByteArray): PublicKey =
	CertificateFactory.getInstance("X.509")
		.generateCertificate(ByteArrayInputStream(x509PublicKeyEncoding))
		.publicKey

class Crypto(random: Random, certification: String = sCertification) {
	private val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding")
	private val key = decodeX509DerPublicKey(decodeBase64(certification))
	
	init {
		cipher.init(Cipher.ENCRYPT_MODE, key)
	}
	
	
	val uuid = random.nextBytes(32).toHexString()
	
	private val genSessionKey = random.nextBytes(8).toHexString()
	private val seedSessionKey =
		genSessionKey.map { it.digitToInt(radix = 16).toByte() }.toByteArray()
	
	val encryptedKey = encryptRsa(genSessionKey.toByteArray())
	
	private val seedRoundKey = KISA_SEED_CBC.SeedRoundKey(KISA_SEED_CBC.KISA_ENC_DEC.KISA_ENCRYPT, seedSessionKey, iv)
	
	fun encryptRsa(data: ByteArray): String {
		return cipher.doFinal(data).toHexString()
	}
	
	fun encryptSeed(data: ByteArray): ByteArray {
		return KISA_SEED_CBC.SEED_CBC_Encrypt(seedRoundKey, data, 0, data.size)
	}
	
	
	fun hmacDigest(message: ByteArray): String {
		val hmacSha256 = "HmacSHA256"
		val signingKey = SecretKeySpec(genSessionKey.toByteArray(), hmacSha256)
		val mac = Mac.getInstance(hmacSha256)
		mac.init(signingKey)
		return mac.doFinal(message).toHexString()
	}
}
