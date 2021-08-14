@file:Suppress(
	"NOTHING_TO_INLINE", "LocalVariableName", "MemberVisibilityCanBePrivate", "FunctionName",
	"SpellCheckingInspection", "ClassName", "ObjectPropertyName", "PropertyName", "SameParameterValue",
	"UnnecessaryVariable"
)

package org.kisa.seed

import kotlin.math.pow


private inline infix fun Byte.and(other: Byte): Int = toInt() and other.toInt()
private inline infix fun Byte.and(other: Int): Int = toInt() and other


const val BIG_ENDIAN = 0
const val LITTLE_ENDIAN = 1

private inline fun memcpy(dst: IntArray, src: IntArray, src_offset: Int, length: Int) {
	val iLen = length / 4 + if(length % 4 != 0) 1 else 0
	if(iLen >= 0) System.arraycopy(src, src_offset, dst, 0, iLen)
}

private fun get_byte_for_int(src: IntArray, b_offset: Int, ENDIAN: Int): Byte {
	return if(ENDIAN == BIG_ENDIAN) {
		val shift_value = (3 - b_offset % 4) * 8
		val mask_value = 0x0ff shl shift_value
		val value = src[b_offset / 4] and mask_value shr shift_value
		value.toByte()
	} else {
		val shift_value = b_offset % 4 * 8
		val mask_value = 0x0ff shl shift_value
		val value = src[b_offset / 4] and mask_value shr shift_value
		value.toByte()
	}
}

private fun byte_to_int(dst: IntArray, dst_offset: Int, src: ByteArray, src_offset: Int, ENDIAN: Int) {
	if(ENDIAN == BIG_ENDIAN) {
		dst[dst_offset] = 0x0ff and src[src_offset].toInt() shl 24 or (0x0ff and src[src_offset + 1]
			.toInt() shl 16) or (0x0ff and src[src_offset + 2].toInt() shl 8) or (0x0ff and src[src_offset + 3]
			.toInt())
	} else {
		dst[dst_offset] = 0x0ff and src[src_offset].toInt() or (0x0ff and src[src_offset + 1]
			.toInt() shl 8) or (0x0ff and src[src_offset + 2].toInt() shl 16) or (0x0ff and src[src_offset + 3]
			.toInt() shl 24)
	}
}

private fun byte_to_int_big_endian(src: ByteArray, src_offset: Int): Int {
	return 0x0ff and src[src_offset].toInt() shl 24 or (0x0ff and src[src_offset + 1].toInt() shl 16) or (0x0ff and src[src_offset + 2]
		.toInt() shl 8) or (0x0ff and src[src_offset + 3].toInt())
}

private fun byte_to_int(src: ByteArray, src_offset: Int, ENDIAN: Int): Int {
	return if(ENDIAN == BIG_ENDIAN) {
		0x0ff and src[src_offset].toInt() shl 24 or (0x0ff and src[src_offset + 1].toInt() shl 16) or (0x0ff and src[src_offset + 2]
			.toInt() shl 8) or (0x0ff and src[src_offset + 3].toInt())
	} else {
		0x0ff and src[src_offset].toInt() or (0x0ff and src[src_offset + 1].toInt() shl 8) or (0x0ff and src[src_offset + 2]
			.toInt() shl 16) or (0x0ff and src[src_offset + 3].toInt() shl 24)
	}
}

private fun get_bytes_for_ints(src: IntArray, offset: Int, ENDIAN: Int): ByteArray {
	val iLen = src.size - offset
	val result = ByteArray(iLen * 4)
	for(i in 0 until iLen) {
		int_to_byte(result, i * 4, src, offset + i, ENDIAN)
	}
	return result
}

private fun set_byte_for_int(dst: IntArray, b_offset: Int, value: Byte, ENDIAN: Int) {
	if(ENDIAN == BIG_ENDIAN) {
		val shift_value = (3 - b_offset % 4) * 8
		val mask_value = 0x0ff shl shift_value
		val mask_value2 = mask_value.inv()
		val value2: Int = value and 0x0ff shl shift_value
		dst[b_offset / 4] = dst[b_offset / 4] and mask_value2 or (value2 and mask_value)
	} else {
		val shift_value = b_offset % 4 * 8
		val mask_value = 0x0ff shl shift_value
		val mask_value2 = mask_value.inv()
		val value2: Int = value and 0x0ff shl shift_value
		dst[b_offset / 4] = dst[b_offset / 4] and mask_value2 or (value2 and mask_value)
	}
}

private fun memcpy(dst: IntArray, src: ByteArray, length: Int, ENDIAN: Int) {
	val iLen = length / 4
	for(i in 0 until iLen) {
		byte_to_int(dst, i, src, i * 4, ENDIAN)
	}
}

private inline fun arraycopy(dst: ByteArray, src: ByteArray, length: Int) {
	if(length > 0) System.arraycopy(src, 0, dst, 0, length)
}

private inline fun int_to_byte(dst: ByteArray, dst_offset: Int, src: IntArray, src_offset: Int, ENDIAN: Int) {
	int_to_byte_unit(dst, dst_offset, src[src_offset], ENDIAN)
}



private fun URShift(x: Int, n: Int): Int {
	if(n == 0) return x
	if(n >= 32) return 0
	val v = x shr n
	val v_mask = (-0x80000000 shr n - 1).inv()
	return v and v_mask
}

private fun intToUnsigned(x: Int): Long {
	return if(x >= 0) x.toLong() else x + INT_RANGE_MAX
}

private val INT_RANGE_MAX = 2.0.pow(32.0).toLong()

private fun int_to_byte_unit_big_endian(dst: ByteArray, dst_offset: Int, src: Int) {
	dst[dst_offset] = (src shr 24 and 0x0ff).toByte()
	dst[dst_offset + 1] = (src shr 16 and 0x0ff).toByte()
	dst[dst_offset + 2] = (src shr 8 and 0x0ff).toByte()
	dst[dst_offset + 3] = (src and 0x0ff).toByte()
}

private fun int_to_byte_unit(dst: ByteArray, dst_offset: Int, src: Int, ENDIAN: Int) {
	if(ENDIAN == BIG_ENDIAN) {
		dst[dst_offset] = (src shr 24 and 0x0ff).toByte()
		dst[dst_offset + 1] = (src shr 16 and 0x0ff).toByte()
		dst[dst_offset + 2] = (src shr 8 and 0x0ff).toByte()
		dst[dst_offset + 3] = (src and 0x0ff).toByte()
	} else {
		dst[dst_offset] = (src and 0x0ff).toByte()
		dst[dst_offset + 1] = (src shr 8 and 0x0ff).toByte()
		dst[dst_offset + 2] = (src shr 16 and 0x0ff).toByte()
		dst[dst_offset + 3] = (src shr 24 and 0x0ff).toByte()
	}
}


/**
 * @author Copyright (c) 2013 by KISA
 * @file org.kisa.seed.KISA_SEED_CBC.java
 * @brief SEED CBC ��ȣ �˰���
 * @remarks http://seed.kisa.or.kr/
 */
object KISA_SEED_CBC {
	// DEFAULT : JAVA = BIG_ENDIAN
	private const val ENDIAN = LITTLE_ENDIAN
	
