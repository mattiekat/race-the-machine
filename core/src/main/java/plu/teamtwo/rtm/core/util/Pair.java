package plu.teamtwo.rtm.core.util;

import java.io.Serializable;

public class Pair<T, U> implements Comparable<Pair<T, U>>, Serializable {
    public T a;
    public U b;

    public Pair() {}
    public Pair(T a, U b) {
        this.a = a;
        this.b = b;
    }


    @Override
    public int hashCode() {
        return (a.hashCode() + 0x2843af83) ^ (b.hashCode() + 0xfaf923d9);
    }

    
    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Pair)) return false;
        Pair o = (Pair)other;
        return a.equals(o.a) && b.equals(o.b);
    }


    @Override
    public int compareTo(Pair<T, U> pair) {
        if(this.equals(pair)) return 0;

        Comparable<T> a = (Comparable<T>)this.a;
        Comparable<U> b = (Comparable<U>)this.b;

        final int ca = a.compareTo(pair.a);
        if(ca < 0) return -1;
        if(ca == 0) {
            final int cb = b.compareTo(pair.b);
            if(cb < 0) return -1;
            if(cb == 0) throw new RuntimeException("Pair found invalid comparison method used by subtype.");
            return 1;
        }
        return 1;
    }
}