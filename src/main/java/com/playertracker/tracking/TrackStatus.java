package com.playertracker.tracking;

/**
 * 同步给客户端的追踪状态。序号即网络包里的字节值，新增时只能往后追加。
 */
public enum TrackStatus {
    /** 未绑定（客户端一般直接从物品组件判断，保留占位）。 */
    UNBOUND,
    /** 目标在线，且与持有者处于同一维度。 */
    ONLINE_SAME_DIM,
    /** 目标在线，但在其他维度。 */
    ONLINE_OTHER_DIM,
    /** 目标已离线。 */
    OFFLINE;

    private static final TrackStatus[] VALUES = values();

    public byte id() {
        return (byte) this.ordinal();
    }

    public static TrackStatus byId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : UNBOUND;
    }
}
