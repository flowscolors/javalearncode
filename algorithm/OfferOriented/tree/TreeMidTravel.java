package tree;

import common.TreeNode;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author flowscolors
 * @date 2022-02-21 13:25
 */
public class TreeMidTravel {

    public static void solution(){

    }

    public static void solution2(){

    }

    public static  void travelTree(TreeNode root){
        Queue queue = new LinkedList<TreeNode>();
        if(root == null){
            return;
        }
        queue.offer(root);
        while(queue.size() > 0 ){
            TreeNode temp = (TreeNode) queue.poll();
            System.out.println(temp.val);
            if(temp.left!=null){
                queue.offer(temp.left);
            }
            if(temp.right!=null){
                queue.offer(temp.right);
            }
        }
    }

    //层次遍历 7 -> 2 -> 9 -> 4 -> 3 -> 1
    //中序遍历
    public static void main(String[] args) {
        TreeNode treeNode1 = new TreeNode(7);
        TreeNode treeNode2 = new TreeNode(2);
        TreeNode treeNode3 = new TreeNode(9);
        TreeNode treeNode4 = new TreeNode(4);
        TreeNode treeNode5 = new TreeNode(3);
        TreeNode treeNode6 = new TreeNode(1);
        treeNode1.left = treeNode2;
        treeNode1.right = treeNode3;
        treeNode2.left = treeNode4;
        treeNode2.right = treeNode5;
        treeNode3.left = treeNode6;
        travelTree(treeNode1);

    }
}
