package editor;
import java.io.*;
import java.util.*;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;
import java.util.Stack;

/**
 * Created by jessicaji on 2/28/16.
 */
public class Editor extends Application {
    public Group textRoot;
    public Group displayRoot;
    public ScrollBar scrollBar;
    public static int WINDOW_WIDTH = 500;
    public static int WINDOW_HEIGHT = 500;
    public static TextLinkedList output = new TextLinkedList();
    public static ArrayList lines = new ArrayList();
    public final Rectangle cursor = new Rectangle(1, 15); //font size 12, cursor height 14..DEFAULT HEIGHT
    public static final int STARTING_FONT_SIZE = 12;
    public static final int STARTING_TEXT_POSITION_X = 5;
    public static final int STARTING_TEXT_POSITION_Y = 0;
    public int MAX_LINE_WIDTH = WINDOW_WIDTH - 10;
    public Text displayText = new Text(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y, "");
    public int fontSize = STARTING_FONT_SIZE;
    public String fontName = "Verdana";
    public String charTyped = new String();
    public int cursorPosX = STARTING_TEXT_POSITION_X;
    public int cursorPosY = STARTING_TEXT_POSITION_Y;
    public int arrayIndex = 0;
    public int currTypedWidth = 0;
    public int lineHeight = 0;
    public int countedWidth = 0;
    public Scene scene;
    public static String filename;
    public Stack<Action> undoStack = new Stack<>();
    public Stack<Action> redoStack = new Stack<>();
    public myNode leftOff;
    public int undoCount = 0;
    public int redoCount = 0;

    public void undo() {
        Action popped = undoStack.pop();
        undoCount--;
        /* execute undo */
        output.setCurrNode(leftOff);
        if (popped.deleting) {
            output.add(popped.item);
            textRoot.getChildren().add(popped.item);
            textRoot.getChildren().add(popped.item);
            updateCursor();
            updateScrollBarAdd();
        } else {
            output.delete();
            textRoot.getChildren().remove(popped.item);
            updateCursor();
            updateScrollBarDelete();
        }
        leftOff = output.getCurrNode();
        redoStack.push(popped);
        redoCount++;
        render();
    }
    /* it breaks */
    public void redo() {
        Action popped = redoStack.pop();
        redoCount--;
        output.setCurrNode(leftOff);
        if (popped.deleting) {
            output.delete();
            textRoot.getChildren().remove(popped.item);
            render();
            updateCursor();
            updateScrollBarDelete();
        } else {
            output.add(popped.item);
            textRoot.getChildren().add(popped.item);
            render();
            updateCursor();

            updateScrollBarAdd();
        }
        leftOff = output.getCurrNode();
        undoStack.push(popped);
        undoCount++;
    }
    //change scroll bar, undo redo should always be in view. moving cursor and then undo doesn't work because i changed current.

