package com.vitalyk.insight.helpers

import com.vitalyk.insight.Insight
import javafx.scene.media.AudioClip

/**
 * E.g. getResourceAudioClip("/sounds/alerts/chime.wav").play()
 */
fun getResourceAudioClip(path: String) = AudioClip(Insight::class.java.getResource(path).toURI().toString())