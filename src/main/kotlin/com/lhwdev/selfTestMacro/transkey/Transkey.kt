package com.lhwdev.selfTestMacro.transkey

import com.lhwdev.fetch.*
import com.lhwdev.fetch.http.HttpMethod
import com.lhwdev.fetch.http.Session
import com.lhwdev.fetch.http.fetch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.random.Random


@Suppress("MayBeConstant")
val useAsyncTranskey = true

private val sGetTokenRegex = Regex("var TK_requestToken=(.*);")

private val sDecInitTimeRegex = Regex("var decInitTime='([0-9]*)';")
private val sInitTimeRegex = Regex("var initTime='([0-9a-fA-F]*)';")
private val sUseSessionRegex = Regex("var useSession=(true|false);")

private val sKeyInfoPointRegex = Regex("key\\.addPoint\\((\\d+), (\\d+)\\);")


suspend fun Transkey(
	session: Session,
	servletUrl: URL,
	random: Random = Random
): Transkey = withContext(Dispatchers.IO) {
	val token = sGetTokenRegex.find(
		session.fetch(servletUrl["op" to "getToken"]).getText()
	)!!.groupValues[1]
	
	val getInitTimeResult = session.fetch(servletUrl["op" to "getInitTime"]).getText()
	
	val decInitTime = sDecInitTimeRegex.find(getInitTimeResult)?.groupValues?.get(1)
	val initTime = sInitTimeRegex.find(getInitTimeResult)!!.groupValues[1]
	val useSession = sUseSessionRegex.find(getInitTimeResult)!!.groupValues[1] == "true"
	
	val certification = fetch(
		servletUrl,
		method = HttpMethod.post,
		body = Bodies.form {
			"op" set "getPublicKey"
			"TK_requestToken" set token
		}
	).getText()
	
	val crypto = Crypto(random, certification)
	
	val keyInfo = fetch(
		servletUrl,
		method = HttpMethod.post,
		body = Bodies.form {
			"op" set "getKeyInfo"
			"key" set crypto.encryptedKey
			"transkeyUuid" set crypto.uuid
			"useCert" set "true"
			"TK_requestToken" set token
			"mode" set "common"
		}
	).getText()
	
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
	val decInitTime: String?,
	val initTime: String,
	val useSession: Boolean,
	val crypto: Crypto,
	val numberKeys: List<Pair<String, String>>,
	val random: Random
) {
	private val allocIndex = random.nextLong(until = 0xffffffff).toString()
	
	suspend fun newKeypad(
		keyType: String = "number",
		name: String,
		inputName: String,
		fieldType: String = "password"
	): KeyPad {
		require(keyType == "number") { "currently only supports keyType=\"number\"; provided $keyType" }
		
		fun FormScope.commonData() {
			"name" set name
			"keyType" set "single"
			"keyboardType" set "${sKeyboardTypes[keyType]}"
			"fieldType" set fieldType
			"inputName" set inputName
			"transkeyUuid" set crypto.uuid
			"exE2E" set "false"
			"isCrt" set "false"
			"allocationIndex" set allocIndex
			"initTime" set initTime
			"TK_requestToken" set token
			"parentKeyboard" set "false"
			"talkBack" set "true"
		}
		
		val keyIndex = if(useAsyncTranskey) {
			fetch(
				servletUrl,
				method = HttpMethod.post,
				body = Bodies.form {
					commonData()
					"op" set "getKeyIndex"
				}
			).getText()
		} else {
			val keyIndex = random.nextInt(from = 10, until = 58).toString()
			crypto.encryptRsa(keyIndex.toByteArray(Charsets.US_ASCII))
		}
		
		val skipData = fetch(
			servletUrl,
			method = HttpMethod.post,
			body = Bodies.form {
				commonData()
				"op" set "getDummy"
				"keyIndex" set keyIndex
			}
		).getText()
		
		val skip = skipData.split(',').map { it.single() }
		
		return KeyPad(
			crypto = crypto,
			keyType = keyType,
			skipData = skip,
			keys = numberKeys,
			random = random,
			initTime = initTime,
			decInitTime = decInitTime,
			useSession = useSession,
			keyIndex = keyIndex
		)
	}
	
	fun hmacDigest(message: ByteArray): String = crypto.hmacDigest(message)
}
