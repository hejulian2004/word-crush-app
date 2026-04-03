package com.example.wordcrush.ui.compose

import android.graphics.Outline
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

@Composable
internal fun UserAvatar(
    imageUrl: String,
    fallbackLabel: String,
    size: Dp
) {
    var loadFailed by remember(imageUrl) { mutableStateOf(false) }
    val placeholderColor = MaterialTheme.colorScheme.primaryContainer.toArgb()

    Box(contentAlignment = Alignment.Center) {
        AvatarBadge(label = fallbackLabel, size = size)
        if (imageUrl.isNotBlank() && !loadFailed) {
            AndroidView(
                modifier = Modifier.size(size),
                factory = { context ->
                    ImageView(context).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        clipToOutline = true
                        outlineProvider = object : ViewOutlineProvider() {
                            override fun getOutline(view: View, outline: Outline) {
                                outline.setOval(0, 0, view.width, view.height)
                            }
                        }
                    }
                },
                update = { imageView ->
                    Glide.with(imageView)
                        .load(imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(ColorDrawable(placeholderColor))
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>,
                                isFirstResource: Boolean
                            ): Boolean {
                                loadFailed = true
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any,
                                target: Target<Drawable>?,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                loadFailed = false
                                return false
                            }
                        })
                        .into(imageView)
                }
            )
        }
    }
}
