package com.pim.server.test;

import java.util.PriorityQueue;
import java.util.Queue;

public class Test {
    public static void main(String[] args) {

        Queue<Integer> priorityQueue = new PriorityQueue<Integer>();
        priorityQueue.add(1);
        priorityQueue.add(4);
        priorityQueue.add(2);
        priorityQueue.add(3);

        System.out.println(priorityQueue.poll());
        System.out.println(priorityQueue.size());
        System.out.println(priorityQueue.poll());
        System.out.println(priorityQueue.size());
        System.out.println(priorityQueue.poll());
        System.out.println(priorityQueue.size());
        System.out.println(priorityQueue.poll());
        System.out.println(priorityQueue.size());
    }
}
