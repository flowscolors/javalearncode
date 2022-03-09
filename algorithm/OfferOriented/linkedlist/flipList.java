package linkedlist;

import common.ListNode;

import java.util.List;

/**
 * @author flowscolors
 * @date 2022-02-21 13:11
 */
public class flipList {

    public static ListNode solution(ListNode head){
        ListNode pre = null;
        ListNode cur = head;

        while(cur!=null){
            //三次位移 + 1次指向变更
            ListNode next = cur.next;
            cur.next = pre;
            pre = cur;
            cur = next;
        }
        return pre;
    }

    public static  void travelList(ListNode head){
        while(head!=null){
            System.out.println(head.val);
            head = head.next;
        }
    }

    //原本方向  7 -> 2 -> 9 -> 4 -> 3 -> 1
    //反转方向  1 -> 3 -> 4 -> 9 -> 2 -> 7
    public static void main(String[] args) {
        ListNode listNode1 = new ListNode(7);
        ListNode listNode2 = new ListNode(2);
        ListNode listNode3 = new ListNode(9);
        ListNode listNode4 = new ListNode(4);
        ListNode listNode5 = new ListNode(3);
        ListNode listNode6 = new ListNode(1);
        listNode1.next = listNode2;
        listNode2.next = listNode3;
        listNode3.next = listNode4;
        listNode4.next = listNode5;
        listNode5.next = listNode6;
        travelList(listNode1);
        System.out.println("======= After Flip======");
        solution(listNode1);
        travelList(listNode1);

    }
}
