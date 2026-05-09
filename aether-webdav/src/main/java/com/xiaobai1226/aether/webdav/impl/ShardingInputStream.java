/*
 * Copyright 2017-2025 noear.org and authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xiaobai1226.aether.webdav.impl;

import java.io.IOException;
import java.io.InputStream;

/**
 * 分片文件流
 *
 * @author 阿范
 */
public class ShardingInputStream extends InputStream {
    private final InputStream in;
    private final long length;
    private long hasLength;

    public ShardingInputStream(InputStream in, long start, long length) {
        this.in = in;
        this.length = length;
        this.hasLength = length;
        if (start > 0) {
            skipFully(start);
        }
    }

    private void skipFully(long bytes) {
        long remaining = bytes;
        byte[] buffer = new byte[8192];
        while (remaining > 0) {
            try {
                long skipped = this.in.skip(remaining);
                if (skipped > 0) {
                    remaining -= skipped;
                    continue;
                }

                int read = this.in.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (read < 0) {
                    throw new IOException("Unexpected end of stream while skipping");
                }
                remaining -= read;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int read() throws IOException {
        if (this.hasLength <= 0) {
            return -1;
        }
        int r = in.read();
        if (r != -1) {
            this.hasLength -= 1;
        }
        return r;
    }

    @Override
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public long skip(long n) throws IOException {
        throw new IOException("此流不支持跳过");
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        if (this.hasLength <= 0) {
            return -1;
        }
        if (this.hasLength < len) {
            len = (int) this.hasLength;
        }
        int c = in.read(b, off, len);
        if (c != -1) {
            this.hasLength -= c;
        }
        return c;
    }

    @Override
    public int available() {
        return (int) Math.min(this.length, Integer.MAX_VALUE);
    }

    @Override
    public void close() {
        try {
            this.in.close();
        } catch (Exception e) {

        }
    }

    @Override
    public void mark(int readlimit) {
        throw new RuntimeException("此流不支持标记");
    }

    @Override
    public void reset() {
        throw new RuntimeException("此流不支持重置");
    }
}
