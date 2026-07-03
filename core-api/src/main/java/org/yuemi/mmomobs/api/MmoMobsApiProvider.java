package org.yuemi.mmomobs.api;

import org.jetbrains.annotations.NotNull;

/**
 * Entry point for accessing the MmoMobsPlugin API.
 *
 * Consumers should depend on this interface, not implementation details.
 */
public interface MmoMobsApiProvider {

    /**
     * @return active API instance
     */
    @NotNull
    MmoMobsApi getApi();
}
