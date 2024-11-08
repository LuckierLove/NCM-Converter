package cn.luckierlove.entity

/**
 * NCM Entity
 */
data class NCM(
    /**
     * NCM文件路径
     */
    var ncmFilePath: String?,

    /**
     * 转换后输出文件路径
     */
    var outputFilePath: String?,

    /**
     * 封面
     */
    var image: ByteArray?,

    /**
     * 头数据
     */
    var meta: Meta?
)