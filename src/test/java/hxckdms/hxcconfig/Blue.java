package hxckdms.hxcconfig;

public class Blue {
    public long xx;
    public long yy;
    public long zz;

    public double asdf;

    public Blue() {}

    public Blue(long xx, long yy, long zz, double asdf) {
        this.xx = xx;
        this.yy = yy;
        this.zz = zz;
        this.asdf = asdf;
    }

    @Override
    public String toString() {
        return "Blue{" +
                "xx=" + xx +
                ", yy=" + yy +
                ", zz=" + zz +
                ", asdf=" + asdf +
                '}';
    }
}