	// S-BOX
	private val SS0 = intArrayOf(
		0x2989a1a8, 0x05858184, 0x16c6d2d4, 0x13c3d3d0, 0x14445054, 0x1d0d111c, 0x2c8ca0ac, 0x25052124,
		0x1d4d515c, 0x03434340, 0x18081018, 0x1e0e121c, 0x11415150, 0x3cccf0fc, 0x0acac2c8, 0x23436360,
		0x28082028, 0x04444044, 0x20002020, 0x1d8d919c, 0x20c0e0e0, 0x22c2e2e0, 0x08c8c0c8, 0x17071314,
		0x2585a1a4, 0x0f8f838c, 0x03030300, 0x3b4b7378, 0x3b8bb3b8, 0x13031310, 0x12c2d2d0, 0x2ecee2ec,
		0x30407070, 0x0c8c808c, 0x3f0f333c, 0x2888a0a8, 0x32023230, 0x1dcdd1dc, 0x36c6f2f4, 0x34447074,
		0x2ccce0ec, 0x15859194, 0x0b0b0308, 0x17475354, 0x1c4c505c, 0x1b4b5358, 0x3d8db1bc, 0x01010100,
		0x24042024, 0x1c0c101c, 0x33437370, 0x18889098, 0x10001010, 0x0cccc0cc, 0x32c2f2f0, 0x19c9d1d8,
		0x2c0c202c, 0x27c7e3e4, 0x32427270, 0x03838380, 0x1b8b9398, 0x11c1d1d0, 0x06868284, 0x09c9c1c8,
		0x20406060, 0x10405050, 0x2383a3a0, 0x2bcbe3e8, 0x0d0d010c, 0x3686b2b4, 0x1e8e929c, 0x0f4f434c,
		0x3787b3b4, 0x1a4a5258, 0x06c6c2c4, 0x38487078, 0x2686a2a4, 0x12021210, 0x2f8fa3ac, 0x15c5d1d4,
		0x21416160, 0x03c3c3c0, 0x3484b0b4, 0x01414140, 0x12425250, 0x3d4d717c, 0x0d8d818c, 0x08080008,
		0x1f0f131c, 0x19899198, 0x00000000, 0x19091118, 0x04040004, 0x13435350, 0x37c7f3f4, 0x21c1e1e0,
		0x3dcdf1fc, 0x36467274, 0x2f0f232c, 0x27072324, 0x3080b0b0, 0x0b8b8388, 0x0e0e020c, 0x2b8ba3a8,
		0x2282a2a0, 0x2e4e626c, 0x13839390, 0x0d4d414c, 0x29496168, 0x3c4c707c, 0x09090108, 0x0a0a0208,
		0x3f8fb3bc, 0x2fcfe3ec, 0x33c3f3f0, 0x05c5c1c4, 0x07878384, 0x14041014, 0x3ecef2fc, 0x24446064,
		0x1eced2dc, 0x2e0e222c, 0x0b4b4348, 0x1a0a1218, 0x06060204, 0x21012120, 0x2b4b6368, 0x26466264,
		0x02020200, 0x35c5f1f4, 0x12829290, 0x0a8a8288, 0x0c0c000c, 0x3383b3b0, 0x3e4e727c, 0x10c0d0d0,
		0x3a4a7278, 0x07474344, 0x16869294, 0x25c5e1e4, 0x26062224, 0x00808080, 0x2d8da1ac, 0x1fcfd3dc,
		0x2181a1a0, 0x30003030, 0x37073334, 0x2e8ea2ac, 0x36063234, 0x15051114, 0x22022220, 0x38083038,
		0x34c4f0f4, 0x2787a3a4, 0x05454144, 0x0c4c404c, 0x01818180, 0x29c9e1e8, 0x04848084, 0x17879394,
		0x35053134, 0x0bcbc3c8, 0x0ecec2cc, 0x3c0c303c, 0x31417170, 0x11011110, 0x07c7c3c4, 0x09898188,
		0x35457174, 0x3bcbf3f8, 0x1acad2d8, 0x38c8f0f8, 0x14849094, 0x19495158, 0x02828280, 0x04c4c0c4,
		0x3fcff3fc, 0x09494148, 0x39093138, 0x27476364, 0x00c0c0c0, 0x0fcfc3cc, 0x17c7d3d4, 0x3888b0b8,
		0x0f0f030c, 0x0e8e828c, 0x02424240, 0x23032320, 0x11819190, 0x2c4c606c, 0x1bcbd3d8, 0x2484a0a4,
		0x34043034, 0x31c1f1f0, 0x08484048, 0x02c2c2c0, 0x2f4f636c, 0x3d0d313c, 0x2d0d212c, 0x00404040,
		0x3e8eb2bc, 0x3e0e323c, 0x3c8cb0bc, 0x01c1c1c0, 0x2a8aa2a8, 0x3a8ab2b8, 0x0e4e424c, 0x15455154,
		0x3b0b3338, 0x1cccd0dc, 0x28486068, 0x3f4f737c, 0x1c8c909c, 0x18c8d0d8, 0x0a4a4248, 0x16465254,
		0x37477374, 0x2080a0a0, 0x2dcde1ec, 0x06464244, 0x3585b1b4, 0x2b0b2328, 0x25456164, 0x3acaf2f8,
		0x23c3e3e0, 0x3989b1b8, 0x3181b1b0, 0x1f8f939c, 0x1e4e525c, 0x39c9f1f8, 0x26c6e2e4, 0x3282b2b0,
		0x31013130, 0x2acae2e8, 0x2d4d616c, 0x1f4f535c, 0x24c4e0e4, 0x30c0f0f0, 0x0dcdc1cc, 0x08888088,
		0x16061214, 0x3a0a3238, 0x18485058, 0x14c4d0d4, 0x22426260, 0x29092128, 0x07070304, 0x33033330,
		0x28c8e0e8, 0x1b0b1318, 0x05050104, 0x39497178, 0x10809090, 0x2a4a6268, 0x2a0a2228, 0x1a8a9298
	)
	private val SS1 = intArrayOf(
		0x38380830, -0x17d73720, 0x2c2d0d21, -0x5bd9795e, -0x33f0303d, -0x23e1312e, -0x4fcc7c4d, -0x47c77750,
		-0x53d0705d, 0x60204060, 0x54154551, -0x3bf8383d, 0x44044440, 0x6c2f4f63, 0x682b4b63, 0x581b4b53,
		-0x3ffc3c3d, 0x60224262, 0x30330333, -0x4bca7a4f, 0x28290921, -0x5fdf7f60, -0x1fdd3d1e, -0x5bd8785d,
		-0x2fec3c2d, -0x6fee7e6f, 0x10110111, 0x04060602, 0x1c1c0c10, -0x43c37350, 0x34360632, 0x480b4b43,
		-0x13d0301d, -0x77f77780, 0x6c2c4c60, -0x57d77760, 0x14170713, -0x3bfb3b40, 0x14160612, -0xbcb3b10,
		-0x3ffd3d3e, 0x44054541, -0x1fde3e1f, -0x2be9392e, 0x3c3f0f33, 0x3c3d0d31, -0x73f1717e, -0x67e77770,
		0x28280820, 0x4c0e4e42, -0xbc9390e, 0x3c3e0e32, -0x5bda7a5f, -0x7c6360f, 0x0c0d0d01, -0x23e0302d,
		-0x27e73730, 0x282b0b23, 0x64264662, 0x783a4a72, 0x24270723, 0x2c2f0f23, -0xfce3e0f, 0x70324272,
		0x40024242, -0x2beb3b30, 0x40014141, -0x3fff3f40, 0x70334373, 0x64274763, -0x53d37360, -0x77f4747d,
		-0xbc8380d, -0x53d2725f, -0x7fff7f80, 0x1c1f0f13, -0x37f5353e, 0x2c2c0c20, -0x57d5755e, 0x34340430,
		-0x2fed3d2e, 0x080b0b03, -0x13d1311e, -0x17d6361f, 0x5c1d4d51, -0x6beb7b70, 0x18180810, -0x7c73710,
		0x54174753, -0x53d1715e, 0x08080800, -0x3bfa3a3f, 0x10130313, -0x33f2323f, -0x7bf9797e, -0x47c6764f,
		-0x3c0300d, 0x7c3d4d71, -0x3ffe3e3f, 0x30310131, -0xbca3a0f, -0x77f5757e, 0x682a4a62, -0x4fce7e4f,
		-0x2fee3e2f, 0x20200020, -0x2be8382d, 0x00020202, 0x20220222, 0x04040400, 0x68284860, 0x70314171,
		0x04070703, -0x27e4342d, -0x63e2726f, -0x67e6766f, 0x60214161, -0x43c1714e, -0x1bd9391e, 0x58194951,
		-0x23e2322f, 0x50114151, -0x6fef7f70, -0x23e33330, -0x67e5756e, -0x5fdc7c5d, -0x57d4745d, -0x2fef3f30,
		-0x7ffe7e7f, 0x0c0f0f03, 0x44074743, 0x181a0a12, -0x1fdc3c1d, -0x13d33320, -0x73f2727f, -0x43c0704d,
		-0x6be9796e, 0x783b4b73, 0x5c1c4c50, -0x5fdd7d5e, -0x5fde7e5f, 0x60234363, 0x20230323, 0x4c0d4d41,
		-0x37f73740, -0x63e1716e, -0x63e37370, 0x383a0a32, 0x0c0c0c00, 0x2c2e0e22, -0x47c5754e, 0x6c2e4e62,
		-0x63e0706d, 0x581a4a52, -0xfcd3d0e, -0x6fed7d6e, -0xfcc3c0d, 0x48094941, 0x78384870, -0x33f33340,
		0x14150511, -0x7c4340d, 0x70304070, 0x74354571, 0x7c3f4f73, 0x34350531, 0x10100010, 0x00030303,
		0x64244460, 0x6c2d4d61, -0x3bf9393e, 0x74344470, -0x2bea3a2f, -0x4bcb7b50, -0x17d5351e, 0x08090901,
		0x74364672, 0x18190911, -0x3c1310e, 0x40004040, 0x10120212, -0x1fdf3f20, -0x43c2724f, 0x04050501,
		-0x7c5350e, 0x00010101, -0xfcf3f10, 0x282a0a22, 0x5c1e4e52, -0x57d6765f, 0x54164652, 0x40034343,
		-0x7bfa7a7f, 0x14140410, -0x77f6767f, -0x67e4746d, -0x4fcf7f50, -0x1bda3a1f, 0x48084840, 0x78394971,
		-0x6be8786d, -0x3c33310, 0x1c1e0e12, -0x7ffd7d7e, 0x20210121, -0x73f37380, 0x181b0b13, 0x5c1f4f53,
		0x74374773, 0x54144450, -0x4fcd7d4e, 0x1c1d0d11, 0x24250521, 0x4c0f4f43, 0x00000000, 0x44064642,
		-0x13d2321f, 0x58184850, 0x50124252, -0x17d4341d, 0x7c3e4e72, -0x27e5352e, -0x37f6363f, -0x3c2320f,
		0x30300030, -0x6bea7a6f, 0x64254561, 0x3c3c0c30, -0x4bc9794e, -0x1bdb3b20, -0x47c4744d, 0x7c3c4c70,
		0x0c0e0e02, 0x50104050, 0x38390931, 0x24260622, 0x30320232, -0x7bfb7b80, 0x68294961, -0x6fec7c6d,
		0x34370733, -0x1bd8381d, 0x24240420, -0x5bdb7b60, -0x37f4343d, 0x50134353, 0x080a0a02, -0x7bf8787d,
		-0x27e6362f, 0x4c0c4c40, -0x7ffc7c7d, -0x73f0707d, -0x33f1313e, 0x383b0b33, 0x480a4a42, -0x4bc8784d
	)
	private val SS2 = intArrayOf(
		-0x5e57d677, -0x7e7bfa7b, -0x2d2be93a, -0x2c2fec3d, 0x50541444, 0x111c1d0d, -0x5f53d374, 0x21242505,
		0x515c1d4d, 0x43400343, 0x10181808, 0x121c1e0e, 0x51501141, -0xf03c334, -0x3d37f536, 0x63602343,
		0x20282808, 0x40440444, 0x20202000, -0x6e63e273, -0x1f1fdf40, -0x1d1fdd3e, -0x3f37f738, 0x13141707,
		-0x5e5bda7b, -0x7c73f071, 0x03000303, 0x73783b4b, -0x4c47c475, 0x13101303, -0x2d2fed3e, -0x1d13d132,
		0x70703040, -0x7f73f374, 0x333c3f0f, -0x5f57d778, 0x32303202, -0x2e23e233, -0xd0bc93a, 0x70743444,
		-0x1f13d334, -0x6e6bea7b, 0x03080b0b, 0x53541747, 0x505c1c4c, 0x53581b4b, -0x4e43c273, 0x01000101,
		0x20242404, 0x101c1c0c, 0x73703343, -0x6f67e778, 0x10101000, -0x3f33f334, -0xd0fcd3e, -0x2e27e637,
		0x202c2c0c, -0x1c1bd839, 0x72703242, -0x7c7ffc7d, -0x6c67e475, -0x2e2fee3f, -0x7d7bf97a, -0x3e37f637,
		0x60602040, 0x50501040, -0x5c5fdc7d, -0x1c17d435, 0x010c0d0d, -0x4d4bc97a, -0x6d63e172, 0x434c0f4f,
		-0x4c4bc879, 0x52581a4a, -0x3d3bf93a, 0x70783848, -0x5d5bd97a, 0x12101202, -0x5c53d071, -0x2e2bea3b,
		0x61602141, -0x3c3ffc3d, -0x4f4bcb7c, 0x41400141, 0x52501242, 0x717c3d4d, -0x7e73f273, 0x00080808,
		0x131c1f0f, -0x6e67e677, 0x00000000, 0x11181909, 0x00040404, 0x53501343, -0xc0bc839, -0x1e1fde3f,
		-0xe03c233, 0x72743646, 0x232c2f0f, 0x23242707, -0x4f4fcf80, -0x7c77f475, 0x020c0e0e, -0x5c57d475,
		-0x5d5fdd7e, 0x626c2e4e, -0x6c6fec7d, 0x414c0d4d, 0x61682949, 0x707c3c4c, 0x01080909, 0x02080a0a,
		-0x4c43c071, -0x1c13d031, -0xc0fcc3d, -0x3e3bfa3b, -0x7c7bf879, 0x10141404, -0xd03c132, 0x60642444,
		-0x2d23e132, 0x222c2e0e, 0x43480b4b, 0x12181a0a, 0x02040606, 0x21202101, 0x63682b4b, 0x62642646,
		0x02000202, -0xe0bca3b, -0x6d6fed7e, -0x7d77f576, 0x000c0c0c, -0x4c4fcc7d, 0x727c3e4e, -0x2f2fef40,
		0x72783a4a, 0x43440747, -0x6d6be97a, -0x1e1bda3b, 0x22242606, -0x7f7fff80, -0x5e53d273, -0x2c23e031,
		-0x5e5fde7f, 0x30303000, 0x33343707, -0x5d53d172, 0x32343606, 0x11141505, 0x22202202, 0x30383808,
		-0xf0bcb3c, -0x5c5bd879, 0x41440545, 0x404c0c4c, -0x7e7ffe7f, -0x1e17d637, -0x7f7bfb7c, -0x6c6be879,
		0x31343505, -0x3c37f435, -0x3d33f132, 0x303c3c0c, 0x71703141, 0x11101101, -0x3c3bf839, -0x7e77f677,
		0x71743545, -0xc07c435, -0x2d27e536, -0xf07c738, -0x6f6beb7c, 0x51581949, -0x7d7ffd7e, -0x3f3bfb3c,
		-0xc03c031, 0x41480949, 0x31383909, 0x63642747, -0x3f3fff40, -0x3c33f031, -0x2c2be839, -0x4f47c778,
		0x030c0f0f, -0x7d73f172, 0x42400242, 0x23202303, -0x6e6fee7f, 0x606c2c4c, -0x2c27e435, -0x5f5bdb7c,
		0x30343404, -0xe0fce3f, 0x40480848, -0x3d3ffd3e, 0x636c2f4f, 0x313c3d0d, 0x212c2d0d, 0x40400040,
		-0x4d43c172, 0x323c3e0e, -0x4f43c374, -0x3e3ffe3f, -0x5d57d576, -0x4d47c576, 0x424c0e4e, 0x51541545,
		0x33383b0b, -0x2f23e334, 0x60682848, 0x737c3f4f, -0x6f63e374, -0x2f27e738, 0x42480a4a, 0x52541646,
		0x73743747, -0x5f5fdf80, -0x1e13d233, 0x42440646, -0x4e4bca7b, 0x23282b0b, 0x61642545, -0xd07c536,
		-0x1c1fdc3d, -0x4e47c677, -0x4e4fce7f, -0x6c63e071, 0x525c1e4e, -0xe07c637, -0x1d1bd93a, -0x4d4fcd7e,
		0x31303101, -0x1d17d536, 0x616c2d4d, 0x535c1f4f, -0x1f1bdb3c, -0xf0fcf40, -0x3e33f233, -0x7f77f778,
		0x12141606, 0x32383a0a, 0x50581848, -0x2f2beb3c, 0x62602242, 0x21282909, 0x03040707, 0x33303303,
		-0x1f17d738, 0x13181b0b, 0x01040505, 0x71783949, -0x6f6fef80, 0x62682a4a, 0x22282a0a, -0x6d67e576
	)
	private val SS3 = intArrayOf(
		0x08303838, -0x371f17d8, 0x0d212c2d, -0x795d5bda, -0x303c33f1, -0x312d23e2, -0x7c4c4fcd, -0x774f47c8,
		-0x705c53d1, 0x40606020, 0x45515415, -0x383c3bf9, 0x44404404, 0x4f636c2f, 0x4b63682b, 0x4b53581b,
		-0x3c3c3ffd, 0x42626022, 0x03333033, -0x7a4e4bcb, 0x09212829, -0x7f5f5fe0, -0x3d1d1fde, -0x785c5bd9,
		-0x3c2c2fed, -0x7e6e6fef, 0x01111011, 0x06020406, 0x0c101c1c, -0x734f43c4, 0x06323436, 0x4b43480b,
		-0x301c13d1, -0x777f77f8, 0x4c606c2c, -0x775f57d8, 0x07131417, -0x3b3f3bfc, 0x06121416, -0x3b0f0bcc,
		-0x3d3d3ffe, 0x45414405, -0x3e1e1fdf, -0x392d2bea, 0x0f333c3f, 0x0d313c3d, -0x717d73f2, -0x776f67e8,
		0x08202828, 0x4e424c0e, -0x390d0bca, 0x0e323c3e, -0x7a5e5bdb, -0x360e07c7, 0x0d010c0d, -0x302c23e1,
		-0x372f27e8, 0x0b23282b, 0x46626426, 0x4a72783a, 0x07232427, 0x0f232c2f, -0x3e0e0fcf, 0x42727032,
		0x42424002, -0x3b2f2bec, 0x41414001, -0x3f3f4000, 0x43737033, 0x47636427, -0x735f53d4, -0x747c77f5,
		-0x380c0bc9, -0x725e53d3, -0x7f7f8000, 0x0f131c1f, -0x353d37f6, 0x0c202c2c, -0x755d57d6, 0x04303434,
		-0x3d2d2fee, 0x0b03080b, -0x311d13d2, -0x361e17d7, 0x4d515c1d, -0x7b6f6bec, 0x08101818, -0x370f07c8,
		0x47535417, -0x715d53d2, 0x08000808, -0x3a3e3bfb, 0x03131013, -0x323e33f3, -0x797d7bfa, -0x764e47c7,
		-0x300c03c1, 0x4d717c3d, -0x3e3e3fff, 0x01313031, -0x3a0e0bcb, -0x757d77f6, 0x4a62682a, -0x7e4e4fcf,
		-0x3e2e2fef, 0x00202020, -0x382c2be9, 0x02020002, 0x02222022, 0x04000404, 0x48606828, 0x41717031,
		0x07030407, -0x342c27e5, -0x726e63e3, -0x766e67e7, 0x41616021, -0x714d43c2, -0x391d1bda, 0x49515819,
		-0x322e23e3, 0x41515011, -0x7f6f6ff0, -0x332f23e4, -0x756d67e6, -0x7c5c5fdd, -0x745c57d5, -0x3f2f2ff0,
		-0x7e7e7fff, 0x0f030c0f, 0x47434407, 0x0a12181a, -0x3c1c1fdd, -0x331f13d4, -0x727e73f3, -0x704c43c1,
		-0x796d6bea, 0x4b73783b, 0x4c505c1c, -0x7d5d5fde, -0x7e5e5fdf, 0x43636023, 0x03232023, 0x4d414c0d,
		-0x373f37f8, -0x716d63e2, -0x736f63e4, 0x0a32383a, 0x0c000c0c, 0x0e222c2e, -0x754d47c6, 0x4e626c2e,
		-0x706c63e1, 0x4a52581a, -0x3d0d0fce, -0x7d6d6fee, -0x3c0c0fcd, 0x49414809, 0x48707838, -0x333f33f4,
		0x05111415, -0x340c07c5, 0x40707030, 0x45717435, 0x4f737c3f, 0x05313435, 0x00101010, 0x03030003,
		0x44606424, 0x4d616c2d, -0x393d3bfa, 0x44707434, -0x3a2e2beb, -0x7b4f4bcc, -0x351d17d6, 0x09010809,
		0x46727436, 0x09111819, -0x310d03c2, 0x40404000, 0x02121012, -0x3f1f1fe0, -0x724e43c3, 0x05010405,
		-0x350d07c6, 0x01010001, -0x3f0f0fd0, 0x0a22282a, 0x4e525c1e, -0x765e57d7, 0x46525416, 0x43434003,
		-0x7a7e7bfb, 0x04101414, -0x767e77f7, -0x746c67e5, -0x7f4f4fd0, -0x3a1e1bdb, 0x48404808, 0x49717839,
		-0x786c6be9, -0x330f03c4, 0x0e121c1e, -0x7d7d7ffe, 0x01212021, -0x737f73f4, 0x0b13181b, 0x4f535c1f,
		0x47737437, 0x44505414, -0x7d4d4fce, 0x0d111c1d, 0x05212425, 0x4f434c0f, 0x00000000, 0x46424406,
		-0x321e13d3, 0x48505818, 0x42525012, -0x341c17d5, 0x4e727c3e, -0x352d27e6, -0x363e37f7, -0x320e03c3,
		0x00303030, -0x7a6e6beb, 0x45616425, 0x0c303c3c, -0x794d4bca, -0x3b1f1bdc, -0x744c47c5, 0x4c707c3c,
		0x0e020c0e, 0x40505010, 0x09313839, 0x06222426, 0x02323032, -0x7b7f7bfc, 0x49616829, -0x7c6c6fed,
		0x07333437, -0x381c1bd9, 0x04202424, -0x7b5f5bdc, -0x343c37f5, 0x43535013, 0x0a02080a, -0x787c7bf9,
		-0x362e27e7, 0x4c404c0c, -0x7c7c7ffd, -0x707c73f1, -0x313d33f2, 0x0b33383b, 0x4a42480a, -0x784c4bc9
	)
	
	
	private const val BLOCK_SIZE_SEED = 16
	private const val BLOCK_SIZE_SEED_INT = 4
	
