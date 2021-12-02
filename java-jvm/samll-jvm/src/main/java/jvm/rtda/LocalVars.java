package jvm.rtda;

import jvm.rtda.heap.methodarea.Object;
/**
 * @author flowscolors
 * @date 2021-11-11 16:17
 */
// 局部变量表 本质是一个数据槽数组，每次从数据槽中中取数据、存数据 。
public class LocalVars {

    private Slot[] slots;

    public LocalVars (int maxLocals){
        if (maxLocals > 0){
            slots = new Slot[maxLocals];
            for(int i = 0;i < maxLocals;i++){
                slots[i] = new Slot();
            }
        }
    }

    // 局部变量表提供方法来对数据槽的某个位置存、取 Int、Long、Float、Object对象

    public void setInt(int idx, int val) {
        this.slots[idx].num = val;
    }

    public int getInt(int idx) {
        return slots[idx].num;
    }

    public void setFloat(int idx, float val) {
        this.slots[idx].num = (Float.valueOf(val)).intValue();
    }

    public Float getFloat(int idx) {
        int num = this.slots[idx].num;
        return (float) num;
    }

    public void setLong(int idx, long val) {
        this.slots[idx].num = (int) val;
        this.slots[idx + 1].num = (int) (val >> 32);
    }

    public Long getLong(int idx) {
        int low = this.slots[idx].num;
        int high = this.slots[idx + 1].num;
        return ((long) high << 32) | (long) low;
    }

    public void setDouble(int idx, double val) {
        setLong(idx, (long) val);
    }

    public Double getDouble(int idx) {
        return Double.valueOf(getLong(idx));
    }

    public void setRef(int idx, Object ref) {
        slots[idx].ref = ref;
    }

    public Object getRef(int idx) {
        return slots[idx].ref;
    }

    public Slot[] getSlots() {
        return slots;
    }

    public void setSlot(int idx, Slot slot) {
        this.slots[idx] = slot;
    }

}
