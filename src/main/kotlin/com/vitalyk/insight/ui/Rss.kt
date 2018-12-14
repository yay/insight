package com.vitalyk.insight.ui

import com.vitalyk.insight.helpers.getResourceAudioClip
import com.vitalyk.insight.main.httpGet
import javafx.beans.property.SimpleIntegerProperty
import javafx.event.EventTarget
import javafx.geometry.Side
import javafx.scene.control.ContextMenu
import javafx.scene.paint.Color
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import tornadofx.*

private fun EventTarget.rss() { // this is just placeholder to prevent compile errors
    button("DePorre") {
        val menu = ContextMenu()
        menu.hide()

        val newStoryChime = getResourceAudioClip("/sounds/alerts/buzz.wav")
        val newStoryCount = SimpleIntegerProperty(0).apply {
            onChange {
                if (it > 0) newStoryChime.play()
            }
        }
        label(newStoryCount) {
            style {
                borderWidth = multi(box(2.px))
                borderRadius = multi(box(20.px))
                borderColor = multi(box(Color.BLACK))
                padding = box(0.px, 4.px, 0.px, 4.px)
                fontFamily = "Menlo"
                fontSize = .9.em
            }
        }

        val rssFeed = "https://realmoney.thestreet.com/node/3203/feed"

        var oldDates = listOf<String>()
        GlobalScope.launch {
            while (isActive) {
                httpGet(rssFeed)?.let { xmlString ->
                    val items = Jsoup.parse(xmlString, "", Parser.xmlParser()).select("item")
                    val dates = items.map { it.select("pubDate").text() }
                    val count = if (oldDates.isNotEmpty()) dates.minus(oldDates).size else 0
                    runLater {
                        newStoryCount.set(newStoryCount.get() + count)
                    }
                    oldDates = dates
                }
                delay(5 * 60 * 1000)
            }
        }

        data class Story(
            val date: String,
            val title: String,
            val link: String
        )

        action {
            newStoryCount.set(0)
            runAsyncWithProgress {
                httpGet(rssFeed)?.let { xml ->
                    val items = Jsoup.parse(xml, "", Parser.xmlParser()).select("item")
                    items.map {
                        val date = it.select("pubDate").text().substringBeforeLast("0")
                        Story(date, it.select("title").text(), it.select("link").text())
                    }
                } ?: emptyList()
            } ui { stories ->
                menu.items.clear()
                stories.forEach { story ->
                    menu.item(story.title).action {
                        runAsyncWithProgress {
                            val html = httpGet(story.link)
                            val content = Jsoup.parse(html).select(".content")
                            content.first()
                                ?.wholeText()
                                ?.substringBefore("Get an email alert")
                                ?.trim()
                                ?.let { text ->
                                    runLater {
//                                        find(InfoFragment::class.java).apply {
//                                            setInfo(story.title, story.date + "\n\n" + text)
//                                            setSize(600, 600)
//                                            setUrl(story.link)
//                                            openWindow()
//                                        }
                                    }
                                }
                            Unit
                        }
                    }
                }
                if (menu.items.isNotEmpty()) {
                    menu.show(this, Side.BOTTOM, 0.0, 0.0)
                }
            }
        }
    }
}