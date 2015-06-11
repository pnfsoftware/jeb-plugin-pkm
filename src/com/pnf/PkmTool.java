package com.pnf;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.pnfsoftware.jeb.util.IO;

public class PkmTool {
	private static final String TEMP_DIR = "pkm_temp";
	private static File TEMP = null;
	static {
		try {
			TEMP = IO.createTempFolder(TEMP_DIR);
		} catch (IOException e) {
			PkmPlugin.LOG.catching(e);
		}
	}

	private ByteBuffer bytes;
	private String name;
	private File etcTool;
	private Dimension textureDim = new Dimension();
	private Dimension originalDim = new Dimension();

	/*
	 * PKM Structure:
	 * 
	 * char tag[6] = "PKM 10"
	 * 
	 * format (2 bytes) = number of mips (zero)
	 * 
	 * texture width (2 bytes) = multiple of 4 (big endian)
	 * texture height (2 bytes) = "						"
	 * 
	 * original width (2 bytes) = Original dimensions (big endian)
	 * original height (2 bytes) = " 					"
	 * 
	 */
	/**
	 * Creates a new {@code PkmTool} object from the given properties and byte data
	 * @param name the name of the PKM image (used when creating the uncompressed PNG)
	 * @param etcTool a {@code File} reference to the etc1tool executable
	 * @param data a {@code byte} array containing the PKM data
	 */
	public PkmTool(String name, File etcTool, byte[] data){
		bytes = ByteBuffer.wrap(data);
		this.etcTool = etcTool;
		this.name = name;

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
	 * @return a String representation of this pkm file's texture dimension
	 */
	public String getTextureDim(){
		return asString(textureDim) + " (multiples of 4)";
	}

	/**
	 * Retrieves the original dimension data read from the pkm
	 * @return a String representation of this pkm file's original dimension
	 */
	public String getOriginalDim(){
		return asString(originalDim);
	}

	private String asString(Dimension d){
		StringBuffer b = new StringBuffer();

		b.append("[");
		b.append(d.width);
		b.append(" x ");
		b.append(d.height);
		b.append("]");

		return b.toString();
	}

	private File dumpPkm(){
		File pkm = TEMP.toPath().resolve(name).toFile();
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(pkm);
			stream.write(bytes.array());
		} catch (IOException e) {
			PkmPlugin.LOG.catching(e);
		} finally {
			if(stream != null){
				try {
					stream.close();
				} catch (IOException e) {
					PkmPlugin.LOG.catching(e);
				}
			}
		}

		return pkm;
	}

	private String wrap(String input){
		StringBuffer buff = new StringBuffer();
		buff.append("\"");
		buff.append(input);
		buff.append("\"");

		return buff.toString();
	}

	/**
	 * Dumps this this PKM to an uncompressed PNG
	 * @return a {@code File} object reference to the uncompressed PNG image
	 */

	public File dumpPng(){
		File input = dumpPkm();

		Process p = null;

		try {
			p = Runtime.getRuntime()
					.exec(new String[]{wrap(etcTool.getAbsolutePath()),
							"--decode",
							wrap(input.getAbsolutePath())});
		} catch (IOException e) {
			PkmPlugin.LOG.catching(e);
		}

		if(p != null){
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				PkmPlugin.LOG.catching(e);
			}
		}

		File output = null;
		for(File f: input.getParentFile().listFiles()){
			if(f.getName().endsWith("png"))
				output = f;
		}

		return output;
	}
}
