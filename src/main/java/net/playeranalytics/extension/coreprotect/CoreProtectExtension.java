/*
    Copyright(c) 2019 AuroraLS3

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
package net.playeranalytics.extension.coreprotect;

import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.NotReadyException;
import com.djrapitops.plan.extension.annotation.InvalidateMethod;
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
 * @author AuroraLS3
 */
@PluginInfo(name = "CoreProtect", iconName = "user-shield", iconFamily = Family.SOLID, color = Color.LIGHT_BLUE)
@InvalidateMethod("blocksPlaced30")
@InvalidateMethod("blocksBroken30")
@InvalidateMethod("blocksInteractedWith30")
public class CoreProtectExtension implements DataExtension {

    private static final int ACTION_REMOVED = 0;
    private static final int ACTION_PLACED = 1;
    private static final int ACTION_INTERACT = 2;
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

    private long lookupInteractionCount(String playerName, int action) {
        if (!api.isEnabled()) {
            throw new NotReadyException();
        }
        return api.performLookup(
                (int) TimeUnit.DAYS.toSeconds(7),
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
            text = "Blocks Placed (7 Days)",
            description = "How many block place actions the player has that have not been rolled back (From last seen).",
            priority = 10,
            iconName = "cube",
            iconColor = Color.LIGHT_BLUE,
            showInPlayerTable = true
    )
    public long blocksPlaced7(String playerName) {
        return lookupInteractionCount(playerName, ACTION_REMOVED);
    }

    @NumberProvider(
            text = "Blocks Broken (7 Days)",
            description = "How many block break actions the player has that have not been rolled back (From last seen).",
            priority = 9,
            iconName = "cube",
            iconColor = Color.BROWN,
            showInPlayerTable = true
    )
    public long blocksBroken7(String playerName) {
        return lookupInteractionCount(playerName, ACTION_PLACED);
    }

    @NumberProvider(
            text = "Block Interactions (7 Days)",
            description = "How many block interact actions the player has that have not been rolled back (From last seen).",
            priority = 8,
            iconName = "fingerprint",
            iconColor = Color.LIGHT_BLUE
    )
    public long blocksInteractedWith7(String playerName) {
        return lookupInteractionCount(playerName, ACTION_INTERACT);
    }

}
