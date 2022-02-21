package tree;

import common.TreeNode;

/**
 * @author flowscolors
 * @date 2022-02-21 13:45
 */
public class BalanceTree {

    public static Boolean solution(TreeNode treeNode){

        return true;
    }



    //层次遍历 7 -> 2 -> 9 -> 4 -> 3 -> 1
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
        System.out.println("是否是平衡二叉树 ： " + solution(treeNode1));
    }
}
