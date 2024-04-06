package utils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 不放回抽样类
 */
public class Sampling<T> {
    List<T> lists;

    public Sampling(List<T> lists) {
        this.lists = new LinkedList<>(lists);
        Collections.shuffle(this.lists);
    }

    public boolean hasNext() {
        return !lists.isEmpty();
    }

    public T next() {
        T ret = lists.get(0);
        lists.remove(0);
        return ret;
    }

    public int size() {
        return lists.size();
    }
}
