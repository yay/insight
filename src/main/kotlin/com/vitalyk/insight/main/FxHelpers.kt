package com.vitalyk.insight.main

import com.vitalyk.insight.Insight
import javafx.scene.media.AudioClip

fun audioClipOf(path: String) = AudioClip(Insight.javaClass.getResource(path).toURI().toString())

// E.g. playSound("/sounds/alerts/chime.wav")
fun playSound(path: String) {
    audioClipOf(path).play()
}