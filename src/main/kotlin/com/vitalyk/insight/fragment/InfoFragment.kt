package com.vitalyk.insight.fragment

import com.vitalyk.insight.helpers.dropSingle
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.*

class InfoFragment : Fragment() {
    private val fragment = this
    val textarea = textarea {
        isWrapText = true
        vgrow = Priority.ALWAYS
    }
    override val root = vbox {
        padding = Insets(10.0)
        this += textarea
        hbox {
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
}
