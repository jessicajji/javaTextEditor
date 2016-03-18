package editor;

import javafx.scene.text.Text;

/**
 * Created by jessicaji on 3/1/16.
 */
public class myNode {
    public Text item;
    public myNode next;
    public myNode prev;

    public myNode(Text i, myNode prev, myNode next) {
        this.item = i;
        this.prev = prev;
        this.next = next;
    }
}