	private inline fun GetB0(A: Int): Byte {
		return (A and 0x0ff).toByte()
	}
	
	private inline fun GetB1(A: Int): Byte {
		return (A shr 8 and 0x0ff).toByte()
	}
	
	private inline fun GetB2(A: Int): Byte {
		return (A shr 16 and 0x0ff).toByte()
	}
	
	private inline fun GetB3(A: Int): Byte {
		return (A shr 24 and 0x0ff).toByte()
	}
	
	// Round function F and adding output of F to L.
	// L0, L1 : left input values at each round
	// R0, R1 : right input values at each round
	// K : round keys at each round
	private fun SeedRound(T: IntArray, LR: IntArray, L0: Int, L1: Int, R0: Int, R1: Int, K: IntArray, K_offset: Int) {
		T[0] = LR[R0] xor K[K_offset]
		T[1] = LR[R1] xor K[K_offset + 1]
		T[1] = T[1] xor T[0]
		T[1] = SS0[GetB0(T[1]) and 0x0ff] xor SS1[GetB1(T[1]) and 0x0ff] xor
			SS2[GetB2(T[1]) and 0x0ff] xor SS3[GetB3(T[1]) and 0x0ff]
		T[0] += T[1]
		T[0] = SS0[GetB0(T[0]) and 0x0ff] xor SS1[GetB1(T[0]) and 0x0ff] xor
			SS2[GetB2(T[0]) and 0x0ff] xor SS3[GetB3(T[0]) and 0x0ff]
		T[1] += T[0]
		T[1] = SS0[GetB0(T[1]) and 0x0ff] xor SS1[GetB1(T[1]) and 0x0ff] xor
			SS2[GetB2(T[1]) and 0x0ff] xor SS3[GetB3(T[1]) and 0x0ff]
		T[0] += T[1]
		LR[L0] = LR[L0] xor T[0]
		LR[L1] = LR[L1] xor T[1]
	}
	
