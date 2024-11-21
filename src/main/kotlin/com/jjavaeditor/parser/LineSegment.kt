@file:Suppress("unused")

package com.jjavaeditor.parser

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class LineSegment(val kind: SegmentKind, val startOffset: Int, val endOffset: Int) {
    companion object {
        private const val INT_SIZE = 4
        private const val SHORT_SIZE = 2
        private const val BYTE_SIZE = 1
        private const val BYTE_MAX_VALUE = Byte.MAX_VALUE.toInt()
        private const val SHORT_MAX_VALUE = Short.MAX_VALUE.toInt()

        fun serialize(segments: Collection<LineSegment>): Pair<ByteArray, UByte> {
            val endOffset = segments.last().endOffset
            var arrayItemSize = BYTE_SIZE
            if (endOffset > BYTE_MAX_VALUE) {
                arrayItemSize = if (endOffset > SHORT_MAX_VALUE) INT_SIZE else SHORT_SIZE
            }
            val buffer = ByteBuffer.allocate((2 * arrayItemSize + BYTE_SIZE) * segments.size)
            buffer.order(ByteOrder.BIG_ENDIAN)
            when (arrayItemSize) {
                BYTE_SIZE ->
                    for (segment in segments) {
                        buffer.put(segment.kind.toByte())
                        buffer.put(segment.startOffset.toByte())
                        buffer.put(segment.endOffset.toByte())
                    }

                SHORT_SIZE ->
                    for (segment in segments) {
                        buffer.put(segment.kind.toByte())
                        buffer.putShort(segment.startOffset.toShort())
                        buffer.putShort(segment.endOffset.toShort())
                    }

                INT_SIZE ->
                    for (segment in segments) {
                        buffer.put(segment.kind.toByte())
                        buffer.putInt(segment.startOffset)
                        buffer.putInt(segment.endOffset)
                    }
            }
            return Pair(buffer.array(), arrayItemSize.toUByte())
        }

        fun deserialize(bytes: ByteArray?, arrayItemSize: UByte): List<LineSegment>? {
            if (bytes == null) return null

            val offsetSize = arrayItemSize.toInt()
            val buffer = ByteBuffer.wrap(bytes)
            buffer.order(ByteOrder.BIG_ENDIAN)

            val itemSize = 2 * offsetSize + 1
            val count = bytes.size / itemSize

            when (offsetSize) {
                BYTE_SIZE ->
                    return List(count) {
                        val kind = buffer.get().toUByte()
                        val startOffset = buffer.get().toInt()
                        val endOffset = buffer.get().toInt()
                        LineSegment(kind, startOffset, endOffset)
                    }

                SHORT_SIZE ->
                    return List(count) {
                        val kind = buffer.get().toUByte()
                        val startOffset = buffer.getShort().toInt()
                        val endOffset = buffer.getShort().toInt()
                        LineSegment(kind, startOffset, endOffset)
                    }

                INT_SIZE ->
                    return List(count) {
                        val kind = buffer.get().toUByte()
                        val startOffset = buffer.getInt()
                        val endOffset = buffer.getInt()
                        LineSegment(kind, startOffset, endOffset)
                    }
            }
            return null
        }

        fun getKind(index: Int, bytes: ByteArray, arrayItemSize: UByte): SegmentKind {
            val offset = index * (2 * arrayItemSize.toInt() + 1)
            require(offset < bytes.size)
            return bytes[offset].toUByte()
        }

        fun getRange(index: Int, bytes: ByteArray, arrayItemSize: UByte): Pair<Int, Int> {
            val offset = index * (2 * arrayItemSize.toInt() + 1) + 1
            require(offset + 2 * INT_SIZE < bytes.size)
            val buffer = ByteBuffer.wrap(bytes, offset, 2 * INT_SIZE)
            return Pair(buffer.getInt(), buffer.getInt())
        }
    }
}