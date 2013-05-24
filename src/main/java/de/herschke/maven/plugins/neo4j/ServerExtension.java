package de.herschke.maven.plugins.neo4j;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * used to configure an extension for the Neo4j Server
 *
 * @author rhk
 */
public class ServerExtension {

    @Parameter
    private String packageName;
    @Parameter
    private String mountPoint;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }
}
