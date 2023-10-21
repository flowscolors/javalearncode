

## 二叉树
二叉树的深度优先搜索可以说是基本操作了，因为大多数树的题的路径都是顺着子节点方向，也即纵向方向，更符合深度优先的特点，关键词 “路径”。

中序遍历、前序遍历、后续遍历。

```text
public List<Integer> inorderTravl(TreeNode root){
    List<Integer> nodes = new LinkedList<Interger>();
    dfs(root,nodes);
    return nodes;
}

public void dfs(TreeNode root,List<Integer> nodes){
    if(root!=null){
        dfs(root.left,nodes);
        nodes.add(root.val);
        dfs(root.right,nodes);
    }
}
```

```text
public List<Integer> inorderTravl(TreeNode root){
    List<Integer> nodes = new LinkedList<Interger>();
    Stack<TreeNode> stack = new Stack<TreeNode>();
    TreeNode cur = root;
    while(cur!=null || !stack.isEmpty()){
        while(cur!=null){
            stack.push(cur);
            cur = cur.left;
        }    
        cur = stack.pop();
        nodes.add(cur.val);
        cur = cur.right;
    }
    return nodes;
}
```



## 二叉搜索树






## TreeSet、TreeMap的应用
如果题目直接给了一个二叉搜索树，则直接使用即可，但如果题目没给，则实现一个平衡的二叉搜索树是很难的。Java使用红黑树实现TreeSet、TreeMap。
如果题目的数据结构的动态的（题目要求逐步在数据集合中添加更多数据），并且需要根据数据的大小实现快速查找，那么可能需要TreeSet或TreeMap。




## 堆 
堆是一种特殊的数据结构，根据根节点的值和子节点的值大小关系，又分为最大堆和最小堆。堆通常用完全二叉树或数组实现。
Java中提供了类型 PriorityQueue 实现了数据结构堆。默认最小堆，如果需要使用最大堆，需要修改排序规则。

add(e)   插入，但是失败抛异常。
remove() 删除，但是失败抛异常。
element() 返回最前面的元素，但是失败抛异常。
offer(e)  插入，失败不抛异常。
poll()    删除，失败不抛异常。
peek()    返回最前面元素，失败不抛异常。

注意这里的 offer()、poll() 就不是按队列先进先出进行，而是按照堆进行排序，每次add(e)、offer(e)会自动进行排序。每次remove()、poll()会删除堆顶元素。
因此插入的时间复杂度是O(logn)，删除的时间复杂度是O(1)。


常用解题方法：
1.TopK问题。由于最大堆、最小堆的性质，堆中最小的数字在堆顶。故可以借用该值充当TopK的阈值。
对于最小堆，堆顶数字代表TopK大数字中的最小一个，只要比这个数字大，就能进入TopK。
对于最大堆，堆顶数字代表TopK小数字中最大的一个，只要比这个数字小，就能进入TopK。

```text
public static int[] topK(int k,int[] nums){
        PriorityQueue heap = new PriorityQueue();
        int[] result = new int[k];
        for(int i = 0;i < nums.length ;i++){
            if(heap.size() < k){
                heap.offer(nums[i]);
            }else {
                if(nums[i] > (Integer) heap.peek()){
                    heap.poll();
                    heap.offer(nums[i]);
                }
            }
        }
        for(int i= 0;i<k;i++){
            result[i] = (int) heap.poll();
        }
        return result;
    }
```
