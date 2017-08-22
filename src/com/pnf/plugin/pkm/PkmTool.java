/*******************************************************************************
 * Copyright (c) 2015 PNF Software, Inc.
 *
 *     https://www.pnfsoftware.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.pnf.plugin.pkm;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.pnfsoftware.jeb.util.io.IO;

/**
 * Class responsible for handling all ETC1 decompression functions
 * 
 * <pre>
 * PKM Structure:
 * - char tag[6] = "PKM 10"
 * - format (2 bytes) = number of mips (zero)
 * - texture width (2 bytes) = multiple of 4 (big endian) texture height (2 bytes) = "                       "
 * - original width (2 bytes) = Original dimensions (big endian) original height (2 bytes) = "                     "
 * </pre>
 * 
 * @author Carlos Gonzales, Nicolas Falliere
 *
 */
public class PkmTool {
    private static final String TEMP_DIR = "pkm_temp";
    private static final String PKM_EXT = ".pkm";
    private static final String PNG_EXT = ".png";

    private static File TEMP = null;
    static {
        try {
            TEMP = IO.createTempFolder(TEMP_DIR);
        }
        catch(IOException e) {
            PkmPlugin.LOG.catching(e);
        }
    }

    private String timestamp = new SimpleDateFormat("yyyyMMddhhmmssSS").format(new Date());;
    private ByteBuffer bytes;
    private File etcTool;
    private Dimension textureDim = new Dimension();
    private Dimension originalDim = new Dimension();

    /**
     * Creates a new {@code PkmTool} object from the given byte data
     * 
     * @param etcTool a {@code File} reference to the etc1tool executable
     * @param data a {@code byte} array containing the PKM image data
     */
    public PkmTool(File etcTool, byte[] data) {
        bytes = ByteBuffer.wrap(data);
        this.etcTool = etcTool;

        // Process PKM info out of data
        bytes.order(ByteOrder.BIG_ENDIAN);
        bytes.position(8); // Skip over the format and tag bytes

        // Retrieve the texture dimension data
        textureDim.width = bytes.getShort();
        textureDim.height = bytes.getShort();

        // Retrieve the texture dimension data
        originalDim.width = bytes.getShort();
        originalDim.height = bytes.getShort();
    }

    /**
     * Retrieves the texture dimension data read from the pkm
     * 
     * @return a String representation of this pkm file's texture dimension
     */
    public String getTextureDim() {
        return asString(textureDim) + " (multiples of 4)";
    }

    /**
     * Retrieves the original dimension data read from the pkm
     * 
     * @return a String representation of this pkm file's original dimension
     */
    public String getOriginalDim() {
        return asString(originalDim);
    }

    private String asString(Dimension d) {
        StringBuffer b = new StringBuffer();

        b.append("[");
        b.append(d.width);
        b.append(" x ");
        b.append(d.height);
        b.append("]");

        return b.toString();
    }

    private File dumpPkm() {
        File pkm = TEMP.toPath().resolve(timestamp + PKM_EXT).toFile();

        try(FileOutputStream stream = new FileOutputStream(pkm)) {
            stream.write(bytes.array());
        }
        catch(IOException e) {
            PkmPlugin.LOG.catching(e);
        }

        pkm.deleteOnExit();

        return pkm;
    }

    private String wrap(String input) {
        StringBuffer buff = new StringBuffer();
        buff.append("\"");
        buff.append(input);
        buff.append("\"");

        return buff.toString();
    }

    /**
     * Dumps this PKM to an uncompressed PNG
     * 
     * @return a {@code File} object reference to the uncompressed PNG image
     */

    public File dumpPng() {
        File input = dumpPkm();
        File output = TEMP.toPath().resolve(timestamp + PNG_EXT).toFile();

        Process p = null;

        try {
            String[] command = new String[]{wrap(etcTool.getAbsolutePath()), wrap(input.getAbsolutePath()), "--decode"};

            p = Runtime.getRuntime().exec(command);
        }
        catch(IOException e) {
            PkmPlugin.LOG.catching(e);
        }

        if(p != null) {
            try {
                p.waitFor();
            }
            catch(InterruptedException e) {
                PkmPlugin.LOG.catching(e);
            }
        }

        output.deleteOnExit();
        return output;
    }
}
