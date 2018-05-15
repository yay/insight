package com.vitalyk.insight.fragment

import com.vitalyk.insight.helpers.bindVisible
import com.vitalyk.insight.helpers.browseTo
import com.vitalyk.insight.helpers.getResourceAudioClip
import com.vitalyk.insight.helpers.newYorkTimeZone
import com.vitalyk.insight.reuters.ReutersWire
import com.vitalyk.insight.reuters.Story
import com.vitalyk.insight.reuters.StoryAlert
import com.vitalyk.insight.trigger.AllKeywordsTrigger
import com.vitalyk.insight.trigger.AnyKeywordTrigger
import com.vitalyk.insight.trigger.RegexTrigger
import com.vitalyk.insight.trigger.TextTrigger
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.transformation.FilteredList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.input.KeyCode
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
    val searchTextProperty = SimpleStringProperty("")
    val showSearchProperty = SimpleBooleanProperty(false).apply {
        onChange {
            if (!it) searchTextProperty.value = ""
        }
    }


    val showNewsProperty = SimpleBooleanProperty(true)
    val newsItems = mutableListOf<Story>().observable()
    val filteredNewsItems = FilteredList(newsItems).apply {
        searchTextProperty.onChange { text ->
            if (text?.isNotBlank() == true) {
                setPredicate {
                    it.headline.contains(text, ignoreCase = true)
                }
            } else {
                setPredicate(null)
            }
        }
        val text = searchTextProperty.value
        if (text?.isNotBlank() == true) setPredicate {
            it.headline.contains(text, ignoreCase = true)
        }
    }

    val alertItems = mutableListOf<StoryAlert>().observable()
    val showAlertsProperty = SimpleBooleanProperty(false)
    val showAlertDetailsProperty = SimpleBooleanProperty(false)

    private val TextTrigger.displayName: String
        get() = when (this) {
            is AllKeywordsTrigger -> "All Keywords"
            is AnyKeywordTrigger -> "Any Keyword"
            is RegexTrigger -> "Regular Expression"
            else -> "Unrecognized Trigger"
        }

    private val TextTrigger.displayValue: String
        get() = when (this) {
            is AllKeywordsTrigger -> keywords.joinToString(", ")
            is AnyKeywordTrigger -> keywords.joinToString(", ")
            is RegexTrigger -> regex.pattern
            else -> "Unrecognized Value"
        }

    val showTriggersProperty = SimpleBooleanProperty(false)
    val triggerItems = mutableListOf<TextTrigger>().observable()


    override val root = vbox {
        toolbar {
            val toggleGroup = ToggleGroup()
            radiobutton ("News", toggleGroup) { isSelected = true }
            radiobutton("Alerts", toggleGroup)
            radiobutton("Triggers", toggleGroup)
            toggleGroup.selectedToggleProperty().addListener(ChangeListener { _, _, _ ->
                toggleGroup.selectedToggle?.let {
                    val index = (it as RadioButton).indexInParent
                    showNewsProperty.value = index == 0
                    showAlertsProperty.value = index == 1
                    showTriggersProperty.value = index == 2
                }
            })

            this += button("+") {
                action { addTrigger() }
            }
        }

        // Only one list is visible at a time.
        vbox {
            vgrow = Priority.ALWAYS
            bindVisible(showNewsProperty)
            toolbar {
                bindVisible(showSearchProperty)
                label("Search:")
                textfield(searchTextProperty) {
                    hgrow = Priority.ALWAYS
                    setOnKeyPressed {
                        if (it.code == KeyCode.ESCAPE)
                            showSearchProperty.value = false
                    }
                }
            }
            listview(filteredNewsItems) {
                vgrow = Priority.ALWAYS

                val dateFormat = SimpleDateFormat("HH:mm:ss").apply {
                    timeZone = newYorkTimeZone
                }

                cellFormat { story ->
                    graphic = vbox {
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

                setOnKeyPressed {
                    when {
                        it.isMetaDown && it.code == KeyCode.F -> showSearchProperty.value = true
                        it.code == KeyCode.ESCAPE -> showSearchProperty.value = false
                    }
                }
            }
        }
        listview(alertItems) {
            vgrow = Priority.ALWAYS
            bindVisible(showAlertsProperty)
            val dateFormat = SimpleDateFormat("HH:mm:ss zzz - EEE, dd MMM yy")
            dateFormat.timeZone = TimeZone.getTimeZone("America/New_York")

            showAlertDetailsProperty.onChange { refresh() }

            cellFormat { alert ->
                graphic = vbox {
                    label("Triggered: ${dateFormat.format(alert.date)}") {
                        textFill = Color.GRAY
                        bindVisible(showAlertDetailsProperty)
                    }
                    label("Appeared: ${dateFormat.format(alert.story.date)}") {
                        textFill = Color.GRAY
                        bindVisible(showAlertDetailsProperty)
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
                customitem {
                    content = CheckBox("Details").apply {
                        bind(showAlertDetailsProperty)
                    }
                }
            }
        }
        listview(triggerItems) {
            val listview = this
            vgrow = Priority.ALWAYS
            bindVisible(showTriggersProperty)

            cellFormat { trigger ->
                graphic = vbox {
                    hbox {
                        alignment = Pos.CENTER_LEFT
                        spacing = 5.0
                        label(trigger.displayName) {
                            textFill = Color.GRAY
                        }
                    }
                    label(trigger.displayValue) {
                        textFill = Color.BLACK
                        isWrapText = true
                        prefWidth = listview.width

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
                separator()
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

    }

    fun addTrigger() {
        val dialog = Dialog<TextTrigger>().apply {
            initOwner(primaryStage)
            title = "New Trigger"
            headerText = "Enter trigger keywords or a regular expression"

            val add = ButtonType("Add", ButtonBar.ButtonData.OK_DONE)
            val cancel = ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)

            dialogPane.buttonTypes.addAll(add, cancel)

            val toggleGroup = ToggleGroup()
            val textField = TextField()

            dialogPane.content = VBox().apply {
                hbox {
                    spacing = 10.0
                    padding = Insets(10.0, 0.0, 20.0, 0.0)
                    radiobutton("All Keywords", toggleGroup) {
                        tooltip("Single keywords (no phrases).\n" +
                            "All keywords must be found.")
                        isSelected = true
                    }
                    radiobutton("Any Keyword", toggleGroup) {
                        tooltip("Single keywords (no phrases).\n" +
                            "At least one keyword must be found.")
                    }
                    radiobutton("Regular Expression", toggleGroup) {
                        tooltip("A regular expression matching something within the text.\n" +
                            "Use this when keywords are not enough.\n" +
                            "For example, to match phrases.")
                    }
                }
                this += textField.apply {
                    runLater { requestFocus() }
                }
            }

            setResultConverter {
                val selectedToggle = toggleGroup.selectedToggle
                if (it == add && selectedToggle != null) {
                    val text = textField.text
                    val index = (selectedToggle as RadioButton).indexInParent
                    when (index) {
                        0 -> AllKeywordsTrigger.of(text)
                        1 -> AnyKeywordTrigger.of(text)
                        else -> RegexTrigger.of(text)
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
        triggerItems.setAll(ReutersWire.triggers)
    }

    fun updateAlerts() {
        alertItems.setAll(ReutersWire.alerts)
    }

    init {
        updateAlerts()
        updateTriggers()

        ReutersWire.apply {
            addStoryListener { stories ->
                runLater {
                    newsItems.setAll(stories)
                }
            }
            addAlertListener { alerts ->
                runLater {
                    getResourceAudioClip("/sounds/alerts/chime.wav").play()
                    updateAlerts()
                }
            }
        }
    }
}