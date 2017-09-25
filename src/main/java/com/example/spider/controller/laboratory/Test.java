package com.example.spider.controller.laboratory;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/8/24
 */

public class Test {

    private static List<List<Integer>> result = new ArrayList<>();

    public static List<List<Integer>> binaryTreePathSum(TreeNode root, int target) {
        result.clear();
        if (root != null) {
            deep(root, new ArrayList<>(), 0, target);
        }
        return result;
    }

    private static void deep(TreeNode node, List<Integer> list, int sum, int target) {
        list.add(node.val);
        int tempSum = sum + node.val;
        if (tempSum == target && node.left == null && node.right == null) {
            result.add(list);
        } else {
            if (node.left != null) {
                deep(node.left, new ArrayList<>(list), tempSum, target);
            }
            if (node.right != null) {
                deep(node.right, new ArrayList<>(list), tempSum, target);
            }
        }
    }

    public static void main(String[] args) {
        String[] array1 = new String[]{"37", "-34", "-48", "#", "-100", "-100", "48", "#", "#", "#", "#", "-54", "#", "-71", "-22", "#", "#", "#", "8"};
        String[] array = new String[]{"1", "3", "4", "2", "#", "1", "1", "#", "#", "7", "8", "-1", "#", "#", "#", "#", "#", "1", "2"};
        List<List<Integer>> lists = binaryTreePathSum(buildTree(array), 6);
        for (List<Integer> list : lists) {
            System.out.println(StringUtils.join(list, " "));
        }
    }

    //
    public static TreeNode buildTree(String[] array) {
        TreeNode root = new TreeNode(Integer.parseInt(array[0]));

        LinkedBlockingQueue<TreeNode> queue = new LinkedBlockingQueue<>();
        queue.add(root);

        int index = 1;
        while (!queue.isEmpty()) {
            TreeNode curr = queue.poll();
            addChild(queue, curr, index < array.length ? array[index++] : "#", true);
            addChild(queue, curr, index < array.length ? array[index++] : "#", false);
        }
        return root;
    }

    private static void addChild(LinkedBlockingQueue<TreeNode> queue, TreeNode parent, String childVal, boolean left) {
        if (!StringUtils.equals("#", childVal)) {
            TreeNode childNode = new TreeNode(Integer.parseInt(childVal));
            queue.add(childNode);
            if (left) {
                parent.left = childNode;
            } else {
                parent.right = childNode;
            }
        }
    }
    /*
                                            37(1)
                            -34(2)                           -48(3)
                   #(4)               -100(5)        -100(6)             48(7)
                                   #(8)      #(9)   #(10)  #(11)     -54(12)         #(13)
                                                                 -71(14)   -22(15)
                                                              #(16)  #(17) #(18)  8(19)
     */

    private static void print(String[] array, int start, int end) {
        start = start - 1;
        end = end <= array.length ? (end - 1) : array.length - 1;
        while (start <= end) {
            System.out.print(array[start]);
            System.out.print("(" + (start + 1) + ")  ");
            start++;
        }
        System.out.println("");
    }
}