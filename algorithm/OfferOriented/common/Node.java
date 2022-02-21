package common;

import java.util.List;

/**
 * @author flowscolors
 * @date 2022-02-17 10:34
 */
public class Node {
    public int val ;
    public List<Node> children;

    public Node (int _val , List<Node> _children) {
        val = _val;
        children = _children;
    }

}