	private fun EndianChange(dwS: Int): Int {
		return /*ROTL(dwS,8)*/dwS shl 8 or (dwS shr 32 - 8 and 0x000000ff) and 0x00ff00ff or ( /*ROTL(dwS,24)*/dwS shl 24 or (dwS shr 32 - 24 and 0x00ffffff) and -0xff0100)
	}
	
	/************************ Constants for Key schedule  */
	private const val KC0 = -0x61c88647
	private const val KC1 = 0x3c6ef373
	private const val KC2 = 0x78dde6e6
	private const val KC3 = -0xe443234
	private const val KC4 = -0x1c886467
	private const val KC5 = -0x3910c8cd
	private const val KC6 = -0x72219199
	private const val KC7 = 0x1bbcdccf
	private const val KC8 = 0x3779b99e
	private const val KC9 = 0x6ef3733c
	private const val KC10 = -0x22191988
	private const val KC11 = -0x4432330f
	private const val KC12 = 0x779b99e3
	private const val KC13 = -0x10c8cc3a
	private const val KC14 = -0x21919873
	private const val KC15 = -0x432330e5
	private const val ABCD_A = 0
	private const val ABCD_B = 1
	private const val ABCD_C = 2
	private const val ABCD_D = 3
	
	private fun RoundKeyUpdate0(T: IntArray, K: IntArray, K_offset: Int, ABCD: IntArray, KC: Int) {
		T[0] = ABCD[ABCD_A] + ABCD[ABCD_C] - KC
		T[1] = ABCD[ABCD_B] + KC - ABCD[ABCD_D]
		K[K_offset] = SS0[GetB0(T[0]) and 0x0ff] xor SS1[GetB1(T[0]) and 0x0ff] xor SS2[GetB2(
			T[0]
		) and 0x0ff] xor SS3[GetB3(T[0]) and 0x0ff]
		K[K_offset + 1] = SS0[GetB0(T[1]) and 0x0ff] xor SS1[GetB1(
			T[1]
		) and 0x0ff] xor SS2[GetB2(T[1]) and 0x0ff] xor SS3[GetB3(
			T[1]
		) and 0x0ff]
		T[0] = ABCD[ABCD_A]
		ABCD[ABCD_A] = ABCD[ABCD_A] shr 8 and 0x00ffffff xor (ABCD[ABCD_B] shl 24)
		ABCD[ABCD_B] = ABCD[ABCD_B] shr 8 and 0x00ffffff xor (T[0] shl 24)
	}
	