    public void render() {
            /* reset display window */
        currTypedWidth = 0;
        arrayIndex = 0;
        cursorPosX = 5;
        cursorPosY = 0;
        countedWidth = 0;
        lineHeight = 0;
        lines = new ArrayList();
        myNode lastSpace = null;
        Text autoHeight = new Text();
        autoHeight.setFont(Font.font(fontName, fontSize));
        lineHeight = (int) Math.round(autoHeight.getLayoutBounds().getHeight());
            /* create iterator */
        Iterator finder = output.iterator();
        while (finder.hasNext()) {
            myNode typedChar = (myNode) finder.next();
            Text t = typedChar.item;
            //System.out.println(t.getText().equals("hello"));
            if (typedChar.prev == output.getHead() && !t.getText().equals("\n")) {
                lineHeight = (int) Math.round(t.getLayoutBounds().getHeight());
            }
            t.setFont(Font.font(fontName, fontSize));
            int tWidth = (int) Math.round(t.getLayoutBounds().getWidth());
            if (t.getText().equals(" "))
                lastSpace = typedChar;
                /* is new line? */
            if (t.getText().equals("\n") || t.getText().equals("\r\n")) {
                    /* add array pointer, increment to new line */
                lines.add(arrayIndex, typedChar.prev);
                arrayIndex++;
                cursorPosX = 5;
                cursorPosY += lineHeight;
                t.setX(5);
                t.setY(cursorPosY);
                currTypedWidth = 0;
            } else {
                if (lastSpace == null) {
                    if (tWidth + currTypedWidth > MAX_LINE_WIDTH) {
                        cursorPosX = 5;
                        cursorPosY += lineHeight;
                        lines.add(arrayIndex, typedChar.prev);
                        arrayIndex++;
                        currTypedWidth = 0;
                    }
                    t.setX(cursorPosX);
                    t.setY(cursorPosY);
                    cursorPosX += tWidth;
                    currTypedWidth += tWidth;
                } else {
                        /* is space? */
                    if (typedChar == lastSpace && (tWidth + currTypedWidth > MAX_LINE_WIDTH)) {
                        cursorPosX = 5;
                        cursorPosY += lineHeight;
                        currTypedWidth = 0;
                        lines.add(arrayIndex, typedChar.prev);
                        arrayIndex++;
                        /* is not space */
                    } else {
                        myNode runner = typedChar; //null cases
                        countedWidth = 0;
                            /* count width of the word */
                        while (runner != lastSpace) {
                            countedWidth += (int) Math.round(runner.item.getLayoutBounds().getWidth());
                            runner = runner.prev;
                        }
                            /* if too long, move whole word to new line */
                        if (tWidth + currTypedWidth > (MAX_LINE_WIDTH)) {
                                /* if word length exceeds entire line */
                            if (countedWidth > MAX_LINE_WIDTH) {
                                cursorPosX = 5;
                                cursorPosY += lineHeight;
                                lines.add(arrayIndex, typedChar.prev);
                                arrayIndex++;
                                currTypedWidth = tWidth;
                            } else {
                                myNode afterSpace = lastSpace.next;
                                currTypedWidth = 0;
                                cursorPosY += lineHeight;
                                cursorPosX = 5;
                                while (afterSpace != typedChar) {
                                    currTypedWidth += afterSpace.item.getLayoutBounds().getWidth();
                                    afterSpace.item.setY(cursorPosY);
                                    afterSpace.item.setX(cursorPosX);
                                    cursorPosX += afterSpace.item.getLayoutBounds().getWidth();
                                    afterSpace = afterSpace.next;
                                }
                                lines.add(arrayIndex, lastSpace);
                                arrayIndex++;
                            }
                        }
                    }
                    t.setX(cursorPosX);
                    t.setY(cursorPosY);
                    cursorPosX += tWidth;
                    currTypedWidth += tWidth;
                }
                displayText.toFront();
            }
        }
    }

    public void updateCursor() {
        Text lastAdded = output.getCurrChar();
        if (output.getCurrNode() == output.getHead()) {
            cursor.setX(5);
            cursor.setY(0);
        } else {
            cursor.setX((int) Math.round(lastAdded.getX()) + (int) Math.round(lastAdded.getLayoutBounds().getWidth()));
            cursor.setY((int) Math.round(lastAdded.getY()));
        }
        cursor.setHeight(lineHeight);
    }

    /**
     * An EventHandler to handle keys that get pressed.
     */
    private class KeyEventHandler implements EventHandler<KeyEvent> {
        /**
         * The Text to display on the screen.
         */

