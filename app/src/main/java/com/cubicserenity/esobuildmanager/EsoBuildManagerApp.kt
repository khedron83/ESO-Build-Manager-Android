package com.cubicserenity.esobuildmanager

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.cubicserenity.esobuildmanager.util.SkillData
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient

@HiltAndroidApp
class EsoBuildManagerApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        SkillData.init(this)
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .okHttpClient {
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        chain.proceed(
                            chain.request().newBuilder()
                                .header("Referer", "https://en.uesp.net/")
                                .build()
                        )
                    }
                    .build()
            }
            .build()
}
