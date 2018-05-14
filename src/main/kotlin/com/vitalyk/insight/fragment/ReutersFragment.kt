package com.vitalyk.insight.fragment

import com.vitalyk.insight.helpers.browseTo
import com.vitalyk.insight.helpers.getResourceAudioClip
import com.vitalyk.insight.helpers.newYorkTimeZone
import com.vitalyk.insight.reuters.ReutersWire
import com.vitalyk.insight.reuters.Story
import com.vitalyk.insight.reuters.StoryAlert
import com.vitalyk.insight.trigger.AllKeywordsTrigger
import com.vitalyk.insight.trigger.AnyKeywordTrigger
import com.vitalyk.insight.trigger.RegExTrigger
import com.vitalyk.insight.trigger.TextTrigger
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
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
    val newsList: ListView<Story> = listview {
        vgrow = Priority.ALWAYS
        managedProperty().bind(visibleProperty())

        val dateFormat = SimpleDateFormat("HH:mm:ss").apply {
            timeZone = newYorkTimeZone
        }

        cellCache { story ->
            vbox {
                hbox {
                    label(story.formattedDate) {
                        textFill = Color.GRAY
                    }
                    pane {
                        hgrow = Priority.ALWAYS
                    }
                    label(dateFormat.format(story.date)) {
                        textFill = Color.GRAY
                    }
                }
                label(story.headline) {
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
            browseTo(ReutersWire.baseUrl + it.url)
        }

        contextmenu {
            item("Copy").action {
                selectedItem?.apply {
                    clipboard.putString(headline)
                }
            }
        }
    }

    val alertList: ListView<StoryAlert> = listview {
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
                label(alert.story.headline) {
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
            browseTo(ReutersWire.baseUrl + it.story.url)
        }

        contextmenu {
            item("Clear All").action {
                alert(Alert.AlertType.CONFIRMATION, "Remove all alerts?") { result ->
                    if (result == ButtonType.OK) {
                        ReutersWire.clearAlerts()
                        updateAlerts()
                    }
                }
            }
        }
    }

    private val TextTrigger.displayName: String
        get() = when (this) {
            is AllKeywordsTrigger -> "All Keywords"
            is AnyKeywordTrigger -> "Any Keyword"
            is RegExTrigger -> "Regular Expression"
            else -> "Unrecognized Trigger"
        }

    private val TextTrigger.displayValue: String
        get() = when (this) {
            is AllKeywordsTrigger -> keywords.joinToString(", ")
            is AnyKeywordTrigger -> keywords.joinToString(", ")
            is RegExTrigger -> regex.pattern
            else -> "Unrecognized Value"
        }

    val triggerList: ListView<TextTrigger> = listview {
        val listview = this
        vgrow = Priority.ALWAYS
        managedProperty().bind(visibleProperty())
        isVisible = false

        val recurringIcon = MaterialDesignIconView(MaterialDesignIcon.REFRESH).apply {
            glyphSize = 16.0
        }

        cellCache { trigger ->
            vbox {
                hbox {
                    alignment = Pos.CENTER_LEFT
                    spacing = 5.0
                    label(trigger.displayName) {
                        textFill = Color.GRAY
                    }
                    if (trigger.recurring) {
                        this += recurringIcon
                    }
                }
                label(trigger.displayValue) {
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
            item("Clear All").action {
                alert(Alert.AlertType.CONFIRMATION, "Remove all triggers?") { result ->
                    if (result == ButtonType.OK) {
                        ReutersWire.clearTriggers()
                        updateTriggers()
                    }
                }
            }
        }

        onUserSelect {
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

            this += button("+") {
                action { addTrigger() }
            }
        }

        // Only one list is visible at a time.
        this += newsList
        this += alertList
        this += triggerList
    }

    fun addTrigger() {
        val dialog = Dialog<TextTrigger>().apply {
            initOwner(primaryStage)
            title = "New Trigger"
            headerText = "Enter trigger keywords or a regular expression"

            val add = ButtonType("Add", ButtonBar.ButtonData.OK_DONE)
            val cancel = ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)

            dialogPane.buttonTypes.addAll(add, cancel)

            val recurringProperty = SimpleBooleanProperty(false)
            val toggleGroup = ToggleGroup()
            val textField = TextField()

            dialogPane.content = VBox().apply {
                hbox {
                    spacing = 10.0
                    padding = Insets(10.0, 0.0, 20.0, 0.0)
                    radiobutton("All Keywords", toggleGroup) {
                        tooltip("Single, comma separated keywords.\n" +
                            "All keywords must be found.")
                        isSelected = true
                    }
                    radiobutton("Any Keyword", toggleGroup) {
                        tooltip("Single, comma separated keywords.\n" +
                            "At least one keyword must be found.")
                    }
                    radiobutton("RegEx", toggleGroup) {
                        tooltip("A regular expression matching something within the text.\n" +
                            "Use this when keywords are not enough.\n" +
                            "For example, to match phrases.")
                    }
                    spacer {}
                    checkbox("Recurring", recurringProperty)
                }
                this += textField
            }

            setResultConverter {
                val selectedToggle = toggleGroup.selectedToggle
                if (it == add && selectedToggle != null) {
                    val text = textField.text
                    val isRecurring = recurringProperty.value
                    val index = (selectedToggle as RadioButton).indexInParent
                    when (index) {
                        0 -> AllKeywordsTrigger.of(text, isRecurring)
                        1 -> AnyKeywordTrigger.of(text, isRecurring)
                        else -> RegExTrigger.of(text, isRecurring)
                    }
                } else {
                    null
                }
            }
        }

        val result = dialog.showAndWait()

        if (result.isPresent) {
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
            addStoryListener { stories ->
                runLater {
                    newsList.items = stories.observable()
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