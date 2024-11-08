package cn.luckierlove.service

import cn.luckierlove.entity.NCM
import cn.luckierlove.utils.AES
import cn.luckierlove.utils.ByteUtils
import cn.luckierlove.utils.RC4
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.flac.metadatablock.MetadataBlockDataPicture
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset
import javax.imageio.ImageIO
import kotlin.experimental.xor
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * 处理 转换NCM2MP3 相关操作
 */
class Converter {

    /**
     * @description NCM转换成MP3
     * 功能: 将NCM音乐转换成MP3音乐
     *
     * @param ncmFilePath NCM文件路径
     * @param outFilePath MP3文件路径
     * @return 是否转换成功
     */
    fun ncm2mp3(ncmFilePath: String, outFilePath: String): Boolean {
        var ncm: NCM = NCM(null, null, null, null)
        ncm.ncmFilePath = ncmFilePath

        val inputStream: FileInputStream = FileInputStream(ncm.ncmFilePath)

        magicHeader(inputStream)

        val key: ByteArray = rc4Key(inputStream)

        // TODO
    }

    /**
     * @description NCM格式头读取
     * 功能: MagicHeader读取
     *
     * @param inputStream ncm文件输入流
     */
    private fun magicHeader(inputStream: FileInputStream) {
        var bytes: ByteArray = ByteArray(10)
        inputStream.read(bytes, 0, 10)
    }

    /**
     * @description 获取RC4密钥
     * 功能: 将用AES128加密的RC4密钥解密
     *
     * @param inputStream ncm文件输入流
     * @return RC4密钥
     */
    private fun rc4Key(inputStream: FileInputStream): ByteArray {
        var bytes: ByteArray = ByteArray(4)
        inputStream.read(bytes, 0, 4) // 读取 KEY Length
        val len: Int = ByteUtils.getLength(bytes)

        bytes = ByteArray(len)
        inputStream.read(bytes, 0, len) // 读取被加密后的KEY
        // 1. 对0x64异或
        IntRange(0, len - 1).forEach {
            bytes[it] = bytes[it] xor 0x64
        }

        // 2.AES解密
        bytes = AES.decrypt(bytes, AES.CORE_KEY, AES.TRANSFORMATION, AES.ALGORITHM)

        // 3.去除 `neteasecloudmusic` 这17个字符
        val key: ByteArray = ByteArray(bytes.size - 17)
        System.arraycopy(bytes, 17, key, 0, key.size)

        return key
    }

    /**
     * @description 获取Meta头部信息
     * 功能: 获取音乐的Meta头部信息
     *
     * @param inputStream ncm文件输入流
     * @return JSON格式头部信息
     */
    @OptIn(ExperimentalEncodingApi::class)
    private fun metaData(inputStream: FileInputStream): String {
        var bytes: ByteArray = ByteArray(4)
        inputStream.read(bytes, 0, 4)  // 获取Meta Length

        val len: Int = ByteUtils.getLength(bytes)
        bytes = ByteArray(len)
        inputStream.read(bytes, 0, len) // 读取Meta Data

        // 跳过CRC 和 Gap
        inputStream.skip(9)

        // 对Meta Data解密

        // 对0x63异或
        IntRange(0, len - 1).forEach {
            bytes[it] = bytes[it] xor 0x63
        }

        // 去除 `163 key(Don't modify):` 22个字符
        var temp: ByteArray = ByteArray(bytes.size - 22)
        System.arraycopy(bytes, 22, temp, 0, temp.size)

        // 解Base64
        temp = Base64.decode(temp)

        // AES 解密
        temp = AES.decrypt(temp, AES.META_KEY, AES.TRANSFORMATION, AES.ALGORITHM)

        // 去除 `music:` 6个字符 并转换成字符串
        val json: String = String(temp, 6, temp.size - 6, Charset.forName("UTF-8"))

        return json
    }

    /**
     * @description 专辑图片
     * 功能: 获取专辑图片数据
     *
     * @param inputStream ncm文件输入流
     * @return 专辑图片数据
     */
    private fun albumImage(inputStream: FileInputStream): ByteArray {
        var bytes: ByteArray = ByteArray(4)
        inputStream.read(bytes, 0, 4) // 读取Image Size
        val len: Int = ByteUtils.getLength(bytes)

        bytes = ByteArray(len)
        inputStream.read(bytes, 0, len) // 读取Image Data

        return bytes
    }

    /**
     * @description 音乐数据
     * 功能: 获取音乐数据
     *
     * @param ncm文件输入流
     * @param outputStream 存音乐的文件输出流
     * @param rc4Key RC4 密钥
     * @return
     */
    private fun musicData(inputStream: FileInputStream, outputStream: FileOutputStream, rc4Key: ByteArray) {
        val rc4: RC4 = RC4()
        rc4.KSA(rc4Key)
        var buffer: ByteArray = ByteArray(0x8000)
        var len: Int
        while (inputStream.read(buffer).also { len = it } > 0) {
            rc4.PRGA(buffer, len)
            outputStream.write(buffer, 0, len)
        }
        inputStream.close()
        outputStream.close()
    }

    /**
     * @description 将NCM中各个信息整合到一起
     * @return
     */
    private fun combineFile(ncm: NCM) {
        var audioFile = AudioFileIO.read(File(ncm.outputFilePath))
        var tag = audioFile.tag

        val meta = ncm.meta
        if (meta != null) {
            tag.setField(FieldKey.ALBUM, meta.album)
            tag.setField(FieldKey.TITLE, meta.musicName)
            tag.setField(FieldKey.ARTIST, *meta.artist[0])
            val image = ImageIO.read(ByteArrayInputStream(ncm.image))
            if(image != null) {
                val coverArt = MetadataBlockDataPicture(
                    ncm.image,
                    0,
                    ByteUtils.albumImageMimeType(ncm.image!!),
                    "",
                    image.width,
                    image.height,
                    if (image.colorModel.hasAlpha()) 32 else 24,
                    0
                )

                val artwork =
                    ArtworkFactory.createArtworkFromMetadataBlockDataPicture(coverArt)

                tag.setField(tag.createField(artwork))
            }

            AudioFileIO.write(audioFile)
        }
    }
}