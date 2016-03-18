package editor;
import javafx.scene.Node;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;
import java.io.*;
import javafx.scene.text.Text;
import java.util.Iterator;

/**
 * Created by jessicaji on 2/28/16.
 */
public class TextLinkedList {
    //public int prevCharWidth;
    // track last space, adding to character width, move recent so you can move left & right accurate widths
    private myNode sentinel;
    private myNode currChar = sentinel;
    private myNode back;
    private int numChar;

    private class LLLIterator implements Iterator {
        private myNode front;
        public LLLIterator() {
            front = sentinel;
        }
        public boolean hasNext() {
            return front.next != null;
        }
        public myNode next() {
            myNode currentThing = front.next;
            front = front.next;
            return currentThing;
        }
    }

    public void setCurrNode(myNode n) {
        currChar = n;
    }

    public Iterator iterator() {
        return new LLLIterator();
    }
    public boolean isEmpty() { return numChar == 0; }
    public int size() { return numChar; }

    public TextLinkedList() {
        numChar = 0;
        Text sent = new Text();
        sentinel = new myNode(sent, null, null);
        sent.setX(5);
        sent.setY(0);
        currChar = sentinel;
        back = sentinel;
    }


    public myNode getBack() {
        return back;
    }

    public int getCurrCharX() {
        Text t = currChar.item;
        int currCharX = (int) Math.round(t.getX() + t.getLayoutBounds().getWidth());
        return currCharX;
    }

    public void add(Text i) {
        numChar++;
        if (currChar.next == null) {
            currChar.next = new myNode(i, currChar, null);
            currChar = currChar.next; //currChar = thing u added
            back = currChar;
        } else {
            myNode added = new myNode(i, currChar, currChar.next);
            currChar.next.prev = added;
            currChar.next = added;
            currChar = currChar.next;
        }
    }

    public Text delete() {
        if (isEmpty()) { return null;}
        if (currChar.prev == null) { return null; }
        Text toReturn = currChar.item;
//        if ((currChar.prev.item.getText() == "\n") || (currChar.prev.item.getText() == "\r\n")) {
//            currChar.prev.prev.next = currChar.next;
//            currChar = currChar.prev.prev;
//            return toReturn;
//        }
        numChar--;
        if (currChar.next != null) {
            currChar.prev.next = currChar.next;
            currChar.next.prev = currChar.prev;
            currChar = currChar.prev;
        } else {
            currChar.prev.next = null;
            currChar = currChar.prev;
            back = currChar;
        }
        return toReturn;
    }

    public Text moveLeft() {
        if (isEmpty()) { return null;}
        if (currChar.prev == null) { return null; }
//        if (currChar.prev.item.getText() == "\n" || currChar.prev.item.getText() == "\r\n") {
//            currChar = currChar.prev;
//        }
        currChar = currChar.prev;
        return currChar.item;
    }

    public Text moveRight() {
        if (currChar.next == null) { return null;}
//        if (currChar.next.item.getText() == "\n" || currChar.next.item.getText() == "\r\n") {
//            currChar = currChar.next;
//        }
        currChar = currChar.next;
        return currChar.item;
    }

    public void moveDown(myNode n) {
        if (n == sentinel) {
            currChar = sentinel; //would add things after sentinel before currChar.next
        } else {
            currChar = n;
        }

    }

    public void moveUp(myNode n) {
        if (n == sentinel) {
            currChar = sentinel; //would add things after sentinel before currChar.next
        } else {
            currChar = n;
        }
    }

    public myNode getCurrNode() {
        return currChar;
    }

    public Text getCurrChar() {
        return currChar.item;
    }

    public myNode getHead() { return sentinel; }

}
