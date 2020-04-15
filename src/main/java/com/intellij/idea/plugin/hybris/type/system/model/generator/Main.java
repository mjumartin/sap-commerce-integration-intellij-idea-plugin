/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * XSD/DTD Model generator tool
 *
 * By Gregory Shrago
 * 2002 - 2006
 */
package com.intellij.idea.plugin.hybris.type.system.model.generator;

import java.io.File;

/**
 * @author Gregory.Shrago
 * @author Konstantin Bulenkov
 */
public class Main {


    public static void main(String argv[]) throws Exception {
        if (argv.length != 4) {
            System.out.println("Usage: Main <XSD or DTD> <input folder> <output folder> <config xml>");
        }
        String mode = argv[0];
        final ModelLoader loader;
        if (mode.equalsIgnoreCase("xsd")) {
            loader = new XSDModelLoader();
        } else if (mode.equalsIgnoreCase("dtd")) {
            loader = new DTDModelLoader();
        } else {
            System.out.println("'" + mode + "' format not supported");
            System.exit(-1);
            return;
        }
        final File modelRoot = new File(argv[1]);
        final File outputRoot = new File(argv[2]);
        final File configXml = new File(argv[3]);

        outputRoot.mkdirs();
        final ModelGen modelGen = new ModelGen(loader);
        modelGen.loadConfig(configXml);
        modelGen.perform(outputRoot, modelRoot);
    }

}
