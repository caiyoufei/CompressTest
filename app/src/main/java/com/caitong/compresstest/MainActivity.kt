package com.caitong.compresstest

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PathUtils
import com.iceteck.silicompressorr.CompressCall
import com.iceteck.silicompressorr.SiliCompressor
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.tv1
import kotlinx.android.synthetic.main.activity_main.tv2
import java.io.File

class MainActivity : AppCompatActivity() {
  private lateinit var mContext: Context
  private var disposable: Disposable? = null
  private var time: Long = 0

  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    mContext = this
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    tv1.setOnClickListener {
      PictureSelector.create(this)
        .openGallery(PictureMimeType.ofVideo())
        .loadImageEngine(GlideEngine.createGlideEngine())
        .forResult(object : OnResultCallbackListener<LocalMedia?> {
          override fun onResult(result: List<LocalMedia?>) {
            // onResult Callback
            try {
              if (result.filterNotNull().isNotEmpty()) {
                val dir = File(PathUtils.getExternalAppMoviesPath())
                if (dir.mkdirs() || dir.isDirectory) {
                  if (disposable != null && disposable?.isDisposed == false) return
                  tv1.alpha = 0.2f
                  tv1.isClickable = false
                  tv2.text = "开始调用压缩..."
                  time = System.currentTimeMillis()
                  disposable = Observable.just(result.filterNotNull().first().path)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .flatMap {
                      Observable.just(SiliCompressor.with(mContext).compressVideo(it, dir.path))
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                      tv2.text = it
                      tv2.append("\n压缩前文件大小=${FileUtils.getSize(result.filterNotNull().first().path)}")
                      tv2.append("\n压缩后文件大小=${FileUtils.getSize(it)}")
                      tv2.append("\n压缩耗时=${System.currentTimeMillis() - time}ms")
                      tv1.alpha = 1f
                      tv1.isClickable = true
                    }, {
                      tv2.text = "压缩失败"
                      tv1.alpha = 1f
                      tv1.isClickable = true
                    })
                }
              }
            } catch (mE: Exception) {
              mE.printStackTrace()
            }
          }

          override fun onCancel() {
            // onCancel Callback
          }

        })
    }
    CompressCall.instance.progressCall = { path, progress -> runOnUiThread { tv2.text = "压缩进度=${progress}" } }
  }

  override fun finish() {
    super.finish()
    CompressCall.instance.release()
  }
}
