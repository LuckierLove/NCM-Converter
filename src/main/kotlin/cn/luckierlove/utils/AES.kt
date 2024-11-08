package cn.luckierlove.utils

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class AES {
    companion object{
        /**
         * 加密RC4密钥的密钥
         */
        val CORE_KEY: ByteArray = byteArrayOf(0x68, 0x7A, 0x48, 0x52, 0x41, 0x6D, 0x73, 0x6F, 0x35, 0x6B, 0x49, 0x6E, 0x62, 0x61, 0x78, 0x57)

        /**
         * 加密META密钥的密钥
         */
        val META_KEY: ByteArray = byteArrayOf(0x23, 0x31, 0x34, 0x6C, 0x6A, 0x6B, 0x5F, 0x21, 0x5C, 0x5D, 0x26, 0x30, 0x55, 0x3C, 0x27, 0x28)

        /**
         * 转换(加密算法/加密模式/填充模式)
         */
        const val TRANSFORMATION: String = "AES/ECB/PKCS5Padding"

        /**
         * 加密算法
         */
        const val ALGORITHM: String = "AES"

        /**
         * @description AES/ECB/PKCS5Padding解密
         * @param str 密文
         * @param key 密钥
         * @param transformation 解密算法(加密算法/加密模式/填充模式)
         * @param algorithm 加密类型
         * @return 明文
         */
        fun decrypt(str: ByteArray, key: ByteArray, transformation: String, algorithm: String): ByteArray{
            val cipher = Cipher.getInstance(transformation)
            val secretKeySpec = SecretKeySpec(key, algorithm)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
            return cipher.doFinal(str)
        }
    }
}