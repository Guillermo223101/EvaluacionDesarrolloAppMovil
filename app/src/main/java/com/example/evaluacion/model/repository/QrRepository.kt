package com.example.evaluacion.model.repository

import com.example.evaluacion.model.QrResult

class QrRepository {
    fun processQrContent(content: String): QrResult{
        //Aca se guardara y procesara el QR en una BD o API
        return QrResult(content)
    }
}