	private fun RoundKeyUpdate1(T: IntArray, K: IntArray, K_offset: Int, ABCD: IntArray, KC: Int) {
		T[0] = ABCD[ABCD_A] + ABCD[ABCD_C] - KC
		T[1] = ABCD[ABCD_B] + KC - ABCD[ABCD_D]
		K[K_offset] = SS0[GetB0(T[0]) and 0x0ff] xor SS1[GetB1(T[0]) and 0x0ff] xor SS2[GetB2(
			T[0]
		) and 0x0ff] xor SS3[GetB3(T[0]) and 0x0ff]
		K[K_offset + 1] = SS0[GetB0(T[1]) and 0x0ff] xor SS1[GetB1(
			T[1]
		) and 0x0ff] xor SS2[GetB2(T[1]) and 0x0ff] xor SS3[GetB3(
			T[1]
		) and 0x0ff]
		T[0] = ABCD[ABCD_C]
		ABCD[ABCD_C] = ABCD[ABCD_C] shl 8 xor (ABCD[ABCD_D] shr 24 and 0x000000ff)
		ABCD[ABCD_D] = ABCD[ABCD_D] shl 8 xor (T[0] shr 24 and 0x000000ff)
	}
	
	private fun BLOCK_XOR_CBC(
		OUT_VALUE: IntArray, out_value_offset: Int, IN_VALUE1: IntArray, in_value1_offset: Int,
		IN_VALUE2: IntArray, in_value2_offset: Int
	) {
		OUT_VALUE[out_value_offset] =
			(if(in_value1_offset < IN_VALUE1.size) IN_VALUE1[in_value1_offset] else 0) xor if(in_value2_offset < IN_VALUE2.size) IN_VALUE2[in_value2_offset] else 0
		OUT_VALUE[out_value_offset + 1] =
			(if(in_value1_offset + 1 < IN_VALUE1.size) IN_VALUE1[in_value1_offset + 1] else 0) xor if(in_value2_offset + 1 < IN_VALUE2.size) IN_VALUE2[in_value2_offset + 1] else 0
		OUT_VALUE[out_value_offset + 2] =
			(if(in_value1_offset + 2 < IN_VALUE1.size) IN_VALUE1[in_value1_offset + 2] else 0) xor if(in_value2_offset + 2 < IN_VALUE2.size) IN_VALUE2[in_value2_offset + 2] else 0
		OUT_VALUE[out_value_offset + 3] =
			(if(in_value1_offset + 3 < IN_VALUE1.size) IN_VALUE1[in_value1_offset + 3] else 0) xor if(in_value2_offset + 3 < IN_VALUE2.size) IN_VALUE2[in_value2_offset + 3] else 0
	}
	
