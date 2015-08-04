package com.pnf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import com.pnfsoftware.jeb.core.IUnitCreator;
import com.pnfsoftware.jeb.core.input.BytesInput;
import com.pnfsoftware.jeb.core.input.IInput;
import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.units.AbstractBinaryUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;
import com.pnfsoftware.jeb.util.IO;

public class PkmUnit extends AbstractBinaryUnit {
	private static final String TYPE = "pkm_image";
	private static final String TOOL = "etc1tool";

	private PkmTool pkmTool;
	private StringBuffer desc;
	private boolean initError = false;

	public PkmUnit(String name, IInput data, IUnitProcessor unitProcessor, IUnitCreator parent, IPropertyDefinitionManager pdm) {
		super(null, data, TYPE, name, unitProcessor, parent, pdm);

		byte[] bytes = null;

		try(InputStream stream = data.getStream()){
			bytes = IO.readInputStream(stream);
		}catch(IOException e){
			PkmPlugin.LOG.catching(e);
		}

		// Retrieve property entered by user
		String property = getPropertyManager().getString(PkmPlugin.PROP_NAME);
		if(property == null || property.isEmpty())
			setStatus(PkmPlugin.ANDROID_TOOLS_DIR + " key must be set for use in parsing PKM files.");

		File platformTools = new File(property);
		File[] files = platformTools.listFiles();

		// Terminate early if platformTools file is not valid
		if(!platformTools.exists() || !platformTools.isDirectory() || files == null){
			initError = true;
			setStatus(platformTools.getAbsolutePath() + " is not a directory.");
		}

		File etcTool = null;

		// Look for etc1tool anywhere in the directory specified by the given path
		for(File f: files){
			if(f.getName().contains(TOOL))
				etcTool = f;
		}

		// Make sure we can find the tool
		if(etcTool == null || !etcTool.exists()){
			initError = true;
			setStatus("Could not find etc1tool in directory " + platformTools.getAbsolutePath());
		}

		pkmTool = new PkmTool(etcTool, bytes);

		// Update description
		desc = new StringBuffer(super.getDescription());
		desc.append("\n");
		desc.append("Properties:\n");
		desc.append("- Texture Dimensions: " + pkmTool.getTextureDim() + "\n");
		desc.append("- Original Dimensions: " + pkmTool.getOriginalDim() + "\n");
	}

	public String getDescription(){
		return desc.toString();
	}

	public boolean process(){
		if(initError){
			processed = false;
			return processed;
		}

		File png = pkmTool.dumpPng();

		if(png != null){
			try {
				byte[] data = Files.readAllBytes(png.toPath());
				addChildUnit(getUnitProcessor().process("Decompressed Image", new BytesInput(data), this));
			} catch (IOException e) {
				PkmPlugin.LOG.catching(e);
			}
		}

		return true;
	}
}
