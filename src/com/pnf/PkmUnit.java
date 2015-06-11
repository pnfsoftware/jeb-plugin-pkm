package com.pnf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.pnfsoftware.jeb.core.events.J;
import com.pnfsoftware.jeb.core.events.JebEvent;
import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.units.AbstractBinaryUnit;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;

public class PkmUnit extends AbstractBinaryUnit {
	private static final String TYPE = "pkm_image";
	private static final String TOOL = "etc1tool";

	private PkmTool pkmTool;
	private StringBuffer desc = new StringBuffer(super.getDescription());

	public PkmUnit(String name, byte[] data, IUnitProcessor unitProcessor, IUnit parent, IPropertyDefinitionManager pdm) {
		super(null, data, TYPE, name, unitProcessor, parent, pdm);
	}

	public String getDescription(){
		return desc.toString();
	}

	public boolean process(){
		// Retrieve property entered by user
		File platformTools = new File(getPropertyManager().getString(PkmPlugin.PROP_NAME));

		// Terminate early if platformTools file is not valid
		if(!platformTools.exists() || !platformTools.isDirectory()){
			processed = false;
			setStatus(platformTools.getAbsolutePath() + " is not a directory.");

			return processed;
		}

		File etcTool = null;
		File[] files = platformTools.listFiles();

		// Look for etc1tool anywhere in the directory specified by the given path
		for(File f: files){
			if(f.getName().contains(TOOL))
				etcTool = f;
		}

		// Make sure we can find the tool
		if(etcTool == null || !etcTool.exists()){
			processed = false;
			setStatus("Could not find etc1tool in directory " + platformTools.getAbsolutePath());

			return processed;
		}

		pkmTool = new PkmTool(name, etcTool, data);

		File png = pkmTool.dumpPng();
		IUnit sub = null;

		if(png != null){
			try {
				byte[] data2 = Files.readAllBytes(png.toPath());
				sub = getUnitProcessor().process(png.getName(), data2, this);
			} catch (IOException e) {
				PkmPlugin.LOG.catching(e);
			}
		}

		if(sub != null)
			getChildren().add(sub);

		// Update description
		desc.append("\n");
		desc.append("Properties:\n");
		desc.append("- Texture Dimensions: " + pkmTool.getTextureDim() + "\n");
		desc.append("- Original Dimensions: " + pkmTool.getOriginalDim() + "\n");

		notifyListeners(new JebEvent(J.UnitChange));
		return true;
	}
}
