package editor;

import javafx.scene.text.Text;

public class Action {
    public Text item;
    public int cursorposX;
    public int cursorposY;
    public boolean adding;
    public boolean deleting;
    public myNode savedNode;

    public Action(Text txt, boolean add, boolean delete, myNode trackedNode) {
        item = txt;
        adding = add;
        deleting = delete;
        savedNode = trackedNode;
    }

}
