package codeOrchestra.colt.core.ui.dialog

import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import javafx.stage.WindowEvent
import org.controlsfx.control.ButtonBar

/**
 * @author Dima Kruk
 */
class Dialog extends VBox {
    Stage stage

    protected HBox header
    protected VBox messageContainer
    protected Label label
    protected Label commentLabel

    ButtonBar buttonBar
    Button okButton
    Integer indexFocus = 0

    Dialog(Window owner) {
        VBox root = this
        stage = new Stage() {
            @Override
            public void showAndWait() {
                Window stageOwner = getOwner();
                if (stageOwner != null) {
                    // because Stage does not seem to centre itself over its owner, we
                    // do it here.
                    final double x = stageOwner.getX() + (stageOwner.getWidth() / 2.0) - (540 / 2.0);
                    final double y = stageOwner.getY() + (stageOwner.getHeight() / 2.0) - (root.prefHeight(-1)) / 2.0 - 50;
                    setX(x);
                    setY(y);
                }
                super.showAndWait();
            }
        }

        stage.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>() {
            @Override
            void handle(WindowEvent event) {
                buttonBar.buttons[indexFocus].requestFocus()
            }
        })

        stage.initModality(Modality.WINDOW_MODAL)
        stage.initOwner(owner)
        stage.resizable = false
        stage.title = "COLT"

        addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            void handle(KeyEvent event) {
                switch (event.code) {
                    case KeyCode.ESCAPE:
                        onCancel()
                        break
                    case KeyCode.LEFT:
                        indexFocus--
                        if (indexFocus < 0) {
                            indexFocus = buttonBar.buttons.size() - 1
                        }
                        buttonBar.buttons[indexFocus].requestFocus()
                        break
                    case [KeyCode.TAB, KeyCode.RIGHT]:
                        indexFocus++
                        if (indexFocus > buttonBar.buttons.size() - 1) {
                            indexFocus = 0
                        }
                        buttonBar.buttons[indexFocus].requestFocus()
                        break
                }
            }
        })

        initView()
    }

    protected void initView() {
        setMinHeight(120)
        setMinWidth(540)
        setPadding(new Insets(17, 30, 30, 30))

        initHeader()
        initCenter()
        initButtons()
    }

    protected void initHeader() {
        header = new HBox()
        header.minHeight = Double.NEGATIVE_INFINITY
        label = new Label(maxWidth: 540, wrapText: true, textAlignment: TextAlignment.LEFT)
        label.styleClass.add("h1")

        commentLabel = new Label(maxWidth: 540, wrapText: true, textAlignment: TextAlignment.LEFT)

        messageContainer = new VBox()
        messageContainer.children.add(label)
        HBox.setMargin(messageContainer, new Insets(17, 0, 0, 0))
        header.children.add(messageContainer)

        children.add(header)
    }

    protected void initCenter() {

    }

    protected void initButtons() {
        buttonBar = new ButtonBar()
        buttonBar.buttonUniformSize = false
        okButton = new Button("OK")
        okButton.prefWidth = 67
        okButton.onAction = {
            stage.hide()
        } as EventHandler
        okButton.defaultButton = true
        ButtonBar.setType(okButton, ButtonBar.ButtonType.OK_DONE)
        buttonBar.buttons.add(okButton)
        setMargin(buttonBar, new Insets(10, 0, 0, 0))

        children.add(buttonBar)
    }

    protected void onCancel() {

    }

    void setMessage(String message) {
        label.text = message
    }

    void setComment(String comment) {
        if (comment == null || comment.isEmpty()) {
            if (messageContainer.children.contains(commentLabel)) {
                messageContainer.children.remove(commentLabel)
            }
        } else {
            if (!messageContainer.children.contains(commentLabel)) {
                messageContainer.children.add(commentLabel)
            }
            commentLabel.text = comment
        }
    }

    void setTitle() {
    }

    void show() {
        stage.scene = new Scene(this)
        stage.showAndWait()
    }
}