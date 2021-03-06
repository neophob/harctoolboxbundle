/*
Copyright (C) 2013, 2014 Bengt Martensson.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see http://www.gnu.org/licenses/.
*/

package org.harctoolbox.irscrutinizer.exporter;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.girr.Command;
import org.harctoolbox.girr.RemoteSet;

/**
 * This class does something interesting and useful. Or not...
 */
public class TextExporter extends RemoteSetExporter implements IRemoteSetExporter {

    private boolean generateRaw;
    private boolean generateCcf;
    private boolean generateParameters;
    private Command.CommandTextFormat[] extraFormatters;
    private PrintStream printStream;

    public TextExporter(boolean generateRaw, boolean generateCcf,
            boolean generateParameters, Command.CommandTextFormat... extraFormatters) {
        super();
        this.generateRaw = generateRaw;
        this.generateCcf = generateCcf;
        this.generateParameters = generateParameters;
        this.extraFormatters = extraFormatters;
    }

    public TextExporter() {
        this(false, true, true, (Command.CommandTextFormat[]) null);
    }

    @Override
    public String[][] getFileExtensions() {
        return new String[][]{ new String[] { "Text files (*.txt *.text)", "txt", "text" } };
    }

    @Override
    public String getFormatName() {
        return "Text";
    }

    @Override
    public String getPreferredFileExtension() {
        return "txt";
    }

    private void open(File file, String charsetName) throws FileNotFoundException, UnsupportedEncodingException {
        printStream = new PrintStream(file, charsetName);
    }

    private void close() {
        if (printStream != null)
            printStream.close();
    }

    public void export(String payload, File exportFile, String charsetName) throws IrpMasterException, FileNotFoundException, UnsupportedEncodingException {
        open(exportFile, charsetName);
        try {
            printStream.println(payload);
        } finally {
            printStream.close();
        }
    }

    public File export(String payload, boolean automaticFilenames, Component parent, File exportDir, String charsetName) throws IrpMasterException, IOException {
        File file = exportFilename(automaticFilenames, parent, exportDir);
        export(payload, file, charsetName);
        return file;
    }

    @Override
    public void export(RemoteSet remoteSet, String title, int count, File file, String charsetName) throws IOException, IrpMasterException {
        open(file, charsetName);
        try {
            for (Command command : remoteSet.getAllCommands()) {
                printStream.println(formatCommand(command, count));
            }
        } finally {
            close();
        }
    }

    private String formatCommand(Command command, int count) throws IrpMasterException {
        StringBuilder str = new StringBuilder(128);
        String linefeed = System.getProperty("line.separator", "\n");
        str.append(generateParameters ? command.nameProtocolParameterString() : command.getName()).append(linefeed);
        if (generateCcf) {
            str.append(command.getCcf()).append(linefeed);
        }
        if (generateRaw) {
            str.append(command.getIntro()).append(linefeed);
            str.append(command.getRepeat()).append(linefeed);
            if (command.getEnding() != null && !command.getEnding().isEmpty())
                str.append(command.getEnding()).append(linefeed);
        }
        for (Command.CommandTextFormat formatter : extraFormatters) {
            command.addFormat(formatter, count);
            str.append(command.getFormat(formatter.getName())).append(linefeed);
        }
        return str.toString();
    }

    @Override
    public boolean supportsEmbeddedFormats() {
        return true;
    }
}
