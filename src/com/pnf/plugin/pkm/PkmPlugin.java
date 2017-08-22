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
 * PKM (ETC1 compressed image) parser for JEB.
 * 
 * @author Carlos Gonzales, Nicolas Falliere
 *
 */
public class PkmPlugin extends AbstractUnitIdentifier {
    static final String TYPE = "pkm_etc1";

    private static final int[] PKM_MAGIC = {(byte)0x50, (byte)0x4B, (byte)0x4D, (byte)0x20, (byte)0x31, (byte)0x30};

    public static final ILogger LOG = GlobalLog.getLogger(PkmPlugin.class);
    public static final String ANDROID_TOOLS_DIR = "AndroidPlatformToolsDirectory";

    public PkmPlugin() {
        super(TYPE, 0);
    }

    @Override
    public PluginInformation getPluginInformation() {
        return new PluginInformation("PKM Plugin", "PKM (ETC1 compressed image) parser", "PNF Software",
                Version.create(1, 0, 2), Version.create(2, 3, 3), null);
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
        //unit.process();
        return unit;
    }
}
