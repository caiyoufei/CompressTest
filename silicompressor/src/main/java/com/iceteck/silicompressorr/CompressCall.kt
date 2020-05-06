package com.iceteck.silicompressorr

import android.util.Log

/**
 * Description:
 * @author: caiyoufei
 * @date: 2020/5/5 16:56
 */
open class CompressCall private constructor() {
  private object SingletonHolder {
    val holder = CompressCall()
  }

  companion object {
    val instance = SingletonHolder.holder
  }

  //外部回调
  var progressCall: ((filPath: String, progress: Float) -> Unit)? = null

  //防止回调太频繁
  private var lastProgress = 0f

  //更新压缩进度
  fun updateCompressProgress(originFilePath: String, progress: Float) {
    if (progress - lastProgress > 1) {
      lastProgress = ((progress * 100).toInt()) / 100f
      // Log.e("CASE", "压缩进度=$lastProgress")
      progressCall?.invoke(originFilePath, Math.min(lastProgress, 99.99f))
    }
  }

  //释放
  fun release() {
    progressCall = null
    lastProgress = 0f
  }
}