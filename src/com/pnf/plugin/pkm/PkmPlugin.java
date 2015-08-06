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

import com.pnfsoftware.jeb.core.IUnitCreator;
import com.pnfsoftware.jeb.core.PluginInformation;
import com.pnfsoftware.jeb.core.Version;
import com.pnfsoftware.jeb.core.input.IInput;
import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.properties.impl.PropertyTypeString;
import com.pnfsoftware.jeb.core.units.AbstractUnitIdentifier;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;
import com.pnfsoftware.jeb.util.logging.GlobalLog;
import com.pnfsoftware.jeb.util.logging.ILogger;

/**
 * Entry-point for ETC1 parser
 * 
 * @author carlos
 *
 */
public class PkmPlugin extends AbstractUnitIdentifier {
    private static final int[] PKM_MAGIC = { (byte)0x50, (byte)0x4B, (byte)0x4D, (byte)0x20, (byte)0x31, (byte)0x30 };
    private static final String ID = "pkm_plugin";

    public static final ILogger LOG = GlobalLog.getLogger(PkmPlugin.class);
    public static final String ANDROID_TOOLS_DIR = "AndroidPlatformToolsDirectory";
    public static final String PROP_NAME = ".parsers." + ID + "." + ANDROID_TOOLS_DIR;

    public PkmPlugin() {
        super(ID, 0);
    }

    public boolean canIdentify(IInput stream, IUnitCreator unit) {
        return checkBytes(stream, 0, PKM_MAGIC);
    }

    public void initialize(IPropertyDefinitionManager parent) {
        super.initialize(parent);

        // We need to use the android tools, so require it as an input before
        // working with PKM files
        PropertyTypeString pts = PropertyTypeString.create();
        getPropertyDefinitionManager().addDefinition(ANDROID_TOOLS_DIR, pts);
    }

    @Override
    public IUnit prepare(String name, IInput data, IUnitProcessor processor, IUnitCreator parent) {
        PkmUnit unit = new PkmUnit(name, data, processor, parent, pdm);
        unit.process();
        return unit;
    }

    @Override
    public PluginInformation getPluginInformation() {
        return new PluginInformation("PKM Plugin", "PKM (ETC1 compressed image) parser", "PNF Software", new Version(1,
                0, 0));
    }
}
