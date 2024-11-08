package cn.luckierlove.utils

import kotlin.experimental.or
import kotlin.experimental.xor

class RC4 {
    /**
     * s-box
     */
    var box: IntArray = IntArray(256)

    /**
     * @description CR4-KSA密钥调度算法
     * 功能: 生成s-box
     * @param key 密钥
     * @return
     */
    fun KSA(key: ByteArray){
        val len = key.size
        IntRange(0, 255).forEach { box[it] = it }
        var j = 0
        IntRange(0, 255).forEach {
            j = (j + box[it] + key[it % len]) and 0xFF
            val swap = box[it]
            box[it] = box[j]
            box[j] = swap
        }
    }

    /**
     * @description RC4-PRGA伪随机数生成算法
     * 功能: 加密或解密
     *
     * @param data 加密|解密的数据
     * @param length 数据长度
     */
    fun PRGA(data: ByteArray, length: Int){
        var i:Int
        var j: Int
        IntRange(0, length - 1).forEach {
            i = (it + 1) and 0xFF
            j = (box[i] + i) and 0xFF
            data[it] = data[it] xor box[(box[i] + box[j]) and 0xff].toByte()
        }
    }
}