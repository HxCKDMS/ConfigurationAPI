package HxCKDMS.HxCConfig;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class Purple {
    public int x;
    public int y;
    public int z;

    public List<Integer> asfd = Arrays.asList(1, 2, 3, 4, 5);

    public Purple() {}

    public Purple(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return "Purple{" + "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", asfd=" + asfd +
                '}';
    }
}
