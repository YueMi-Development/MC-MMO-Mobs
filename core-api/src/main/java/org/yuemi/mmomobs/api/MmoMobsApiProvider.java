package org.yuemi.mmomobs.api;

import org.jetbrains.annotations.NotNull;

/**
 * Entry point for accessing the MmoMobsPlugin API.
 *
 * Consumers should depend on this interface, not implementation details.
 */
public interface MmoMobsApiProvider {

    /**
     * Retrieves the currently registered MmoMobsApi implementation.
     *
     * @return active API instance
     */
    @NotNull
    MmoMobsApi getApi();
}
