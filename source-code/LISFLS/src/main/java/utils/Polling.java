package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Polling<T> {
    List<T> lists;
    Iterator<T> nextIt;

    public Polling(List<T> lists) {
        if (lists.isEmpty()) { throw new IllegalArgumentException("list can not be empty"); }
        this.lists = new ArrayList<>(lists);
        Collections.shuffle(this.lists);
        this.nextIt = this.lists.iterator();
    }

    public T next() {
        if (!this.nextIt.hasNext()) {
            this.nextIt = this.lists.iterator();
        }
        T ret = this.nextIt.next();
        return ret;
    }

    public int size() {
        return lists.size();
    }
}
