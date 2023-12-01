/*
 * Copyright (c) 2023.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.boiler.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

public class Util {

    public static float map(float value, float inMin, float inMax, float outMin, float outMax) {
        return (value - inMin) / (inMax - inMin) * (outMax - outMin) + outMin;
    }
    public static final MiniMessage MM = MiniMessage.miniMessage();
    public static final Component PREFIX = MM.deserialize("<b><color:#52a3ff>[Boiler]</color></b> ");

    public static void sendMsg(CommandSender sender, String msg, Object... args) {
        String[] colored = new String[args.length];
        for(int i = 0; i < args.length; i++) {
            colored[i] = "<color:#52a3ff>" + args[i].toString() + "</color>";
        }

        sender.sendMessage(PREFIX.append(MM.deserialize(msg.formatted(colored))));
    }

    public static void sendErrMsg(CommandSender sender, String msg, Object... args) {
        String[] colored = new String[args.length];
        for(int i = 0; i < args.length; i++) {
            colored[i] = "<b><color:#52a3ff>" + args[i].toString() + "</color></b>";
        }

        sender.sendMessage(PREFIX.append(MM.deserialize("<b><color:#ff4a56>[Error]</color></b> ")).append(MM.deserialize(msg.formatted(colored))));
    }
}
