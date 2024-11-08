package cn.luckierlove.entity

/**
 * Meat Entity
 */
data class Meta(
    /**
     * 音乐名
     */
    var musicName: String?,

    /**
     * 艺术家
     */
    var artist: Array<Array<String>>,

    /**
     * 专辑
     */
    var album: String?,

    /**
     * 格式
     */
    var format: String?
)