	private const val LR_L0 = 0
	private const val LR_L1 = 1
	private const val LR_R0 = 2
	private const val LR_R1 = 3
	
	private fun KISA_SEED_Encrypt_Block_forCBC(
		input: IntArray,
		in_offset: Int,
		out: IntArray,
		out_offset: Int,
		ks: IntArray
	) {
		val LR = IntArray(4) // Iuput/output values at each rounds
		val T = IntArray(2) // Temporary variables for round function F
		val K = ks // Pointer of round keys
		
		// Set up input values for first round
		LR[LR_L0] = input[in_offset]
		LR[LR_L1] = input[in_offset + 1]
		LR[LR_R0] = input[in_offset + 2]
		LR[LR_R1] = input[in_offset + 3]
		
		// Reorder for big endian
		// Because SEED use little endian order in default
		if(BIG_ENDIAN != ENDIAN) {
			LR[LR_L0] = EndianChange(LR[LR_L0])
			LR[LR_L1] = EndianChange(LR[LR_L1])
			LR[LR_R0] = EndianChange(LR[LR_R0])
			LR[LR_R1] = EndianChange(LR[LR_R1])
		}
		SeedRound(T, LR, LR_L0, LR_L1, LR_R0, LR_R1, K, 0) // Round 1
		SeedRound(T, LR, LR_R0, LR_R1, LR_L0, LR_L1, K, 2) // Round 2
		SeedRound(T, LR, LR_L0, LR_L1, LR_R0, LR_R1, K, 4) // Round 3
		SeedRound(T, LR, LR_R0, LR_R1, LR_L0, LR_L1, K, 6) // Round 4
		SeedRound(T, LR, LR_L0, LR_L1, LR_R0, LR_R1, K, 8) // Round 5
		SeedRound(T, LR, LR_R0, LR_R1, LR_L0, LR_L1, K, 10) // Round 6
		SeedRound(T, LR, LR_L0, LR_L1, LR_R0, LR_R1, K, 12) // Round 7
		SeedRound(T, LR, LR_R0, LR_R1, LR_L0, LR_L1, K, 14) // Round 8
		SeedRound(T, LR, LR_L0, LR_L1, LR_R0, LR_R1, K, 16) // Round 9
		SeedRound(T, LR, LR_R0, LR_R1, LR_L0, LR_L1, K, 18) // Round 10
		SeedRound(T, LR, LR_L0, LR_L1, LR_R0, LR_R1, K, 20) // Round 11
		SeedRound(T, LR, LR_R0, LR_R1, LR_L0, LR_L1, K, 22) // Round 12
		SeedRound(T, LR, LR_L0, LR_L1, LR_R0, LR_R1, K, 24) // Round 13
		SeedRound(T, LR, LR_R0, LR_R1, LR_L0, LR_L1, K, 26) // Round 14
		SeedRound(T, LR, LR_L0, LR_L1, LR_R0, LR_R1, K, 28) // Round 15
		SeedRound(T, LR, LR_R0, LR_R1, LR_L0, LR_L1, K, 30) // Round 16
		if(BIG_ENDIAN != ENDIAN) {
			LR[LR_L0] = EndianChange(LR[LR_L0])
			LR[LR_L1] = EndianChange(LR[LR_L1])
			LR[LR_R0] = EndianChange(LR[LR_R0])
			LR[LR_R1] = EndianChange(LR[LR_R1])
		}
		
		// Copying output values from last round to pbData
		out[out_offset] = LR[LR_R0]
		out[out_offset + 1] = LR[LR_R1]
		out[out_offset + 2] = LR[LR_L0]
		out[out_offset + 3] = LR[LR_L1]
	}
	
	private fun KISA_SEED_Decrypt_Block_forCBC(
		input: IntArray,
		in_offset: Int,
		out: IntArray,
		out_offset: Int,
		ks: IntArray
	) {
		val LR = IntArray(4) // Iuput/output values at each rounds
		val T = IntArray(2) // Temporary variables for round function F
		val K = ks // Pointer of round keys
		
		// Set up input values for first round
		LR[LR_L0] = input[in_offset]
		LR[LR_L1] = input[in_offset + 1]
		LR[LR_R0] = input[in_offset + 2]
		LR[LR_R1] = input[in_offset + 3]
		
		// Reorder for big endian
		if(BIG_ENDIAN != ENDIAN) {
			LR[LR_L0] = EndianChange(LR[LR_L0])
			LR[LR_L1] = EndianChange(LR[LR_L1])
			LR[LR_R0] = EndianChange(LR[LR_R0])
			LR[LR_R1] = EndianChange(LR[LR_R1])
		}
		SeedRound(T, LR, LR_L0, LR_L1, LR_R0, LR_R1, K, 30) // Round 1
		SeedRound(T, LR, LR_R0, LR_R1, LR_L0, LR_L1, K, 28) // Round 2
		SeedRound(T, LR, LR_L0, LR_L1, LR_R0, LR_R1, K, 26) // Round 3
		SeedRound(T, LR, LR_R0, LR_R1, LR_L0, LR_L1, K, 24) // Round 4
		SeedRound(T, LR, LR_L0, LR_L1, LR_R0, LR_R1, K, 22) // Round 5
		SeedRound(T, LR, LR_R0, LR_R1, LR_L0, LR_L1, K, 20) // Round 6
		SeedRound(T, LR, LR_L0, LR_L1, LR_R0, LR_R1, K, 18) // Round 7
		SeedRound(T, LR, LR_R0, LR_R1, LR_L0, LR_L1, K, 16) // Round 8
		SeedRound(T, LR, LR_L0, LR_L1, LR_R0, LR_R1, K, 14) // Round 9
		SeedRound(T, LR, LR_R0, LR_R1, LR_L0, LR_L1, K, 12) // Round 10
		SeedRound(T, LR, LR_L0, LR_L1, LR_R0, LR_R1, K, 10) // Round 11
		SeedRound(T, LR, LR_R0, LR_R1, LR_L0, LR_L1, K, 8) // Round 12
		SeedRound(T, LR, LR_L0, LR_L1, LR_R0, LR_R1, K, 6) // Round 13
		SeedRound(T, LR, LR_R0, LR_R1, LR_L0, LR_L1, K, 4) // Round 14
		SeedRound(T, LR, LR_L0, LR_L1, LR_R0, LR_R1, K, 2) // Round 15
		SeedRound(T, LR, LR_R0, LR_R1, LR_L0, LR_L1, K, 0) // Round 16
		if(BIG_ENDIAN != ENDIAN) {
			LR[LR_L0] = EndianChange(LR[LR_L0])
			LR[LR_L1] = EndianChange(LR[LR_L1])
			LR[LR_R0] = EndianChange(LR[LR_R0])
			LR[LR_R1] = EndianChange(LR[LR_R1])
		}
		
		// Copy output values from last round to pbData
		out[out_offset] = LR[LR_R0]
		out[out_offset + 1] = LR[LR_R1]
		out[out_offset + 2] = LR[LR_L0]
		out[out_offset + 3] = LR[LR_L1]
	}
	
