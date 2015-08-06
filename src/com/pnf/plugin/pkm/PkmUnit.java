package com.pnf.plugin.pkm;

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

/**
 * Class responsible for parsing PKM image and then delegating the resultant
 * decompressed image to the appropriate parser
 * @author carlos
 *
 */
public class PkmUnit extends AbstractBinaryUnit {
	private static final String TYPE = "pkm_image";
	private static final String TOOL = "etc1tool";

	private PkmTool pkmTool;
	private StringBuffer desc;
	private boolean initError = false;

	public PkmUnit(String name, IInput data, IUnitProcessor unitProcessor, IUnitCreator parent, IPropertyDefinitionManager pdm) {
		super(null, data, TYPE, name, unitProcessor, parent, pdm);

		// Read the entire stream into an array for processing
		byte[] bytes = null;
		try(InputStream stream = data.getStream()){
			bytes = IO.readInputStream(stream);
		}catch(IOException e){
			PkmPlugin.LOG.catching(e);
		}

		// Retrieve property entered by user
		String property = getPropertyManager().getString(PkmPlugin.PROP_NAME);

		// Check to make sure the user entered a valid value for the property
		if(property == null || property.isEmpty()){
			initError = true;
			setStatus(PkmPlugin.ANDROID_TOOLS_DIR + " key must be set for use in parsing PKM files.");
		}

		File platformTools = new File(property);
		File[] files = platformTools.listFiles();
		File etcTool = null;

		// Terminate early if platformTools path is not valid
		if(!platformTools.exists() || !platformTools.isDirectory() || files == null){
			initError = true;
			setStatus(platformTools.getAbsolutePath() + " is not a valid directory or is empty.");
		}else{
			// Look for etc1tool anywhere in the directory specified by the given path
			for(File f: files){
				if(f.getName().contains(TOOL))
					etcTool = f;
			}

			// Make sure we can find the tool
			if(etcTool == null || !etcTool.exists()){
				initError = true;
				setStatus("Could not find " + TOOL + " executable in directory: " + platformTools.getAbsolutePath());
			}
		}

		if(!initError){
			pkmTool = new PkmTool(etcTool, bytes);

			// Update description
			desc = new StringBuffer(super.getDescription());
			desc.append("\n");
			desc.append("ETC1 Compression Properties:\n");
			desc.append("- Texture Dimensions: " + pkmTool.getTextureDim() + "\n");
			desc.append("- Original Dimensions: " + pkmTool.getOriginalDim() + "\n");
		}
	}

	public String getDescription(){
		return desc.toString();
	}

	public boolean process(){
		// Don't process if there was an error during initialization
		if(initError){
			processed = false;
			return processed;
		}

		// Get a reference to the decompressed ETC1 image
		File png = pkmTool.dumpPng();

		if(png != null){
			try {
				// Read all data from dumped png into a byte array
				byte[] data = Files.readAllBytes(png.toPath());

				// Have the unit processor add the proper image unit for the decompresssed image
				addChildUnit(getUnitProcessor().process("Decompressed Image", new BytesInput(data), this));
			} catch (IOException e) {
				PkmPlugin.LOG.catching(e);
			}
		}

		return true;
	}
}
