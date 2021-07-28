package com.lhwdev.selfTestMacro.transkey

import com.lhwdev.selfTestMacro.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.random.Random


private val sGetTokenRegex = Regex("var TK_requestToken=(.*);")

private val sDecInitTimeRegex = Regex("var decInitTime='(.*)';")
private val sInitTimeRegex = Regex("var initTime='(.*)';")
private val sUseSessionRegex = Regex("var useSession=(.*);")

private val sKeyInfoPointRegex = Regex("key\\.addPoint\\((\\d+), (\\d+)\\);")


suspend fun Transkey(
	session: Session,
	servletUrl: URL,
	random: Random = Random
): Transkey = withContext(Dispatchers.IO) {
	val token = sGetTokenRegex.find(
		session.fetch(servletUrl["op" to "getToken"]).value
	)!!.groupValues[1]
	
	val getInitTimeResult = session.fetch(servletUrl["op" to "getInitTime"]).value
	
	val decInitTime = sDecInitTimeRegex.find(getInitTimeResult)!!.groupValues[1]
	val initTime = sInitTimeRegex.find(getInitTimeResult)!!.groupValues[1]
	val useSession = sUseSessionRegex.find(getInitTimeResult)!!.groupValues[1] == "true"
	
	val publicKey = session.fetch(
		servletUrl,
		method = HttpMethod.post,
		body = HttpBodies.form {
			"op" set "getPublicKey"
			"TK_requestToken" set token
		}
	).value
	
	val crypto = Crypto(random, publicKey)
	
	val keyInfo = session.fetch(
		servletUrl,
		method = HttpMethod.post,
		body = HttpBodies.form {
			"op" set "getKeyInfo"
			"key" set crypto.encryptedKey
			"transkeyUuid" set crypto.uuid
			"useCert" set "true"
			"TK_requestToken" set token
			"mode" set "common"
		}
	).value
	
	val (_, num) = keyInfo.split("var number = new Array();")
	val numberKeys = num.split("number.push(key);").dropLast(1).map { p ->
		val groups = sKeyInfoPointRegex.find(p)!!.groupValues
		groups[1] to groups[2]
	}
	
	
	Transkey(
		session = session,
		servletUrl = servletUrl,
		token = token,
		decInitTime = decInitTime,
		initTime = initTime,
		useSession = useSession,
		crypto = crypto,
		numberKeys = numberKeys,
		random = random
	)
}


private val sKeyboardTypes = mapOf(
	"number" to "number"
)


class Transkey(
	val session: Session,
	val servletUrl: URL,
	val token: String,
	val decInitTime: String,
	val initTime: String,
	val useSession: Boolean,
	val crypto: Crypto,
	val numberKeys: List<Pair<String, String>>,
	val random: Random
) {
	private val allocIndex = random.nextInt().toString()
	private val keyIndex = random.nextInt(from = 0, until = 68).toString()
	
	val encryptedKeyIndex = crypto.encryptRsa(keyIndex.toByteArray(Charsets.US_ASCII))
	
	suspend fun newKeypad(
		keyType: String = "number",
		name: String,
		inputName: String,
		fieldType: String = "password"
	): KeyPad {
		require(keyType == "number") { "currently only supports keyType=\"number\"; provided $keyType" }
		
		val skipData = session.fetch(
			servletUrl,
			method = HttpMethod.post,
			body = HttpBodies.form {
				"op" set "getDummy"
				"name" set name
				"keyType" set "single"
				"keyboardType" set "${sKeyboardTypes[keyType]}"
				"fieldType" set fieldType
				"inputName" set inputName
				"transkeyUuid" set crypto.uuid
				"exE2E" set "false"
				"isCrt" set "false"
				"allocationIndex" set allocIndex
				"keyIndex" set encryptedKeyIndex
				"initTime" set initTime
				"TK_requestToken" set token
				"talkBack" set "true"
			}
		).value
		
		val skip = skipData.split(',').map { it.single() }
		
		return KeyPad(
			crypto = crypto,
			keyType = keyType,
			skipData = skip,
			keys = numberKeys,
			random = random,
			decInitTime = decInitTime,
			useSession = useSession
		)
	}
	
	fun hmacDigest(message: ByteArray): String = crypto.hmacDigest(message)
}
