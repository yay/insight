package com.vitalyk.insight.style

import javafx.scene.paint.Color
import javafx.scene.paint.Stop
import tornadofx.*

class Styles : Stylesheet() {

    companion object {
        val wrapper by cssclass()
    }

    init {
        // use addClass(styles.wrapper) in target code
        s(wrapper) {
            //            s(chartHorizontalGridLines) {
//                visibility = FXVisibility.HIDDEN
//            }
        }

        listCell {
            and(selected) {
                label {
                    textFill = Color.WHITE
                }
            }
        }
        splitPane {
            backgroundInsets += box(0.px)
            padding = box(0.px)
        }

        // https://gist.github.com/maxd/63691840fc372f22f470
        // https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html#cell

        // TODO: read about precedence in chapter "CSS and the JavaFX Scene Graph"

        // -fx-accent: #0096C9;
        // -fx-selection-bar: -fx-accent;
        // -fx-selection-bar-non-focused: lightgrey;

        // /* A light grey that is the base color for objects.  Instead of using
        //  * -fx-base directly, the sections in this file will typically use -fx-color.
        //  */
        // -fx-base: #ececec;

        // /* A very light grey used for the background of windows.  See also
        //  * -fx-text-background-color, which should be used as the -fx-text-fill
        //  * value for text painted on top of backgrounds colored with -fx-background.
        //  */
        // -fx-background: derive(-fx-base,26.4%);

        // /* The color to use for -fx-text-fill when text is to be painted on top of
        //  * a background filled with the -fx-background color.
        //  */
        // -fx-text-background-color: ladder(
        //     -fx-background,
        //     -fx-light-text-color 45%,
        //     -fx-dark-text-color  46%,
        //     -fx-dark-text-color  59%,
        //     -fx-mid-text-color   60%
        // );

        // On insets:
        // For example, suppose a series of three values is given for the -fx-background-color
        // property. A series of three values should also be specified for the
        // -fx-background-radius and -fx-background-insets properties. The first background
        // will be painted using the first radius value and first insets value,
        // the second background will be painted with the second radius value and second
        // insets value, and so forth.

        // Each row in the table is a table-row-cell. Inside a table-row-cell is any
        // number of table-cell.

        tableView {
            tableRowCell {
                and(filled) {
                    and(selected) {
                        val bg = Color(0.99, 0.76, 0.18, 1.00)
                        backgroundColor = multi(bg, bg.desaturate(), bg)
                        backgroundInsets = multi(box(0.px), box(1.px), box(2.px))
                        tableCell {
                            borderColor += box(
                                Color.TRANSPARENT,
                                bg.desaturate(),
                                Color.TRANSPARENT,
                                Color.TRANSPARENT
                            )
                            unsafe("-fx-text-fill", bg.ladder(Stop(0.5, Color.BLACK)))
                            // TODO: why this style overrides the value set in ChangeBlinkTableCell?
//                            textFill = bg.ladder(Stop(0.5, Color.BLACK))
                        }
                    }
                }
            }
        }
    }

}