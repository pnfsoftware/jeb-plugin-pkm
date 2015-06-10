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
	private StringBuffer desc;
	private boolean processed = false;

	public PkmUnit(String name, byte[] data, IUnitProcessor unitProcessor, IUnit parent, IPropertyDefinitionManager pdm) {
		super(null, data, TYPE, name, unitProcessor, parent, pdm);
		
		// Make sure we can find the tool
		File platformTools = new File(parent.getPropertyManager().getString(PkmPlugin.ANDROID_TOOLS_DIR));
		File etcTool = platformTools.toPath().resolve(TOOL).toFile();
		
		if(!etcTool.exists()){
			throw new RuntimeException("etc1tool not found in directory " + platformTools.getAbsolutePath());
		}
		
		pkmTool = new PkmTool(name, etcTool, data);
	}

	public String getDescription(){
		if(!processed){
			desc = new StringBuffer(super.getDescription());
		}

		return desc.toString();
	}

	public boolean process(){
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
		
		
		
		notifyListeners(new JebEvent(J.UnitChange));
		return true;
	}
}
