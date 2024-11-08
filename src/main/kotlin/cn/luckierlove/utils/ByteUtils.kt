package cn.luckierlove.utils

class ByteUtils {
    companion object {
        /**
         * @description 图片专辑MIME类型
         * 功能: 获取图片的MIME类型
         *
         * @param albumImage 图片数据
         * @return 图片类型
         */
        fun albumImageMimeType(albumImage: ByteArray): String {
            val mPNG: ByteArray = byteArrayOf(0x89 as Byte, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) // PNG 文件 头标识
            if(albumImage.size > 8){
                IntRange(0, 7).forEach {
                    if(albumImage[it] != mPNG[it]) return "image/jpg"
                }
            }

            return "image/png"
        }


        /**
         * @description 获取长度
         * 功能: 将用小端字节排序, 无符号整数4字节的长度信息转换为十进制数
         *
         * @param bytes 长度信息的字节数组
         * @return 长度
         */
        fun getLength(bytes: ByteArray): Int {
            var len:Int = 0
            len = len or (bytes[0].toInt() and 0xff)
            len = len or ((bytes[1].toInt() and 0xff) shl 8)
            len = len or ((bytes[2].toInt() and 0xff) shl 16)
            len = len or ((bytes[3].toInt() and 0xff) shl 24)
            return len
        }
    }
}