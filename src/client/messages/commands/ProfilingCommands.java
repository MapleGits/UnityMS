/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package client.messages.commands;

import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.MessageCallback;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import tools.performance.CPUSampler;

public class ProfilingCommands implements Command {
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProfilingCommands.class);

    @Override
    public CommandDefinition[] getDefinition() {
        return new CommandDefinition[]{
            new CommandDefinition("startProfiling", "", "Starts the CPU Sampling based profiler", 500),
            new CommandDefinition("stopProfiling", "[fileName]", "Stops the Profiler and saves the results to the given fileName", 500),};
    }

    @Override
    public void execute(MapleClient c, MessageCallback mc, String[] splitted) {
        if (splitted[0].equals("!startProfiling")) {
            CPUSampler sampler = CPUSampler.getInstance();
            sampler.addIncluded("net.sf.odinms");
            sampler.start();
        } else if (splitted[0].equals("!stopProfiling")) {
            CPUSampler sampler = CPUSampler.getInstance();
            try {
                String filename = "odinprofile.txt";
                if (splitted.length > 1) {
                    filename = splitted[1];
                }
                File file = new File(filename);
                if (file.exists()) {
                    mc.dropMessage("The entered filename already exists, choose a different one");
                    return;
                }
                sampler.stop();
                FileWriter fw = new FileWriter(file);
                sampler.save(fw, 1, 10);
                fw.close();
            } catch (IOException e) {
                log.error("THROW", e);
            }
            sampler.reset();
        }
    }
}
