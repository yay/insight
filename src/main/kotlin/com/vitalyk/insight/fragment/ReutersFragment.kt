package com.vitalyk.insight.fragment

import com.vitalyk.insight.helpers.browseTo
import com.vitalyk.insight.helpers.getResourceAudioClip
import com.vitalyk.insight.reuters.Headline
import com.vitalyk.insight.reuters.HeadlineAlert
import com.vitalyk.insight.reuters.ReutersWire
import com.vitalyk.insight.trigger.TextTrigger
import com.vitalyk.insight.ui.PlusButton
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import tornadofx.*
import java.text.SimpleDateFormat
import java.util.*

// Non-UI Reuters news fetcher singleton
// https://www.reuters.com/assets/jsonWireNews?startTime=1525694056000

class ReutersFragment : Fragment("Reuters Wire") {
    val newsList: ListView<Headline> = listview {
        vgrow = Priority.ALWAYS
        managedProperty().bind(visibleProperty())

        cellCache { headline ->
            vbox {
                label(headline.formattedDate) {
                    textFill = Color.GRAY
                }
                label(headline.headline) {
                    textFill = Color.BLACK
                    isWrapText = true
                    prefWidthProperty().bind(this@listview.widthProperty().subtract(36))

                    style {
                        font = Font.font("Tahoma", 9.0)
                        fontWeight = FontWeight.BOLD
                    }
                }
            }
        }

        onUserSelect {
            browseTo("https://www.reuters.com" + it.url)
        }

        contextmenu {
            item("Copy").action {
                selectedItem?.apply {
                    clipboard.putString(headline)
                }
            }
        }
    }

    val alertList: ListView<HeadlineAlert> = listview {
        vgrow = Priority.ALWAYS
        managedProperty().bind(visibleProperty())
        isVisible = false
        val dateFormat = SimpleDateFormat("HH:mm:ss zzz - EEE, dd MMM yy")
        dateFormat.timeZone = TimeZone.getTimeZone("America/New_York")

        cellCache { alert ->
            vbox {
                label(dateFormat.format(alert.date)) {
                    textFill = Color.GRAY
                }
                label(alert.headline.headline) {
                    textFill = Color.BLACK
                    isWrapText = true
                    prefWidthProperty().bind(this@listview.widthProperty().subtract(36))

                    style {
                        font = Font.font("Tahoma", 9.0)
                        fontWeight = FontWeight.BOLD
                    }
                }
            }
        }
    }

    val triggerList: ListView<TextTrigger> = listview {
        val listview = this
        vgrow = Priority.ALWAYS
        managedProperty().bind(visibleProperty())
        isVisible = false

        cellCache { trigger ->
            vbox {
                label(trigger.type.name) {
                    textFill = Color.GRAY
                }
                label(trigger.value) {
                    textFill = Color.BLACK
                    isWrapText = true
                    prefWidthProperty().bind(this@listview.widthProperty().subtract(36))

                    style {
                        font = Font.font("Tahoma", 9.0)
                        fontWeight = FontWeight.BOLD
                    }
                }
            }
        }

        contextmenu {
            item("Remove").action {
                selectedItem?.let {
                    ReutersWire.removeTrigger(it)
                    listview.items = ReutersWire.triggers.observable()
                }
            }
        }
    }

    override val root = vbox {
        toolbar {
            val toggleGroup = ToggleGroup()
            radiobutton ("News", toggleGroup) { isSelected = true }
            radiobutton("Alerts", toggleGroup)
            radiobutton("Triggers", toggleGroup)
            toggleGroup.selectedToggleProperty().addListener(ChangeListener { _, _, _ ->
                toggleGroup.selectedToggle?.let {
                    val index = (it as RadioButton).indexInParent
                    newsList.isVisible = index == 0
                    alertList.isVisible = index == 1
                    triggerList.isVisible = index == 2
                }
            })

            this += PlusButton("New Trigger...").apply {
                action { addTrigger() }
            }
        }
        this += newsList
        this += alertList
        this += triggerList
    }

    fun addTrigger() {
        val dialog = Dialog<TextTrigger>().apply {
            title = "New Trigger"
            headerText = "Enter trigger keywords, a regular expression or a script"
            isResizable = true

            val toggleGroup = ToggleGroup()
            val textArea = TextArea().apply {
                vgrow = Priority.ALWAYS
            }

            val add = ButtonType("Add", ButtonBar.ButtonData.OK_DONE)
            val cancel = ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)

            dialogPane.buttonTypes.addAll(add, cancel)

            dialogPane.content = VBox().apply {
                hbox {
                    spacing = 10.0
                    padding = Insets(0.0, 0.0, 10.0, 0.0)
                    radiobutton("Keywords", toggleGroup) { isSelected = true }
                    radiobutton("RegEx", toggleGroup)
                    radiobutton("Script", toggleGroup)
                }
                this += textArea
            }

            var triggerType = TextTrigger.Type.KEYWORDS
            toggleGroup.selectedToggleProperty().addListener(ChangeListener { _, _, _ ->
                toggleGroup.selectedToggle?.let {
                    val index = (it as RadioButton).indexInParent
                    when (index) {
                        0 -> triggerType = TextTrigger.Type.KEYWORDS
                        1 -> triggerType = TextTrigger.Type.REGEX
                        2 -> triggerType = TextTrigger.Type.SCRIPT
                    }
                }
            })

            setResultConverter {
                val selectedToggle = toggleGroup.selectedToggle
                if (it == add && selectedToggle != null) {
                    TextTrigger(textArea.text, triggerType)
                } else {
                    null
                }
            }
        }

        val result = dialog.showAndWait()

        if (result.isPresent) {
            println(result.get())
            ReutersWire.addTrigger(result.get())
            updateTriggers()
        }
    }

    fun updateTriggers() {
        triggerList.items = ReutersWire.triggers.observable()
    }

    fun updateAlerts() {
        alertList.items = ReutersWire.alerts.observable()
    }

    init {
        updateAlerts()
        updateTriggers()

        ReutersWire.apply {
            addHeadlineListener { headlines ->
                runLater {
                    newsList.items = headlines.observable()
                }
            }
            addAlertListener { _ ->
                runLater {
                    getResourceAudioClip("/sounds/alerts/chime.wav").play()
                    updateAlerts()
                }
            }
        }
    }
}