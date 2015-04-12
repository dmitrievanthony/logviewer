package org.logviewer.core.index;

/**
 * Abstract index entity helps to look up row in a log file.
 * Index consists of the pointer to a block (as the block size and the block offset)
 * and the row position inside this block.
 * Blocks with small size provide faster access, but they increase memory usage.
 */
public class Index {

    private long blockOffset;

    private long blockSize;

    private long positionInBlock;

    private long blockLength;

    public Index(long blockOffset, long blockSize, long positionInBlock, long blockLength) {
        this.blockOffset = blockOffset;
        this.blockSize = blockSize;
        this.positionInBlock = positionInBlock;
        this.blockLength = blockLength;
    }

    public long getBlockOffset() {
        return blockOffset;
    }

    public void setBlockOffset(long blockOffset) {
        this.blockOffset = blockOffset;
    }

    public long getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(long blockSize) {
        this.blockSize = blockSize;
    }

    public long getPositionInBlock() {
        return positionInBlock;
    }

    public void setPositionInBlock(long positionInBlock) {
        this.positionInBlock = positionInBlock;
    }

    public long getBlockLength() {
        return blockLength;
    }

    public void setBlockLength(long blockLength) {
        this.blockLength = blockLength;
    }
}
