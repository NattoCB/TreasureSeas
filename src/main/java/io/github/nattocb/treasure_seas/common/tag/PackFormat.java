package io.github.nattocb.treasure_seas.common.tag;

public enum PackFormat {
    V1_13(3, "1.13"),
    V1_14(4, "1.14"),
    V1_15(4, "1.15"),
    V1_16(5, "1.16"),
    V1_17(6, "1.17"),
    V1_18(7, "1.18"),
    V1_18_2(8, "1.18.2"),
    V1_19(9, "1.19"),
    V1_19_3(10, "1.19.3"),
    V1_20(15, "1.20");

    private final int packFormat;
    private final String versionString;

    PackFormat(int packFormat, String versionString) {
        this.packFormat = packFormat;
        this.versionString = versionString;
    }

    public int getPackFormat() {
        return packFormat;
    }

    public String getVersionString() {
        return versionString;
    }
}