	fun chartoint32_for_SEED_CBC(input: ByteArray, inLen: Int): IntArray {
		
		val len = if(inLen % 4 > 0) inLen / 4 + 1 else inLen / 4
		val data = IntArray(len)
		var i = 0
		while(i < len) {
			byte_to_int(data, i, input, i * 4, ENDIAN)
			i++
		}
		return data
	}
	
	fun int32tochar_for_SEED_CBC(input: IntArray, inLen: Int): ByteArray {
		var i: Int
		val data = ByteArray(inLen)
		if(ENDIAN != BIG_ENDIAN) {
			i = 0
			while(i < inLen) {
				data[i] = (input[i / 4] shr i % 4 * 8).toByte()
				i++
			}
		} else {
			i = 0
			while(i < inLen) {
				data[i] = (input[i / 4] shr (3 - i % 4) * 8).toByte()
				i++
			}
		}
		return data
	}
	
	fun SEED_CBC_Process(
		pInfo: KISA_SEED_INFO,
		input: IntArray,
		inLen: Int,
		output: IntArray,
		outLen: IntArray
	) {
		var nCurrentCount = BLOCK_SIZE_SEED
		var pdwXOR: IntArray
		var in_offset = 0
		var out_offset = 0
		var pdwXOR_offset = 0
		
		if(pInfo.encrypt == KISA_ENC_DEC.KISA_ENCRYPT) {
			pdwXOR = pInfo.ivec
			while(nCurrentCount <= inLen) {
				BLOCK_XOR_CBC(output, out_offset, input, in_offset, pdwXOR, pdwXOR_offset)
				KISA_SEED_Encrypt_Block_forCBC(output, out_offset, output, out_offset, pInfo.seed_key)
				pdwXOR = output
				pdwXOR_offset = out_offset
				nCurrentCount += BLOCK_SIZE_SEED
				in_offset += BLOCK_SIZE_SEED_INT
				out_offset += BLOCK_SIZE_SEED_INT
			}
			outLen[0] = nCurrentCount - BLOCK_SIZE_SEED
			pInfo.buffer_length = inLen - outLen[0]
			memcpy(pInfo.ivec, pdwXOR, pdwXOR_offset, BLOCK_SIZE_SEED)
			memcpy(pInfo.cbc_buffer, input, in_offset, pInfo.buffer_length)
		} else {
			pdwXOR = pInfo.ivec
			while(nCurrentCount <= inLen) {
				KISA_SEED_Decrypt_Block_forCBC(input, in_offset, output, out_offset, pInfo.seed_key)
				BLOCK_XOR_CBC(output, out_offset, output, out_offset, pdwXOR, pdwXOR_offset)
				pdwXOR = input
				pdwXOR_offset = in_offset
				nCurrentCount += BLOCK_SIZE_SEED
				in_offset += BLOCK_SIZE_SEED_INT
				out_offset += BLOCK_SIZE_SEED_INT
			}
			outLen[0] = nCurrentCount - BLOCK_SIZE_SEED
			memcpy(pInfo.ivec, pdwXOR, pdwXOR_offset, BLOCK_SIZE_SEED)
			memcpy(pInfo.cbc_last_block, output, out_offset - BLOCK_SIZE_SEED_INT, BLOCK_SIZE_SEED)
		}
	}
	
	fun SEED_CBC_Close(pInfo: KISA_SEED_INFO, out: IntArray, out_offset: Int, outLen: IntArray): Int {
		val nPaddngLeng: Int
		var i: Int
		outLen[0] = 0
		if(pInfo.encrypt == KISA_ENC_DEC.KISA_ENCRYPT) {
			nPaddngLeng = BLOCK_SIZE_SEED - pInfo.buffer_length
			i = pInfo.buffer_length
			while(i < BLOCK_SIZE_SEED) {
				set_byte_for_int(pInfo.cbc_buffer, i, nPaddngLeng.toByte(), ENDIAN)
				i++
			}
			BLOCK_XOR_CBC(pInfo.cbc_buffer, 0, pInfo.cbc_buffer, 0, pInfo.ivec, 0)
			KISA_SEED_Encrypt_Block_forCBC(pInfo.cbc_buffer, 0, out, out_offset, pInfo.seed_key)
			outLen[0] = BLOCK_SIZE_SEED
			return 1
		} else {
			nPaddngLeng = get_byte_for_int(pInfo.cbc_last_block, BLOCK_SIZE_SEED - 1, ENDIAN).toInt()
			if(nPaddngLeng in 1..BLOCK_SIZE_SEED) {
				i = nPaddngLeng
				while(i > 0) {
					set_byte_for_int(out, out_offset - i, 0x00.toByte(), ENDIAN)
					i--
				}
				outLen[0] = nPaddngLeng
			} else return 0
		}
		return 1
	}
	
	fun SEED_CBC_Encrypt(
		roundKey: RoundKey,
		message: ByteArray,
		message_offset: Int,
		message_length: Int
	): ByteArray {
		val info = roundKey.newSeedInfo()
		val nRetOutLeng = intArrayOf(0)
		val nPaddingLeng = intArrayOf(0)
		val pbszPlainText = ByteArray(message_length)
		
		System.arraycopy(message, message_offset, pbszPlainText, 0, message_length)
		val nPlainTextLen = pbszPlainText.size
		val nPlainTextPadding = BLOCK_SIZE_SEED - nPlainTextLen % BLOCK_SIZE_SEED
		val newpbszPlainText = ByteArray(nPlainTextLen + nPlainTextPadding)
		arraycopy(newpbszPlainText, pbszPlainText, nPlainTextLen)
		val pbszCipherText = ByteArray(newpbszPlainText.size)
		
		// SeedRoundKey(info, KISA_ENC_DEC.KISA_ENCRYPT, pbszUserKey, pbszIV);
		val outlen = newpbszPlainText.size / BLOCK_SIZE_SEED * BLOCK_SIZE_SEED_INT
		val outbuf = IntArray(outlen)
		val data = chartoint32_for_SEED_CBC(newpbszPlainText, nPlainTextLen)
		SEED_CBC_Process(info, data, nPlainTextLen, outbuf, nRetOutLeng)
		SEED_CBC_Close(info, outbuf, nRetOutLeng[0] / 4, nPaddingLeng)
		val cdata = int32tochar_for_SEED_CBC(outbuf, nRetOutLeng[0] + nPaddingLeng[0])
		arraycopy(pbszCipherText, cdata, nRetOutLeng[0] + nPaddingLeng[0])
		return pbszCipherText
	}
	
