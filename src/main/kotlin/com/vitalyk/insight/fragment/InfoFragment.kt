package com.vitalyk.insight.fragment

import com.vitalyk.insight.helpers.browseTo
import com.vitalyk.insight.helpers.dropSingle
import com.vitalyk.insight.helpers.getResourceAudioClip
import com.vitalyk.insight.main.sendEmail
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import tornadofx.*

class InfoFragment : Fragment() {
    private val fragment = this
    private val urlProperty = SimpleStringProperty()
    private val url = label(urlProperty) {
        padding = Insets(0.0, 0.0, 5.0, 0.0)
        isVisible = false
        isManaged = false
        setOnMouseClicked {
            browseTo(urlProperty.value)
        }
        onHover {
            isUnderline = it
        }
    }
    val textarea = textarea {
        isWrapText = true
        vgrow = Priority.ALWAYS
    }
    override val root = vbox {
        padding = Insets(10.0)
        this += url
        this += textarea
        hbox {
            button("Email").action {
                GlobalScope.async {
                    sendEmail("rarename@icloud.com", fragment.title, textarea.text)
                    getResourceAudioClip("/sounds/alerts/email_sent.wav").play()
                }
            }
            spacer {}
            button("OK") {
                action {
                    fragment.close()
                }
                minWidth = 60.0
            }
            padding = Insets(10.0, 0.0, 0.0, 0.0)
            alignment = Pos.CENTER_RIGHT
        }
    }

    fun setSize(width: Int, height: Int) {
        root.setMinSize(width.toDouble(), height.toDouble())
    }

    fun setInfo(title: String, text: String, trim: Boolean = false) {
        fragment.title = title
        textarea.text = if (trim)
            dropSingle(text.trimIndent(), '\n', " ")
        else
            text
    }

    fun setUrl(value: String) {
        urlProperty.set(value)
        url.isVisible = true
        url.isManaged = true
    }
}
