package org.example;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Cluster {
    public static void main(String[] args) {
        Config config = new Config();
        config.setClusterName("cluster 1");

        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz3 = Hazelcast.newHazelcastInstance(config);

        Map<String, Value> map = hz1.getMap("my-distributed-map");

        String key = "counter";
        System.out.println("Start counting.");
        long start = System.currentTimeMillis();

        map.put(key, new Value(0));
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                for (int k = 0; k < 10000; k++) {
                    Value value = map.get(key);
                    value.increment();

                    map.put(key, value);
                }
            });

            thread.start();
            threads.add(thread);
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException("Something went wrong");
            }
        }

        long finish = System.currentTimeMillis();
        System.out.println("Time: " + (finish - start) + " ms");

        for (Map.Entry<String, Value> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue().getValue());
        }

        System.out.println("End counting.");
    }

    static class Value implements Serializable {
        private long value;

        public Value(long value) {
            this.value = value;
        }

        public void increment() {
            value++;
        }

        public long getValue() {
            return value;
        }
    }
}