	fun SEED_CBC_Decrypt(
		roundKey: RoundKey,
		message: ByteArray,
		message_offset: Int,
		message_length: Int
	): ByteArray? {
		val info = roundKey.newSeedInfo()
		val outbuf: IntArray
		val data: IntArray
		val cdata: ByteArray
		val outlen: Int
		val nRetOutLeng = intArrayOf(0)
		val nPaddingLeng = intArrayOf(0)
		val pbszCipherText = ByteArray(message_length)
		System.arraycopy(message, message_offset, pbszCipherText, 0, message_length)
		var nCipherTextLen = pbszCipherText.size
		if(nCipherTextLen % BLOCK_SIZE_SEED != 0) {
			return null
		}
		val newpbszCipherText = ByteArray(nCipherTextLen)
		arraycopy(newpbszCipherText, pbszCipherText, nCipherTextLen)
		nCipherTextLen = newpbszCipherText.size
		outlen = nCipherTextLen / 16 * 4
		outbuf = IntArray(outlen)
		data = chartoint32_for_SEED_CBC(newpbszCipherText, nCipherTextLen)
		SEED_CBC_Process(info, data, nCipherTextLen, outbuf, nRetOutLeng)
		return if(SEED_CBC_Close(info, outbuf, nRetOutLeng[0], nPaddingLeng) == 1) {
			cdata = int32tochar_for_SEED_CBC(outbuf, nRetOutLeng[0] - nPaddingLeng[0])
			val pbszPlainText = ByteArray(nRetOutLeng[0] - nPaddingLeng[0])
			arraycopy(pbszPlainText, cdata, nRetOutLeng[0] - nPaddingLeng[0])
			val pdmessage_length = nRetOutLeng[0] - nPaddingLeng[0]
			val result = ByteArray(pdmessage_length)
			System.arraycopy(pbszPlainText, 0, result, 0, pdmessage_length)
			result
		} else {
			null
		}
	}
	
	fun SeedRoundKey(
		enc: KISA_ENC_DEC,
		pbszUserKey: ByteArray,
		pbszIV: ByteArray
	): RoundKey {
		val ABCD = IntArray(4) // Iuput/output values at each rounds
		val T = IntArray(2) // Temporary variable
		
		val K = IntArray(32)
		val iv = IntArray(4)
		memcpy(iv, pbszIV, 16, ENDIAN)
		
		// Set up input values for Key Schedule
		ABCD[ABCD_A] = byte_to_int(pbszUserKey, 0, ENDIAN)
		ABCD[ABCD_B] = byte_to_int(pbszUserKey, 4, ENDIAN)
		ABCD[ABCD_C] = byte_to_int(pbszUserKey, 2 * 4, ENDIAN)
		ABCD[ABCD_D] = byte_to_int(pbszUserKey, 3 * 4, ENDIAN)
		
		// Reorder for big endian
		if(BIG_ENDIAN != ENDIAN) {
			ABCD[ABCD_A] = EndianChange(ABCD[ABCD_A])
			ABCD[ABCD_B] = EndianChange(ABCD[ABCD_B])
			ABCD[ABCD_C] = EndianChange(ABCD[ABCD_C])
			ABCD[ABCD_D] = EndianChange(ABCD[ABCD_D])
		}
		
		// i-th round keys( K_i,0 and K_i,1 ) are denoted as K[2*(i-1)] and K[2*i-1], respectively
		RoundKeyUpdate0(T, K, 0, ABCD, KC0) // K_1,0 and K_1,1
		RoundKeyUpdate1(T, K, 2, ABCD, KC1) // K_2,0 and K_2,1
		RoundKeyUpdate0(T, K, 4, ABCD, KC2) // K_3,0 and K_3,1
		RoundKeyUpdate1(T, K, 6, ABCD, KC3) // K_4,0 and K_4,1
		RoundKeyUpdate0(T, K, 8, ABCD, KC4) // K_5,0 and K_5,1
		RoundKeyUpdate1(T, K, 10, ABCD, KC5) // K_6,0 and K_6,1
		RoundKeyUpdate0(T, K, 12, ABCD, KC6) // K_7,0 and K_7,1
		RoundKeyUpdate1(T, K, 14, ABCD, KC7) // K_8,0 and K_8,1
		RoundKeyUpdate0(T, K, 16, ABCD, KC8) // K_9,0 and K_9,1
		RoundKeyUpdate1(T, K, 18, ABCD, KC9) // K_10,0 and K_10,1
		RoundKeyUpdate0(T, K, 20, ABCD, KC10) // K_11,0 and K_11,1
		RoundKeyUpdate1(T, K, 22, ABCD, KC11) // K_12,0 and K_12,1
		RoundKeyUpdate0(T, K, 24, ABCD, KC12) // K_13,0 and K_13,1
		RoundKeyUpdate1(T, K, 26, ABCD, KC13) // K_14,0 and K_14,1
		RoundKeyUpdate0(T, K, 28, ABCD, KC14) // K_15,0 and K_15,1
		
		T[0] = ABCD[ABCD_A] + ABCD[ABCD_C] - KC15
		T[1] = ABCD[ABCD_B] - ABCD[ABCD_D] + KC15
		K[30] = SS0[GetB0(T[0]) and 0x0ff] xor SS1[GetB1(T[0]) and 0x0ff] xor  // K_16,0
			SS2[GetB2(T[0]) and 0x0ff] xor SS3[GetB3(T[0]) and 0x0ff]
		K[31] = SS0[GetB0(T[1]) and 0x0ff] xor SS1[GetB1(T[1]) and 0x0ff] xor  // K_16,1
			SS2[GetB2(T[1]) and 0x0ff] xor SS3[GetB3(T[1]) and 0x0ff]
		
		return RoundKey(enc, iv, K)
	}
	
	
	enum class KISA_ENC_DEC { KISA_ENCRYPT, KISA_DECRYPT }
	
	
	class RoundKey internal constructor(
		val encryptMode: KISA_ENC_DEC,
		val iv: IntArray,
		val seedKey: IntArray
	) {
		internal fun newSeedInfo(): KISA_SEED_INFO = KISA_SEED_INFO(encryptMode, iv.copyOf(), seedKey)
	}
	
	class KISA_SEED_INFO(
		val encrypt: KISA_ENC_DEC, // immutable
		val ivec: IntArray, // mutable
		val seed_key: IntArray // read-only
	) {
		var buffer_length = 0 // mutable
		val cbc_buffer = IntArray(4) // mutable
		val cbc_last_block = IntArray(4) // mutable
	}
	
}
