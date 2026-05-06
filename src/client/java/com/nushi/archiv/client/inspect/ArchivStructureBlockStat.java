package com.nushi.archiv.client.inspect;

public class ArchivStructureBlockStat {
    private final String blockState;
    private final long count;
    private final double percentOfNonAir;

    public ArchivStructureBlockStat(String blockState, long count, double percentOfNonAir) {
        this.blockState = blockState == null ? "" : blockState.trim();
        this.count = count;
        this.percentOfNonAir = percentOfNonAir;
    }

    public String getBlockState() {
        return blockState;
    }

    public long getCount() {
        return count;
    }

    public double getPercentOfNonAir() {
        return percentOfNonAir;
    }

    public String getShortBlockName() {
        String clean = blockState;

        int namespaceIndex = clean.indexOf(":");
        if (namespaceIndex >= 0 && namespaceIndex < clean.length() - 1) {
            clean = clean.substring(namespaceIndex + 1);
        }

        int propertyIndex = clean.indexOf("[");
        if (propertyIndex >= 0) {
            clean = clean.substring(0, propertyIndex);
        }

        return clean;
    }
}