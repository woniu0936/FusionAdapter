package com.fusion.example.utils

import android.widget.ImageView
import coil.load
import coil.transform.CircleCropTransformation
import com.fusion.example.R

fun ImageView.loadUrl(url: String, isCircle: Boolean = false) {
    this.load(url) {
        crossfade(true)
        placeholder(R.drawable.bg_placeholder)
        error(R.drawable.bg_placeholder)
        if (isCircle) transformations(CircleCropTransformation())
    }
}