package plu.teamtwo.rtm.core.util;

import java.io.Serializable;

public class Triple<T, U, V> implements Comparable<Triple<T, U, V>>, Serializable {
    public T a;
    public U b;
    public V c;

    public Triple() {}
    public Triple(T a, U b, V c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }


    @Override
    public int hashCode() {
        return (a.hashCode() + 0x87453f84) ^ (b.hashCode() + 0xfa882315) ^ (c.hashCode() + 0x2341c233);
    }


    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Triple)) return false;
        Triple o = (Triple)other;
        return a.equals(o.a) && b.equals(o.b) && c.equals(o.c);
    }


    @Override
    public int compareTo(Triple<T, U, V> pair) {
        if(this.equals(pair)) return 0;

        Comparable<T> a = (Comparable<T>)this.a;
        Comparable<U> b = (Comparable<U>)this.b;
        Comparable<V> c = (Comparable<V>)this.c;

        final int ca = a.compareTo(pair.a);
        if(ca < 0) return -1;
        if(ca == 0) {
            final int cb = b.compareTo(pair.b);
            if(cb < 0) return -1;
            if(cb == 0) {
                if(cb < 0) return -1;
                if(cb == 0) throw new RuntimeException("Triple found invalid comparison method used by subtype.");
                return 1;
            }
            return 1;
        }
        return 1;
    }
}