        KeyEventHandler(final Group root, int windowWidth, int windowHeight, Scene passedScene) {
            displayRoot = root;
            displayText = new Text(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y, "");
            displayText.setTextOrigin(VPos.TOP);
            displayText.setFont(Font.font(fontName, fontSize));
            root.getChildren().add(displayText);
            scene = passedScene;
            scene.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(
                        ObservableValue<? extends Number> observableValue,
                        Number oldScreenWidth,
                        Number newScreenWidth) {
                    MAX_LINE_WIDTH = newScreenWidth.intValue() - 10 - (int)scrollBar.getLayoutBounds().getWidth();
                    render();
                    updateCursor();
                    scrollBar.setLayoutX(newScreenWidth.intValue() - (int)scrollBar.getLayoutBounds().getWidth());
                }
            });
            scene.heightProperty().addListener(new ChangeListener<Number>() {
                @Override public void changed(
                        ObservableValue<? extends Number> observableValue,
                        Number oldScreenHeight,
                        Number newScreenHeight) {
                    WINDOW_HEIGHT = (newScreenHeight.intValue());
                    render();
                    updateCursor();
                    scrollBar.setPrefHeight(WINDOW_HEIGHT);
                    scrollBar.setMax((int)(output.getBack().item.getY()-WINDOW_HEIGHT+fontSize));

                }
            });

        }

        public boolean isEndOfLine(myNode n) {
            if (n.next.item.getText().equals("\n") || n.next.item.getText().equals("\r\n")) {
                return true;
            } else {
                return false;
            }
        }

        public void updateFont() {
            displayText.setFont(Font.font(fontName, fontSize));
            displayText.setTextOrigin(VPos.TOP);
            render();
            updateCursor();
            scrollBar.setMax((int)(output.getBack().item.getY()-WINDOW_HEIGHT+fontSize));
        }

        @Override
        public void handle(KeyEvent keyEvent) {
            /* handle shortcuts */
            if (keyEvent.isShortcutDown()) {
                KeyCode code = keyEvent.getCode();
                if (code == KeyCode.P) {
                    System.out.println("" + (int) Math.round(cursor.getX()) + ", " + (int) Math.round(cursor.getY()));
                    return;
                }
                if (code == KeyCode.PLUS || code == KeyCode.EQUALS) {
                    fontSize += 4;
                    updateFont();
                } else if (code == KeyCode.MINUS) {
                    fontSize = Math.max(0, fontSize - 4);
                    updateFont();
                } else if (code == KeyCode.S) {
                    saveFile();
                } else if (code == KeyCode.Z) {
                    if (!undoStack.empty())
                        undo();
                } else if (code == KeyCode.Y) {
                    if (!redoStack.empty())
                        redo();
                }
                /* handle delete, arrow keys */
            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                KeyCode code = keyEvent.getCode();
                /* backspace */
                if (code == KeyCode.BACK_SPACE) {
                    Text toRemove = output.delete();
                    textRoot.getChildren().remove(toRemove); //removes nothing bc never added new line to root
                    render(); updateCursor();
                    updateScrollBarDelete();
                    if (undoCount == 100)
                        undoStack.remove(0);
                    Action newAction = new Action(toRemove, false, true, output.getCurrNode());
                    leftOff = output.getCurrNode();
                    undoStack.push(newAction);
                } else if (code == KeyCode.LEFT) {
                    Text adjChar = output.moveLeft();
                    if (adjChar != null) {
                        cursor.setX((int) Math.round(adjChar.getX()) + (int) Math.round(output.getCurrChar().getLayoutBounds().getWidth()));
                        cursor.setY((int) Math.round(adjChar.getY()));
                    }
                } else if (code == KeyCode.RIGHT) {
                    Text adjChar = output.moveRight();
                    if (adjChar != null) {
                        cursor.setX((int) Math.round(adjChar.getX()) +
                                (int) Math.round(output.getCurrChar().getLayoutBounds().getWidth()));
                        cursor.setY((int) Math.round(adjChar.getY()));
                    }
                } else if (code == KeyCode.UP) {
                    /* on the first line */
                    if ((int) cursor.getY() == 0 || (((int) cursor.getY() == lineHeight) && (int) cursor.getX() == 5)) {
                        cursor.setX(5);
                        output.moveUp(output.getHead());
                    } else {
                        int index = ((int) Math.round(cursor.getY()) / lineHeight) - 1; //line marker before it
                        myNode prevLine = (myNode) lines.get(index);
                        /* cursor is at start of a line that is not the second line */
                        if (cursor.getX() == 5) {
                            myNode prevPrevLine = ((myNode) lines.get(index - 1));
                            if (prevLine.next.item.getText().equals("\n") || prevLine.next.item.getText().equals("\r\n")) {
                                output.moveUp(prevPrevLine.next); //make currChar point to the enter node
                            } else {
                                output.moveUp(prevPrevLine); //make currChar point to the space or letter (normal wrapped)
                            }
                        } /* previous line is shorter */ else if (prevLine.item.getX() + prevLine.item.getLayoutBounds().getWidth() < cursor.getX()) {
                            cursor.setX((int) Math.round(prevLine.item.getX() + prevLine.item.getLayoutBounds().getWidth()));
                            output.moveUp(prevLine);
                        } /* previous line is same height or longer */ else {
                            while (prevLine.item.getX() + prevLine.item.getLayoutBounds().getWidth() >= (int)cursor.getX()) {
                                prevLine = prevLine.prev;
                            }
                            int pos1 = (int) Math.round(prevLine.item.getX() + prevLine.item.getLayoutBounds().getWidth());
                            int pos2 = (int) Math.round(prevLine.next.item.getX() + prevLine.next.item.getLayoutBounds().getWidth());

                            int diff1 = (int)cursor.getX() - pos1;
                            int diff2 = pos2 - (int)cursor.getX();
                            if (diff1 < diff2) {
                                cursor.setX(pos1);
                                output.moveUp(prevLine);
                            } else {
                                cursor.setX(pos2);
                                output.moveUp(prevLine.next);
                            }
                        }
                    }
                    /* move up one line */
                    if (cursor.getY() > 0)
                        cursor.setY((int) Math.round(cursor.getY()) - lineHeight);
                    /* if it is out of context change scrollBar value */
                    if (scrollBar.getMax() > 0) {
                        if (cursor.getY() < scrollBar.getValue()) {
                            scrollBar.setValue((int)(cursor.getY()));
                        }
                    }
                } else if (code == KeyCode.DOWN) {
                    /* on the last line */
                    if ((int) cursor.getY() == output.getBack().item.getY()) {
                        cursor.setX(output.getBack().item.getX() + output.getBack().item.getLayoutBounds().getWidth());
                        output.moveDown(output.getBack());
                    } else {
                        int index = ((int) Math.round(cursor.getY()) / lineHeight); //line marker before it
                        myNode myLine = (myNode) lines.get(index);
                        if (myLine.next.item.getText().equals("\n") || myLine.next.item.getText().equals("\r\n")) {
                            myLine = myLine.next.next;
                        } else {
                            myLine = myLine.next;
                        }
                        myNode behindMyLine = myLine.prev;
                        while (myLine != output.getBack() && !isEndOfLine(myLine) && myLine.item.getX() + myLine.item.getLayoutBounds().getWidth() <= cursor.getX()) {
                            behindMyLine = myLine;
                            myLine = myLine.next;
                        }
                        if (myLine == output.getBack()) {
                            cursor.setX(output.getBack().item.getX() + output.getBack().item.getLayoutBounds().getWidth());
                            output.moveDown(output.getBack());
                        } else if (isEndOfLine(myLine)) {
                            cursor.setX(myLine.item.getX() + myLine.item.getLayoutBounds().getWidth());
                            output.moveDown(myLine);
                        } else {
                            int pos1 = (int) Math.round(myLine.item.getX() + myLine.item.getLayoutBounds().getWidth());
                            int pos2 = (int) Math.round(behindMyLine.item.getX() + behindMyLine.item.getLayoutBounds().getWidth());
                            int diff1 = pos1 - (int) cursor.getX();
                            int diff2 =(int) cursor.getX() -  pos2;
                            if (diff1 < diff2) {
                                cursor.setX(pos1);
                                output.moveDown(myLine);
                            } else {
                                cursor.setX(pos2);
                                output.moveDown(behindMyLine);
                            }
                        }   cursor.setY((int) Math.round(cursor.getY()) + lineHeight);
                    }
                    if (scrollBar.getMax() > 0) {
                        if (cursor.getY() + fontSize > scrollBar.getValue() + WINDOW_HEIGHT) {
                            /* move up by changing scrollBar value */
                            scrollBar.setValue((int)(cursor.getY()+fontSize-WINDOW_HEIGHT));
                        }
                    }
                }
                displayText.setText(charTyped);
            }
            else if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                String characterTyped = keyEvent.getCharacter();
                if (characterTyped.equals("\r") || characterTyped.equals("\n")) {
                    Text enter = new Text("\n");
                    enter.setTextOrigin(VPos.TOP);
                    output.add(enter);
                    textRoot.getChildren().add(enter);
                    render();
                    cursor.setX(STARTING_TEXT_POSITION_X);
                    cursor.setY((int) Math.round(cursor.getY() + lineHeight));
                    if (undoCount == 100)
                        undoStack.remove(0);
                    Action newAction = new Action(enter, true, false, output.getCurrNode());
                    undoStack.push(newAction);
                } else if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8) {
                    Text newChar = new Text(characterTyped);
                    newChar.setTextOrigin(VPos.TOP);
                    output.add(newChar);
                    textRoot.getChildren().add(newChar);
                    render();
                    updateCursor();
                    if (undoCount == 100)
                        undoStack.remove(0);
                    Action newAction = new Action(newChar, true, false, output.getCurrNode());
                    undoStack.push(newAction);
                }
                leftOff = output.getCurrNode();
                updateScrollBarAdd();
                redoStack = new Stack<>();
                redoCount = 0;
            }
        }
    }

    public void updateScrollBarDelete() {
        if (output.getBack().item.getY() + fontSize < WINDOW_HEIGHT) {
            scrollBar.setMax(0);
            scrollBar.setValue(0);
        } else if (cursor.getY() < scrollBar.getValue()) {
            scrollBar.setValue(cursor.getY());
        } else if (output.getBack().item.getY() + fontSize < WINDOW_HEIGHT + scrollBar.getMax()) {
            scrollBar.setMax((int)(output.getBack().item.getY() + fontSize - WINDOW_HEIGHT));
        } else if (output.getBack().item.getY() + fontSize < WINDOW_HEIGHT + scrollBar.getValue()) {
            scrollBar.setValue((int)(output.getBack().item.getY() + fontSize - WINDOW_HEIGHT));
        }
    }

    public void updateScrollBarAdd() {
        if (cursor.getY() < scrollBar.getValue()) {
            scrollBar.setValue(cursor.getY());
        } else if (output.getBack().item.getY() + fontSize < WINDOW_HEIGHT + scrollBar.getMax()) {
            scrollBar.setMax((int)(output.getBack().item.getY() + fontSize - WINDOW_HEIGHT));
        } else if (output.getBack().item.getY() + fontSize > WINDOW_HEIGHT + scrollBar.getMax()) {
            scrollBar.setMax((int)(output.getBack().item.getY() + fontSize - WINDOW_HEIGHT));
        } else if (output.getBack().item.getY() + fontSize > WINDOW_HEIGHT + scrollBar.getValue()) {
            scrollBar.setValue((int)(output.getBack().item.getY() + fontSize - WINDOW_HEIGHT));
        } else if (cursor.getY() + fontSize > WINDOW_HEIGHT + scrollBar.getValue()) {
            scrollBar.setValue((int)(cursor.getY() + fontSize - WINDOW_HEIGHT));
        }
    }

        private class RectangleBlinkEventHandler implements EventHandler<ActionEvent> {
            private int currentColorIndex = 0;
            private Color[] boxColors = {Color.BLACK, Color.TRANSPARENT};

            RectangleBlinkEventHandler() {
                // Set the color to be the first color in the list.
                changeColor();
            }

            private void changeColor() {
                cursor.setFill(boxColors[currentColorIndex]);
                currentColorIndex = (currentColorIndex + 1) % boxColors.length;
            }

            @Override
            public void handle(ActionEvent event) {
                changeColor();
            }
        }

        /**
         * Makes the text bounding box change color periodically.
         */
        public void makeRectangleColorChange() {
            // Create a Timeline that will call the "handle" function of RectangleBlinkEventHandler
            // every 1 second.
            final Timeline timeline = new Timeline();
            // The rectangle should continue blinking forever.
            timeline.setCycleCount(Timeline.INDEFINITE);
            RectangleBlinkEventHandler cursorChange = new RectangleBlinkEventHandler();
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), cursorChange);
            timeline.getKeyFrames().add(keyFrame);
            timeline.play();
        }

    private class MouseClickEventHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent mouseEvent) {
            double mousePressedX = mouseEvent.getX();
            double mousePressedY = mouseEvent.getY() + scrollBar.getValue();
            System.out.println(mousePressedX + ", " + mousePressedY);
            /* first set y position & index!! */
            int index = (int)mousePressedY/lineHeight;
            if (lines.size() == 0) {
                if (mousePressedX < STARTING_TEXT_POSITION_X) {
                    cursor.setX(STARTING_TEXT_POSITION_X);
                    output.moveUp(output.getHead());
                    return;
                }
                myNode myLine = output.getBack();
                int endPos = (int)(myLine.item.getLayoutBounds().getWidth() + myLine.item.getX());
                if (endPos < mousePressedX) {
                    cursor.setX(endPos);
                    output.moveUp(myLine);
                } else {
                    while (myLine.item.getX() + myLine.item.getLayoutBounds().getWidth() >= (int) mousePressedX) {
                        myLine = myLine.prev;
                    }
                    int pos1 = (int) Math.round(myLine.item.getX() + myLine.item.getLayoutBounds().getWidth());
                    int pos2 = (int) Math.round(myLine.next.item.getX() + myLine.next.item.getLayoutBounds().getWidth());
                    int diff1 = (int) mousePressedX - pos1;
                    int diff2 = pos2 - (int) mousePressedX;
                    if (diff1 < diff2) {
                        cursor.setX(pos1);
                        output.moveUp(myLine);
                    } else {
                        cursor.setX(pos2);
                        output.moveUp(myLine.next);
                    }
                } return;
            } else if (mousePressedY > output.getBack().item.getY() + lineHeight) {
                cursor.setX((int)output.getBack().item.getX() + (int)output.getBack().item.getLayoutBounds().getWidth());
                cursor.setY((int)output.getBack().item.getY()); return;
            } else if (index == lines.size()) {
                if (((myNode)lines.get(index-1)).next != null) {
                    cursor.setY(index*lineHeight);
                } else {
                    cursor.setX((int)output.getBack().item.getX() + (int)output.getBack().item.getLayoutBounds().getWidth());
                    cursor.setY((int)output.getBack().item.getY());
                    return;
                }
            } else { //all inside
                cursor.setY(index*lineHeight);
            }
            /* set x position */
            myNode myLine;
            if (index == output.getBack().item.getY()/lineHeight) {
                myLine = output.getBack();
            } else {
                myLine = (myNode) lines.get(index);
            }
            int endPos = (int)(myLine.item.getLayoutBounds().getWidth() + myLine.item.getX());
            if (endPos < mousePressedX) {
                cursor.setX(endPos);
                output.moveUp(myLine);
            } else {
                while (myLine.item.getX() + myLine.item.getLayoutBounds().getWidth() >= (int) mousePressedX) {
                    myLine = myLine.prev;
                }
                int pos1 = (int) Math.round(myLine.item.getX() + myLine.item.getLayoutBounds().getWidth());
                int pos2 = (int) Math.round(myLine.next.item.getX() + myLine.next.item.getLayoutBounds().getWidth());
                int diff1 = (int) mousePressedX - pos1;
                int diff2 = pos2 - (int) mousePressedX;
                if (diff1 < diff2) {
                    cursor.setX(pos1);
                    output.moveUp(myLine);
                } else {
                    cursor.setX(pos2);
                    output.moveUp(myLine.next);
                }
            }
        }
    }

        public void openFile() {
            try {
                File inputFile = new File(filename);
                // Check to make sure that the input file exists!
                if (!inputFile.exists()) {
                    inputFile.createNewFile();
                } else {
                    FileReader reader = new FileReader(filename);
                    BufferedReader bufferedReader = new BufferedReader(reader);

                    int intRead = -1;
                    while ((intRead = bufferedReader.read()) != -1) {
                        char charRead = (char) intRead;
                        String stringChar = Character.toString(charRead);
                        Text toAdd = new Text(stringChar);
                        output.add(toAdd);
                        toAdd.setTextOrigin(VPos.TOP);
                        textRoot.getChildren().add(toAdd);
                        render();
                        updateCursor();
                    }
                    if (output.getBack().item.getY() + fontSize > WINDOW_HEIGHT) {
                        scrollBar.setMax((int)(output.getBack().item.getY() + fontSize - WINDOW_HEIGHT));
                        scrollBar.setValue((int)scrollBar.getMax());
                    }

                    System.out.println("Successfully opened file " + filename);
                    // Close the reader and writer.
                    bufferedReader.close();
                }
            } catch (FileNotFoundException fileNotFoundException) {
                System.out.println("File not found! Exception was: " + fileNotFoundException);
            } catch (IOException ioException) {
                System.out.println("Error when copying; exception was: " + ioException);
            }
        }

        public void saveFile() {
            try {
                File inputFile = new File(filename);
                // Check to make sure that the input file exists!
                if (!inputFile.exists()) {
                    System.out.println("Unable to save because file " + filename
                            + " does not exist");
                    return;
                }
                FileWriter writer = new FileWriter(filename);
                myNode fileStart = output.getHead();
                fileStart = fileStart.next; //set cursor
                while (fileStart != null) {
                    char charRead = fileStart.item.getText().charAt(0);
                    writer.write(charRead);
                    fileStart = fileStart.next;
                }
                writer.close();
                System.out.println("Successfully saved file");

            } catch (FileNotFoundException fileNotFoundException) {
                System.out.println("File not found! Exception was: " + fileNotFoundException);
            } catch (IOException ioException) {
                System.out.println("Error when copying; exception was: " + ioException);
            }
        }

    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();
        textRoot = new Group();
        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, Color.WHITE);
        EventHandler<KeyEvent> keyEventHandler =
                new KeyEventHandler(root, WINDOW_WIDTH, WINDOW_HEIGHT, scene);
        // Register the event handler to be called for all KEY_PRESSED and KEY_TYPED events.
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);
        scene.setOnMouseClicked(new MouseClickEventHandler());

        displayRoot.getChildren().add(textRoot);

        textRoot.getChildren().add(cursor);
        cursor.setX(STARTING_TEXT_POSITION_X);
        makeRectangleColorChange();


        primaryStage.setTitle("CS61B Text Editor");

        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();

        scrollBar = new ScrollBar();
        scrollBar.setOrientation(Orientation.VERTICAL);
        // Set the height of the scroll bar so that it fills the whole window.
        scrollBar.setPrefHeight(WINDOW_HEIGHT);

        scrollBar.setMin(0);
        scrollBar.setMax(0);

        displayRoot.getChildren().add(scrollBar);
        MAX_LINE_WIDTH = (int)(WINDOW_WIDTH - 10 - scrollBar.getLayoutBounds().getWidth());
        scrollBar.setLayoutX(WINDOW_WIDTH - (int)scrollBar.getLayoutBounds().getWidth());

        scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldValue,
                    Number newValue) {
                textRoot.setLayoutY(-1 * newValue.doubleValue());
            }
        });
        openFile();
    }

        public static void main(String[] args) {
            if (args.length < 1) {
                System.out.println("Error: File name not found");
                System.exit(1);
            } else {
                filename = args[0];
                launch(args);
            }
        }

}
