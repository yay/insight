package com.vitalyk.insight.fragment

import com.vitalyk.insight.helpers.bindVisibleAndManaged
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
    private val searchTextProperty = SimpleStringProperty("")
    private val showSearchProperty = SimpleBooleanProperty(false).apply {
        onChange {
            if (!it) searchTextProperty.value = ""
        }
    }


    private val showNewsProperty = SimpleBooleanProperty(true)
    private val newsItems = mutableListOf<Story>().asObservable()
    private val filteredNewsItems = FilteredList(newsItems).apply {
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

    private val alertItems = mutableListOf<StoryAlert>().asObservable()
    private val showAlertsProperty = SimpleBooleanProperty(false)
    private val showAlertDetailsProperty = SimpleBooleanProperty(false)

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

    private val showTriggersProperty = SimpleBooleanProperty(false)
    private val triggerItems = mutableListOf<TextTrigger>().asObservable()


    override val root = vbox {
        hgrow = Priority.ALWAYS
        hbox {
            padding = Insets(5.0)
            spacing = 5.0
            alignment = Pos.CENTER_LEFT
            val toggleGroup = ToggleGroup()
            radiobutton ("News", toggleGroup) { isSelected = true }
            radiobutton("Alerts", toggleGroup)
            radiobutton("Triggers", toggleGroup)
            toggleGroup.selectedToggleProperty().addListener(ChangeListener { _, _, _ ->
                toggleGroup.selectedToggle?.let {
                    val index = toggleGroup.toggles.indexOf(it)
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
            bindVisibleAndManaged(showNewsProperty)
            toolbar {
                bindVisibleAndManaged(showSearchProperty)
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
            bindVisibleAndManaged(showAlertsProperty)
            val dateFormat = SimpleDateFormat("HH:mm:ss zzz - EEE, dd MMM yy")
            dateFormat.timeZone = TimeZone.getTimeZone("America/New_York")

            showAlertDetailsProperty.onChange { refresh() }

            cellFormat { alert ->
                graphic = vbox {
                    hbox {
                        label(dateFormat.format(alert.date)) {
                            textFill = Color.GRAY
                            padding = Insets(0.0, 0.0, 0.0, 5.0)
                        }
                        bindVisibleAndManaged(showAlertDetailsProperty)
                    }
                    hbox {
                        label(dateFormat.format(alert.story.date)) {
                            textFill = Color.GRAY
                            padding = Insets(0.0, 0.0, 0.0, 5.0)
                        }
                        bindVisibleAndManaged(showAlertDetailsProperty)
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
                    alert(Alert.AlertType.CONFIRMATION, "Remove all alerts?", owner = primaryStage) { result ->
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
            vgrow = Priority.ALWAYS
            bindVisibleAndManaged(showTriggersProperty)

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
                        updateTriggers()
                    }
                }
                separator()
                item("Clear All").action {
                    alert(Alert.AlertType.CONFIRMATION, "Remove all triggers?", owner = primaryStage) { result ->
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

    private fun addTrigger() {
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
                    val index = toggleGroup.toggles.indexOf(selectedToggle)
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

    private fun updateTriggers() {
        triggerItems.setAll(ReutersWire.triggers)
    }

    private fun updateAlerts() {
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