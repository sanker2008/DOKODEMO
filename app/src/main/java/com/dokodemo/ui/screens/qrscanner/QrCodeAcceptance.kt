package com.dokodemo.ui.screens.qrscanner

import com.dokodemo.core.ImportSource
import com.dokodemo.core.ImportSourceClassifier

/** Camera and gallery results use the same supported sources as clipboard import. */
internal fun acceptedQrCode(code: String?): String? = when (
    val source = code?.let(ImportSourceClassifier::classify)
) {
    is ImportSource.Node -> source.uri
    is ImportSource.Subscription -> source.url
    else -> null
}
