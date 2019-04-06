/*
    Copyright(c) 2019 Risto Lahtela (Rsl1122)

    The MIT License(MIT)

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files(the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions :
    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
*/
package com.djrapitops.extension;

import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.annotation.NumberProvider;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * DataExtension for CoreProtect.
 *
 * @author Rsl1122
 */
@PluginInfo(name = "CoreProtect", iconName = "user-shield", iconFamily = Family.SOLID, color = Color.LIGHT_BLUE)
public class CoreProtectExtension implements DataExtension {

    private static final int ACTION_REMOVED = 0;
    private static final int ACTION_PLACED = 1;
    private static final int ACTION_INTERACT = 2;
    private static final int ACTION_CHAT = 3; // Not sure of the value, need trials.
    private CoreProtectAPI api;

    CoreProtectExtension(boolean b) {
        /* For tests */
    }

    public CoreProtectExtension() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CoreProtect");

        // Check that CoreProtect is loaded
        if (!(plugin instanceof CoreProtect)) {
            throw new IllegalStateException();
        }

        // Check that the API is enabled
        api = ((CoreProtect) plugin).getAPI();
        if (!api.isEnabled()) {
            throw new IllegalStateException();
        }

        // Check that a compatible version of the API is loaded (Api 5 for 1.12.2 and lower | Api 6 for 1.13.x)
        if (api.APIVersion() < 5) {
            throw new IllegalStateException();
        }
    }

    @Override
    public CallEvents[] callExtensionMethodsOn() {
        return new CallEvents[]{
                CallEvents.PLAYER_LEAVE
        };
    }

    private long lookupInteractionCount(int days, String playerName, int action) {
        return api.performLookup(
                (int) TimeUnit.DAYS.toSeconds(days),
                Collections.singletonList(playerName),
                null,
                null,
                null,
                Collections.singletonList(action),
                0,
                null
        ).stream().map(api::parseResult)
                .filter(result -> !result.isRolledBack())
                .count();
    }

    @NumberProvider(
            text = "Blocks Placed (Last 30 Days)",
            description = "How many block place actions the player has that have not been rolled back.",
            priority = 20,
            iconName = "cube",
            iconColor = Color.LIGHT_BLUE
    )
    public long blocksPlaced30(String playerName) {
        return lookupInteractionCount(30, playerName, ACTION_REMOVED);
    }

    @NumberProvider(
            text = "Blocks Broken (Last 30 Days)",
            description = "How many block break actions the player has that have not been rolled back.",
            priority = 19,
            iconName = "cube",
            iconColor = Color.BROWN
    )
    public long blocksBroken30(String playerName) {
        return lookupInteractionCount(30, playerName, ACTION_PLACED);
    }

    @NumberProvider(
            text = "Block Interactions (Last 30 Days)",
            description = "How many block interact actions the player has that have not been rolled back.",
            priority = 18,
            iconName = "fingerprint",
            iconColor = Color.LIGHT_BLUE
    )
    public long blocksInteractedWith30(String playerName) {
        return lookupInteractionCount(30, playerName, ACTION_INTERACT);
    }
    
    @NumberProvider(
            text = "Messages Sent (Last 30 Days)",
            description = "How many messages has been sent by players.",
            priority = 17,
            iconName = "comments",
            iconColor = Color.GREEN
    )
    public long messagesSentWith30(String playerName) {
        return lookupInteractionCount(30, playerName, ACTION_CHAT);
    }

    @NumberProvider(
            text = "Blocks Placed (Last 7 Days)",
            description = "How many block place actions the player has that have not been rolled back.",
            priority = 10,
            iconName = "cube",
            iconColor = Color.LIGHT_BLUE
    )
    public long blocksPlaced7(String playerName) {
        return lookupInteractionCount(7, playerName, ACTION_REMOVED);
    }

    @NumberProvider(
            text = "Blocks Broken (Last 7 Days)",
            description = "How many block break actions the player has that have not been rolled back.",
            priority = 9,
            iconName = "cube",
            iconColor = Color.BROWN
    )
    public long blocksBroken7(String playerName) {
        return lookupInteractionCount(7, playerName, ACTION_PLACED);
    }

    @NumberProvider(
            text = "Block Interactions (Last 7 Days)",
            description = "How many block interact actions the player has that have not been rolled back.",
            priority = 8,
            iconName = "fingerprint",
            iconColor = Color.LIGHT_BLUE
    )
    public long blocksInteractedWith7(String playerName) {
        return lookupInteractionCount(7, playerName, ACTION_INTERACT);
    }
    
    @NumberProvider(
            text = "Messages Sent (Last 7 Days)",
            description = "How many messages has been sent by players.",
            priority = 7,
            iconName = "comments",
            iconColor = Color.GREEN
    )
    public long messagesSentWith7(String playerName) {
        return lookupInteractionCount(7, playerName, ACTION_CHAT);
    }

}
