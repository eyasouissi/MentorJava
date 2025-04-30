package tn.esprit.tools;

import javafx.application.HostServices;

public class HostServicesProvider {
    private static HostServices hostServices;

    public static void setHostServices(HostServices hostServices) {
        if (hostServices == null) {
            HostServicesProvider.hostServices = hostServices;
        }
    }

    public static HostServices getHostServices() {
        if (hostServices == null) {
            throw new IllegalStateException("HostServices not initialized. Call setHostServices() first.");
        }
        return hostServices;
    }
}